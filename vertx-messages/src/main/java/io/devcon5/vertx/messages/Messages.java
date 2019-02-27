package io.devcon5.vertx.messages;

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.function.Consumer;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;

/**
 *
 */
public class Messages {

  private static final Logger LOG = getLogger(Messages.class);

  public static <T> Future<Message<T>> send(Vertx vertx, final String address, Object msg) {
    final EventBus eb = vertx.eventBus();
    final Future<Message<T>> response = Future.future();
    eb.send(address, msg, response.completer());
    return response;
  }

  public static <T> Future<Message<T>> send(final String address, Object msg) {
    return send(Vertx.currentContext().owner(), address, msg);
  }

  public static <T> T ofContract(Vertx vertx, Class<T> contract){
    if(!contract.isInterface()){
      throw new IllegalArgumentException("Contract " + contract.getName() + " is no interface");
    }
    return (T) Proxy.newProxyInstance(contract.getClassLoader(), new Class[]{contract}, new MessageInvocationHandler(vertx));
  }

  public static <T> T ofContract(Class<T> contract){
    return ofContract(Vertx.currentContext().owner(), contract);
  }

  public static <T,V extends Verticle> T of(final Class<V> actorClass) {
    return of(Vertx.currentContext().owner(), actorClass);
  }

  public static <T,V extends Verticle> T of(Vertx vertx, final Class<V> actorClass) {
    final Contract interaction = actorClass.getDeclaredAnnotation(Contract.class);
    if(interaction == null){
      throw new IllegalArgumentException("Class " + actorClass + " has no contract declaration");
    }
    return ofContract(vertx, (Class<T>) interaction.value());
  }

  public static <T extends Verticle> void consumedBy(final T actor) {
    Arrays.stream(actor.getClass().getDeclaredMethods())
          .filter(m -> m.getAnnotation(Address.class) != null)
          .forEach(registerAddress(actor));

    Arrays.stream(actor.getClass().getDeclaredMethods())
          .filter(m -> m.getAnnotation(Address.class) == null && Modifier.isPublic(m.getModifiers()))
          .forEach(registerImplicitAddress(actor));


  }

  private static <T extends Verticle> Consumer<? super Method> registerImplicitAddress(final T actor) {
    final EventBus eb = actor.getVertx().eventBus();
    return method -> {
      final String addr = actor.getClass().getSimpleName() + "." + method.getName();
      eb.consumer(addr, messageHandlerFor(actor, method));

    };
  }

  private static <T extends Verticle> Consumer<Method> registerAddress(final T actor) {
    final EventBus eb = actor.getVertx().eventBus();
    return method -> {
      final String addr = method.getAnnotation(Address.class).value();
      eb.consumer(addr, messageHandlerFor(actor, method));

    };
  }

  private static <A extends Verticle, T> Handler<Message<T>> messageHandlerFor(final A actor, final Method method) {

    return msg -> {
      final Class[] params = method.getParameterTypes();

      if(params.length == 1 && Message.class.isAssignableFrom(params[0])){
        try {
          if(!Modifier.isPublic(method.getModifiers())){
            //Note: this option requires the target modules to be opened (open module ...)
            method.setAccessible(true);
          }
          method.invoke(actor, msg);
        } catch (Exception e) {
          LOG.error("Message could not be processed", e);
          msg.fail(500, "Invocation failed: " + e.getMessage());
        }

      } else {
        if(method.getReturnType() != Void.class){
          //TODO send reply
        }
      }

    };
  }
}

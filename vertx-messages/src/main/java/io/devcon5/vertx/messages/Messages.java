package io.devcon5.vertx.messages;

import static io.devcon5.vertx.messages.MessageMethodHandler.isNativeMethodHandler;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import io.devcon5.vertx.encoding.MessageEncoder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
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

  public static <T> void reply(final Message<T> msg, final AsyncResult<?> res) {

    if(res.succeeded()) {
      Object result = res.result();
      if(result instanceof AsyncResult){
        reply(msg, (AsyncResult)result);
      } else {
        msg.reply(MessageEncoder.INSTANCE.encode(res.result()));
      }
    } else {
      msg.fail(500, res.cause().getMessage());
    }
  }

  public static <T> T ofContract(Vertx vertx, Class<T> contract) {

    if (!contract.isInterface()) {
      throw new IllegalArgumentException("Contract " + contract.getName() + " is no interface");
    }
    return (T) Proxy.newProxyInstance(contract.getClassLoader(),
                                      new Class[] { contract },
                                      new MessageInvocationHandler(vertx, contract));
  }

  public static <T> T ofContract(Class<T> contract) {

    return ofContract(Vertx.currentContext().owner(), contract);
  }

  public static <T, V extends Verticle> T of(final Class<V> actorClass) {

    return of(Vertx.currentContext().owner(), actorClass);
  }

  public static <T, V extends Verticle> T of(Vertx vertx, final Class<V> actorClass) {

    final Contract interaction = actorClass.getDeclaredAnnotation(Contract.class);
    if (interaction == null) {
      throw new IllegalArgumentException("Class " + actorClass + " has no contract declaration");
    }
    return ofContract(vertx, (Class<T>) interaction.value());
  }

  public static <T extends Verticle> void registerActor(final T actor) {

    Arrays.stream(actor.getClass().getDeclaredMethods())
          .filter(Messages::isReceiverMethod)
          .forEach(registerAddress(actor));

  }

  private static boolean isReceiverMethod(final Method m) {

    if(m.getAnnotation(Address.class) != null) {
      return true;
    }
    return Modifier.isPublic(m.getModifiers()) && isAllowed(m) && isSuitable(m);
  }

  private static boolean isAllowed(final Method method) {

    final Class<?> declaringClass = method.getDeclaringClass();
    if (declaringClass.equals(Object.class)) {
      return false;
    }

    final Class<?> superClass = declaringClass.getSuperclass();
    if(superClass == AbstractVerticle.class && declaresMethod(superClass, method)){
      return false;
    }
    for (Class<?> iface : declaringClass.getInterfaces()) {
      if(iface == Verticle.class && declaresMethod(iface, method)){
        return false;
      }
    }
    return true;
  }

  private static boolean declaresMethod(final Class<?> type, final Method method) {
    try {
      type.getMethod(method.getName(), method.getParameterTypes());
      return true;
    } catch (NoSuchMethodException e) {
      for (Class<?> iface : type.getInterfaces()) {
          try {
            iface.getMethod(method.getName(), method.getParameterTypes());
            return true;
          } catch (NoSuchMethodException ignored) {

          }
      }
      return false;
    }
  }

  private static boolean isSuitable(final Method method) {
    if(method.getReturnType() == Void.class) {
      return isNativeMethodHandler(method);
    }
    return method.getParameterTypes().length > 0;
  }



  private static <T extends Verticle> Consumer<Method> registerAddress(final T actor) {

    final EventBus eb = actor.getVertx().eventBus();
    return method -> {
      final String addr = Optional.ofNullable(method.getAnnotation(Address.class))
                                  .map(Address::value)
                                  .orElseGet(() -> getImplicitAddress(actor, method));
      LOG.debug("registering {} at address {}", method, addr);
      eb.consumer(addr, new MessageMethodHandler<>(actor, method));
    };
  }

  private static <T extends Verticle> String getImplicitAddress(final T actor, final Method method) {

    for(Class iface : actor.getClass().getInterfaces()){
      if(declaresMethod(iface, method)){
        return iface.getSimpleName() + "." + method.getName();
      }
    }
    return actor.getClass().getSimpleName() + "." + method.getName();
  }



}

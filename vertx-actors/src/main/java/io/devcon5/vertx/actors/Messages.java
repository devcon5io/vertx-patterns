package io.devcon5.vertx.actors;

import static io.devcon5.vertx.codec.GenericTypeDecoding.isSimpleType;
import static io.devcon5.vertx.actors.MessageMethodHandler.isNativeMethodHandler;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import io.devcon5.vertx.codec.GenericTypeArrayCodec;
import io.devcon5.vertx.codec.GenericTypeCodec;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;

/**
 *
 */
public class Messages {

  private static final Logger LOG = getLogger(Messages.class);

  /**
   * Sends a message to a vert.x event bus address. This is a convenience method for sending a message
   * and dealing with the response in a future and not a callback. This allows for simpler message chaining or
   * bundling (i.e. in a CompositeFuture)
   *
   * @param vertx
   *     the current vertx instance whose event bus should be used
   * @param address
   *     the address to sent the message to
   * @param msg
   *     the message object to sent. Calls have to ensure that it's a either a natively supported type (simple types,
   *     Json, Buffer) or that a codec was registered on the event bus
   * @param <T>
   *     the type of the resulting message
   *
   * @return a future of the response received
   */
  public static <T> Future<Message<T>> send(Vertx vertx, final String address, Object msg) {

    final EventBus eb = vertx.eventBus();
    final Future<Message<T>> response = Future.future();
    eb.send(address, msg, response.completer());
    return response;
  }

  public static <T> Future<Message<T>> send(final String address, Object msg) {

    return send(Vertx.currentContext().owner(), address, msg);
  }

  /**
   * Sends a reply to the given message, returning the specified response. The reply may contain a return value
   * that is provided via the returned future.
   *
   * @param msg
   *     the message to reply to
   * @param res
   *     the async result whose payload should be returned. The result has to be completed in order to be sent. This
   *     method does not register as a new handler, but checks whether the result is succeeded or failed
   * @param <T>
   *     the type of the message that should be replied to
   * @param <R>
   *     the type of the expected reply response
   *
   * @return a future of a response to the reply
   */
  public static <T, R> Future<Message<R>> reply(final Message<T> msg, final AsyncResult<?> res) {

    final Future<Message<R>> response = Future.future();
    if (res.succeeded()) {
      Object result = res.result();
      if (result instanceof AsyncResult) {
        return reply(msg, (AsyncResult) result);
      } else {
        msg.reply(res.result(),
                  new DeliveryOptions().setCodecName(GenericTypeCodec.codecNameFor(result.getClass())),
                  response.completer());
      }
    } else {
      msg.fail(500, res.cause().getMessage());
      response.fail("Reply message alread indicated an error, no reply-response expected");
    }
    return response;
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

    //TODO add support for Contract marker annotation
    for (Class iface : actorClass.getInterfaces()) {
      if (Verticle.class.isAssignableFrom(iface)) {
        continue;
      }
      return ofContract(vertx, (Class<T>) iface);
    }
    throw new IllegalArgumentException(actorClass.getName() + " does not implement a contractual interface");
  }

  /**
   * Registers an actor {@link io.vertx.core.Verticle} and it's methods on the event bus so that
   * messages can be sent directly to the actor's method without having to explicitly define it's addresses and
   * handlers. Only public methods or methods annoted with {@link io.devcon5.vertx.actors.Address} are
   * registered. For each registerd method a codec is registered on the event bus that decodes messages to match
   * the signature of the method so that native objects (Pojos) can be transmitted.
   *
   * @param actor
   *     the actor {@link io.vertx.core.Verticle} whose methods should be registered as addresses
   * @param <T>
   *     the type of the actor
   */
  public static <T extends Verticle> void registerActor(final T actor) {

    Arrays.stream(actor.getClass().getDeclaredMethods())
          .filter(Messages::isReceiverMethod)
          .forEach(registerAddress(actor));

  }

  private static boolean isReceiverMethod(final Method m) {

    if (m.getAnnotation(Address.class) != null) {
      return true;
    }
    return Modifier.isPublic(m.getModifiers()) && isAllowedAsReceiver(m) && isSuitable(m);
  }

  /**
   * Checks if the method is not declared by {@link java.lang.Object}, {@link io.vertx.core.Verticle} or
   * {@link io.vertx.core.AbstractVerticle}
   *
   * @param method
   *     the method to check
   *
   * @return true if method is not defined by one of the types listed above
   */
  private static boolean isAllowedAsReceiver(final Method method) {

    final Class<?> declaringClass = method.getDeclaringClass();
    if (declaringClass.equals(Object.class)) {
      return false;
    }

    final Class<?> superClass = declaringClass.getSuperclass();
    if (superClass == AbstractVerticle.class && declaresMethod(superClass, method)) {
      return false;
    }
    for (Class<?> iface : declaringClass.getInterfaces()) {
      if (iface == Verticle.class && declaresMethod(iface, method)) {
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

  /**
   * Checks if a method is suitable as receiver type by it's parameter types and return values
   *
   * @param method
   *     the method to check
   *
   * @return if the method returns void and has a single {@link io.vertx.core.eventbus.Message} parameter
   * or if the message has at least one parameter
   */
  private static boolean isSuitable(final Method method) {

    if (method.getReturnType() == Void.class) {
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
      //TODO add security
      registerCodecs(eb, method).consumer(addr, new MessageMethodHandler<>(actor, method));
    };
  }

  private static EventBus registerCodecs(final EventBus eb, final Method method) {

    //unfortunately there is no access to the internal code map of the event bus, so we
    //have to check a pre-registered codec the hard way as there is no globally safe way
    //to track all registered codecs
    if (!isSimpleType(method.getGenericReturnType())) {
      try {
        eb.registerCodec(new GenericTypeCodec(method.getGenericReturnType()));
      } catch (IllegalStateException e) {
        LOG.info("Skipped registering codec for return type of {}: {}", method, e.getMessage());
      }
    }
    try {
      eb.registerCodec(new GenericTypeArrayCodec(method.getParameterTypes()));
    } catch (IllegalStateException e) {
      LOG.info("Skipped registering codec for parameters of {}: {}", method, e.getMessage());
    }
    return eb;
  }

  static <T extends Verticle> String getImplicitAddress(final T actor, final Method method) {

    for (Class iface : actor.getClass().getInterfaces()) {
      if (declaresMethod(iface, method)) {
        return getImplicitAddress(iface, method);
      }
    }
    return actor.getClass().getName() + "." + method.getName();
  }

  static String getImplicitAddress(final Class iface, final Method method) {

    final StringBuilder buf = new StringBuilder(64);
    buf.append(iface.getName());
    buf.append('.');
    buf.append(method.getName());
    buf.append('(');
    buf.append(GenericTypeArrayCodec.codecNameFor(method.getGenericParameterTypes()));
    buf.append(')');
    return buf.toString();
  }

}

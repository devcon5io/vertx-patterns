package io.devcon5.vertx.actors;

import static io.devcon5.vertx.actors.MessageMethodHandler.isNativeMethodHandler;
import static io.devcon5.vertx.codec.GenericTypes.isSimpleType;
import static io.vertx.core.logging.LoggerFactory.getLogger;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.function.Consumer;

import io.devcon5.vertx.codec.GenericTypeArrayCodec;
import io.devcon5.vertx.codec.GenericTypeCodec;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.logging.Logger;

/**
 *
 */
public class Actors {

  private static final Logger LOG = getLogger(Actors.class);

  public static <T> T withContract(Class<T> contract) {

    return withContract(Vertx.currentContext().owner(), contract);
  }

  public static <T> T withContract(Vertx vertx, Class<T> contract) {

    if (!contract.isInterface()) {
      throw new IllegalArgumentException("Contract " + contract.getName() + " is no interface");
    }
    return (T) Proxy.newProxyInstance(contract.getClassLoader(),
                                      new Class[] { contract },
                                      new MessageInvocationHandler(vertx));
  }

  public static <T, V extends Verticle> T of(final Class<V> actorClass) {

    return of(Vertx.currentContext().owner(), actorClass);
  }

  public static <T, V extends Verticle> T of(Vertx vertx, final Class<V> actorClass) {

    for (Class iface : actorClass.getInterfaces()) {
      if (Verticle.class.isAssignableFrom(iface)) {
        continue;
      }
      return withContract(vertx, (Class<T>) iface);
    }
    throw new IllegalArgumentException(actorClass.getName() + " does not implement a contractual interface");
  }

  /**
   * Registers an actor {@link io.vertx.core.Verticle} and the methods of it's internfaces on the event bus so that
   * messages can be sent directly to the actor's method without having to explicitly define it's addresses and
   * handlers. For each registered method a codec is registered on the event bus that decodes messages to match
   * the signature of the method so that native objects (Pojos) can be transmitted.
   *
   * @param actor
   *     the actor {@link io.vertx.core.Verticle} whose methods should be registered as addresses
   * @param <T>
   *     the type of the actor
   */
  public static <T extends Verticle> void register(final T actor) {

    Arrays.stream(actor.getClass().getInterfaces())
          .filter(c -> c != Verticle.class)
          .flatMap(c -> Arrays.stream(c.getMethods()))
          .filter(Actors::isSuitable)
          .forEach(registerAddress(actor));

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
      final String addr = getContractMethodAddress(method);
      LOG.debug("registering {} at address {}", method, addr);
      //TODO add security
      registerCodecs(eb, method).consumer(addr, new MessageMethodHandler<>(actor, method));
    };
  }

  private static EventBus registerCodecs(final EventBus eb, final Method method) {

    if (!isSimpleType(method.getGenericReturnType())) {
      registerCodec(eb, GenericTypeCodec.forType(method.getGenericReturnType()));
    }
    registerCodec(eb, GenericTypeArrayCodec.forType(method.getGenericParameterTypes()));
    return eb;
  }

  private static void registerCodec(final EventBus eb, final MessageCodec codec) {

    if (codec == null || codec.name() == null) {
      //the codec might be null, i.e. for simple types / natively supported types
      return;
    }

    //unfortunately there is no access to the internal code map of the event bus, so we
    //have to check a pre-registered codec the hard way as there is no globally safe way
    //to track all registered codecs
    try {
      eb.registerCodec(codec);
    } catch (IllegalStateException e) {
      LOG.debug("Skipped registering codec: {}", e.getMessage());
    }
  }

  static String getContractMethodAddress(final Method method) {

    final StringBuilder buf = new StringBuilder(64);
    buf.append(method.getDeclaringClass().getName());
    buf.append('.');
    buf.append(method.getName());
    buf.append('(');
    buf.append(GenericTypeArrayCodec.codecNameFor(method.getGenericParameterTypes()));
    buf.append(')');
    return buf.toString();
  }

}

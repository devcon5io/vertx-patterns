package io.devcon5.vertx.actors;

import static io.devcon5.vertx.codec.GenericTypes.isSimpleType;
import static io.devcon5.vertx.codec.GenericTypes.unwrapFutureType;
import static io.vertx.core.logging.LoggerFactory.getLogger;
import static java.util.function.Predicate.not;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.devcon5.vertx.codec.GenericTypeArrayCodec;
import io.devcon5.vertx.codec.GenericTypeCodec;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;

/**
 * Marker interface for actors.
 * If you wan't to use the {@link java.util.ServiceLoader} mechanism of the {@link io.devcon5.vertx.actors.Actor#deployAll(io.vertx.core.json.JsonObject)}
 * you have to implement this interface
 * <br>
 * Further, this interface provides static entry points for actors that want to either provide or use communication
 * contract.
 * <br>
 * Providers are Vertx' actor implementation - {@link io.vertx.core.Verticle} - may implement a contractual interface
 * and must register themselves using the {@link #register(io.vertx.core.Verticle)} method in order to make the
 * implemented interfaces available over the event bus.
 * Example:<br>
 * <pre>
 *   class Actor extends Verticle implements MyContract {
 *     public void start() throws Exception {
 *       Actors.register(this);
 *     }
 *   }
 * </pre>
 * <br>
 * Consumers may create a dynamic client for the contractual interface using the {@link #withContract(Class)} method
 * in order to send messages and receive responses in a type-safe way.
 * Example:
 * <pre>
 *   MyContract actor = Actors.withContract(MyContract.class);
 * </pre>
 * <br>
 * Contractual interfaces are java interfaces that define methods, but have some limitations:
 * Method arguments can be
 * <ul>
 * <li>Simple types: <ul>
 * <li>primitive types and their Object counterparts</li>
 * <li>Strings</li>
 * <li>byte arrays</li>
 * <li>{@link io.vertx.core.json.JsonObject}</li>
 * <li>{@link io.vertx.core.json.JsonArray}</li>
 * <li>{@link io.vertx.core.buffer.Buffer}</li>
 * </ul></li>
 * <li>POJOs (Java Beans) that implement {@link io.vertx.core.shareddata.Shareable} (recommended) or are
 * {@link java.io.Serializable}</li>
 * <li>{@link java.util.List}, {@link java.util.Set} or {@link java.util.Map} of simple types, pojos or list,
 * sets or maps</li>
 * </ul>
 * Return types of the method should be a {@link io.vertx.core.Future} of a type of the list above (simple types,
 * Pojos, Lists, Sets or Maps). If the return type is not Future, the methods may not be invoked from an event-loop
 * thread as the caller thread will be blocked until the result is received.
 *
 */
public interface Actor extends Verticle {


  /**
   * Deploys all Actors that are found in the classpath/modulepath that have registered as service via
   * META-INF/services/io.devcon5.vertx.actors.Actor using the current Vertx instance and it's configuration.
   *
   * @return future handle for tracking the deployment process. The future is a composite future of futures for each
   *  actor deployment.
   */
  public static Future<CompositeFuture> deployAll() {

    final JsonObject config = Vertx.currentContext().config();
    return deployAll(config);
  }

  /**
   * Deploys all Actors that are found in the classpath/modulepath that have registered as service via
   * META-INF/services/io.devcon5.vertx.actors.Actor using the current Vertx instance and the specified configuration.
   *
   * @param config
   *     a configuration object that is passed as deployment config to each of the found actors
   *
   * @return future handle for tracking the deployment process. The future is a composite future of futures for each
   *  actor deployment.
   */
  static Future<CompositeFuture> deployAll(JsonObject config) {

    final Vertx vertx = Vertx.currentContext().owner();
    return deployAll(vertx, config);
  }

  /**
   * Deploys all Actors that are found in the classpath/modulepath that have registered as service via
   * META-INF/services/io.devcon5.vertx.actors.Actor into the specified Vertx instance using the specified
   * configuration.
   *
   * @param vertx
   *     the vertx instance to deploy the actor into
   * @param config
   *     a configuration object that is passed as deployment config to each of the found actors
   *
   * @return future handle for tracking the deployment process. The future is a composite future of futures for each
   * actor deployment.
   */
  static Future<CompositeFuture> deployAll(Vertx vertx, JsonObject config) {

    return CompositeFuture.all(ServiceLoader.load(Actor.class).stream().map(actor -> {
      Future<String> result = Future.future();
      vertx.deployVerticle(actor.type().getName(), new DeploymentOptions().setConfig(config), result.completer());
      return result;
    }).collect(Collectors.toList()));
  }

  /**
   * Creates a dynamic client to communicate with an actor that implements the specified interface.
   *
   * @param contract
   *     the contract the actor implements and which defines the messages (methods) that can be sent to
   *     the actual actor ({@link io.vertx.core.Verticle})
   * @param <T>
   *     the type of the contract. The type must be an interface.
   *
   * @return a dynamic client that implements the interface
   */
  static <T> T withContract(Class<T> contract) {

    return withContract(Vertx.currentContext().owner(), contract);
  }

  /**
   * Creates a dynamic client to communicate with an actor that implements the specified interface.
   *
   * @param vertx
   *     the vertx instance the backing actor {@link io.vertx.core.Verticle} is deployed
   * @param contract
   *     the contract the actor implements and which defines the messages (methods) that can be sent to
   *     the actual actor ({@link io.vertx.core.Verticle})
   * @param <T>
   *     the type of the contract. The type must be an interface.
   *
   * @return a dynamic client that implements the interface
   */
  static <T> T withContract(Vertx vertx, Class<T> contract) {

    if (!contract.isInterface()) {
      throw new IllegalArgumentException("Contract " + contract.getName() + " is no interface");
    }
    return (T) Proxy.newProxyInstance(contract.getClassLoader(),
                                      new Class[] { contract },
                                      new MessageInvocationHandler(vertx));
  }

  /**
   * Registers an actor {@link io.vertx.core.Verticle} and the methods of it's interfaces on the event bus so that
   * messages can be sent directly to the actor's method without having to explicitly define it's addresses and
   * handlers. All the actor's interfaces are registered, except the {@link io.vertx.core.Verticle} interfaces. For
   * each registered method a codec is registered on the event bus that decodes messages to match
   * the signature of the method so that native objects (Pojos) can be transmitted.
   *
   * @param actor
   *     the actor {@link io.vertx.core.Verticle} whose methods should be registered as addresses
   * @param <T>
   *     the type of the actor
   */
  static <T extends Verticle> void register(final T actor) {

    final Set<Class> ignoreSet = getIgnoredInterfaces(actor);
    Arrays.stream(actor.getClass().getInterfaces())
          .filter(not(ignoreSet::contains))
          .flatMap(c -> Arrays.stream(c.getMethods()))
          .filter(Actor::isSuitable)
          .forEach(registerAddress(actor));

  }

  private static <T extends Verticle> Set<Class> getIgnoredInterfaces(final T actor) {

    final Set<Class> result = new HashSet<>();
    result.add(Verticle.class);
    result.add(Actor.class);

    final Contracts.Ignore ignored = actor.getClass().getAnnotation(Contracts.Ignore.class);
    if (ignored != null) {
      result.addAll(Set.of(ignored.value()));
    }
    return result;
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

    return method.getAnnotation(Contracts.Ignore.class) == null;
  }

  private static <T extends Verticle> Consumer<Method> registerAddress(final T actor) {
    final Logger LOG = getLogger(Actor.class);
    final EventBus eb = actor.getVertx().eventBus();
    return method -> {
      final String addr = getContractMethodAddress(method);
      LOG.debug("registering {} at address {}", method, addr);
      //TODO add security
      registerCodecs(eb, method).consumer(addr, new MessageMethodHandler<>(actor, method));
    };
  }

  private static EventBus registerCodecs(final EventBus eb, final Method method) {

    //register the codec for the return type
    final Type returnType = unwrapFutureType(method.getGenericReturnType());
    if (!isSimpleType(returnType)) {
      registerCodec(eb, GenericTypeCodec.forType(returnType));
    }
    //register a codec for the argument types
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
      getLogger(Actor.class).debug("Skipped registering codec: {}", e.getMessage());
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

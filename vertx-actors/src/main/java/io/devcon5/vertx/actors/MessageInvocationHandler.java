package io.devcon5.vertx.actors;

import static io.devcon5.vertx.codec.GenericTypeArrayCodec.codecNameFor;
import static io.vertx.core.logging.LoggerFactory.getLogger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;

/**
 * Invokation Handler for a dynamic proxy that sends method arguments as payload to a receiving actor over the event
 * bus.
 */
class MessageInvocationHandler implements InvocationHandler {

  private static final Logger LOG = getLogger(MessageInvocationHandler.class);

  private final EventBus eb;

  public MessageInvocationHandler(final Vertx vertx) {

    this.eb = vertx.eventBus();
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

    final String ebAddress = Actors.getImplicitAddress(method);
    //TODO add support for security

    final DeliveryOptions opts = new DeliveryOptions().setCodecName(codecNameFor(method.getGenericParameterTypes()));
    final Future result = Future.future();

    LOG.debug("Sending message to {} using codec {}", ebAddress, opts.getCodecName());

    eb.send(ebAddress, args, opts, result.completer());
    if (getReturnType(method) == Void.class) {
      //check if really needed
      return null;
    } else if (isNonBlocking(method)) {
        if(getReturnType(method) == Message.class){
          return result;
        } else {
          return result.map(oMsg -> ((Message)oMsg).body());
        }
    } else {
        //TODO throw exception only when running on eventloop thread
        throw new UnsupportedOperationException("Blocking methods are not supported. All methods must have a future "
                                                    + "as return type, but "
                                                    + method
                                                    + " returns "
                                                    + method.getReturnType());
    }
  }

  private boolean isNonBlocking(final Method method) {

    return Future.class.isAssignableFrom(method.getReturnType());
  }

  private Class<?> getReturnType(final Method method) {

    final Class<?> returnType = method.getReturnType();
    if (Future.class.isAssignableFrom(returnType)) {
      return findTypeArguments(method.getGenericReturnType());
    }
    return returnType;
  }

  private <T> Class<T> findTypeArguments(Type t) {

    if (t == null) {
      return null;
    }
    if (t instanceof ParameterizedType) {
      return (Class<T>) ((ParameterizedType) t).getActualTypeArguments()[0];
    }
    final Class<T> cls = findTypeArguments(((Class) t).getGenericSuperclass());
    if (cls == null) {
      return (Class) t;
    } else {
      return cls;
    }

  }
}

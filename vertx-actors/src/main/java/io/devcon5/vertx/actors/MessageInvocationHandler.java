package io.devcon5.vertx.actors;

import static io.devcon5.vertx.codec.GenericTypeArrayCodec.codecNameFor;
import static io.devcon5.vertx.codec.GenericTypes.unwrapFutureType;
import static io.vertx.core.logging.LoggerFactory.getLogger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.logging.Logger;

/**
 * Invokation Handler for a dynamic proxy that sends method arguments as payload to a receiving actor over the event
 * bus.
 */
class MessageInvocationHandler implements InvocationHandler {

  private static final Logger LOG = getLogger(MessageInvocationHandler.class);

  private final EventBus eb;

  MessageInvocationHandler(final Vertx vertx) {

    this.eb = vertx.eventBus();
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

    checkIgnored(method);

    final DeliveryOptions opts = new DeliveryOptions().setCodecName(codecNameFor(method.getGenericParameterTypes()));
    final Future result = Future.future();

    final String ebAddress = Actor.getContractMethodAddress(method);
    LOG.debug("Sending message to {} using codec {}", ebAddress, opts.getCodecName());
    eb.send(ebAddress, args, opts, result.completer());

    if (getReturnType(method) == void.class) {
      return null;
    } else if (isNonBlocking(method)) {
      return handleResponseNonBlocking(result, method);
    } else {
      return handleResponseBlocking(result, method);
    }
  }

  private void checkIgnored(final Method method) {

    if (method.getAnnotation(Contracts.Ignore.class) != null) {
      throw new UnsupportedOperationException(method + " is not supported");
    }
  }

  private Object handleResponseNonBlocking(final Future result, final Method method) {

    if (getReturnType(method) == Message.class) {
      return result;
    } else {
      return result.map(this::unwrapBody).recover(exceptionHandler(method));
    }
  }

  private Object handleResponseBlocking(final Future result, final Method method) throws Throwable {

    if (isEventLoopThred()) {
      throw new UnsupportedOperationException("Blocking methods are not supported to be executed on the eventloop."
                                                  + "Non-blocking method must have a io.vertx.core.Future "
                                                  + "as return type, but "
                                                  + method
                                                  + " returns "
                                                  + method.getReturnType());
    }
    while (!result.isComplete()) {
      Thread.onSpinWait();
    }
    if (result.succeeded()) {
      return result.map(this::unwrapBody).result();
    } else {
      throw result.recover(exceptionHandler(method)).cause();
    }
  }

  private Function<Throwable, Future> exceptionHandler(final Method method) {

    return ex -> {
      if (ex instanceof ReplyException) {
        switch (((ReplyException) ex).failureType()) {
          case TIMEOUT:
            return Future.failedFuture(new TimeoutException("Method timed out"));
          case NO_HANDLERS:
            return Future.failedFuture(new UnsupportedOperationException(method + " is not supported"));
          default:
            //TODO deserialize exception if possible
            return Future.failedFuture(ex);
        }
      }
      return Future.failedFuture(ex);
    };
  }

  private Object unwrapBody(final Object oMsg) {

    return ((Message) oMsg).body();
  }

  private boolean isEventLoopThred() {

    return Thread.currentThread().getName().startsWith("vert.x-eventloop-thread");
  }

  private boolean isNonBlocking(final Method method) {

    return Future.class.isAssignableFrom(method.getReturnType());
  }

  private Class<?> getReturnType(final Method method) {

    final Type returnType = unwrapFutureType(method.getGenericReturnType());
    if (returnType instanceof ParameterizedType) {
      return (Class<?>) ((ParameterizedType) returnType).getRawType();
    } else if(returnType instanceof Class){
      return (Class<?>) returnType;
    }
    return null;
  }
}

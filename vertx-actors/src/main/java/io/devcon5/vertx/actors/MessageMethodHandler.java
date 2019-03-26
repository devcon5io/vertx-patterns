package io.devcon5.vertx.actors;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import io.devcon5.vertx.codec.GenericTypeCodec;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;

/**
 * Handler to invoke methods on an actor that correspond to the address of a received event bus message.
 */
class MessageMethodHandler<A extends Verticle, T>  implements Handler<Message<T>> {

  private final A actor;
  private final Method method;
  private final boolean isNative;
  private final String returnTypeCodec;

  public MessageMethodHandler(A actor, Method m){
    this.actor = actor;
    this.method = m;
    this.returnTypeCodec = getReturnTypeCodec(m.getGenericReturnType());
    this.isNative = isNativeMethodHandler(m);
  }

  private String getReturnTypeCodec(Type type) {

    return GenericTypeCodec.codecNameFor(type);
  }

  @Override
  public void handle(final Message<T> msg) {
    invoke(actor, method, extractMethodArgs(msg)).setHandler(res -> {
      if(res.succeeded()){
        Object result = res.result();
        msg.reply(result, getDeliveryOpts());
      } else {
        msg.fail(500, res.cause().getMessage());
      }
    });
  }

  private DeliveryOptions getDeliveryOpts() {

    final DeliveryOptions opts = new DeliveryOptions();
    if(returnTypeCodec != null) {
      opts.setCodecName(returnTypeCodec);
    }
    return opts;
  }

  private <A extends Verticle> Future<?> invoke(final A actor, final Method method, final Object... arg) {

    try {
      openMethod(method);
      final Object returnValue = method.invoke(actor, arg);
      if(returnValue instanceof Future){
        return (Future)returnValue;
      } else {
        return Future.succeededFuture(returnValue);
      }
    } catch (Exception e) {
      return Future.failedFuture(e);
    }
  }

  private Object[] extractMethodArgs(final Message<T> msg) {

    final Object[] args;
    if (isNative) {
      args = new Object[]{msg};
    } else {
      args = (Object[])msg.body();
    }
    return args;
  }

  private void openMethod(final Method method) {

    if (!Modifier.isPublic(method.getModifiers())) {
      //Note: this option requires the target modules to be opened (open module ...)
      method.setAccessible(true);
    }
  }

  static boolean isNativeMethodHandler(final Method method) {
    final Class[] params = method.getParameterTypes();
    return params.length == 1 && Message.class == params[0];
  }


}

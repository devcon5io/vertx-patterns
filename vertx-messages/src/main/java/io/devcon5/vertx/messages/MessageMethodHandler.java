package io.devcon5.vertx.messages;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.eventbus.Message;

/**
 *
 */
class MessageMethodHandler<A extends Verticle, T>  implements Handler<Message<T>> {

  private final A actor;
  private final Method method;
  private final boolean isNative;

  public MessageMethodHandler(A actor, Method m){
    this.actor = actor;
    this.method = m;
    this.isNative = isNativeMethodHandler(m);
  }

  @Override
  public void handle(final Message<T> msg) {
    invoke(actor, method, extractMethodArgs(msg)).setHandler(res -> {
      if(res.failed()){
        msg.fail(500, res.cause().getMessage());
      }
      //TODO add reply
    });
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

  private <A extends Verticle> Future<?> invoke(final A actor, final Method method, final Object... arg) {

    Future result = Future.future();
    try {
      openMethod(method);
      result.complete(method.invoke(actor, arg));
    } catch (Exception e) {
      result.fail(e);
    }
    return result;
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

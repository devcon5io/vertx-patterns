package io.devcon5.vertx.messages;

import static io.devcon5.vertx.messages.Messages.reply;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;

/**
 *
 */
class MessageMethodHandler<A extends Verticle, T>  implements Handler<Message<T>> {

  private final A actor;
  private final Method method;
  private final MessageEncoder enc;
  private final MessageDecoder dec;

  public MessageMethodHandler(A actor, Method m){
    this.actor = actor;
    this.method = m;
    this.dec = new MessageDecoder();
    this.enc = new MessageEncoder();
  }

  @Override
  public void handle(final Message<T> msg) {
    if (isNativeMethodHandler(method)) {
      invoke(actor, method, msg).setHandler(res -> {
        if(res.failed()){
          msg.fail(500, res.cause().getMessage());
        }
      });
    } else {
      final Class[] paramTypes = method.getParameterTypes();
      final Object body = msg.body();
      if(paramTypes.length == 1){
        invoke(actor, method,dec.decode(body, paramTypes[0]))
            .setHandler(res -> reply(msg, res));
      } else if(msg.body() instanceof JsonArray && paramTypes.length <= ((JsonArray)body).size()){
        final JsonArray bodyArr = (JsonArray)body;
        final Object[] args = new Object[paramTypes.length];
        for(int i =0, len = paramTypes.length; i < len; i++){
          args[i] = dec.decode(bodyArr.getValue(i), paramTypes[i]);
        }
        invoke(actor, method,args).setHandler(res -> reply(msg, res));
      } else {
        msg.fail(501, "Could not find matching method for msg body " + msg.body());
      }
    }
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

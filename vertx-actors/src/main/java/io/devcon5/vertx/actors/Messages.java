package io.devcon5.vertx.actors;

import io.devcon5.vertx.codec.GenericTypeCodec;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

/**
 * Helper to send and reply to message based on futures of the corresponding responses.
 */
public class Messages {

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
}

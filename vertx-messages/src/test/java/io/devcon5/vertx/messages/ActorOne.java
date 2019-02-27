package io.devcon5.vertx.messages;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 *
 */
@io.devcon5.vertx.messages.Contract(ActorOne.Contract.class)
public class ActorOne extends AbstractVerticle {

  public static final String PROCESS_REQUEST_STRING =  "procoess_request_addr_string";
  public static final String PROCESS_REQUEST_JSON =  "procoess_request_addr_json";

  @Override
  public void start(final Future<Void> startFuture) throws Exception {

    vertx.eventBus().consumer(PROCESS_REQUEST_STRING, this::processRequestString);
    vertx.eventBus().consumer(PROCESS_REQUEST_JSON, this::processRequestJson);
    super.start(startFuture);
  }

  private void processRequestString(final Message<String> msg) {
    msg.reply("Hello " + msg.body());
  }


  private void processRequestJson(final Message<JsonObject> msg) {
    msg.reply(new JsonObject().put("greeting", "Hello").put("user", msg.body()));
  }

  /**
   * All interactions of ActorOne modelled as type-safe Java methods
   */
  public interface Contract {
    @Address(PROCESS_REQUEST_STRING)
    Future<String> hello(final String bob);

    @Address(PROCESS_REQUEST_JSON)
    Future<Greeting> hello(User bob);

    //this method should not work as it returns an immediate/blocking result
    @Address(PROCESS_REQUEST_JSON)
    Greeting helloBlocking(User bob);
  }
}

package io.devcon5.vertx.messages;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 *
 */
public class ActorTwo extends AbstractVerticle {

  public static final String PROCESS_REQUEST_STRING =  "procoess_request_addr_string";
  public static final String PROCESS_REQUEST_JSON =  "procoess_request_addr_json";

  @Override
  public void start(final Future<Void> startFuture) throws Exception {

    Messages.consumedBy(this);
    super.start(startFuture);
  }

  @Address(PROCESS_REQUEST_STRING)
  public void processStringRequest(final Message<String> msg) {
    msg.reply("Hello " + msg.body());
  }
  @Address(PROCESS_REQUEST_JSON)
  private void processJsonRequest(final Message<JsonObject> msg) {
    msg.reply(new JsonObject().put("greeting", "Hello").put("user", msg.body()));
  }

  public void handleStringRequest(final Message<String> msg) {
    msg.reply("Hello " + msg.body());
  }

  public Future<Greeting> hello(User user){
    Greeting greet = new Greeting();
    greet.setGreeting("Hello");
    greet.setUser(user);
    return Future.succeededFuture(greet);
  }

  public Greeting helloBlocking(User user){
    Greeting greet = new Greeting();
    greet.setGreeting("Hello");
    greet.setUser(user);
    return greet;
  }

  public List<Greeting> helloUsers(User user1, User user2){
    List<Greeting> greetings = new ArrayList<>();
    Greeting greet1 = new Greeting();
    greet1.setGreeting("Hello");
    greet1.setUser(user1);
    Greeting greet2 = new Greeting();
    greet2.setGreeting("Hello");
    greet2.setUser(user2);
    greetings.add(greet1);
    greetings.add(greet2);
    return greetings;
  }



}

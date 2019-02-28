package io.devcon5.vertx.messages;

import static org.slf4j.LoggerFactory.getLogger;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

/**
 *
 */

@RunWith(VertxUnitRunner.class)
public class MessageReceiverTest {

  private static final Logger LOG = getLogger(MessageReceiverTest.class);

  @Rule
  public RunTestOnContext context = new RunTestOnContext();
  private EventBus eb;

  @Before
  public void setUp(TestContext ctx) throws Exception {

    Async deployed = ctx.async();
    context.vertx().deployVerticle(ActorTwo.class.getName(), done -> deployed.complete());
    this.eb = context.vertx().eventBus();
  }

  @Test
  public void receiveStringMessage_byExplicitAddress(TestContext ctx) throws Exception {

    Async done = ctx.async();
    Future<Message<String>> result = Future.future();
    eb.send("procoess_request_addr_string", "Bob", result.completer());

    result.setHandler(msg -> {
      ctx.assertTrue(msg.succeeded());
      ctx.assertEquals("Hello Bob", msg.result().body());
      done.complete();
    });
  }

  @Test
  public void receiveStringMessage_byImplicitAddress(TestContext ctx) throws Exception {

    Async done = ctx.async();
    Future<Message<String>> result = Future.future();
    eb.send("ActorTwo.handleStringRequest", "Bob", result.completer());

    result.setHandler(msg -> {
      ctx.assertTrue(msg.succeeded());
      ctx.assertEquals("Hello Bob", msg.result().body());
      done.complete();
    });
  }

  @Test
  public void receiveJsonMessage_byExplicitAddress(TestContext ctx) throws Exception {

    Async done = ctx.async();
    Future<Message<Greeting>> result = Future.future();
    eb.send("procoess_request_addr_json", createUserJson(), result.completer());

    result.setHandler(msg -> {
      ctx.assertTrue(msg.succeeded());
      ctx.assertEquals(new JsonObject().put("greeting", "Hello").put("user", createUserJson()), msg.result().body());
      done.complete();
    });
  }

  @Test
  public void receivePojoMessage_byImplicitAddress(TestContext ctx) throws Exception {

    Async done = ctx.async();
    Future<Message<String>> result = Future.future();
    eb.send("ActorTwo.hello", createUserJson(), result.completer());

    result.setHandler(msg -> {
      if (msg.failed()) {
        LOG.error("", msg.cause());
      }
      ctx.assertTrue(msg.succeeded());
      ctx.assertEquals(new JsonObject().put("greeting", "Hello").put("user", createUserJson()), msg.result().body());
      done.complete();
    });
  }

  @Test
  public void receivePojoMessage_byImplicitAddress_Blocking(TestContext ctx) throws Exception {

    Async done = ctx.async();
    Future<Message<String>> result = Future.future();
    eb.send("ActorTwo.helloBlocking", createUserJson(), result.completer());

    result.setHandler(msg -> {
      if (msg.failed()) {
        LOG.error("", msg.cause());
      }
      ctx.assertTrue(msg.succeeded());
      ctx.assertEquals(new JsonObject().put("greeting", "Hello").put("user", createUserJson()), msg.result().body());
      done.complete();
    });
  }

  @Test
  public void receiveMultiPojoMessage_byImplicitAddress(TestContext ctx) throws Exception {

    Async done = ctx.async();
    Future<Message<String>> result = Future.future();
    eb.send("ActorTwo.helloUsers",
            new JsonArray().add(createUserJson("Bob")).add(createUserJson("Alice")),
            result.completer());

    result.setHandler(msg -> {
      if (msg.failed()) {
        LOG.error("", msg.cause());
      }
      ctx.assertTrue(msg.succeeded());
      ctx.assertEquals(new JsonArray().add(new JsonObject().put("greeting", "Hello").put("user", createUserJson("Bob")))
                                      .add(new JsonObject().put("greeting", "Hello")
                                                           .put("user", createUserJson("Alice"))), msg.result().body());
      done.complete();
    });
  }

  private JsonObject createUserJson(final String name) {

    return new JsonObject().put("name", name);
  }

  private JsonObject createUserJson() {

    return createUserJson("Bob");
  }

}

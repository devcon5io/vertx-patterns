package io.devcon5.vertx.messages;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */

@RunWith(VertxUnitRunner.class)
public class MessageReceiverTest {

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
  public void receiveStringMessage_byAddressAnnotatedPublicHandlerMethod(TestContext ctx) throws Exception {

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
  public void receivePojoMessage_byAddressAnnotatedPrivateHandlerMethod(TestContext ctx) throws Exception {

    Async done = ctx.async();
    Future<Message<Greeting>> result = Future.future();
    eb.send("procoess_request_addr_json", new JsonObject().put("name","Bob"), result.completer());

    result.setHandler(msg -> {
      ctx.assertTrue(msg.succeeded());
      ctx.assertEquals(new JsonObject().put("greeting", "Hello").put("user", new JsonObject().put("name", "Bob")),
                       msg.result().body());
      done.complete();
    });
  }


  @Test
  public void receivePojoMessage_byImplicitAddress(TestContext ctx) throws Exception {

    Async done = ctx.async();
    Future<Message<String>> result = Future.future();
    eb.send("ActorTwo.handleStringRequest", "Bob", result.completer());

    result.setHandler(msg -> {
      ctx.assertTrue(msg.succeeded());
      ctx.assertEquals("Hello Bob", msg.result().body());
      done.complete();
    });
  }



}

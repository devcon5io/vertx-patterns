package io.devcon5.vertx.messages;

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
public class MessageSenderTest {

  @Rule
  public RunTestOnContext context = new RunTestOnContext();

  @Before
  public void setUp(TestContext ctx) throws Exception {
    Async deployed = ctx.async();
    context.vertx().deployVerticle(ActorOne.class.getName(), done -> deployed.complete());
  }

  @Test
  public void sendMessage_ByStringAddress(TestContext ctx) throws Exception {

    Async done = ctx.async();
    Messages.send(ActorOne.PROCESS_REQUEST_STRING, "Bob")
            .setHandler(reply -> {
      ctx.assertTrue(reply.succeeded());
      ctx.assertEquals("Hello Bob", reply.result().body());
      done.complete();
    });
  }

  @Test
  public void sendMessage_byDeclaredContract(TestContext ctx) throws Exception {

    Async done = ctx.async();
    ActorOne.Contract actorOne = Messages.of(ActorOne.class);

    actorOne.hello("bob")
            .setHandler(reply -> {
      ctx.assertTrue(reply.succeeded());
      ctx.assertEquals("Hello bob", reply.result());
      done.complete();
    });

  }

  @Test
  public void sendPojoMessage_byDeclaredContract(TestContext ctx) throws Exception {

    Async done = ctx.async();
    ActorOne.Contract actorOne = Messages.of(ActorOne.class);
    User bob = new User("Bob");

    actorOne.hello(bob)
            .setHandler(reply -> {
      ctx.assertTrue(reply.succeeded());
      ctx.assertEquals("Bob", reply.result().getUser().getName());
      ctx.assertEquals("Hello Bob!", reply.result().toString());
      done.complete();
    });

  }

  @Test
  public void sendPojoMessage_byContract(TestContext ctx) throws Exception {

    Async done = ctx.async();
    ActorOne.Contract actorOne = Messages.ofContract(ActorOne.Contract.class);

    User bob = new User("Bob");

    actorOne.hello(bob)
            .setHandler(reply -> {
      ctx.assertTrue(reply.succeeded());
      ctx.assertEquals("Hello Bob!", reply.result().toString());
      done.complete();
    });

  }

  //blocking APIs are not allowed when running on the eventloop
  @Test(expected = UnsupportedOperationException.class)
  public void sendPojoMessage_byDeclaredContract_blocking_onEventLoop(TestContext ctx) throws Exception {

    ActorOne.Contract actorOne = Messages.of(ActorOne.class);

    User bob = new User("Bob");

    actorOne.helloBlocking(bob);
  }



}

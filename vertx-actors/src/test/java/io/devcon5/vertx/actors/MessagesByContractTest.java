package io.devcon5.vertx.actors;

import io.devcon5.vertx.actors.model.Greeting;
import io.devcon5.vertx.actors.model.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
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
public class MessagesByContractTest {

    @Rule
    public RunTestOnContext context = new RunTestOnContext();

    @Before
    public void setUp(TestContext ctx) throws Exception {
      Async deployed = ctx.async();
      context.vertx().deployVerticle(Actor.class.getName(), done -> deployed.complete());
    }

  @Test
  public void testSimpleMessage(TestContext ctx) throws Exception {

      Contract actor = Actors.withContract(Contract.class);

    actor.hello("Bob").setHandler(assertResult("Hello Bob", ctx));

  }



  private <T> Handler<AsyncResult<T>> assertResult(T expected, final TestContext ctx) {
    Async done = ctx.async();
    return reply -> {
      ctx.assertTrue(reply.succeeded());
      ctx.assertEquals(expected, reply.result());
      done.complete();
    };
  }

  /**
   * An example contract
   */
  public interface Contract {

    Future<String> hello(final String bob);

    Future<Greeting> hello(User bob);

    //this method should not work as it returns an immediate/blocking result
    Greeting helloBlocking(User bob);
  }

  /**
   * An actor that fulfills a contract
   */
  public static class Actor extends AbstractActor implements Contract {

    @Override
    public Future<String> hello(final String name) {
      return Future.succeededFuture("Hello " + name);
    }

    @Override
    public Future<Greeting> hello(final User bob) {

      Greeting g = new Greeting();
      g.setUser(bob);
      g.setGreeting("Hello ");
      return Future.succeededFuture(g);
    }

    @Override
    public Greeting helloBlocking(final User bob) {

      Greeting g = new Greeting();
      g.setUser(bob);
      g.setGreeting("Hello ");
      return g;
    }
  }
}

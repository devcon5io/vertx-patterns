package io.devcon5.vertx.actors;

import static io.vertx.core.logging.LoggerFactory.getLogger;

import io.devcon5.vertx.actors.model.Salutation;
import io.devcon5.vertx.actors.model.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
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

  private static final Logger LOG = getLogger(MessagesByContractTest.class);

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

  @Test
  public void testPojoMessage(TestContext ctx) throws Exception {

    Contract actor = Actors.withContract(Contract.class);

    actor.hello(new User().withName("Bob"))
         .setHandler(assertResult(new Salutation().withGreeting("Hello").withUser(new User().withName("Bob")), ctx));

  }

  private <T> Handler<AsyncResult<T>> assertResult(T expected, final TestContext ctx) {

    Async done = ctx.async();
    return reply -> {
      if (reply.failed()) {
        LOG.error("Test failed", reply.cause());
      }
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

    Future<Salutation> hello(User bob);

    //this method should not work as it returns an immediate/blocking result
    Salutation helloBlocking(User bob);
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
    public Future<Salutation> hello(final User bob) {

      Salutation g = new Salutation();
      g.setUser(bob);
      g.setGreeting("Hello");
      return Future.succeededFuture(g);
    }

    @Override
    public Salutation helloBlocking(final User bob) {

      Salutation g = new Salutation();
      g.setUser(bob);
      g.setGreeting("Hello");
      return g;
    }
  }
}

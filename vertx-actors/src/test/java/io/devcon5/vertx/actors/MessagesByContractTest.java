package io.devcon5.vertx.actors;

import static io.vertx.core.logging.LoggerFactory.getLogger;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    Future<String> actual = actor.hello("Bob");

    actual.setHandler(assertResult(ctx, "Hello Bob"));

  }

  @Test
  public void testPojoMessage(TestContext ctx) throws Exception {

    Contract actor = Actors.withContract(Contract.class);

    Future<Salutation> actual = actor.hello(new User().withName("Bob"));

    final Salutation expected = new Salutation().withGreeting("Hello").withUser(new User().withName("Bob"));
    actual.setHandler(assertResult(ctx, expected));

  }

  @Test
  public void testMultiPojoMessage(TestContext ctx) throws Exception {

    Contract actor = Actors.withContract(Contract.class);

    Future<String> actual = actor.hello(new User().withName("Bob"), new User().withName("Alice"));

    actual.setHandler(assertResult(ctx, "Hello Bob and Alice"));

  }

  @Test
  public void testPojoListMessage(TestContext ctx) throws Exception {


    Contract actor = Actors.withContract(Contract.class);

    final List<User> users = List.of(new User().withName("Bob"), new User().withName("Alice"));

    Future<List<Salutation>> salutes = actor.hello(users);



    final List<Salutation> expected = List.of(new Salutation().withGreeting("Hello")
                                                              .withUser(new User().withName("Bob")),
                                              new Salutation().withGreeting("Hello")
                                                              .withUser(new User().withName("Alice")));
    salutes.setHandler(assertResult(ctx, expected));

  }

  @Test
  public void testNoArgMessage(TestContext ctx) throws Exception {


    Contract actor = Actors.withContract(Contract.class);

    Future<Set<User>> users = actor.whoIsThere();

    users.setHandler(assertContains(ctx));

  }

  @Test
  public void testVoidReturnTypeMessage(TestContext ctx) throws Exception {

    Async done = ctx.async();
    final String replyAddress = "replyAddress";
    context.vertx().eventBus().consumer(replyAddress, msg -> {
      ctx.assertEquals("ok", msg.body());
      done.complete();
    });

    Contract actor = Actors.withContract(Contract.class);

    actor.sendReplyTo(replyAddress);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void nonFutureMethodOnEventLoop() throws Exception {
    Contract actor = Actors.withContract(Contract.class);
    //making a blocking call on the current (eventloop) thread must cause an error
    Salutation greeting = actor.helloBlocking(new User().withName("Bob"));
    LOG.info("{}", greeting);
  }

  @Test
  public void nonFutureMethodCallBlocking(TestContext ctx) throws Exception {
    Contract actor = Actors.withContract(Contract.class);

    Async done = ctx.async();
    //by executing in a blocking way, we ensure it's no executed on a event loop thread
    context.vertx().executeBlocking(fut -> {
      //send a blocking call
      Salutation greeting = actor.helloBlocking(new User().withName("Bob"));

      fut.complete(greeting);
    }, result -> {

      if(result.failed()){
        LOG.error("Execution failed", result.cause());
        ctx.fail(result.cause());
      }
      ctx.assertEquals(new Salutation().withGreeting("Hello").withUser(new User().withName("Bob")), result.result());
      done.complete();
    });

  }


  private Handler<AsyncResult<Set<User>>> assertContains(final TestContext ctx, User... expectedUsers) {

    return reply -> {
      if (reply.failed()) {
        LOG.error("Test failed", reply.cause());
      }
      ctx.assertTrue(reply.succeeded());
      Set<User> actualUsers = reply.result();
      for(User expectedUser : expectedUsers){
        ctx.assertTrue(actualUsers.contains(expectedUser), expectedUser + " was not in the response");
      }
    };
  }

  private <T> Handler<AsyncResult<T>> assertResult(final TestContext ctx, T expected) {

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

    Future<String> hello(final User one, User two);

    Future<Salutation> hello(User bob);

    Future<List<Salutation>> hello(List<User> users);

    Future<Set<User>> whoIsThere();

    void sendReplyTo(String add);

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
    public Future<String> hello(final User one, final User two) {

      return Future.succeededFuture("Hello " + one.getName() + " and " + two.getName());
    }

    @Override
    public Future<Salutation> hello(final User bob) {

      Salutation g = new Salutation();
      g.setUser(bob);
      g.setGreeting("Hello");
      return Future.succeededFuture(g);
    }

    @Override
    public Future<List<Salutation>> hello(final List<User> users) {

      return Future.succeededFuture(users.stream()
                                         .map(user -> new Salutation().withGreeting("Hello").withUser(user))
                                         .collect(Collectors.toList()));
    }

    @Override
    public Future<Set<User>> whoIsThere() {

      Set<User> users = Set.of(new User().withName("Bob"), new User().withName("Alice"), new User().withName("Eve"));
      return Future.succeededFuture(users);
    }

    @Override
    public void sendReplyTo(final String add) {

      vertx.eventBus().send(add, "ok");
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

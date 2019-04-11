package io.devcon5.vertx.actors;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(VertxUnitRunner.class)
public class ActorAutoDeploymentTest {

  @Rule
  public RunTestOnContext context = new RunTestOnContext();


  @Test
  public void deployAll_twoRegisteredActors(TestContext ctx) throws Exception {

    Async async = ctx.async(2);
    Actor.deployAll().setHandler(ready -> {

      ContractOne c1 = Actor.withContract(ContractOne.class);
      ContractTwo c2 = Actor.withContract(ContractTwo.class);

      c1.helloWorld().setHandler(result -> {
        ctx.assertTrue(result.succeeded());
        ctx.assertEquals("Hello World", result.result());
        async.countDown();
      });

      c2.helloTwo().setHandler(result -> {
        ctx.assertTrue(result.succeeded());
        ctx.assertEquals("Hello Default", result.result());
        async.countDown();
      });
    });
  }

  @Test
  public void deployAll_withConfig(TestContext ctx) throws Exception {

    Async async = ctx.async();
    Actor.deployAll(new JsonObject().put("greeting", "Hello Two")).setHandler(ready -> {

      ContractTwo c2 = Actor.withContract(ContractTwo.class);
      c2.helloTwo().setHandler(result -> {
        ctx.assertTrue(result.succeeded());
        ctx.assertEquals("Hello Two", result.result());
        async.complete();
      });
    });
  }


  @Test
  public void deployAll_unregisteredActors_expectsException(TestContext ctx) throws Exception {

    Async async = ctx.async();
    Actor.deployAll().setHandler(ready -> {

      //actor three is not defined in META-INF/services/io.devcon5.vertx.actors.Actor
      ContractThree c3 = Actor.withContract(ContractThree.class);

      c3.helloThree().setHandler(result -> {
        ctx.assertTrue(result.failed());
        async.complete();
      });
    });
  }



  public interface ContractOne {
    Future<String> helloWorld();
  }
  public interface ContractTwo{
    Future<String> helloTwo();
  }
  public interface ContractThree{
    Future<String> helloThree();
  }
  public static class ActorOne extends AbstractActor implements ContractOne {

    @Override
    public Future<String> helloWorld() {

      return Future.succeededFuture("Hello World");
    }
  }
  public static class ActorTwo extends AbstractActor implements ContractTwo{



    @Override
    public Future<String> helloTwo() {
      return Future.succeededFuture(config().getString("greeting", "Hello Default"));
    }
  }
  public static class ActorThree extends AbstractActor implements ContractThree {

    @Override
    public Future<String> helloThree() {

      return Future.succeededFuture("Hello Three");
    }
  }
}

package io.devcon5.vertx.actors;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 *
 */
public abstract class AbstractActor extends AbstractVerticle {

  @Override
  public void start(final Future<Void> startFuture) throws Exception {
    Actors.register(this);
    super.start(startFuture);
  }
}

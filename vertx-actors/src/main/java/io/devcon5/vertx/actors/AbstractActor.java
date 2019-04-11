package io.devcon5.vertx.actors;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * Base class for contract based actors that auto-registers it's contractual interfaces upon start.
 */
public abstract class AbstractActor extends AbstractVerticle implements Actor{

  @Override
  public void start(final Future<Void> startFuture) throws Exception {
    Actors.register(this);
    super.start(startFuture);
  }
}

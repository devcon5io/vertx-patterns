package io.devcon5.vertx.actors;

import io.vertx.core.Verticle;

/**
 * Marker interface for actors.
 * If you wan't to use the {@link java.util.ServiceLoader} mechanism of the {@link io.devcon5.vertx.actors.Actors#deployAll(io.vertx.core.json.JsonObject)}
 * you have to implement this interface
 *
 */
public interface Actor extends Verticle {


}

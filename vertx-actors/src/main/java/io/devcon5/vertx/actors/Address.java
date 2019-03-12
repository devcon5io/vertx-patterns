package io.devcon5.vertx.actors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation on interface method to define which {@link io.vertx.core.eventbus.EventBus} address the
 * method corresponds to.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Address {

  /**
   * The eventbus address. Must be unique in the Vert.x application
   * @return
   */
  String value();

}

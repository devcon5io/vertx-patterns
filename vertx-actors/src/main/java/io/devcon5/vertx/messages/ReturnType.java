package io.devcon5.vertx.messages;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation on interface methods to define what the return type is, i.e. when used with a non-generic Future
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReturnType {

  /**
   * The eventbus address. Must be unique in the Vert.x application
   * @return
   */
  Class value();

}

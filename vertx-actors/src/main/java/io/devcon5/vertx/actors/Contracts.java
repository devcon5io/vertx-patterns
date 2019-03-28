package io.devcon5.vertx.actors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
public final class Contracts {

  private Contracts(){}
  /**
   * Marker annotation for contractual interfaces to either ignore a particular method from being exposed actor
   * method or for ignoring entire interfaces (on the actor type level) from being exposes as contract..
   *
   * When being used on a method, an {@link java.lang.UnsupportedOperationException} will
   * be thrown if being invoked from a contract consumer.
   */
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @interface Ignore{

    /**
     * This optional parameter allows to specify interface class. This value is only evaluated if the annotation
     * is used on an actor on type level. It is not evaluated when being used on a method. Any class defined with
     * this value won't be registered respectively exposed as an contractual interface. This can be used if an actor
     * class implements multiple interfaces, but only some should be exposed as contract while other - those in this
     * list - should not.
     * @return
     *  a list of interface classes that should not be registered as contractual interfaces
     */
    Class[] value() default {};
  }

}

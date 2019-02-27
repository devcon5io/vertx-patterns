package io.devcon5.vertx.messages;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to decorate a Verticle and specify an Interface that defines the Contract or Messages
 * a Verticle may receive. The referenced interface's methods must be annotated with {@link Address}
 * in order to link the messages with {@link io.vertx.core.eventbus.EventBus} addresses.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Contract {

  /**
   * The interface class that defines the messages the verticle may receive
   * @return
   */
  Class value();

}

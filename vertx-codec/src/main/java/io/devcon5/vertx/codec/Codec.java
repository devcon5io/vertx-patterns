package io.devcon5.vertx.codec;

/**
 * Describes a oodec to encode or decode messages for the Vert.x event bus.
 * @param <MSG> the type of the encoded form. This type must be compatible with
 *             the Vert.x event bus types (JsonObject, JsonArray, Buffer, String)
 */
public interface Codec<MSG> {

  /**
   * Encodes a Java object so it can be sent over the event bus
   * @param encode
   *  the object to encode
   * @return
   *  a representation of the object that can be sent over the event bus
   */
  <IN> MSG encode(IN encode);

  /**
   * Decodes a message from the event bus into a type supported by this codec
   * @param message
   *  the message object received over the event bus
   * @return
   *  a type-specific representation of the message payload
   */
  <OUT> OUT decode(MSG message);


}

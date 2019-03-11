package io.devcon5.vertx.codec;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 *
 */
public class DynamicCodec implements MessageCodec {

  public static void registerCodecs(EventBus eb, Class<?> contract){

  }

  public static boolean isNativeType(Class<?> type) {

    return type.isPrimitive()
        || type == String.class
        || type == byte[].class
        || Buffer.class.isAssignableFrom(type)
        || type.isAssignableFrom(JsonObject.class)
        || type.isAssignableFrom(JsonArray.class);
  }

  @Override
  public void encodeToWire(final Buffer buffer, final Object o) {

    if (isNativeType(o.getClass())) {
      encodeNative(buffer, o);
    } else {
      buffer.appendBuffer( Json.encodeToBuffer(o));
    }
  }

  @Override
  public Object decodeFromWire(int pos, final Buffer buffer) {
    int length = buffer.getInt(pos);
    pos += 4;
    return new JsonObject(buffer.slice(pos, pos + length));
  }

  @Override
  public Object transform(final Object o) {
    return o;
  }

  @Override
  public String name() {

    return "dynamic-pojo-codec";
  }

  @Override
  public byte systemCodecID() {

    return 97;
  }

  private void encodeNative(final Buffer buffer, final Object o) {

    if (o instanceof Integer) {
      buffer.appendInt((Integer) o);
    } else if (o instanceof Long) {
      buffer.appendLong((Long) o);
    } else if (o instanceof Short) {
      buffer.appendShort((Short) o);
    } else if (o instanceof Float) {
      buffer.appendFloat((Float) o);
    } else if (o instanceof Double) {
      buffer.appendDouble((Double) o);
    } else if (o instanceof Byte) {
      buffer.appendByte((Byte) o);
    } else if (o instanceof byte[]) {
      buffer.appendBytes((byte[]) o);
    } else if (o instanceof String) {
      buffer.appendString((String) o);
    } else if (o instanceof Buffer) {
      buffer.appendBuffer((Buffer) o);
    } else if (o instanceof JsonObject) {
      buffer.appendBuffer(((JsonObject) o).toBuffer());
    } else if (o instanceof JsonArray) {
      buffer.appendBuffer(((JsonArray) o).toBuffer());
    }
  }
}

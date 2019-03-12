package io.devcon5.vertx.codec;

import static io.devcon5.vertx.codec.GenericTypeDecoding.decode;

import java.lang.reflect.Type;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;

/**
 * A codec for the return type of a method.
 */
public class GenericTypeCodec implements MessageCodec<Object, Object> {

  private final Type type;
  private final String name;

  public GenericTypeCodec(final Type type) {
    this.type = type;
    this.name = codecNameFor(type);
  }

  public static String codecNameFor(final Type type) {
    return type.getTypeName();
  }

  @Override
  public void encodeToWire(final Buffer buffer, final Object o) {
    Buffer objBuffer = Json.encodeToBuffer(o);
    buffer.appendInt(objBuffer.length());
    buffer.appendBuffer(objBuffer);
  }

  @Override
  public Object decodeFromWire(int pos, final Buffer buffer) {
    int length = buffer.getInt(pos);
    pos += 4;
    return decode(buffer.slice(pos, pos + length), type);
  }

  @Override
  public Object transform(final Object o) {

    return o;
  }

  @Override
  public String name() {

    return name;
  }

  @Override
  public byte systemCodecID() {

    return -1;
  }


}

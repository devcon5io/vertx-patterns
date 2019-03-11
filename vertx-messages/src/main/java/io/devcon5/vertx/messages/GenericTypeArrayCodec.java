package io.devcon5.vertx.messages;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;

/**
 *
 */
public class GenericTypeArrayCodec implements MessageCodec<Object[], Object[]> {

  private final String name;
  private final Type[] types;

  public GenericTypeArrayCodec(Type[] types) {
    this.types = types;
    this.name = codecNameFor(types);
  }

  /**
   * Generates a codec name for the signature of the method. The codec name consists
   * of the declaring class, the method name and the argument types. No return type or
   * visibility modifier is used
   *
   * @param types
   *     the array of types to generate a codec name for
   *
   * @return a codec name for the method's signature. If the method has a single argument that is
   * natively supported by the vert.x event bus, null is returned indicating no codec is needed.
   */
  public static String codecNameFor(Type[] types) {
    final StringBuilder buf = new StringBuilder(128);
    buf.append('[');
    for (int i = 0, len = types.length; i < len; i++) {
      Type t = types[0];
      if(t instanceof ParameterizedType){

      }

      buf.append(types[i].getTypeName());

      if (i < len - 1) {
        buf.append(',');
      }
    }
    buf.append(']');
    return buf.toString();
  }

  @Override
  public void encodeToWire(final Buffer buffer, final Object[] objects) {

    final JsonArray arr = new JsonArray();
    for (Object o : objects) {
      arr.add(Json.encode(o));
    }
    Buffer encoded = arr.toBuffer();
    buffer.appendInt(encoded.length());
    buffer.appendBuffer(encoded);
  }

  @Override
  public Object[] decodeFromWire(int pos, final Buffer buffer) {

    int length = buffer.getInt(pos);
    pos += 4;
    final JsonArray arr = new JsonArray(buffer.slice(pos, pos + length));

    final Object[] result = new Object[arr.size()];
    for (int i = 0, len = arr.size(); i < len; i++) {
      result[i] = GenericTypeCodec.decode(arr.getValue(i), this.types[i]);
    }
    return result;
  }

  @Override
  public Object[] transform(final Object[] objects) {

    return objects;
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

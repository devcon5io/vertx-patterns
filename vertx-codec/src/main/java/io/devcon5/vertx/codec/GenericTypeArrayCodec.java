package io.devcon5.vertx.codec;

import static io.devcon5.vertx.codec.GenericTypes.decode;
import static io.devcon5.vertx.codec.GenericTypes.unwrapFutureType;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;

/**
 * A codec for arrays of generic types. This includes raw types (such as .class) and generic types (i.e. List&lt;
 * Pojo&gt;). This can be used for encoding arrays of mixed types, such as in method signatures. To encode
 * arrays of a single type (i.e. Pojo[]), use the {@link io.devcon5.vertx.codec.GenericTypeCodec}.
 * <br>
 * The codec implements the flyweight pattern if instantiated using the {@link #forType(java.lang.reflect.Type[])}
 * method with an instance cache size that can be configured using the <code>codec.instanceCacheSize</code>
 * jvm parameter that defaults to 1024.
 */
public class GenericTypeArrayCodec implements MessageCodec<Object[], Object[]> {

  //we can't use the type array as a key as it may be a different instance each time the signature
  //of a method is read so we use a hash of the types instead. This implies, we cannot use a weak hashmap
  //as the references to the type-array / the hash value maybe freed too eagerly making the cache ineffective.
  //so we use a linkedHashMap instead as an LRU cache with 1024 entries (=instances).
  private static final ThreadLocal<Map<Integer, GenericTypeArrayCodec>> INSTANCE_CACHE = ThreadLocal.withInitial(() -> new LinkedHashMap<>(
      Integer.getInteger("codec.instanceCacheSize", 1024),
      0.75f,
      true));
  private final String name;
  private final Type[] types;

  public GenericTypeArrayCodec(Type[] types) {

    this.types = types;
    this.name = codecNameFor(types);
  }

  public static GenericTypeArrayCodec forType(Type[] type) {

    return INSTANCE_CACHE.get().computeIfAbsent(Objects.hash((Object[])type), hash -> new GenericTypeArrayCodec(type));
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
      final Type t = types[i];
      buf.append(unwrapFutureType(t).getTypeName());

      if (i < len - 1) {
        buf.append(", ");
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
      result[i] = decode(arr.getValue(i), this.types[i]);
    }
    return result;
  }

  @Override
  public Object[] transform(final Object[] objects) {
    //we can not pass the object here directly as this would allow to modify the object on the caller side
    //while the receiver is reading it and as both could happen in different threads, this would not be threadsafe and
    //prone to side effects

    return GenericTypeTransformation.copy(objects, this);
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

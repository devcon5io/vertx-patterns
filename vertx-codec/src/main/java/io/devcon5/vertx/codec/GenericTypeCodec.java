package io.devcon5.vertx.codec;

import static io.devcon5.vertx.codec.GenericTypes.decode;
import static io.devcon5.vertx.codec.GenericTypes.unwrapFutureType;
import static io.devcon5.vertx.codec.GenericTypes.getRawType;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;

/**
 * A codec for generic types. This includes raw types (such as .class) and generic types (i.e. List&lt;
 * * Pojo&gt;). This can be used to encode/decode single types such as in return types of a method or single fields.
 * <br>
 * The codec implements the flyweight pattern if instantiated using the {@link #forType(java.lang.reflect.Type)}
 * method with an instance cache size that can be configured using the <code>codec.instanceCacheSize</code>
 * jvm parameter that defaults to 1024.
 */
public class GenericTypeCodec implements MessageCodec<Object, Object> {

  //we use a linkedHashMap as LRU cache instead of a weak hashmap to have better control over instance creation
  //and number of instances kept in cache. The cache is thread local so that each thread (i.e. eventloop threads)
  //have their own cache. That way we don't have to synchronize the cache itself, making it a potential bottleneck
  //with the tradeoff that we have to create more instances of the (rather small) codec per jvm
  private static final ThreadLocal<Map<Type, GenericTypeCodec>> INSTANCE_CACHE = ThreadLocal.withInitial(() -> new LinkedHashMap<>(
      Integer.getInteger("codec.instanceCacheSize", 1024),
      0.75f,
      true));
  private final Type type;
  private final String name;

  public GenericTypeCodec(final Type type) {

    this.type = type;
    this.name = codecNameFor(type);
  }

  /**
   * Factory method that uses an internal instance cache to allow flyweight pattern as the codecs itself are
   * stateless and instances can be reused.
   *
   * @param type
   *     the type to create a codec for
   *
   * @return a codec for the specified type
   */
  public static GenericTypeCodec forType(Type type) {

    return INSTANCE_CACHE.get().computeIfAbsent(type, GenericTypeCodec::new);
  }

  public static String codecNameFor(final Type type) {

    if (GenericTypes.isSimpleType(type)) {
      return null;
    }
    return getRawType(type).getTypeName();
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
    return decode(buffer.slice(pos, pos + length), unwrapFutureType(type));
  }

  @Override
  public Object transform(final Object o) {

    //we can not pass the object here directly as this would allow to modify the object on the caller side
    //while the receiver is reading it and as both could happen in different threads, this would not be threadsafe and
    //prone to side effects
    return GenericTypeTransformation.copy(o, this);
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

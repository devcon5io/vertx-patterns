package io.devcon5.vertx.codec;

import static io.vertx.core.logging.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.logging.Logger;
import io.vertx.core.shareddata.Shareable;

/**
 * Helper class to define an efficient way for creating copies of objects when being passed between actors inside the
 * same JVM.
 */
public final class GenericTypeTransformation {

  private static final Logger LOG = getLogger(GenericTypeTransformation.class);

  private GenericTypeTransformation() {

  }

  public static Object[] copy(Object[] object, MessageCodec<Object[], Object[]> codec) {

    //we can not pass the object here directly as this would allow to modify the object on the caller side
    //while the receiver is reading it and as both could happen in different threads, this would not be threadsafe and
    //prone to side effects
    Object[] copy;

    //TODO check if it's faster to immediately use codec copy
    if ((copy = tryArrayCopy(object))!= null ||
        (copy = tryCodecCopy(object, codec)) != null) {
      return copy;
    }

    throw new UnsupportedOperationException("Creating a copy of object of type "
                                                + object.getClass()
                                                + " not supported");
  }

  private static Object[] tryArrayCopy(final Object[] object) {

    final Object[] result = new Object[object.length];
    Object copy;

    for(int i = 0, len = object.length; i < len; i++){

      if ((copy = tryPrimitiveOrShareable(object[i])) != null ||
          (copy = trySerializationCopy(object[i])) != null) {
        result[i] = copy;
      } else {
        return null;
      }
    }
    return result;
  }

  public static Object copy(Object object, MessageCodec<Object, Object> codec) {

    //we can not pass the object here directly as this would allow to modify the object on the caller side
    //while the receiver is reading it and as both could happen in different threads, this would not be threadsafe and
    //prone to side effects
    Object copy;

    if ((copy = tryPrimitiveOrShareable(object)) != null ||
        (copy = trySerializationCopy(object)) != null ||
        (copy = tryCodecCopy(object, codec)) != null) {
      return copy;
    }

    throw new UnsupportedOperationException("Creating a copy of object of type "
                                                + object.getClass()
                                                + " not supported");
  }

  private static <T> T tryCodecCopy(final T object, final MessageCodec<T, T> codec) {

    final Buffer buf = Buffer.buffer();
    codec.encodeToWire(buf, object);
    return codec.decodeFromWire(0, buf);
  }

  private static <T> T tryPrimitiveOrShareable(final T object) {

    Class type = object.getClass();
    if(GenericTypes.isPrimitive(type)) {
      return object;
    }
    if (Shareable.class.isAssignableFrom(type)) {
      return (T) ((Shareable) object).copy();
    }
    return null;
  }

  private static <T> T trySerializationCopy(final T object) {

    if (object instanceof Serializable) {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
           ObjectOutputStream dos = new ObjectOutputStream(baos)) {
        //serialize
        dos.writeObject(object);

        //deserialize
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
          return (T) ois.readObject();
        }
      } catch (ClassNotFoundException | IOException e) {
        LOG.trace("Creating a serialization copy failed", e);
      }
    }
    return null;
  }
}

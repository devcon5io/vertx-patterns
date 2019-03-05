package io.devcon5.vertx.messages;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 *
 */
public class MethodArgsCodec implements MessageCodec<Object[], Object[]> {

  private final Method method;
  private final String name;

  public MethodArgsCodec(Method method) {

    this.method = method;
    this.name = codecNameFor(method);
  }

  /**
   * Generates a codec name for the signature of the method. The codec name consists
   * of the declaring class, the method name and the argument types. No return type or
   * visibility modifier is used
   *
   * @param m
   *     the method to generate a codec name for
   *
   * @return a codec name for the method's signature
   */
  public static String codecNameFor(Method m) {

    final StringBuilder buf = new StringBuilder(128);
    buf.append(m.getDeclaringClass().getName()).append("::");
    buf.append(m.getName()).append('(');
    final Class[] argTypes = m.getParameterTypes();
    for (int i = 0, len = argTypes.length; i < len; i++) {
      buf.append(argTypes[i].getName());
      if (i < len - 1) {
        buf.append(',');
      }
    }
    buf.append(')');
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

    final Type[] genericType = method.getGenericParameterTypes();
    final Class[] paramTypes = method.getParameterTypes();
    final Object[] result = new Object[arr.size()];

    for (int i = 0, len = arr.size(); i < len; i++) {
      result[i] = decode(arr.getValue(i), paramTypes[i], genericType[i]);
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

  private Object decode(final Object value, final Class type, final Type genericType) {

    if (isSimpleType(type)) {
      return value;
    } else {
      return decodeComplexType(value, type, genericType);
    }
  }

  private Object decodeComplexType(final Object value, final Class type, final Type genericType) {

    String valueAsString = (String) value;
    if (!Collection.class.isAssignableFrom(type)
        && !Map.class.isAssignableFrom(type)) {
      return Json.decodeValue(valueAsString, type);
    }
    if (genericType instanceof ParameterizedType) {
      final Type[] itemTypes = ((ParameterizedType) genericType).getActualTypeArguments();
      if(Map.class.isAssignableFrom(type)){
        final JsonObject rawMap = new JsonObject(valueAsString);
        return decodeToMap(rawMap, (Class)itemTypes[0], (Class)itemTypes[1]);
      }
      final JsonArray arr = new JsonArray(valueAsString);
      if (List.class.isAssignableFrom(type)) {
        return decodeToList(arr, (Class) itemTypes[0]);
      }
      if (Set.class.isAssignableFrom(type)) {
        return decodeToSet(arr, (Class) itemTypes[0]);
      }
    }
    return null;
  }

  private List decodeToList(final JsonArray arr, final Class itemType) {
    final List list = new ArrayList();
    for (Object o : arr) {
      list.add(decodeValue(o, itemType));
    }
    return list;
  }

  private Set decodeToSet(final JsonArray arr, final Class itemType) {
    final Set set = new HashSet();
    for (Object o : arr) {
      set.add(decodeValue(o, itemType));
    }
    return set;
  }
  private Map decodeToMap(final JsonObject obj, final Class keyType, Class valueType) {
    final Map map = new HashMap();
    for(Map.Entry e : obj){
      map.put(decodeValue(e.getKey(), keyType), decodeValue(e.getValue(), valueType));
    }
    return map;
  }


  private Object decodeValue(final Object o, final Class itemType) {

    Object decodedValue;
    if(isSimpleType(itemType)){
      decodedValue = o;
    } else {
      decodedValue = Json.mapper.convertValue(o, itemType);
    }
    return decodedValue;
  }

  private boolean isSimpleType(final Class type) {

    return type.isPrimitive()
        || type == String.class
        || type == Byte.class
        || type == Short.class
        || type == Integer.class
        || type == Long.class
        || type == Float.class
        || type == Double.class
        || type == Boolean.class
        || type == byte[].class;
  }
}

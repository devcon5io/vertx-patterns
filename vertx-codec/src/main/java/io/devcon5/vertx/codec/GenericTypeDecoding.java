package io.devcon5.vertx.codec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 *
 */
public final class GenericTypeDecoding {

  private GenericTypeDecoding(){}

  public static Object decode(final Object value, Type type) {

    if (isSimpleType(type)) {
      return value;
    } else {
      return decodeComplexType(value, type);
    }
  }

  public static Object decodeComplexType(final Object value, Type type) {

    final Class rawType = getRawType(type);

    final String valueAsString = (String) value;
    if (!Collection.class.isAssignableFrom(rawType)
        && !Map.class.isAssignableFrom(rawType)) {
      return Json.decodeValue(valueAsString, rawType);
    }

    if (type instanceof ParameterizedType) {
      final Type[] itemTypes = ((ParameterizedType) type).getActualTypeArguments();

      if(Map.class.isAssignableFrom(rawType)){
        final JsonObject rawMap = new JsonObject(valueAsString);
        return decodeMap(rawMap, (Class)itemTypes[0], (Class)itemTypes[1]);
      }

      final JsonArray arr = new JsonArray(valueAsString);
      if (List.class.isAssignableFrom(rawType)) {
        return decodeList(arr, (Class) itemTypes[0]);
      }
      if (Set.class.isAssignableFrom(rawType)) {
        return decodeSet(arr, (Class) itemTypes[0]);
      }
    }
    return null;
  }

  public static Object decodeValue(final Object o, final Type itemType) {

    Object decodedValue;
    if(isSimpleType(itemType)){
      decodedValue = o;
    } else {
      decodedValue = Json.mapper.convertValue(o, getRawType(itemType));
    }
    return decodedValue;
  }

  public static List decodeList(final JsonArray arr, final Class itemType) {
    final List list = new ArrayList();
    for (Object o : arr) {
      list.add(decodeValue(o, itemType));
    }
    return list;
  }

  public static Set decodeSet(final JsonArray arr, final Class itemType) {
    final Set set = new HashSet();
    for (Object o : arr) {
      set.add(decodeValue(o, itemType));
    }
    return set;
  }
  public static Map decodeMap(final JsonObject obj, final Class keyType, Class valueType) {
    final Map map = new HashMap();
    for(Map.Entry e : obj){
      map.put(decodeValue(e.getKey(), keyType), decodeValue(e.getValue(), valueType));
    }
    return map;
  }

  public static Class getRawType(final Type type) {

    Class rawType;
    if(type instanceof Class){
      rawType = (Class) type;
    } else if(type instanceof ParameterizedType){
      rawType = (Class)((ParameterizedType) type).getRawType();
    } else {
      rawType = Object.class;
    }
    return rawType;
  }

  /**
   * Checks if the specified type is natively supported by Vertx. Native types are:
   * <ul>
   *   <li>all primitive types and their Object-type counterparts</li>
   *   <li>byte[]</li>
   *   <li>{@link java.lang.String}</li>
   *   <li>{@link io.vertx.core.buffer.Buffer}</li>
   *   <li>{@link io.vertx.core.json.JsonObject}</li>
   *   <li>{@link io.vertx.core.json.JsonArray}</li>
   * </ul>
   * @param type
   *  the type to check
   * @return
   *  true if the type is natively supported by vertx and no decoder for that type is needed
   */
  public static boolean isSimpleType(final Type type) {

    return type instanceof Class
        && (((Class)type).isPrimitive()
        || type == String.class
        || type == Byte.class
        || type == Short.class
        || type == Integer.class
        || type == Long.class
        || type == Float.class
        || type == Double.class
        || type == Boolean.class
        || type == byte[].class
        || type == JsonObject.class
        || type == JsonArray.class);
  }
}

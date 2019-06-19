package io.devcon5.vertx.codec;

import static org.junit.Assert.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.*;

/**
 *
 */
public class GenericTypesTest {

  @Test
  public void decode_simpleTypes_boolean() {
    assertEquals(true, GenericTypes.decode(true, boolean.class));
    assertEquals(Boolean.TRUE, GenericTypes.decode(true, Boolean.class));
  }

  @Test
  public void decode_simpleTypes_byte() {

    assertEquals((byte) 12, GenericTypes.decode((byte)12, byte.class));
    assertEquals(Byte.valueOf("12"), GenericTypes.decode(Byte.valueOf((byte) 12), Byte.class));
  }

  @Test
  public void decode_simpleTypes_short() {

    assertEquals((short) 512, GenericTypes.decode((short)512, short.class));
    assertEquals(Short.valueOf("512"), GenericTypes.decode(Short.valueOf((short) 512), Short.class));
  }

  @Test
  public void decode_simpleTypes_int() {

    assertEquals(128000, GenericTypes.decode(128000, int.class));
    assertEquals(Integer.valueOf("128000"), GenericTypes.decodeValue(Integer.valueOf(128000), Integer.class));
  }

  @Test
  public void decode_simpleTypes_long() {
    assertEquals(256000000L, GenericTypes.decode(256000000L, long.class));
    assertEquals(Long.valueOf("256000000"), GenericTypes.decode(Long.valueOf(256000000L), Long.class));
  }

  @Test
  public void decode_simpleTypes_float() {

    assertEquals(256.0f, GenericTypes.decodeValue(256.0f, float.class));
    assertEquals(Float.valueOf("256.0"), GenericTypes.decodeValue(Float.valueOf(256.0f), Float.class));
  }

  @Test
  public void decode_simpleTypes_double() {

    assertEquals(256.0, GenericTypes.decode(256.0, double.class));
    assertEquals(Double.valueOf("256.0"), GenericTypes.decode(Double.valueOf(256.0), Double.class));
  }

  @Test
  public void decode_simpleTypes_byteArray() {

    assertArrayEquals(new byte[] { 1, 2, 3 },
                      (byte[]) GenericTypes.decode(new byte[] { 1, 2, 3 }, byte[].class));
  }

  @Test
  public void decode_simpleTypes_buffer() {

    assertEquals(Buffer.buffer("123"), GenericTypes.decodeValue(Buffer.buffer("123"), Buffer.class));
  }

  @Test
  public void decode_simpleTypes_JsonObject() {

    assertEquals(new JsonObject().put("1", "one"),
                 GenericTypes.decode(new JsonObject().put("1", "one"), JsonObject.class));
  }


  @Test
  public void decode_simpleTypes_JsonArray() {

    assertEquals(new JsonArray().add("1").add("2"),
                 GenericTypes.decode(new JsonArray().add("1").add("2"), JsonArray.class));
  }


  @Test
  public void decode_complexTypes_pojo() {

    final String input = new JsonObject().put("name", "bob").toString();
    assertEquals(new Pojo().withName("bob"),
                 GenericTypes.decode(input, Pojo.class));
  }

  @Test
  public void decode_complexTypes_listOfPojos() {

    final String input = new JsonArray().add(new JsonObject().put("name", "bob"))
                                        .add(new JsonObject().put("name", "alice"))
                                        .toString();

    ParameterizedType pt = new ParameterizedTypeImpl(List.class, null, Pojo.class);

    assertEquals(Arrays.asList(new Pojo().withName("bob"),
                               new Pojo().withName("alice")),
                 GenericTypes.decode(input, pt));
  }

  @Test
  public void decode_complexTypes_setOfPojos() {

    final String input = new JsonArray().add(new JsonObject().put("name", "bob"))
                                        .add(new JsonObject().put("name", "alice"))
                                        .add(new JsonObject().put("name", "alice"))
                                        .toString();

    ParameterizedType pt = new ParameterizedTypeImpl(Set.class, null, Pojo.class);

    assertEquals(new HashSet<>(Arrays.asList(new Pojo().withName("bob"),
                                             new Pojo().withName("alice"))),
                 GenericTypes.decode(input, pt));
  }

  @Test
  public void decode_complexTypes_mapOfPojos() {

    final String input = new JsonObject().put("bob", new JsonObject().put("name", "bob"))
                                        .put("alice", new JsonObject().put("name", "alice"))
                                        .toString();

    ParameterizedType pt = new ParameterizedTypeImpl(Map.class, null, String.class,Pojo.class);

    Map<String, Pojo> pojos = new HashMap<>();
    pojos.put("bob", new Pojo().withName("bob"));
    pojos.put("alice", new Pojo().withName("alice"));

    assertEquals(pojos, GenericTypes.decode(input, pt));
  }



  @Test
  public void decodeValue_boolean() {

    assertEquals(true, GenericTypes.decodeValue(true, boolean.class));
    assertEquals(Boolean.TRUE, GenericTypes.decodeValue(true, Boolean.class));
  }

  @Test
  public void decodeValue_byte() {

    assertEquals((byte) 12, GenericTypes.decodeValue((byte)12, byte.class));
    assertEquals(Byte.valueOf("12"), GenericTypes.decodeValue(Byte.valueOf((byte) 12), Byte.class));
  }

  @Test
  public void decodeValue_short() {

    assertEquals((short) 512, GenericTypes.decodeValue((short)512, short.class));
    assertEquals(Short.valueOf("512"), GenericTypes.decodeValue(Short.valueOf((short) 512), Short.class));
  }

  @Test
  public void decodeValue_int() {

    assertEquals(128000, GenericTypes.decodeValue(128000, int.class));
    assertEquals(Integer.valueOf("128000"), GenericTypes.decodeValue(Integer.valueOf(128000), Integer.class));
  }

  @Test
  public void decodeValue_long() {

    assertEquals(256000000L, GenericTypes.decodeValue(256000000L, long.class));
    assertEquals(Long.valueOf("256000000"), GenericTypes.decodeValue(Long.valueOf(256000000L), Long.class));
  }

  @Test
  public void decodeValue_float() {

    assertEquals(256.0f, GenericTypes.decodeValue(256.0f, float.class));
    assertEquals(Float.valueOf("256.0"), GenericTypes.decodeValue(Float.valueOf(256.0f), Float.class));
  }

  @Test
  public void decodeValue_double() {

    assertEquals(256.0, GenericTypes.decodeValue(256.0, double.class));
    assertEquals(Double.valueOf("256.0"), GenericTypes.decodeValue(Double.valueOf(256.0), Double.class));
  }

  @Test
  public void decodeValue_byteArray() {

    assertArrayEquals(new byte[] { 1, 2, 3 },
                      (byte[]) GenericTypes.decodeValue(new byte[] { 1, 2, 3 }, byte[].class));
  }

  @Test
  public void decodeValue_buffer() {

    assertEquals(Buffer.buffer("123"), GenericTypes.decodeValue(Buffer.buffer("123"), Buffer.class));
  }

  @Test
  public void decodeValue_JsonObject() {

    assertEquals(new JsonObject().put("1", "one"),
                 GenericTypes.decodeValue(new JsonObject().put("1", "one"), JsonObject.class));
  }


  @Test
  public void decodeValue_JsonArray() {

    assertEquals(new JsonArray().add("1").add("2"),
                 GenericTypes.decodeValue(new JsonArray().add("1").add("2"), JsonArray.class));
  }


  @Test
  public void decodeValue_pojo() {

    assertEquals(new Pojo().withName("bob"), GenericTypes.decodeValue(new JsonObject().put("name", "bob"), Pojo.class));
  }

  @Test
  public void decodeList_simpleType() {

    JsonArray input = new JsonArray().add("1").add("1").add("2");

    List<String> list = GenericTypes.decodeList(input, String.class);

    assertEquals("1", list.get(0));
    assertEquals("1", list.get(1));
    assertEquals("2", list.get(2));
    assertEquals(3, list.size());
  }

  @Test
  public void decodeList_pojoType() {

    JsonArray input = new JsonArray().add(new Pojo().withName("alice").toJson())
                                     .add(new Pojo().withName("alice").toJson())
                                     .add(new Pojo().withName("bob").toJson());

    List<Pojo> list = GenericTypes.decodeList(input, Pojo.class);

    assertEquals(new Pojo().withName("alice"), list.get(0));
    assertEquals(new Pojo().withName("alice"), list.get(1));
    assertEquals(new Pojo().withName("bob"), list.get(2));
    assertEquals(3, list.size());
  }

  @Test
  public void decodeSet_simpleType() {

    JsonArray input = new JsonArray().add("1").add("1").add("2");

    Set<String> set = GenericTypes.decodeSet(input, String.class);

    assertTrue(set.contains("1"));
    assertTrue(set.contains("2"));
    assertEquals(2, set.size());
  }

  @Test
  public void decodeSet_pojoTypeAsJson() {

    JsonArray input = new JsonArray().add(new Pojo().withName("alice").toJson())
                                     .add(new Pojo().withName("bob").toJson())
                                     .add(new Pojo().withName("alice").toJson());

    Set<String> set = GenericTypes.decodeSet(input, Pojo.class);

    assertTrue(set.contains(new Pojo().withName("alice")));
    assertTrue(set.contains(new Pojo().withName("bob")));
    assertEquals(2, set.size());
  }

  @Test
  public void decodeMap_simpleType() {

    JsonObject input = new JsonObject().put("1", 1).put("2", 2);

    Map<String, Integer> map = GenericTypes.decodeMap(input, String.class, Integer.class);

    assertEquals(Integer.valueOf(1), map.get("1"));
    assertEquals(Integer.valueOf(2), map.get("2"));
    assertEquals(2, map.size());

  }

  @Test
  public void decodeMap_pojoType() {

    JsonObject input = new JsonObject().put("1", new Pojo().withName("bob").toJson())
                                       .put("2", new Pojo().withName("alice").toJson());

    Map<String, Pojo> map = GenericTypes.decodeMap(input, String.class, Pojo.class);

    assertEquals(new Pojo().withName("bob"), map.get("1"));
    assertEquals(new Pojo().withName("alice"), map.get("2"));
    assertEquals(2, map.size());

  }

  @Test
  public void getRawType_class() {

    assertEquals(String.class, GenericTypes.getRawType(String.class));
  }

  @Test
  public void getRawType_parametrizedType() throws Exception {

    Type type = GenericExample.class.getMethod("getGenericList").getGenericReturnType();
    assertEquals(List.class, GenericTypes.getRawType(type));
  }

  @Test
  public void getRawType_wildcardType() throws Exception {

    Type type = GenericExample.class.getMethod("getWildcardList").getGenericReturnType();
    assertEquals(List.class, GenericTypes.getRawType(type));
  }

  @Test
  public void getRawType_genericArrayType() throws Exception {

    Type type = GenericExample.class.getMethod("getGenericListArray").getGenericReturnType();
    assertEquals(List.class, GenericTypes.getRawType(type));
  }

  @Test
  public void getRawType_otherType() throws Exception {

    Type type = new Type() {

    };
    assertEquals(Object.class, GenericTypes.getRawType(type));
  }

  @Test
  public void isSimpleType_String() {

    assertTrue(GenericTypes.isSimpleType(String.class));
  }

  @Test
  public void isSimpleType_boolean() {

    assertTrue(GenericTypes.isSimpleType(boolean.class));
    assertTrue(GenericTypes.isSimpleType(Boolean.class));
  }

  @Test
  public void isSimpleType_byte() {

    assertTrue(GenericTypes.isSimpleType(byte.class));
    assertTrue(GenericTypes.isSimpleType(Byte.class));
  }

  @Test
  public void isSimpleType_short() {

    assertTrue(GenericTypes.isSimpleType(short.class));
    assertTrue(GenericTypes.isSimpleType(Short.class));
  }

  @Test
  public void isSimpleType_int() {

    assertTrue(GenericTypes.isSimpleType(int.class));
    assertTrue(GenericTypes.isSimpleType(Integer.class));
  }

  @Test
  public void isSimpleType_long() {

    assertTrue(GenericTypes.isSimpleType(long.class));
    assertTrue(GenericTypes.isSimpleType(Long.class));
  }

  @Test
  public void isSimpleType_float() {

    assertTrue(GenericTypes.isSimpleType(float.class));
    assertTrue(GenericTypes.isSimpleType(Float.class));
  }

  @Test
  public void isSimpleType_double() {

    assertTrue(GenericTypes.isSimpleType(double.class));
    assertTrue(GenericTypes.isSimpleType(Double.class));
  }

  @Test
  public void isSimpleType_byteArray() {

    assertTrue(GenericTypes.isSimpleType(byte[].class));
  }

  @Test
  public void isSimpleType_Buffer() {

    assertTrue(GenericTypes.isSimpleType(Buffer.class));
  }

  @Test
  public void isSimpleType_JsonObject() {

    assertTrue(GenericTypes.isSimpleType(JsonObject.class));
  }

  @Test
  public void isSimpleType_JsonArray() {

    assertTrue(GenericTypes.isSimpleType(JsonArray.class));
  }

  @Test
  public void isSimpleType_Object_false() {

    assertFalse(GenericTypes.isSimpleType(Object.class));
  }

  @Test
  public void isSimpleType_List_false() {

    assertFalse(GenericTypes.isSimpleType(List.class));
  }

  @Test
  public void isSimpleType_Map_false() {

    assertFalse(GenericTypes.isSimpleType(Map.class));
  }

  @Test
  public void isSimpleType_Set_false() {

    assertFalse(GenericTypes.isSimpleType(Set.class));
  }

  @Test
  public void isSimpleType_Pojo_false() {

    assertFalse(GenericTypes.isSimpleType(Pojo.class));
  }

  @Test
  public void unwrapFuture_nonGenericFuture_Object() throws Exception {
    Type type = FutureExample.getType("objectFuture");

    Type unwrapped = GenericTypes.unwrapFutureType(type);

    assertEquals(Object.class, unwrapped);
  }
  @Test
  public void unwrapFuture_pojoFuture_pojoType() throws Exception {
    Type type = FutureExample.getType("pojoFuture");

    Type unwrapped = GenericTypes.unwrapFutureType(type);

    assertEquals(Pojo.class, unwrapped);
  }

  @Test
  public void unwrapFuture_nestedPojoFuture_pojoType() throws Exception {
    Type type = FutureExample.getType("nestedPojoFuture");

    Type unwrapped = GenericTypes.unwrapFutureType(type);

    assertEquals(Pojo.class, unwrapped);
  }

  @Test
  public void unwrapFuture_pojoListFuture_pojoType() throws Exception {
    Type type = FutureExample.getType("pojoListFuture");

    Type unwrapped = GenericTypes.unwrapFutureType(type);

    assertEquals(List.class, ((ParameterizedType)unwrapped).getRawType());
    assertEquals(Pojo.class, ((ParameterizedType)unwrapped).getActualTypeArguments()[0]);
  }

  @Test
  public void unwrapFuture_objectListFuture_pojoType() throws Exception {
    Type type = FutureExample.getType("objectListFuture");

    Type unwrapped = GenericTypes.unwrapFutureType(type);

    assertEquals(List.class, unwrapped);
  }


  public interface FutureExample {

    static Type getType(String methodName){
      try {
        return FutureExample.class.getMethod(methodName).getGenericReturnType();
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }

    Future objectFuture();
    Future<Pojo> pojoFuture();
    Future<Future<Pojo>> nestedPojoFuture();
    Future<List<Pojo>> pojoListFuture();
    Future<List> objectListFuture();

  }

  public static class GenericExample {

    public List<String> getGenericList() {

      return null;
    }

    public List<?> getWildcardList() {

      return null;
    }

    public List<String>[] getGenericListArray() {

      return null;
    }
  }

  public static class Pojo {

    private String name;

    public String getName() {

      return name;
    }

    public void setName(final String name) {

      this.name = name;
    }

    public Pojo withName(final String name) {

      this.name = name;
      return this;
    }

    @Override
    public int hashCode() {

      return Objects.hash(name);
    }

    @Override
    public boolean equals(final Object o) {

      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      final Pojo pojo = (Pojo) o;
      return Objects.equals(name, pojo.name);
    }

    public JsonObject toJson() {

      return new JsonObject().put("name", name);
    }
  }

  public static class ParameterizedTypeImpl implements ParameterizedType {

    private final Class<?> rawType;
    private final Class<?> ownerType;
    private final Class<?>[] typeParams;


    public ParameterizedTypeImpl(final Class<?> rawType,
                                 final Class<?> ownerType,
                                 final Class<?>... typeParams) {
      this.rawType = rawType;
      this.ownerType = ownerType;
      this.typeParams = typeParams;
    }

    @Override
    public Type[] getActualTypeArguments() {

      return typeParams;
    }

    @Override
    public Type getRawType() {

      return rawType;
    }

    @Override
    public Type getOwnerType() {

      return ownerType;
    }

    @Override
    public String getTypeName() {

      return rawType + "<" + Arrays.toString(typeParams) + ">";
    }
  }
}

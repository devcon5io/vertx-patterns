package io.devcon5.vertx.codec;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

/**
 *
 */
public class GenericTypeDecodingTest {

  @Test
  public void decode_simpleTypes_boolean() {
    assertEquals(true, GenericTypeDecoding.decode(true, boolean.class));
    assertEquals(Boolean.TRUE, GenericTypeDecoding.decode(true, Boolean.class));
  }

  @Test
  public void decode_simpleTypes_byte() {

    assertEquals((byte) 12, GenericTypeDecoding.decode((byte)12, byte.class));
    assertEquals(Byte.valueOf("12"), GenericTypeDecoding.decode(Byte.valueOf((byte) 12), Byte.class));
  }

  @Test
  public void decode_simpleTypes_short() {

    assertEquals((short) 512, GenericTypeDecoding.decode((short)512, short.class));
    assertEquals(Short.valueOf("512"), GenericTypeDecoding.decode(Short.valueOf((short) 512), Short.class));
  }

  @Test
  public void decode_simpleTypes_int() {

    assertEquals(128000, GenericTypeDecoding.decode(128000, int.class));
    assertEquals(Integer.valueOf("128000"), GenericTypeDecoding.decodeValue(Integer.valueOf(128000), Integer.class));
  }

  @Test
  public void decode_simpleTypes_long() {
    assertEquals(256000000L, GenericTypeDecoding.decode(256000000L, long.class));
    assertEquals(Long.valueOf("256000000"), GenericTypeDecoding.decode(Long.valueOf(256000000L), Long.class));
  }

  @Test
  public void decode_simpleTypes_float() {

    assertEquals(256.0f, GenericTypeDecoding.decodeValue(256.0f, float.class));
    assertEquals(Float.valueOf("256.0"), GenericTypeDecoding.decodeValue(Float.valueOf(256.0f), Float.class));
  }

  @Test
  public void decode_simpleTypes_double() {

    assertEquals(256.0, GenericTypeDecoding.decode(256.0, double.class));
    assertEquals(Double.valueOf("256.0"), GenericTypeDecoding.decode(Double.valueOf(256.0), Double.class));
  }

  @Test
  public void decode_simpleTypes_byteArray() {

    assertArrayEquals(new byte[] { 1, 2, 3 },
                      (byte[]) GenericTypeDecoding.decode(new byte[] { 1, 2, 3 }, byte[].class));
  }

  @Test
  public void decode_simpleTypes_buffer() {

    assertEquals(Buffer.buffer("123"), GenericTypeDecoding.decodeValue(Buffer.buffer("123"), Buffer.class));
  }

  @Test
  public void decode_simpleTypes_JsonObject() {

    assertEquals(new JsonObject().put("1", "one"),
                 GenericTypeDecoding.decode(new JsonObject().put("1", "one"), JsonObject.class));
  }


  @Test
  public void decode_simpleTypes_JsonArray() {

    assertEquals(new JsonArray().add("1").add("2"),
                 GenericTypeDecoding.decode(new JsonArray().add("1").add("2"), JsonArray.class));
  }


  @Test
  public void decode_complexTypes_pojo() {

    final String input = new JsonObject().put("name", "bob").toString();
    assertEquals(new Pojo().withName("bob"),
                 GenericTypeDecoding.decode(input, Pojo.class));
  }

  @Test
  public void decode_complexTypes_listOfPojos() {

    final String input = new JsonArray().add(new JsonObject().put("name", "bob"))
                                        .add(new JsonObject().put("name", "alice"))
                                        .toString();

    ParameterizedType pt = new ParameterizedTypeImpl(List.class, null, Pojo.class);

    assertEquals(Arrays.asList(new Pojo().withName("bob"),
                               new Pojo().withName("alice")),
                 GenericTypeDecoding.decode(input, pt));
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
                 GenericTypeDecoding.decode(input, pt));
  }

  @Test
  public void decode_complexTypes_mapOfPojos() {

    final String input = new JsonObject().put("bob", new JsonObject().put("name", "bob"))
                                        .put("alice", new JsonObject().put("name", "alice"))
                                        .toString();

    ParameterizedType pt = new ParameterizedTypeImpl(Map.class, null, String.class,Pojo.class);

    Map<String, Pojo> expected = Map.of("bob", new Pojo().withName("bob"),
                                            "alice", new Pojo().withName("alice"));
    assertEquals(expected, GenericTypeDecoding.decode(input, pt));
  }



  @Test
  public void decodeValue_boolean() {

    assertEquals(true, GenericTypeDecoding.decodeValue(true, boolean.class));
    assertEquals(Boolean.TRUE, GenericTypeDecoding.decodeValue(true, Boolean.class));
  }

  @Test
  public void decodeValue_byte() {

    assertEquals((byte) 12, GenericTypeDecoding.decodeValue((byte)12, byte.class));
    assertEquals(Byte.valueOf("12"), GenericTypeDecoding.decodeValue(Byte.valueOf((byte) 12), Byte.class));
  }

  @Test
  public void decodeValue_short() {

    assertEquals((short) 512, GenericTypeDecoding.decodeValue((short)512, short.class));
    assertEquals(Short.valueOf("512"), GenericTypeDecoding.decodeValue(Short.valueOf((short) 512), Short.class));
  }

  @Test
  public void decodeValue_int() {

    assertEquals(128000, GenericTypeDecoding.decodeValue(128000, int.class));
    assertEquals(Integer.valueOf("128000"), GenericTypeDecoding.decodeValue(Integer.valueOf(128000), Integer.class));
  }

  @Test
  public void decodeValue_long() {

    assertEquals(256000000L, GenericTypeDecoding.decodeValue(256000000L, long.class));
    assertEquals(Long.valueOf("256000000"), GenericTypeDecoding.decodeValue(Long.valueOf(256000000L), Long.class));
  }

  @Test
  public void decodeValue_float() {

    assertEquals(256.0f, GenericTypeDecoding.decodeValue(256.0f, float.class));
    assertEquals(Float.valueOf("256.0"), GenericTypeDecoding.decodeValue(Float.valueOf(256.0f), Float.class));
  }

  @Test
  public void decodeValue_double() {

    assertEquals(256.0, GenericTypeDecoding.decodeValue(256.0, double.class));
    assertEquals(Double.valueOf("256.0"), GenericTypeDecoding.decodeValue(Double.valueOf(256.0), Double.class));
  }

  @Test
  public void decodeValue_byteArray() {

    assertArrayEquals(new byte[] { 1, 2, 3 },
                      (byte[]) GenericTypeDecoding.decodeValue(new byte[] { 1, 2, 3 }, byte[].class));
  }

  @Test
  public void decodeValue_buffer() {

    assertEquals(Buffer.buffer("123"), GenericTypeDecoding.decodeValue(Buffer.buffer("123"), Buffer.class));
  }

  @Test
  public void decodeValue_JsonObject() {

    assertEquals(new JsonObject().put("1", "one"),
                 GenericTypeDecoding.decodeValue(new JsonObject().put("1", "one"), JsonObject.class));
  }


  @Test
  public void decodeValue_JsonArray() {

    assertEquals(new JsonArray().add("1").add("2"),
                 GenericTypeDecoding.decodeValue(new JsonArray().add("1").add("2"), JsonArray.class));
  }


  @Test
  public void decodeValue_pojo() {

    assertEquals(new Pojo().withName("bob"),GenericTypeDecoding.decodeValue(new JsonObject().put("name", "bob"), Pojo.class));
  }

  @Test
  public void decodeList_simpleType() {

    JsonArray input = new JsonArray().add("1").add("1").add("2");

    List<String> list = GenericTypeDecoding.decodeList(input, String.class);

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

    List<Pojo> list = GenericTypeDecoding.decodeList(input, Pojo.class);

    assertEquals(new Pojo().withName("alice"), list.get(0));
    assertEquals(new Pojo().withName("alice"), list.get(1));
    assertEquals(new Pojo().withName("bob"), list.get(2));
    assertEquals(3, list.size());
  }

  @Test
  public void decodeSet_simpleType() {

    JsonArray input = new JsonArray().add("1").add("1").add("2");

    Set<String> set = GenericTypeDecoding.decodeSet(input, String.class);

    assertTrue(set.contains("1"));
    assertTrue(set.contains("2"));
    assertEquals(2, set.size());
  }

  @Test
  public void decodeSet_pojoTypeAsJson() {

    JsonArray input = new JsonArray().add(new Pojo().withName("alice").toJson())
                                     .add(new Pojo().withName("bob").toJson())
                                     .add(new Pojo().withName("alice").toJson());

    Set<String> set = GenericTypeDecoding.decodeSet(input, Pojo.class);

    assertTrue(set.contains(new Pojo().withName("alice")));
    assertTrue(set.contains(new Pojo().withName("bob")));
    assertEquals(2, set.size());
  }

  @Test
  public void decodeMap_simpleType() {

    JsonObject input = new JsonObject().put("1", 1).put("2", 2);

    Map<String, Integer> map = GenericTypeDecoding.decodeMap(input, String.class, Integer.class);

    assertEquals(Integer.valueOf(1), map.get("1"));
    assertEquals(Integer.valueOf(2), map.get("2"));
    assertEquals(2, map.size());

  }

  @Test
  public void decodeMap_pojoType() {

    JsonObject input = new JsonObject().put("1", new Pojo().withName("bob").toJson())
                                       .put("2", new Pojo().withName("alice").toJson());

    Map<String, Pojo> map = GenericTypeDecoding.decodeMap(input, String.class, Pojo.class);

    assertEquals(new Pojo().withName("bob"), map.get("1"));
    assertEquals(new Pojo().withName("alice"), map.get("2"));
    assertEquals(2, map.size());

  }

  @Test
  public void getRawType_class() {

    assertEquals(String.class, GenericTypeDecoding.getRawType(String.class));
  }

  @Test
  public void getRawType_parametrizedType() throws Exception {

    Type type = GenericExample.class.getMethod("getGenericList").getGenericReturnType();
    assertEquals(List.class, GenericTypeDecoding.getRawType(type));
  }

  @Test
  public void getRawType_wildcardType() throws Exception {

    Type type = GenericExample.class.getMethod("getWildcardList").getGenericReturnType();
    assertEquals(List.class, GenericTypeDecoding.getRawType(type));
  }

  @Test
  public void getRawType_genericArrayType() throws Exception {

    Type type = GenericExample.class.getMethod("getGenericListArray").getGenericReturnType();
    assertEquals(List.class, GenericTypeDecoding.getRawType(type));
  }

  @Test
  public void getRawType_otherType() throws Exception {

    Type type = new Type() {

    };
    assertEquals(Object.class, GenericTypeDecoding.getRawType(type));
  }

  @Test
  public void isSimpleType_String() {

    assertTrue(GenericTypeDecoding.isSimpleType(String.class));
  }

  @Test
  public void isSimpleType_boolean() {

    assertTrue(GenericTypeDecoding.isSimpleType(boolean.class));
    assertTrue(GenericTypeDecoding.isSimpleType(Boolean.class));
  }

  @Test
  public void isSimpleType_byte() {

    assertTrue(GenericTypeDecoding.isSimpleType(byte.class));
    assertTrue(GenericTypeDecoding.isSimpleType(Byte.class));
  }

  @Test
  public void isSimpleType_short() {

    assertTrue(GenericTypeDecoding.isSimpleType(short.class));
    assertTrue(GenericTypeDecoding.isSimpleType(Short.class));
  }

  @Test
  public void isSimpleType_int() {

    assertTrue(GenericTypeDecoding.isSimpleType(int.class));
    assertTrue(GenericTypeDecoding.isSimpleType(Integer.class));
  }

  @Test
  public void isSimpleType_long() {

    assertTrue(GenericTypeDecoding.isSimpleType(long.class));
    assertTrue(GenericTypeDecoding.isSimpleType(Long.class));
  }

  @Test
  public void isSimpleType_float() {

    assertTrue(GenericTypeDecoding.isSimpleType(float.class));
    assertTrue(GenericTypeDecoding.isSimpleType(Float.class));
  }

  @Test
  public void isSimpleType_double() {

    assertTrue(GenericTypeDecoding.isSimpleType(double.class));
    assertTrue(GenericTypeDecoding.isSimpleType(Double.class));
  }

  @Test
  public void isSimpleType_byteArray() {

    assertTrue(GenericTypeDecoding.isSimpleType(byte[].class));
  }

  @Test
  public void isSimpleType_Buffer() {

    assertTrue(GenericTypeDecoding.isSimpleType(Buffer.class));
  }

  @Test
  public void isSimpleType_JsonObject() {

    assertTrue(GenericTypeDecoding.isSimpleType(JsonObject.class));
  }

  @Test
  public void isSimpleType_JsonArray() {

    assertTrue(GenericTypeDecoding.isSimpleType(JsonArray.class));
  }

  @Test
  public void isSimpleType_Object_false() {

    assertFalse(GenericTypeDecoding.isSimpleType(Object.class));
  }

  @Test
  public void isSimpleType_List_false() {

    assertFalse(GenericTypeDecoding.isSimpleType(List.class));
  }

  @Test
  public void isSimpleType_Map_false() {

    assertFalse(GenericTypeDecoding.isSimpleType(Map.class));
  }

  @Test
  public void isSimpleType_Set_false() {

    assertFalse(GenericTypeDecoding.isSimpleType(Set.class));
  }

  @Test
  public void isSimpleType_Pojo_false() {

    assertFalse(GenericTypeDecoding.isSimpleType(Pojo.class));
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

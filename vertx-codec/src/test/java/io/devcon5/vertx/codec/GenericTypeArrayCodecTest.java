package io.devcon5.vertx.codec;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.vertx.core.buffer.Buffer;
import org.junit.Test;

/**
 *
 */
public class GenericTypeArrayCodecTest {

  @Test
  public void codecNameFor_primitiveObjectArray() throws NoSuchMethodException {

    String codecName = GenericTypeArrayCodec.codecNameFor(new Class[]{String.class, Integer.class, Boolean.class});
    final String expected = "[java.lang.String, java.lang.Integer, java.lang.Boolean]";
    assertEquals(expected,codecName);
  }


  @Test
  public void codecNameFor_genericCollectionsArray() throws NoSuchMethodException {

    String codecName = GenericTypeArrayCodec.codecNameFor(new Type[]{ ComplexTypes.LIST_OF_POJO_TYPE, ComplexTypes.MAP_OF_POJO_TYPE,
                                                                      ComplexTypes.SET_OF_POJO_TYPE});
    final String expected = "[java.util.List<io.devcon5.vertx.codec.GenericTypeCodecTest$Pojo>, "
        + "java.util.Map<java.lang.String, io.devcon5.vertx.codec.GenericTypeCodecTest$Pojo>, "
        + "java.util.Set<io.devcon5.vertx.codec.GenericTypeCodecTest$Pojo>]";
    assertEquals(expected,codecName);
  }

  @Test
  public void encodeToWire_and_decodeFromWire_pojoArg() throws Exception {

    GenericTypeArrayCodec codec = new GenericTypeArrayCodec(TestContract.class.getMethod("testMethod", Pojo.class).getGenericParameterTypes());

    Pojo pojo = new Pojo();
    pojo.setValue("test");

    Object[] recv = transcode(codec, pojo);

    assertEquals(1, recv.length);
    assertEquals(pojo, recv[0]);

  }

  @Test
  public void encodeToWire_and_decodeFromWire_listOfPojosArg() throws Exception {

    GenericTypeArrayCodec codec = new GenericTypeArrayCodec(TestContract.class.getMethod("testMethod", List.class).getGenericParameterTypes());

    List pojo = Arrays.asList(new Pojo().withValue("test1"), new Pojo().withValue("test2"));

    Object[] recv = transcode(codec, pojo);

    assertEquals(1, recv.length);
    assertEquals(pojo, recv[0]);
  }

  @Test
  public void encodeToWire_and_decodeFromWire_setOfPojosArg() throws Exception {

    GenericTypeArrayCodec codec = new GenericTypeArrayCodec(TestContract.class.getMethod("testMethod", Set.class).getGenericParameterTypes());

    Set<Pojo> pojos = new HashSet();
    pojos.add(new Pojo().withValue("test1"));
    pojos.add(new Pojo().withValue("test2"));

    Object[] recv = transcode(codec, pojos);

    assertEquals(1, recv.length);
    assertEquals(pojos, recv[0]);
  }

  @Test
  public void encodeToWire_and_decodeFromWire_mapOfPojosArg() throws Exception {

    GenericTypeArrayCodec codec = new GenericTypeArrayCodec(TestContract.class.getMethod("testMethod", Map.class).getGenericParameterTypes());


    Map<String, Pojo> pojos = new HashMap<>();
    pojos.put("one", new Pojo().withValue("test1"));
    pojos.put("two", new Pojo().withValue("test2"));

    Object[] recv = transcode(codec, pojos);

    assertEquals(1, recv.length);
    assertEquals(pojos, recv[0]);
  }


  @Test
  public void name_pojoType() {
    GenericTypeArrayCodec codec = new GenericTypeArrayCodec(new Class[]{Pojo.class});

    assertEquals("[io.devcon5.vertx.codec.GenericTypeArrayCodecTest$Pojo]", codec.name());
  }

  @Test
  public void systemCodecID() {

    GenericTypeArrayCodec codec = new GenericTypeArrayCodec(new Class[]{Pojo.class});

    assertEquals(-1, codec.systemCodecID());

  }

  private Object[] transcode(final GenericTypeArrayCodec codec, final Object... input) {

    Buffer buffer = Buffer.buffer();
    codec.encodeToWire(buffer, input);
    return codec.decodeFromWire(0, buffer);
  }



  public interface TestContract {

    String testMethod(String a, Integer i, Boolean b);

    String testMethod(Pojo pojo);

    String testMethod(List<Pojo> pojos);
    String testMethod(Set<Pojo> pojos);
    String testMethod(Map<String, Pojo> pojos);
  }

  public static class Pojo {
    private String value;

    public String getValue() {

      return value;
    }

    public Pojo withValue(String value){
      this.value = value;
      return this;
    }
    public void setValue(final String value) {

      this.value = value;
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
      return Objects.equals(value, pojo.value);
    }

    @Override
    public int hashCode() {

      return Objects.hash(value);
    }
  }

  public static class ComplexTypes {

    public List<GenericTypeCodecTest.Pojo> listOfPojos;
    public Map<String, GenericTypeCodecTest.Pojo> mapOfPojos;
    public Set<GenericTypeCodecTest.Pojo> setOfPojos;
    public GenericTypeCodecTest.Pojo[] arrayOfPojos;

    public static Type getType(String fieldName){

      try {
        return ComplexTypes.class.getField(fieldName).getGenericType();
      } catch (NoSuchFieldException e) {
        throw new RuntimeException(e);
      }
    }
    public static final Type LIST_OF_POJO_TYPE = getType("listOfPojos");
    public static final Type MAP_OF_POJO_TYPE = getType("mapOfPojos");
    public static final Type SET_OF_POJO_TYPE = getType("setOfPojos");
    public static final Type ARRAY_OF_POJO_TYPE = getType("arrayOfPojos");
  }
}

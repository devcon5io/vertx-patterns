package io.devcon5.vertx.codec;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.vertx.core.buffer.Buffer;
import org.junit.Test;

/**
 *
 */
public class GenericTypeCodecTest {

  @Test
  public void codecNameFor_pojo() {

    Type type = Pojo.class;

    assertEquals("io.devcon5.vertx.codec.GenericTypeCodecTest$Pojo", GenericTypeCodec.codecNameFor(type));
  }

  @Test
  public void name_genericListType() throws Exception {

    Type type = ListTypes.class.getField("listOfPojos").getGenericType();

    assertEquals("java.util.List<io.devcon5.vertx.codec.GenericTypeCodecTest$Pojo>",
                 GenericTypeCodec.codecNameFor(type));
  }

  @Test
  public void name_genericMapType() throws Exception {

    Type type = ListTypes.class.getField("mapOfPojos").getGenericType();

    assertEquals("java.util.Map<java.lang.String, io.devcon5.vertx.codec.GenericTypeCodecTest$Pojo>",
                 GenericTypeCodec.codecNameFor(type));
  }

  @Test
  public void name_genericSetType() throws Exception {

    Type type = ListTypes.class.getField("setOfPojos").getGenericType();

    assertEquals("java.util.Set<io.devcon5.vertx.codec.GenericTypeCodecTest$Pojo>",
                 GenericTypeCodec.codecNameFor(type));
  }

  @Test
  public void name_arrayType() throws Exception {

    Type type = ListTypes.class.getField("arrayOfPojos").getGenericType();

    assertEquals("io.devcon5.vertx.codec.GenericTypeCodecTest$Pojo[]", GenericTypeCodec.codecNameFor(type));
  }

  @Test
  public void encodeToWire() {

    Pojo type = new Pojo().withName("bob");
    GenericTypeCodec codec = new GenericTypeCodec(Pojo.class);
    Buffer buffer = Buffer.buffer();

    //this should write an empty jsonObject onto the buffer,
    // with a leading integer value indicating the size of the object
    codec.encodeToWire(buffer, type);

    //the length of the written data
    assertEquals(14, buffer.getInt(0));
    //the encoded data
    assertEquals("{\"name\":\"bob\"}", buffer.slice(4, buffer.length()).toString("UTF-8"));
  }

  @Test
  public void decodeFromWire() {


    GenericTypeCodec codec = new GenericTypeCodec(Pojo.class);
    Buffer buffer = Buffer.buffer();
    buffer.appendInt(14).appendString("{\"name\":\"bob\"}");

    Pojo actual = (Pojo) codec.decodeFromWire(0, buffer);

    Pojo expected = new Pojo().withName("bob");
    assertEquals(expected, actual);
  }

  @Test
  public void transform() {

    Pojo type = new Pojo();
    GenericTypeCodec codec = new GenericTypeCodec(Pojo.class);

    Pojo actual = (Pojo) codec.transform(type);

    assertEquals(type, actual);

  }

  @Test
  public void name_pojoType() {

    GenericTypeCodec codec = new GenericTypeCodec(Pojo.class);

    assertEquals("io.devcon5.vertx.codec.GenericTypeCodecTest$Pojo", codec.name());
  }

  @Test
  public void systemCodecID() {

    GenericTypeCodec codec = new GenericTypeCodec(Pojo.class);

    assertEquals(-1, codec.systemCodecID());

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

    @Override
    public int hashCode() {

      return Objects.hash(name);
    }
  }

  public static class ListTypes {

    public List<Pojo> listOfPojos;
    public Map<String, Pojo> mapOfPojos;
    public Set<Pojo> setOfPojos;
    public Pojo[] arrayOfPojos;

  }
}

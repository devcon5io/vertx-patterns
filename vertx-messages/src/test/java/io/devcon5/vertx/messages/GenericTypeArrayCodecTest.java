package io.devcon5.vertx.messages;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
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
  public void codecNameFor() throws NoSuchMethodException {

    Method method = TestContract.class.getMethod("testMethod", String.class, Integer.class, Boolean.class);
    String codecName = GenericTypeArrayCodec.codecNameFor(method.getGenericParameterTypes());

    final String expected = "io.devcon5.vertx.messages.GenericTypeArrayCodecTest$TestContract"
        + "::testMethod("
        + "java.lang.String,"
        + "java.lang.Integer,"
        + "java.lang.Boolean)";
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
  public void encodeToWire_and_decodeFromWire_pojoListArg() throws Exception {

    GenericTypeArrayCodec codec = new GenericTypeArrayCodec(TestContract.class.getMethod("testMethod", List.class).getGenericParameterTypes());

    List pojo = Arrays.asList(new Pojo().withValue("test1"), new Pojo().withValue("test2"));

    Object[] recv = transcode(codec, pojo);

    assertEquals(1, recv.length);
    assertEquals(pojo, recv[0]);
  }

  @Test
  public void encodeToWire_and_decodeFromWire_pojoSetArg() throws Exception {

    GenericTypeArrayCodec codec = new GenericTypeArrayCodec(TestContract.class.getMethod("testMethod", Set.class).getGenericParameterTypes());

    Set<Pojo> pojos = new HashSet();
    pojos.add(new Pojo().withValue("test1"));
    pojos.add(new Pojo().withValue("test2"));

    Object[] recv = transcode(codec, pojos);

    assertEquals(1, recv.length);
    assertEquals(pojos, recv[0]);
  }

  @Test
  public void encodeToWire_and_decodeFromWire_pojoMapArg() throws Exception {

    GenericTypeArrayCodec codec = new GenericTypeArrayCodec(TestContract.class.getMethod("testMethod", Map.class).getGenericParameterTypes());


    Map<String, Pojo> pojos = new HashMap<>();
    pojos.put("one", new Pojo().withValue("test1"));
    pojos.put("two", new Pojo().withValue("test2"));

    Object[] recv = transcode(codec, pojos);

    assertEquals(1, recv.length);
    assertEquals(pojos, recv[0]);
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
}

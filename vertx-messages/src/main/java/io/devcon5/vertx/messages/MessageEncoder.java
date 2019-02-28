package io.devcon5.vertx.messages;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 *
 */
class MessageEncoder {

  final static MessageEncoder INSTANCE = new MessageEncoder();

  public Object encode(final Object... args)  {
    if(args.length == 1){
      Object obj = args[0];
      if(isNative(obj)){
        return obj;
      }
      if(isBinary(obj)){
        return Buffer.buffer((byte[])obj);
      }
      if(isStream(obj)){
        return encodeStream((InputStream)obj);
      }
      if(obj instanceof Collection) {
        return new JsonArray(Json.encodeToBuffer(obj));
      }
      return new JsonObject(Json.encodeToBuffer(obj));
    }
    final JsonArray array = new JsonArray();
    for(Object obj : args){
      array.add(encode(obj));
    }
    return array;
  }

  private Buffer encodeStream(final InputStream obj) {

    try {
      return Buffer.buffer(obj.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException("Could not encode stream", e);
    }
  }

  private boolean isBinary(final Object obj) {

    return obj instanceof byte[];
  }

  private boolean isStream(final Object obj) {

    return obj instanceof InputStream;
  }

  private boolean isNative(final Object obj) {

    return obj instanceof String
        || obj instanceof JsonObject
        || obj instanceof JsonArray
        || obj instanceof Buffer;
  }
}

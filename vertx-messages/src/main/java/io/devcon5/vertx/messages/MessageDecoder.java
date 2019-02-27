package io.devcon5.vertx.messages;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 *
 */
class MessageDecoder {

  public Object decode(Object obj, final Class<?> targetType){
    if(obj == null || targetType == obj.getClass()) {
      return obj;
    }
    if(byte[].class.isAssignableFrom(targetType)){
      return toBuffer(obj).getBytes();
    }
    if(Buffer.class.isAssignableFrom(targetType)){
      return toBuffer(obj);
    }
    return Json.decodeValue(toBuffer(obj), targetType);
  }

  private Buffer toBuffer(final Object obj) {

    if(obj instanceof Buffer){
      return ((Buffer)obj);
    }
    if(obj instanceof JsonObject){
      return ((JsonObject)obj).toBuffer();
    }
    if(obj instanceof JsonArray){
      return ((JsonArray)obj).toBuffer();
    }
    if(obj instanceof String){
        return Buffer.buffer((String)obj);
    }
    throw new UnsupportedOperationException("Converting " + obj.getClass() + " to Buffer is not supported");
  }

}

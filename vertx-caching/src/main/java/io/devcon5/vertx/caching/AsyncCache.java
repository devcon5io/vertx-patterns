package io.devcon5.vertx.caching;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public class AsyncCache<T> {

   private final Map<String, CacheEntry<T>> issueLRUCache;

   private Duration maxAge = Duration.ofSeconds(5);

   public AsyncCache<T> withMaxAge(Duration maxAge){
      this.maxAge = maxAge;
      return this;
   }

   public AsyncCache(int size){
      this.issueLRUCache = new LinkedHashMap<>(size, 0.75f, true);
   }

}

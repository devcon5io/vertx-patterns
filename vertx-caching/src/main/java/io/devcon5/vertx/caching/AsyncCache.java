package io.devcon5.vertx.caching;

import static io.vertx.core.logging.LoggerFactory.getLogger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;

/**
 * Experimental asynchronous caching for vertx
 * @param <T>
 */
public class AsyncCache<T> {

  private final Map<String, CacheEntry<T>> issueLRUCache;

  private Duration maxAge = Duration.ofSeconds(5);

  public AsyncCache(int size) {

    this.issueLRUCache = new LinkedHashMap<>(size, 0.75f, true);
  }

  public AsyncCache<T> withMaxAge(Duration maxAge) {

    this.maxAge = maxAge;
    return this;
  }

  /**
   * Cache Entry to keep track of already requested issues. The cache entry ensures, no entry is older than 5s. An
   * update policy can be defined, to update the value when the entry is expired. If nothing is done, the entry will
   * expire causing read accesses to fail. The Entry holds a future that separates the registering of the entry ("the
   * entry was requested") from the actual reading of the entry ("the entry was fetched"). This ensures, the entry is
   * only fetched once (in 5s), even if additional requests for the same entry arrive before the entry is actually
   * fetched.
   *
   * @param <T>
   */
  public static class CacheEntry<T> {

    private static final Logger LOG = getLogger(CacheEntry.class);
    private static final long DEFAULT_MAX_AGE = Duration.ofSeconds(5).toMillis();
    private final String key;
    private final List<Handler<AsyncResult<T>>> listeners = new ArrayList<>();
    private long maxAge = DEFAULT_MAX_AGE;
    private long expires;
    private Future<T> value;
    private Supplier<Future<T>> updater = expireEntryPolicy();
    private CacheEntry(String key, final Future<T> value) {

      this.key = key;
      updateValue(value);
    }

    /**
     * Default policy that expires the entry
     */
    public static final <T> Supplier<Future<T>> expireEntryPolicy() {

      return () -> Future.failedFuture("Cache Entry expired");
    }

    public static <T> CacheEntry<T> createWithAutoUpdate(String key, Supplier<Future<T>> valueProvider) {

      return new CacheEntry<>(key, valueProvider.get()).onExpiration(valueProvider);
    }

    public static <T> CacheEntry<T> create(String key, Future<T> valueProvider) {

      return new CacheEntry<>(key, valueProvider);
    }

    /**
     * Async read of the value.
     *
     * @param valueHandler
     *     will be called, when the cached value is available.
     *
     * @return
     */
    public CacheEntry<T> readValue(Handler<AsyncResult<T>> valueHandler) {

      if (isExpired()) {
        updateValue(updater.get());
      }

      if (this.value.isComplete()) {
        LOG.trace("Cache hit: {}", key);
        valueHandler.handle(this.value);
      } else {
        this.listeners.add(valueHandler);
      }
      return this;
    }

    /**
     * Defines an update policy that is used, when the cache entry expires.
     *
     * @param updatedValueProvider
     *     provider for an updated version of the entry
     *
     * @return
     */
    public CacheEntry<T> onExpiration(Supplier<Future<T>> updatedValueProvider) {

      this.updater = updatedValueProvider;
      return this;
    }

    /**
     * Defines the maximum age of the cache entry before it expires. When the value expires following read operation
     * will either fail or execute any action that is defined as update policy. See onExpiration
     *
     * @param maxAge
     *     the maximum age for the object
     *
     * @return this entry
     */
    public CacheEntry<T> maxAge(Duration maxAge) {

      this.maxAge = maxAge.toMillis();
      return this;
    }

    private boolean isExpired() {

      return System.currentTimeMillis() > expires;
    }

    private void updateValue(Future<T> newValueFuture) {

      this.expires = System.currentTimeMillis() + maxAge;
      this.value = newValueFuture;
      //the handler ensures that all registered handler receive the new value once the future is completed.
      //the handler list is cleared afterwards to not retain references when they are no longer needed.
      //once the future is completed, any consecutive read operations will NOT add a new listener, but
      //will forward the value directly (see readValue)
      this.value.setHandler(fetched -> {
        LOG.trace("Cache hit (lazy): {}", key);
        listeners.forEach(l -> l.handle(this.value));
        listeners.clear();
      });
    }
  }

}

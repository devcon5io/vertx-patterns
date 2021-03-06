package io.devcon5.vertx.services;

import java.util.ServiceLoader;
import java.util.stream.Collectors;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.Router;

/**
 * Defines a service containing a set of endpoints and routes
 */
public interface Service<T extends Service> {

  /**
   * Mounts all Services to the specified parent router.
   * @param parentRouter
   *  the parent router on which the services are mounted
   * @param config
   *  the global configuration where all services find the relevant information to initialize
   * @param authProvider
   *  the authentication provider which services may use to authenticate their users
   * @return
   *  a list of futures indicating the initialization state of each services.
   */
  static Future<CompositeFuture> mountAll(Router parentRouter, JsonObject config, AuthProvider authProvider) {

    return CompositeFuture.all(ServiceLoader.load(Service.class)
                        .stream()
                        .map(svc -> svc.get().withConfig(config).withAuth(authProvider).mount(parentRouter))
                        .collect(Collectors.toList()));
  }

  /**
   * Mounts this service to the parent router. The completion has to be indicated using the future.
   *
   * @param parent
   *     the parent router this service should be mounted to. It's recommended to mount as subrouter.
   *
   * @return future to indicate mounting is complete
   */
  Future<Void> mount(Router parent);

  /**
   * Fluent setting of authentication provider if the server requires authentication
   *
   * @param provider
   *     the auth provider to use
   *
   * @param <A> the type of the auth provider
   *
   * @return the service instance for fluent configuration
   */
  <A extends AuthProvider> T withAuth(A provider);

  /**
   * Fluent setting of configuration to use for the router
   *
   * @param config
   *     the configuration this service should use
   *
   * @return the service instance for fluent configuration
   */
  T withConfig(JsonObject config);

}

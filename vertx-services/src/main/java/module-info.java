/**
 *
 */
module io.devcon5.vertx.services {
  exports io.devcon5.vertx.services;
  requires vertx.core;
  requires vertx.auth.common;
  requires vertx.web;
  uses io.devcon5.vertx.services.Service;
}

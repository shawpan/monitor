package io.vertx.monitor;

import io.vertx.core.Vertx;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.core.json.Json;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.vertx.monitor.models.Service;

@ExtendWith(VertxExtension.class)
public class TestMonitorAPI {

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MonitorAPI(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  @DisplayName("Should start a Web Server")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void start_http_server(Vertx vertx, VertxTestContext testContext) throws Throwable {
    vertx.createHttpClient().getNow(8080, "localhost", "/", response -> testContext.verify(() -> {
      assertTrue(response.statusCode() == 200);
      response.handler(body -> {
        assertTrue(body.toString().contains("Welcome to monitor"));
        testContext.completeNow();
      });
    }));
  }

  @Test
  @DisplayName("Should return all services")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void get_all_services(Vertx vertx, VertxTestContext testContext) throws Throwable {
    vertx.createHttpClient().getNow(8080, "localhost", "/service", response -> testContext.verify(() -> {
      assertTrue(response.statusCode() == 200);
      response.handler(body -> {
        assertTrue(response.headers().get("content-type").contains("application/json"));
        testContext.completeNow();
      });
    }));
  }

  @Test
  @DisplayName("Should add new service to the service list")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void add_service(Vertx vertx, VertxTestContext testContext) throws Throwable {
    final String json = Json.encodePrettily(new Service("http://google.com"));
    final String length = Integer.toString(json.length());
    vertx.createHttpClient().post(8080, "localhost", "/service")
        .putHeader("content-type", "application/json")
        .putHeader("content-length", length)
        .handler(response -> testContext.verify(() -> {
          assertTrue(response.statusCode() == 201);
          assertTrue(response.headers().get("content-type").contains("application/json"));
          response.bodyHandler(body -> testContext.verify( () -> {
            final Service service = Json.decodeValue(body.toString(), Service.class);
            assertTrue(service.getId() != null);
            assertTrue(service.getUrl().equals("http://google.com"));
            assertTrue(service.getStatus() == Service.Status.UNKNOWN);
            testContext.completeNow();
          }));
        }))
        .write(json)
        .end();
  }
}

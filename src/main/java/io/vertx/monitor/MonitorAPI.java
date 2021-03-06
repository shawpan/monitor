package io.vertx.monitor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.monitor.models.Database;
import io.vertx.monitor.models.Service;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import io.vertx.ext.web.handler.StaticHandler;

public class MonitorAPI extends AbstractVerticle {

  private static Database database;

  private void initializeServer(Future<Void> startFuture) throws Exception {
    Router router = getRouter();
    Integer port = config().getInteger("HTTP_PORT", 8080);
    vertx.createHttpServer()
          .requestHandler(router::accept)
          .listen(
          port,
          http -> {
            if (http.succeeded()) {
              startFuture.complete();
              System.out.println("HTTP server started on http://localhost:" + port.toString());
            } else {
              startFuture.fail(http.cause());
            }
          });
  }

  private void initializeDatabase() {
    database = new Database(vertx);
    vertx.eventBus().consumer("sync-api", receivedMessage -> {
     database.syncToAPI();
   });
  }

  private Router getRouter() {
    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create());
    // router.route().handler(routingContext -> {
    //   String apiKey = routingContext.request().getParam("apiKey");
    //   if(!"mUHtTfV5O1aPYpQ7PF8lng==".equals(apiKey)) {
    //     routingContext.fail(401);
    //   }
    // });
    router.get("/api").handler(this::handleHome);
    router.get("/api/service").handler(this::handleGetAllServices);
    router.post("/api/service").handler(this::handleAddService);
    router.delete("/api/service/:id").handler(this::handleDeleteService);
    router.route("/*").handler(StaticHandler.create());

    return router;
  }

  private void handleHome(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    response.putHeader("content-type", "text").end("Welcome to monitor");
  }

  private void handleGetAllServices(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    response.putHeader("content-type", "application/json")
            .end(Json.encodePrettily(database.getAllServices().values()));
  }

  private void handleAddService(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    Service service = Json.decodeValue(routingContext.getBodyAsString(),Service.class);
    database.addService(service);
    response.putHeader("content-type", "application/json")
            .setStatusCode(201)
            .end(Json.encodePrettily(service));
  }

  private void handleDeleteService(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    String id = routingContext.request().getParam("id");
    if (id == null) {
      routingContext.response().setStatusCode(400).end();
      return;
    }

    database.removeService(Integer.valueOf(id));
    routingContext.response().setStatusCode(204).end();
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    initializeDatabase();
    initializeServer(startFuture);
    vertx.deployVerticle(new Monitor(database.getInFile(), database.getOutFile()));
  }
}

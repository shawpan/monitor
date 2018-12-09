package io.vertx.monitor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.CompositeFuture;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.monitor.models.Database;
import io.vertx.monitor.models.Service;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.sql.Timestamp;
import io.vertx.monitor.models.Database;

public class Monitor extends AbstractVerticle {

  public static boolean inProgress = false;

  private JsonArray json = new JsonArray();

  private String inFile;

  private String outFile;

  public Monitor(String inFile, String outFile) {
    this.inFile = inFile;
    this.outFile = outFile;
  }

  private void checkAllStatus(String file) {
    inProgress = true;
    Future<Void> readFileFuture = readFromFile(file);
    readFileFuture.setHandler(readFileResult -> {
      if (readFileResult.succeeded()) {
        Future<JsonArray> resultFuture = performRequests();
        resultFuture.setHandler(ar -> {
          if (ar.succeeded()) {
            inProgress = false;
            json = ar.result();
            Future<Void> writeFuture = writeToFile(inFile);
            writeFuture.setHandler( writeResult -> {
              if(writeResult.succeeded()) {
                vertx.eventBus().send("sync-api", null);
              }
            });
          }
        });
      }
    });
  }

  private Future<JsonObject> httpGet(JsonObject service) {
    Future<JsonObject> future = Future.future();
    try {
      URL url = new URL(service.getString("url"));
      Integer port = url.getPort() < 0 ? 80 : url.getPort();
      System.out.println("Getting " + port + " " + service.getString("url"));
      HttpClient httpClient = vertx.createHttpClient();
      httpClient.getNow(port, url.getHost(), url.getFile(), response -> {
        String status = response.statusCode() == 200 ? "OK" : "FAIL";
        service.put("status", status);
        service.put("lastCheckedAt", (new Timestamp(System.currentTimeMillis())).toString());
        future.complete(service);
        httpClient.close();
      });
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }

    return future;
  }

  private Future<JsonArray> performRequests() {
    final List<Future> futures = new ArrayList<>();
    Future<JsonArray> future = Future.future();

    for (int i = 0; i < json.size(); i++) {
      final Future<JsonObject> statusFuture = httpGet(json.getJsonObject(i));
      futures.add(statusFuture);
    }

    CompositeFuture.all(futures)
                    .setHandler(ar -> {
                      if(ar.succeeded()) {
                        JsonArray result = new JsonArray();
                        for(Future<JsonObject> aFuture : futures) {
                          result.add(aFuture.result());
                        }
                        future.complete(result);
                      }
                    });

    return future;
  }

  public Future<Void> writeToFile(String file) {
    Future<Void> future = Future.future();

		vertx.fileSystem().writeFile(file, Buffer.buffer(json.encodePrettily()), handler -> {
			if (handler.succeeded()) {
				System.out.println("write succeeded");
				future.complete();
			} else {
        future.fail(handler.cause());
        System.err.println("Error writing file: " + handler.cause().getMessage());
      }
		});

    return future;
  }

  private Future<Void> readFromFile(String file) {
    Future<Void> future = Future.future();

    vertx.fileSystem().readFile(file, handler -> {
			if (handler.succeeded()) {
        json = new JsonArray(handler.result());
        future.complete();
			} else {
        System.err.println("Error while reading from file: " + handler.cause().getMessage());
        future.fail(handler.cause());
      }
		});

    return future;
  }

  @Override
  public void start() throws Exception {
    vertx.setPeriodic(1000, id -> {
      if(false == inProgress) {
        checkAllStatus(outFile);
      }
    });
  }
}

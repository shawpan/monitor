package io.vertx.monitor.models;

import io.vertx.core.Vertx;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonArray;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import io.vertx.core.json.Json;
import io.vertx.core.buffer.Buffer;

public class Database {

  private HashMap<Integer, Service> services;

  /**
   * Background service reads and api writes to this file
   */
  private String outFile;

  /**
   * Background service writes and api reads to this file
   */
  private String inFile;

  public Database() {
    services = new HashMap<Integer, Service>();
    outFile = "../monitor/src/main/java/io/vertx/monitor/resources/db_out.json";
    inFile = "../monitor/src/main/java/io/vertx/monitor/resources/db_in.json";
    loadFromFile(outFile);
  }

  public String getInFile() {
    return inFile;
  }

  public String getOutFile() {
    return outFile;
  }

  public HashMap<Integer, Service> getAllServices() {
    return services;
  }

  public void addService(Service service) {
    services.put(service.getId(), service);
    syncFromAPI();
  }

  public void removeService(Integer id) {
    services.remove(id);
    syncFromAPI();
  }

  /**
   * Sync data from api to file
   */
  public void syncFromAPI() {
    writeToFile(outFile);
  }

  /**
   * Sync data to api from file
   */
  public void syncToAPI() {
    Future<Void> readFuture = readFromFile(inFile);
    readFuture.setHandler(ar -> {
      if(ar.succeeded()) {
        writeToFile(outFile);
      }
    });
  }

  public Future<Void> writeToFile(String file) {
    Vertx vertx = Vertx.vertx();
    Future<Void> future = Future.future();

		vertx.fileSystem().writeFile(file, Buffer.buffer(Json.encodePrettily(services.values())), handler -> {
			if (handler.succeeded()) {
				future.complete();
			} else {
        future.fail(handler.cause());
        System.err.println("Error writing file: " + handler.cause().getMessage());
      }
		});

    return future;
  }

  public Future<Void> readFromFile(String file) {
    Vertx vertx = Vertx.vertx();
    Future<Void> future = Future.future();

    vertx.fileSystem().readFile(file, handler -> {
			if (handler.succeeded()) {
        JsonArray json = new JsonArray(handler.result());
        updateStatusOfServices(json);
        future.complete();
			} else {
        System.err.println("Error while reading from file: " + handler.cause().getMessage());
        future.fail(handler.cause());
      }
		});

    return future;
  }

  public Future<Void> loadFromFile(String file) {
    Vertx vertx = Vertx.vertx();
    Future<Void> future = Future.future();

    vertx.fileSystem().readFile(file, handler -> {
			if (handler.succeeded()) {
        JsonArray json = new JsonArray(handler.result());
        transformJsonToServices(json);
        future.complete();
			} else {
        System.err.println("Error while reading from file: " + handler.cause().getMessage());
        future.fail(handler.cause());
      }
		});

    return future;
  }

  public void updateStatusOfServices(JsonArray json) {
    System.out.println(services.size());
    for (int i = 0; i < json.size(); i++) {
      Integer id = json.getJsonObject(i).getInteger("id");
      String status = json.getJsonObject(i).getString("status");
      String lastCheckedAt = json.getJsonObject(i).getString("lastCheckedAt");
      Service service = services.get(id);
      if(service != null) {
        service.setStatus(Service.Status.valueOf(status));
        service.setLastCheckedAt(lastCheckedAt);
      }
    }
  }

  public void transformJsonToServices(JsonArray json) {
    HashMap<Integer, Service> services = new HashMap<Integer, Service>();

    for (int i = 0; i < json.size(); i++) {
      Integer id = json.getJsonObject(i).getInteger("id");
      String status = json.getJsonObject(i).getString("status");
      String url = json.getJsonObject(i).getString("url");
      Service service = new Service(id, url, Service.Status.valueOf(status));
      services.put(service.getId(), service);
    }
    this.services = services;
  }
}

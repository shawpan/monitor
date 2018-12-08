package io.vertx.monitor.models;

import io.vertx.core.file.AsyncFile;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.file.OpenOptions;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class Database {
  private HashMap<Integer, Service> services;

  public Database() {
    services = new HashMap<Integer, Service>();
  }

  public HashMap<Integer, Service> getAllServices() {
    return services;
  }

  public void addService(Service service) {
    services.put(service.getId(), service);
  }

  public void removeService(Integer id) {
    services.remove(id);
  }
}

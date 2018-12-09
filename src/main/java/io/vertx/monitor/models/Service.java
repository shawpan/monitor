package io.vertx.monitor.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.concurrent.atomic.AtomicInteger;

public class Service {

  private static final AtomicInteger COUNTER = new AtomicInteger(1);

  private Status status;

  private String url;

  private final Integer id;

  public enum Status {
    OK, FAIL, UNKNOWN
  }

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public Service(@JsonProperty("url") String url) {
    this.id = COUNTER.getAndIncrement();
    this.status = Status.UNKNOWN;
    this.url = url;
  }

  public Service(Integer id, String url, Status status) {
    this.id = id;
    this.status = status;
    this.url = url;
  }

  @JsonProperty("id")
  public Integer getId() {
    return id;
  }

  @JsonProperty("url")
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @JsonProperty("status")
  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "Service{" + "url=" + url + ", status=" + status + '}';
  }
}

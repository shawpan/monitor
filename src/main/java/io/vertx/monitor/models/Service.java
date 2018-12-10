package io.vertx.monitor.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.concurrent.atomic.AtomicInteger;
import java.sql.Timestamp;

public class Service {

  private Status status;

  private String url;

  private Integer id;

  private String lastCheckedAt;

  public enum Status {
    OK, FAIL, UNKNOWN
  }

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public Service(@JsonProperty("url") String url) {
    this.id = 0;
    this.status = Status.UNKNOWN;
    this.url = url;
    this.lastCheckedAt = (new Timestamp(System.currentTimeMillis())).toString();
  }

  public Service(Integer id, String url, Status status) {
    this.id = id;
    this.status = status;
    this.url = url;
    this.lastCheckedAt = (new Timestamp(System.currentTimeMillis())).toString();
  }

  @JsonProperty("id")
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
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

  @JsonProperty("lastCheckedAt")
  public String getLastCheckedAt() {
    return lastCheckedAt;
  }

  public void setLastCheckedAt(String lastCheckedAt) {
    this.lastCheckedAt = lastCheckedAt;
  }

  @Override
  public String toString() {
    return "Service{" + "url=" + url + ", status=" + status + '}';
  }
}

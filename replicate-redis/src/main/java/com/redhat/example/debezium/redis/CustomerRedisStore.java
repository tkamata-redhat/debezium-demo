package com.redhat.example.debezium.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.example.debezium.model.CustomerSnapshot;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.keys.ReactiveKeyCommands;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomerRedisStore {

  private final ReactiveValueCommands<String, String> values;
  private final ReactiveKeyCommands<String> keys;
  private final ObjectMapper objectMapper;

  public CustomerRedisStore(
      ReactiveRedisDataSource reactiveRedisDataSource, ObjectMapper objectMapper) {
    this.values = reactiveRedisDataSource.value(String.class);
    this.keys = reactiveRedisDataSource.key(String.class);
    this.objectMapper = objectMapper;
  }

  public Uni<Void> upsert(CustomerSnapshot customer) {
    final String json;
    try {
      json = objectMapper.writeValueAsString(customer);
    } catch (JsonProcessingException e) {
      return Uni.createFrom()
          .failure(new IllegalStateException("Failed to serialize customer for Redis", e));
    }
    return values.set(redisKey(customer.getCustomerId()), json).replaceWithVoid();
  }

  public Uni<Void> delete(String customerId) {
    return keys.del(redisKey(customerId)).replaceWithVoid();
  }

  private String redisKey(String customerId) {
    return customerId;
  }
}

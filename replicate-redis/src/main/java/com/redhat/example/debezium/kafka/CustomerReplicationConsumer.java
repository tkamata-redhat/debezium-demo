package com.redhat.example.debezium.kafka;

import com.redhat.example.debezium.debezium.DebeziumEnvelope;
import com.redhat.example.debezium.debezium.DebeziumEventParser;
import com.redhat.example.debezium.model.CustomerSnapshot;
import com.redhat.example.debezium.redis.CustomerRedisStore;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
@Slf4j
public class CustomerReplicationConsumer {

  private final DebeziumEventParser parser;
  private final CustomerRedisStore redisStore;

  public CustomerReplicationConsumer(
      DebeziumEventParser parser, CustomerRedisStore redisStore) {
    this.parser = parser;
    this.redisStore = redisStore;
  }

  @Incoming("customer-events")
  public Uni<Void> onCustomerChange(IncomingKafkaRecord<String, String> record) {
    return process(record)
        .onItem()
        .transformToUni(ignored -> Uni.createFrom().completionStage(record.ack()))
        .onFailure()
        .recoverWithUni(
            error -> {
              log.error("Failed to replicate customer event", error);
              return Uni.createFrom().completionStage(record.nack(error));
            });
  }

  private Uni<Void> process(IncomingKafkaRecord<String, String> record) {
    String payload = record.getPayload();
    String key = record.getKey();

    if (payload == null || payload.isBlank()) {
      String customerId = key != null ? parser.parseCustomerIdFromKey(key) : null;
      if (customerId == null) {
        log.warn("Tombstone event without customer id in key; skipping");
        return Uni.createFrom().voidItem();
      }
      return redisStore
          .delete(customerId)
          .invoke(() -> log.debug("Deleted customer {} from Redis (tombstone)", customerId));
    }

    DebeziumEnvelope envelope = parser.parseValue(payload);
    if (envelope == null || envelope.op() == null) {
      log.warn("Skipping event without op: {}", payload);
      return Uni.createFrom().voidItem();
    }

    return switch (envelope.op()) {
      case "c", "r", "u" -> upsert(envelope);
      case "d" -> delete(envelope, key);
      case "t" -> {
        log.debug("Truncate event ignored");
        yield Uni.createFrom().voidItem();
      }
      default -> {
        log.warn("Unsupported Debezium op '{}'", envelope.op());
        yield Uni.createFrom().voidItem();
      }
    };
  }

  private Uni<Void> upsert(DebeziumEnvelope envelope) {
    CustomerSnapshot customer = envelope.rowData();
    if (customer == null || customer.getCustomerId() == null) {
      return Uni.createFrom()
          .failure(new IllegalArgumentException("Create/update event without customer_id"));
    }
    return redisStore
        .upsert(customer)
        .invoke(
            () ->
                log.debug(
                    "Upserted customer {} in Redis (op={})",
                    customer.getCustomerId(),
                    envelope.op()));
  }

  private Uni<Void> delete(DebeziumEnvelope envelope, String key) {
    String customerId = null;
    if (envelope.before() != null) {
      customerId = envelope.before().getCustomerId();
    }
    if (customerId == null && envelope.after() != null) {
      customerId = envelope.after().getCustomerId();
    }
    if (customerId == null && key != null) {
      customerId = parser.parseCustomerIdFromKey(key);
    }
    if (customerId == null) {
      return Uni.createFrom()
          .failure(new IllegalArgumentException("Delete event without customer_id"));
    }
    final String idToDelete = customerId;
    return redisStore
        .delete(idToDelete)
        .invoke(() -> log.debug("Deleted customer {} from Redis (op=d)", idToDelete));
  }
}

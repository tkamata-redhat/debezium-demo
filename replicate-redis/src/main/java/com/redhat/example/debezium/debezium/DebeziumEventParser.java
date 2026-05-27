package com.redhat.example.debezium.debezium;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.example.debezium.model.CustomerSnapshot;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DebeziumEventParser {

  private final ObjectMapper objectMapper;

  public DebeziumEventParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public DebeziumEnvelope parseValue(String json) {
    if (json == null || json.isBlank()) {
      return null;
    }
    try {
      JsonNode root = objectMapper.readTree(json);
      JsonNode payload = unwrapPayload(root);
      String op = textOrNull(payload, "op");
      return new DebeziumEnvelope(
          op, toCustomer(payload.get("after")), toCustomer(payload.get("before")));
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to parse Debezium value: " + json, e);
    }
  }

  public String parseCustomerIdFromKey(String keyJson) {
    if (keyJson == null || keyJson.isBlank()) {
      return null;
    }
    try {
      JsonNode root = objectMapper.readTree(keyJson);
      JsonNode payload = unwrapPayload(root);
      CustomerSnapshot customer = toCustomer(payload);
      if (customer != null && customer.getCustomerId() != null) {
        return customer.getCustomerId();
      }
      return textOrNull(payload, "customer_id");
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to parse Debezium key: " + keyJson, e);
    }
  }

  private static JsonNode unwrapPayload(JsonNode root) {
    if (root != null && root.hasNonNull("payload")) {
      return root.get("payload");
    }
    return root;
  }

  private CustomerSnapshot toCustomer(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    return CustomerSnapshot.builder()
        .customerId(textOrNull(node, "customer_id"))
        .firstName(textOrNull(node, "first_name"))
        .lastName(textOrNull(node, "last_name"))
        .build();
  }

  private static String textOrNull(JsonNode node, String field) {
    if (node == null || !node.hasNonNull(field)) {
      return null;
    }
    return node.get(field).asText();
  }
}

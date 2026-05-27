package com.redhat.example.debezium.debezium;

import com.redhat.example.debezium.model.CustomerSnapshot;

public record DebeziumEnvelope(String op, CustomerSnapshot after, CustomerSnapshot before) {

  public CustomerSnapshot rowData() {
    if (after != null) {
      return after;
    }
    return before;
  }
}

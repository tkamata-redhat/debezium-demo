package com.redhat.example.debezium.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Entity
@Table(name = "customer", schema = "example_schema")
@Data
@NoArgsConstructor
@Schema(description = "Customer record in example_schema.customer")
public class Customer {

  @Id
  @Column(name = "customer_id")
  @Schema(description = "Primary key", example = "C001", required = true)
  private String customerId;

  @Column(name = "first_name")
  @Schema(description = "First name", example = "Taro")
  private String firstName;

  @Column(name = "last_name")
  @Schema(description = "Last name", example = "Yamada")
  private String lastName;
}

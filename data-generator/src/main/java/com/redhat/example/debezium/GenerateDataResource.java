package com.redhat.example.debezium;

import com.redhat.example.debezium.entity.Customer;
import com.redhat.example.debezium.repository.CustomerRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/generate-data")
@ApplicationScoped
@Slf4j
@Tag(name = "Customer", description = "Customer data operations for CDC testing")
public class GenerateDataResource {

  private final CustomerRepository customerRepository;

  public GenerateDataResource(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  @GET
  @Transactional
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "List all customers")
  @APIResponse(
      responseCode = "200",
      description = "All customers",
      content = @Content(schema = @Schema(implementation = Customer.class)))
  public Response generateData() {
    return Response.ok(customerRepository.listAll()).build();
  }

  @GET
  @Path("/{customerId}")
  @Transactional
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get a customer by ID")
  @APIResponse(
      responseCode = "200",
      description = "Customer found",
      content = @Content(schema = @Schema(implementation = Customer.class)))
  @APIResponse(responseCode = "404", description = "Customer not found")
  public Response getCustomer(@PathParam("customerId") String customerId) {
    Customer customer = customerRepository.find("customerId", customerId).firstResult();
    if (customer == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(customer).build();
  }

  @POST
  @Transactional
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Insert a customer")
  @APIResponse(
      responseCode = "200",
      description = "Customer created",
      content = @Content(schema = @Schema(implementation = Customer.class)))
  public Response insertCustomer(Customer customer) {
    log.info("Inserting customer: {}", customer);
    customerRepository.persist(customer);
    return Response.ok(customer).build();
  }

  @PUT
  @Transactional
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Update a customer")
  @APIResponse(
      responseCode = "200",
      description = "Customer updated",
      content = @Content(schema = @Schema(implementation = Customer.class)))
  @APIResponse(responseCode = "404", description = "Customer not found")
  public Response updateCustomer(Customer customer) {
    log.info("Updating customer: {}", customer);
    Customer existing =
        customerRepository.find("customerId", customer.getCustomerId()).firstResult();
    if (existing == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    existing.setFirstName(customer.getFirstName());
    existing.setLastName(customer.getLastName());
    return Response.ok(existing).build();
  }

  @DELETE
  @Path("/{customerId}")
  @Transactional
  @Operation(summary = "Delete a customer by ID")
  @APIResponse(responseCode = "204", description = "Customer deleted")
  @APIResponse(responseCode = "404", description = "Customer not found")
  public Response deleteCustomer(@PathParam("customerId") String customerId) {
    log.info("Deleting customer: {}", customerId);
    long deleted = customerRepository.delete("customerId", customerId);
    if (deleted == 0) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.noContent().build();
  }
}

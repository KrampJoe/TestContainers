package org.joey.testcontainers.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

class CustomerServiceTest {

    static PostgreSQLContainer<?> postgres = TestContainerManager.getPostgresContainer();

    CustomerService customerService;

    @BeforeEach
    void setUp() {
        DBConnectionProvider connectionProvider = new DBConnectionProvider(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );
        customerService = new CustomerService(connectionProvider);
        clearCustomersTable();
    }

    void clearCustomersTable() {
        try (Connection conn = customerService.getConnectionProvider().getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM customers");
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldGetCustomers() {
        customerService.createCustomer(new Customer(1L, "George"));
        customerService.createCustomer(new Customer(2L, "John"));
        customerService.createCustomer(new Customer(3L, "Joe"));

        List<Customer> customers = customerService.getAllCustomers();
        assertEquals(3, customers.size());
    }

    @Test
    void shouldUpdateCustomer() {
        customerService.createCustomer(new Customer(1L, "George"));
        customerService.updateCustomer(new Customer(1L, "George Updated"));

        Customer updatedCustomer = customerService.getCustomerById(1L);
        assertEquals("George Updated", updatedCustomer.name());
    }

    @Test
    void shouldDeleteCustomer() {
        customerService.createCustomer(new Customer(1L, "George"));
        customerService.deleteCustomer(1L);

        Customer deletedCustomer = customerService.getCustomerById(1L);
        assertNull(deletedCustomer);
    }
}
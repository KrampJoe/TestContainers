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

class OrderServiceTest {

    static PostgreSQLContainer<?> postgres = TestContainerManager.getPostgresContainer();

    OrderService orderService;

    @BeforeEach
    void setUp() {
        DBConnectionProvider connectionProvider = new DBConnectionProvider(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );
        orderService = new OrderService(connectionProvider);
        clearOrdersTable();
    }

    void clearOrdersTable() {
        try (Connection conn = orderService.getConnectionProvider().getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM orders");
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldCreateOrder() {
        orderService.createOrder(new Order(1L, "Order1"));
        Order order = orderService.getOrderById(1L);
        assertEquals("Order1", order.name());
    }

    @Test
    void shouldUpdateOrder() {
        orderService.createOrder(new Order(1L, "Order1"));
        orderService.updateOrder(new Order(1L, "Order1 Updated"));

        Order updatedOrder = orderService.getOrderById(1L);
        assertEquals("Order1 Updated", updatedOrder.name());
    }

    @Test
    void shouldDeleteOrder() {
        orderService.createOrder(new Order(1L, "Order1"));
        orderService.deleteOrder(1L);

        Order deletedOrder = orderService.getOrderById(1L);
        assertNull(deletedOrder);
    }
}
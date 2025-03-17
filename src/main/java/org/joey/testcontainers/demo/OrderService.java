package org.joey.testcontainers.demo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderService {

    private final DBConnectionProvider connectionProvider;

    public OrderService(DBConnectionProvider dbConnectionProvider) {
        this.connectionProvider = dbConnectionProvider;
        createOrdersTableIfNotExists();
    }

    public DBConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    public void createOrder(Order order) {
        try (Connection conn = this.connectionProvider.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "insert into orders(id,name) values(?,?)"
            );
            pstmt.setLong(1, order.id());
            pstmt.setString(2, order.name());
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateOrder(Order order) {
        try (Connection conn = this.connectionProvider.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "update orders set name = ? where id = ?"
            );
            pstmt.setString(1, order.name());
            pstmt.setLong(2, order.id());
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteOrder(long id) {
        try (Connection conn = this.connectionProvider.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "delete from orders where id = ?"
            );
            pstmt.setLong(1, id);
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Order getOrderById(long id) {
        try (Connection conn = this.connectionProvider.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "select id, name from orders where id = ?"
            );
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Order(rs.getLong("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void createOrdersTableIfNotExists() {
        try (Connection conn = this.connectionProvider.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(
                    """
                    create table if not exists orders (
                        id bigint not null,
                        name varchar not null,
                        primary key (id)
                    )
                    """
            );
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
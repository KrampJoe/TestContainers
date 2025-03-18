package org.joey.testcontainers.tests;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerService {

    private final DBConnectionProvider connectionProvider;

    public CustomerService(DBConnectionProvider dbConnectionProvider) {
        this.connectionProvider = dbConnectionProvider;
        createCustomersTableIfNotExists();
    }

    public DBConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    public void createCustomer(Customer customer) {
        try (Connection conn = this.connectionProvider.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "insert into customers(id,name) values(?,?)"
            );
            pstmt.setLong(1, customer.id());
            pstmt.setString(2, customer.name());
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();

        try (Connection conn = this.connectionProvider.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "select id, name from customers"
            );
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                customers.add(new Customer(id, name));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return customers;
    }

    public void updateCustomer(Customer customer) {
        try (Connection conn = this.connectionProvider.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "update customers set name = ? where id = ?"
            );
            pstmt.setString(1, customer.name());
            pstmt.setLong(2, customer.id());
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteCustomer(long id) {
        try (Connection conn = this.connectionProvider.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "delete from customers where id = ?"
            );
            pstmt.setLong(1, id);
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Customer getCustomerById(long id) {
        try (Connection conn = this.connectionProvider.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "select id, name from customers where id = ?"
            );
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Customer(rs.getLong("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void createCustomersTableIfNotExists() {
        try (Connection conn = this.connectionProvider.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(
                    """
                            create table if not exists customers (
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
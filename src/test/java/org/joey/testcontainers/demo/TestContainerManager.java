package org.joey.testcontainers.demo;

import org.testcontainers.containers.PostgreSQLContainer;

public class TestContainerManager {

    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        postgresContainer.start();
    }

    public static PostgreSQLContainer<?> getPostgresContainer() {
        return postgresContainer;
    }
}

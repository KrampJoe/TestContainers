package org.joey.testcontainers.tests.config;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgressTestContainerSetup {

    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        postgresContainer.start();
    }

    public static PostgreSQLContainer<?> getPostgresContainer() {
        return postgresContainer;
    }
}

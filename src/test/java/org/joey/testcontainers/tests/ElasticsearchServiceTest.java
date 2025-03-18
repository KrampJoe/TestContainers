package org.joey.testcontainers.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.joey.testcontainers.tests.config.ElasticsearchTestContainerSetup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;

@SpringBootTest
@ContextConfiguration(classes = ElasticsearchTestContainerSetup.class)
class ElasticsearchServiceTest {

    @Autowired
    private RestClient client;

    @AfterAll
    static void afterAll(@Autowired RestClient client) {
        try {
            if (client != null) {
                client.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to close RestClient", e);
        }
    }

    @Test
    void shouldGetClusterHealth() throws IOException {
        Response response = client.performRequest(new Request("GET", "/_cluster/health"));
        assertNotNull(response);
    }

    @Test
    void shouldCreateIndex() throws IOException {
        Request checkRequest = new Request("HEAD", "/test-index");
        Response checkResponse = client.performRequest(checkRequest);

        if (checkResponse.getStatusLine().getStatusCode() == 404) {
            Request request = new Request("PUT", "/test-index");
            Response response = client.performRequest(request);
            assertEquals(200, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    void shouldInsertDocument() throws IOException {
        Request request = new Request("POST", "/test-index/_doc");
        request.setJsonEntity("{\"field\": \"value\"}");
        Response response = client.performRequest(request);
        assertEquals(201, response.getStatusLine().getStatusCode());
    }

    @Test
    void shouldRetrieveDocument() throws IOException {
        Request insertRequest = new Request("PUT", "/test-index/_doc/1");
        insertRequest.setJsonEntity("{\"field\": \"value\"}");
        client.performRequest(insertRequest);

        Request getRequest = new Request("GET", "/test-index/_doc/1");
        Response response = client.performRequest(getRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    void shouldDeleteIndex() throws IOException {
        Request createRequest = new Request("PUT", "/test-index");
        client.performRequest(createRequest);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Request deleteRequest = new Request("DELETE", "/test-index");
        Response response = client.performRequest(deleteRequest);

        assertEquals(200, response.getStatusLine().getStatusCode());
    }
}
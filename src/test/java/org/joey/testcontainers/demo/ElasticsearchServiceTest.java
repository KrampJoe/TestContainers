package org.joey.testcontainers.demo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.HttpHost;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.IOException;

class ElasticsearchServiceTest {

    static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.11.0")
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false");

    static RestClient client;

    @BeforeAll
    static void beforeAll() throws IOException {
        elasticsearchContainer.start();

        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", System.getenv().getOrDefault("ES_PASSWORD", "password"))
        );

        RestClientBuilder builder = RestClient.builder(
                new HttpHost(elasticsearchContainer.getHost(), elasticsearchContainer.getFirstMappedPort())
        ).setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

        client = builder.build();
    }

    @AfterAll
    static void afterAll() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to close RestClient", e);
        } finally {
            elasticsearchContainer.stop();
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
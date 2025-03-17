package org.joey.testcontainers.demo;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(
            "docker.elastic.co/elasticsearch/elasticsearch:8.11.0"
    )
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false")
            .withCreateContainerCmdModifier(cmd -> cmd.withMemory(2048L * 1024L * 1024L).withCpuShares(1024)); // 2GB memory

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
}

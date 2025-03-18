package org.joey.testcontainers.tests.config;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

@TestConfiguration(proxyBeanMethods = false)
public class ElasticsearchTestContainerSetup {

    @Bean
    @ServiceConnection
    public ElasticsearchContainer elasticsearchContainer() {
        return new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.11.0"))
                .withEnv("discovery.type", "single-node")
                .withEnv("xpack.security.enabled", "false");
    }

    @Bean
    public RestClient restClient(ElasticsearchContainer elasticsearchContainer) throws IOException {
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", System.getenv().getOrDefault("ES_PASSWORD", "password"))
        );

        RestClientBuilder builder = RestClient.builder(
                new HttpHost(elasticsearchContainer.getHost(), elasticsearchContainer.getFirstMappedPort())
        ).setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

        return builder.build();
    }
}
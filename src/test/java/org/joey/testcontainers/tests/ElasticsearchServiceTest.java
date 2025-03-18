package org.joey.testcontainers.tests;

        import static org.junit.jupiter.api.Assertions.assertNotNull;
        import static org.junit.jupiter.api.Assertions.assertEquals;

        import org.elasticsearch.client.Request;
        import org.elasticsearch.client.Response;
        import org.elasticsearch.client.RestClient;
        import org.joey.testcontainers.tests.config.ElasticsearchTestContainerSetup;
        import org.joey.testcontainers.tests.util.ElasticsearchTestUtility;
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

            @Autowired
            private ElasticsearchTestUtility testUtility;

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
                testUtility.removeIndex("test-index"); // Ensure the index does not exist
                testUtility.ingestData("test-index", "1", "{\"field\": \"value\"}");
                Response response = client.performRequest(new Request("HEAD", "/test-index"));
                assertEquals(200, response.getStatusLine().getStatusCode());
            }

            @Test
            void shouldInsertDocument() throws IOException {
                testUtility.removeIndex("test-index"); // Ensure the index does not exist
                testUtility.ingestData("test-index", "1", "{\"field\": \"value\"}");
                Response response = client.performRequest(new Request("GET", "/test-index/_doc/1"));
                assertNotNull(response);
                assertEquals(200, response.getStatusLine().getStatusCode());
            }

            @Test
            void shouldRetrieveDocument() throws IOException {
                testUtility.removeIndex("test-index"); // Ensure the index does not exist
                testUtility.ingestData("test-index", "1", "{\"field\": \"value\"}");
                Response response = client.performRequest(new Request("GET", "/test-index/_doc/1"));
                assertNotNull(response);
                assertEquals(200, response.getStatusLine().getStatusCode());
            }

            @Test
            void shouldDeleteIndex() throws IOException {
                testUtility.ingestData("test-index", "1", "{\"field\": \"value\"}");
                testUtility.removeIndex("test-index");
                Response response = client.performRequest(new Request("HEAD", "/test-index"));
                assertEquals(404, response.getStatusLine().getStatusCode());
            }
        }
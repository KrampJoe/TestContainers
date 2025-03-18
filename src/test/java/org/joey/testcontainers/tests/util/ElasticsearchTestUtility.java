package org.joey.testcontainers.tests.util;

import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ElasticsearchTestUtility {

    @Autowired
    private RestClient client;

    public void ingestData(String index, String id, String jsonData) throws IOException {
        Request request = new Request("PUT", "/" + index + "/_doc/" + id);
        request.setJsonEntity(jsonData);
        Response response = client.performRequest(request);
        if (response.getStatusLine().getStatusCode() != 201 && response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Failed to ingest data: " + response.getStatusLine().getReasonPhrase());
        }
    }

    public void removeData(String index, String id) throws IOException {
        Request request = new Request("DELETE", "/" + index + "/_doc/" + id);
        Response response = client.performRequest(request);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Failed to remove data: " + response.getStatusLine().getReasonPhrase());
        }
    }

    public void removeIndex(String index) throws IOException {
        Request request = new Request("DELETE", "/" + index);
        Response response = client.performRequest(request);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Failed to remove index: " + response.getStatusLine().getReasonPhrase());
        }
    }
}
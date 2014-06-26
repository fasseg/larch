package net.objecthunter.larch.service.backend.elasticsearch;

import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract service for providing utilitxy methods used by all the different ElasticSearch services
 */
public class AbstractElasticSearchService {

    @Autowired
    protected Client client;

    protected void refreshIndex(String... indices) {
        client.admin().indices().refresh(new RefreshRequest(indices)).actionGet();
    }

    protected void checkAndOrCreateIndex(String indexName) {
        if (!indexExists(indexName)) {
            client.admin().indices().prepareCreate(indexName).execute().actionGet();
        }
    }

    protected boolean indexExists(String indexName) {
        return client.admin().indices().exists(new IndicesExistsRequest(indexName)).actionGet().isExists();
    }

    protected void waitForIndex(String indexName) {
        this.client.admin().cluster().prepareHealth(indexName).setWaitForYellowStatus().execute().actionGet();
    }
}

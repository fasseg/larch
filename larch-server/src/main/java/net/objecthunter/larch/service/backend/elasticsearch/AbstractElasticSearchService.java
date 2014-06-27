package net.objecthunter.larch.service.backend.elasticsearch;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Abstract service for providing utility methods used by all the different ElasticSearch services
 */
public class AbstractElasticSearchService {

    @Autowired
    protected Environment env;

    @Autowired
    protected Client client;

    @Autowired
    protected ObjectMapper mapper;

    protected void refreshIndex(String... indices) {
        client.admin().indices().refresh(new RefreshRequest(indices)).actionGet();
    }

    protected void checkAndOrCreateIndex(String indexName) {
        if (!indexExists(indexName)) {
            Map mappings = getMappings(indexName);
            if (mappings != null && !mappings.isEmpty()) {
                CreateIndexRequestBuilder requestBuilder = client.admin().indices().prepareCreate(indexName);
                for (String key : ((Set<String>) mappings.keySet())) {
                    try {
                        requestBuilder.addMapping(key, mapper.writeValueAsString(mappings.get(key)));
                    }
                    catch (Exception e) {
                    }
                }
                requestBuilder.execute().actionGet();
            }
            else {
                client.admin().indices().prepareCreate(indexName).execute().actionGet();
            }
        }
    }

    protected boolean indexExists(String indexName) {
        return client.admin().indices().exists(new IndicesExistsRequest(indexName)).actionGet().isExists();
    }

    protected void waitForIndex(String indexName) {
        this.client.admin().cluster().prepareHealth(indexName).setWaitForYellowStatus().execute().actionGet();
    }

    private Map getMappings(String indexName) {
        System.out.println(env.getProperty("elasticsearch.config.path") + indexName + ".json");
        InputStream in =
            this.getClass().getResourceAsStream(
                env.getProperty("elasticsearch.config.path") + indexName + "_mappings.json");
        if (in != null) {
            try {
                String mappings = IOUtils.toString(in);
                return mapper.readValue(mappings, Map.class);
            }
            catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}

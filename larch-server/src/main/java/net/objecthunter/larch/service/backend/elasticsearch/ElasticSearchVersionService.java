package net.objecthunter.larch.service.backend.elasticsearch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.model.Entities;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Version;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;
import net.objecthunter.larch.service.backend.BackendVersionService;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service implementation on top of ElasticSearch
 */
public class ElasticSearchVersionService extends AbstractElasticSearchService implements BackendVersionService {
    public static final String INDEX_VERSIONS = "versions";

    public static final String TYPE_VERSIONS = "version";

    private static final Logger log = LoggerFactory.getLogger(ElasticSearchVersionService.class);

    @Autowired
    private BackendBlobstoreService backendBlobstoreService;

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    public void init() throws IOException {
        log.debug("initialising ElasticSearchVersionService");
        this.checkAndOrCreateIndex(INDEX_VERSIONS);
        this.waitForIndex(INDEX_VERSIONS);
    }

    @Override
    public void addOldVersion(Entity e) throws IOException {
        final String path = this.backendBlobstoreService.createOldVersionBlob(e);
        final Version version = new Version();
        version.setEntityId(e.getId());
        version.setVersionNumber(e.getVersion());
        version.setPath(path);
        final IndexResponse resp =
            this.client
                .prepareIndex(INDEX_VERSIONS, TYPE_VERSIONS).setSource(this.mapper.writeValueAsBytes(version))
                .execute().actionGet();
        this.refreshIndex(INDEX_VERSIONS);
        log.info("added entity {} version {}", version.getEntityId(), version.getVersionNumber());
    }

    @Override
    public Entity getOldVersion(String id, int versionNumber) throws IOException {
        final SearchResponse resp =
            client
                .prepareSearch(INDEX_VERSIONS)
                .setQuery(
                    QueryBuilders
                    .boolQuery().must(QueryBuilders.matchQuery("entityId", id))
                    .must(QueryBuilders.matchQuery("versionNumber", versionNumber))).setFrom(0).setSize(1)
                .execute().actionGet();
        if (resp.getHits().getTotalHits() == 0) {
            throw new FileNotFoundException("Entity " + id + " does not exists with version " + versionNumber);
        }
        final Version v = this.mapper.readValue(resp.getHits().getAt(0).getSourceAsString(), Version.class);
        return this.mapper.readValue(this.backendBlobstoreService.retrieveOldVersionBlob(v.getPath()), Entity.class);
    }

    @Override
    public Entities getOldVersions(String id) throws IOException {
        final SearchResponse resp =
            client
                .prepareSearch(INDEX_VERSIONS)
                // .setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("entityId", id))).setSize(1000)
                .setQuery(
                    QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
                        FilterBuilders.termFilter("entityId", id))).setSize(1000)
                .addSort("versionNumber", SortOrder.DESC).execute().actionGet();
        System.out.println("MIH: found " + resp.getHits().getHits().length + " hits");
        final List<Entity> entities = new ArrayList<Entity>();
        for (final SearchHit hit : resp.getHits()) {
            final Version v = this.mapper.readValue(hit.getSourceAsString(), Version.class);
            final Entity e =
                this.mapper.readValue(this.backendBlobstoreService.retrieveOldVersionBlob(v.getPath()), Entity.class);
            entities.add(e);
        }
        Entities entit = new Entities();
        entit.setEntities(entities);
        System.out.println("MIH: found " + entities.size() + " entities");
        return entit;
    }

}

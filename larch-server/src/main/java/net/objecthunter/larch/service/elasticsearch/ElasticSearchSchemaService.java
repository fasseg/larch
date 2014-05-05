/* 
* Copyright 2014 Frank Asseg
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*/
package net.objecthunter.larch.service.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.objecthunter.larch.helpers.MetadataTypes;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.MetadataType;
import net.objecthunter.larch.model.MetadataValidationResult;
import net.objecthunter.larch.service.SchemaService;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ElasticSearchSchemaService implements SchemaService {
    public static final String INDEX_MD_SCHEMATA = "mdschema";
    public static final String INDEX_MD_SCHEMATA_TYPE = "mdschema-type";
    private static final Logger log = LoggerFactory.getLogger(ElasticSearchSchemaService.class);


    @Autowired
    private Client client;

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    public void init() throws IOException {
        /* check if the metadata type index exists */
        log.debug("initialising ElasticSearchSchemaService");
        boolean indexExists = client.admin()
                .indices()
                .exists(new IndicesExistsRequest(INDEX_MD_SCHEMATA))
                .actionGet()
                .isExists();
        if (!indexExists) {
            /* create the necessary index */
            client.admin()
                    .indices()
                    .create(new CreateIndexRequest(INDEX_MD_SCHEMATA))
                    .actionGet();
            /* create the default metadata types */
            for (MetadataType type : MetadataTypes.getDefaultMetadataTypes()) {
                final IndexResponse resp = this.client.prepareIndex(INDEX_MD_SCHEMATA, INDEX_MD_SCHEMATA_TYPE,
                        type.getName())
                        .setSource(mapper.writeValueAsBytes(type))
                        .execute()
                        .actionGet();
            }
        }
        this.client.admin().cluster()
                .prepareHealth(INDEX_MD_SCHEMATA)
                .setWaitForYellowStatus()
                .execute()
                .actionGet();
    }

    private void refreshIndex(String... indices) {
        client.admin()
                .indices()
                .refresh(new RefreshRequest(indices))
                .actionGet();
    }

    public String getSchemUrlForType(String type) throws IOException {
        final GetResponse get = this.client.prepareGet(INDEX_MD_SCHEMATA, INDEX_MD_SCHEMATA_TYPE, type)
                .execute()
                .actionGet();
        if (!get.isExists()) {
            throw new IOException("Metadata type '" + type + "' does not exist");
        }
        final MetadataType mdType = mapper.readValue(get.getSourceAsBytes(), MetadataType.class);
        return mdType.getSchemaUrl();
    }

    @Override
    public List<MetadataType> getSchemaTypes() throws IOException {
        final SearchResponse search = this.client.prepareSearch(INDEX_MD_SCHEMATA)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.matchAllQuery())
                .execute()
                .actionGet();
        final List<MetadataType> mdTypes = new ArrayList<>(search.getHits().getHits().length);
        for (SearchHit h : search.getHits()) {
            mdTypes.add(mapper.readValue(h.getSourceAsString(), MetadataType.class));
        }
        return mdTypes;
    }

    @Override
    public String createSchemaType(MetadataType newType) throws IOException {
        /* check if the metadata type already exists */
        final boolean exists = this.client.prepareGet(INDEX_MD_SCHEMATA, INDEX_MD_SCHEMATA_TYPE, newType.getName())
                .execute()
                .actionGet()
                .isExists();
        if (exists) {
            throw new IOException("Metadata type " + newType.getName() + " already exists");
        }
        /* add the new type to the index */
        final IndexResponse resp = this.client.prepareIndex(INDEX_MD_SCHEMATA, INDEX_MD_SCHEMATA_TYPE, newType.getName())
                .setSource(mapper.writeValueAsBytes(newType))
                .execute()
                .actionGet();
        this.refreshIndex(INDEX_MD_SCHEMATA);
        return resp.getId();
    }

    @Override
    public void deleteMetadataType(String name) throws IOException {
        /* first check if the type is still used by Entities or Binaries */
        final CountResponse count = this.client.prepareCount(ElasticSearchIndexService.INDEX_ENTITIES)
                .setQuery(QueryBuilders.nestedQuery("metadata", QueryBuilders.matchQuery("type", name)))
                .execute()
                .actionGet();
        if (count.getCount() > 0) {
            throw new IOException("Unable to delete metadata type ' " + name + "' since it's still in use");
        }
        /* the metadata type is safe to delete, since it's no longer used */
        log.debug("deleting meta data type {} ", name);
        final DeleteResponse delete = this.client.prepareDelete(ElasticSearchSchemaService.INDEX_MD_SCHEMATA,
                INDEX_MD_SCHEMATA_TYPE, name)
                .execute()
                .actionGet();
    }

    @Override
    public MetadataValidationResult validate(String id, String metadataName) throws IOException {
        /* fetch the entity first */
        final GetResponse resp = this.client.prepareGet(ElasticSearchIndexService
                        .INDEX_ENTITIES,
                ElasticSearchIndexService.INDEX_ENTITY_TYPE, id
        )
                .execute()
                .actionGet();
        if (!resp.isExists()) {
            throw new IOException("The entity '" + id + "' does not exist");
        }
        /* fetch the named metadata which will be validatet */
        final Entity e = mapper.readValue(resp.getSourceAsBytes(), Entity.class);
        if (e.getMetadata() == null || !e.getMetadata().containsKey(metadataName)) {
            throw new IOException("The entity '" + id + "' has no meta data record named '" + metadataName);
        }
        final Metadata md = e.getMetadata().get(metadataName);
        final String schemaUrl = this.getSchemUrlForType(md.getType());

        /* validate the schema against the given URL */
        final MetadataValidationResult result = new MetadataValidationResult();
        try {
            final URL schemaFile = new URL(schemaUrl);
            final Source xmlFile = new StreamSource(new ByteArrayInputStream(md.getData().getBytes()));
            final SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = schemaFactory.newSchema(schemaFile);
            final Validator validator = schema.newValidator();
            try {
                validator.validate(xmlFile);
                result.setSuccess(true);
                result.setTimestamp(ZonedDateTime.now(ZoneOffset.UTC).toString());
                result.setDetails("Validation successful");
                return result;
            } catch (SAXException validationException) {
                result.setSuccess(false);
                result.setTimestamp(ZonedDateTime.now(ZoneOffset.UTC).toString());
                result.setDetails(validationException.getMessage());
                return result;
            }
        } catch (SAXException se) {
            throw new IOException(se);
        }
    }

}

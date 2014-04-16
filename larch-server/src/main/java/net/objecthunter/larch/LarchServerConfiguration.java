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
package net.objecthunter.larch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import net.objecthunter.larch.elasticsearch.ElasticSearchIndexService;
import net.objecthunter.larch.elasticsearch.ElasticSearchNode;
import net.objecthunter.larch.elasticsearch.ElasticSearchSearchService;
import net.objecthunter.larch.fs.FilesystemBlobstoreService;
import net.objecthunter.larch.service.ExportService;
import net.objecthunter.larch.service.SchemaService;
import net.objecthunter.larch.service.SearchService;
import net.objecthunter.larch.service.impl.DefaultEntityService;
import net.objecthunter.larch.service.impl.DefaultExportService;
import net.objecthunter.larch.service.impl.DefaultRepositoryService;
import net.objecthunter.larch.service.impl.LarchElasticSearchAuthenticationManager;
import net.objecthunter.larch.weedfs.WeedFSBlobstoreService;
import net.objecthunter.larch.weedfs.WeedFsMaster;
import net.objecthunter.larch.weedfs.WeedFsVolume;
import org.elasticsearch.client.Client;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@EnableAutoConfiguration
@ComponentScan(basePackages = "net.objecthunter.larch.controller")
@Configuration
@EnableGlobalMethodSecurity
public class LarchServerConfiguration {

    @Bean
    public AuthenticationManager authenticationManager() {
        return new LarchElasticSearchAuthenticationManager();
    }

    @Bean
    public SchemaService schemaService() {
        return new SchemaService();
    }

    @Bean
    public DefaultEntityService defaultEntityService() {
        return new DefaultEntityService();
    }

    @Bean
    public DefaultRepositoryService defaultRepositoryService() {
        return new DefaultRepositoryService();
    }

    @Bean
    public ElasticSearchIndexService elasticSearchIndexService() {
        return new ElasticSearchIndexService();
    }

    @Bean
    public Client elasticSearchClient() {
        return this.elasticSearchNode().getClient();
    }

    @Bean
    public ElasticSearchNode elasticSearchNode() {
        return new ElasticSearchNode();
    }

    @Bean
    @Profile("fs")
    public FilesystemBlobstoreService filesystemBlobstoreService() {
        return new FilesystemBlobstoreService();
    }

    @Bean
    @Profile("weedfs")
    @Order(40)
    public WeedFsMaster weedFsMaster() {
        return new WeedFsMaster();
    }

    @Bean
    @Profile("weedfs")
    @Order(50)
    public WeedFsVolume weedfsVolume() {
        return new WeedFsVolume();
    }

    @Bean
    @Profile("weedfs")
    public WeedFSBlobstoreService weedFSBlobstoreService() {
        return new WeedFSBlobstoreService();
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper;
    }

    @Bean
    public SerializationConfig serializationConfig() {
        return objectMapper().getSerializationConfig();
    }

    @Bean
    public ExportService exportService() {
        return new DefaultExportService();
    }

    @Bean
    public SearchService service() {
        return new ElasticSearchSearchService();
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        return new CommonsMultipartResolver();
    }


}
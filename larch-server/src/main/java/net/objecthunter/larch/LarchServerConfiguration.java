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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.objecthunter.larch.elasticsearch.ElasticSearchIndexService;
import net.objecthunter.larch.elasticsearch.ElasticSearchNode;
import net.objecthunter.larch.fs.FilesystemBlobstoreService;
import net.objecthunter.larch.model.json.ZonedDateTimeDeserializer;
import net.objecthunter.larch.model.json.ZonedDateTimeSerializer;
import net.objecthunter.larch.service.impl.DefaultEntityService;
import net.objecthunter.larch.service.impl.DefaultRepositoryService;
import net.objecthunter.larch.weedfs.WeedFSBlobstoreService;
import net.objecthunter.larch.weedfs.WeedFsMaster;
import net.objecthunter.larch.weedfs.WeedFsVolume;
import org.elasticsearch.client.Client;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.ZonedDateTime;

@EnableAutoConfiguration
@ComponentScan(basePackages = "net.objecthunter.larch.controller")
@Configuration
@EnableWebMvc
public class LarchServerConfiguration {

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
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        final SimpleModule zoneDateTimeModule = new SimpleModule("ZoneDateTimeModule",new Version(1,0,0,"static version","net.objecthunter.larch","larch-common"));
        zoneDateTimeModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer());
        zoneDateTimeModule.addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());
        mapper.registerModule(zoneDateTimeModule);
        return mapper;
    }
    @Bean
    public ApplicationListener<ContextStoppedEvent> contextStoppedEvent() {
        return new ApplicationListener<ContextStoppedEvent>() {
            @Override
            public void onApplicationEvent(ContextStoppedEvent event) {
                System.out.println("\n\n\nSHUTDOWN\n\n\n");
            }
        };
    }
}
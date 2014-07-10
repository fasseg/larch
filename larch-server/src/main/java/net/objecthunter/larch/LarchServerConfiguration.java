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

import java.io.File;

import javax.jms.Queue;

import net.objecthunter.larch.service.*;
import net.objecthunter.larch.service.backend.*;
import net.objecthunter.larch.service.backend.elasticsearch.*;
import net.objecthunter.larch.service.backend.fs.FilesystemBlobstoreService;
import net.objecthunter.larch.service.backend.weedfs.WeedFSBlobstoreService;
import net.objecthunter.larch.service.backend.weedfs.WeedFsMaster;
import net.objecthunter.larch.service.backend.weedfs.WeedFsVolume;
import net.objecthunter.larch.service.impl.*;
import net.objecthunter.larch.util.FileSystemUtil;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

/**
 * General JavaConfig class for the larch repository containing all the necessary beans for a larch repository except
 * the security context configuration
 */
@EnableAutoConfiguration
@ComponentScan(basePackages = "net.objecthunter.larch.controller")
@Configuration
public class LarchServerConfiguration {

    @Autowired
    public Environment env;

    /**
     * Get a {@link net.objecthunter.larch.service.impl.DefaultEntityService} Spring bean
     * 
     * @return the {@link net.objecthunter.larch.service.DefaultEntityService} implementation
     */
    @Bean
    public EntityService defaultEntityService() {
        return new DefaultEntityService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.impl.DefaultPublishService} Spring bean
     * 
     * @return the {@link net.objecthunter.larch.service.DefaultPublishService} implementation
     */
    @Bean
    public PublishService defaultPublishService() {
        return new DefaultPublishService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.impl.DefaultSchemaService} Spring bean
     * 
     * @return the {@link net.objecthunter.larch.service.DefaultSchemaService} implementation
     */
    @Bean
    public SchemaService defaultSchemaService() {
        return new DefaultSchemaService();
    }

    /**
     * Get the {@link net.objecthunter.larch.service.impl.DefaultRepositoryService} Spring bean
     * 
     * @return the {@link net.objecthunter.larch.service.impl.DefaultRepositoryService} implementation
     */
    @Bean
    public RepositoryService defaultRepositoryService() {
        return new DefaultRepositoryService();
    }

    @Bean
    public MailService mailService() {
        return new DefaultMailService();
    }

    /**
     * Get the {@link net.objecthunter.larch.service.ExportService} Spring bean
     * 
     * @return a {@link net.objecthunter.larch.service.ExportService} implementation to be used by the repository
     */
    @Bean
    public ExportService exportService() {
        return new DefaultExportService();
    }

    /**
     * Get a ElasticSearch {@link org.elasticsearch.client.Client} Spring bean
     * 
     * @return the {@link org.elasticsearch.client.Client} implementation
     */
    @Bean
    public Client elasticSearchClient() {
        return this.elasticSearchNode().getClient();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.BackendAuditService} Spring bean
     * 
     * @return the {@link net.objecthunter.larch.service.backend.BackendAuditService} implementation
     */
    @Bean
    public BackendAuditService backendAuditService() {
        return new ElasticSearchAuditService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.BackendSchemaService} Spring bean
     * 
     * @return the {@link net.objecthunter.larch.service.backend.BackendSchemaService} implementation
     */
    @Bean
    public BackendSchemaService backendSchemaService() {
        return new ElasticSearchSchemaService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.BackendEntityService} implementation Spring bean
     * 
     * @return a {@link net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService}
     *         implementation
     */
    @Bean
    public BackendEntityService elasticSearchIndexService() {
        return new ElasticSearchEntityService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.BackendPublishService} implementation Spring bean
     * 
     * @return a {@link net.objecthunter.larch.service.backend.BackendPublishService} implementation
     */
    @Bean
    public BackendPublishService backendPublishService() {
        return new ElasticSearchPublishService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.BackendVersionService} Spring bean
     * 
     * @return a BackendVersionService implementation
     */
    @Bean
    public BackendVersionService backendVersionService() {
        return new ElasticSearchVersionService();
    }

    /**
     * Get {@link net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchNode} Spring bean responsible for
     * starting and stopping the ElasticSearch services
     * 
     * @return the {@link net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchNode} object
     */
    @Bean
    public ElasticSearchNode elasticSearchNode() {
        return new ElasticSearchNode();
    }

    /**
     * Get {@link net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchWorkspaceService} Spring bean for
     * interaction with the workspace index
     * 
     * @return the {@link net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchWorkspaceService} singleton
     */
    @Bean
    public BackendWorkspaceService backendWorkspaceService() {
        return new ElasticSearchWorkspaceService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.fs.FilesystemBlobstoreService} implementation for usage as
     * a {@link net.objecthunter.larch.service.backend.BackendBlobstoreService} in the repository
     * 
     * @return the {@link net.objecthunter.larch.service.backend.fs.FilesystemBlobstoreService} implementation
     */
    @Bean
    @Profile("fs")
    public FilesystemBlobstoreService filesystemBlobstoreService() {
        return new FilesystemBlobstoreService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.weedfs.WeedFsMaster} object responsible for starting and
     * stopping the Weed FS master node
     * 
     * @return the {@link net.objecthunter.larch.service.backend.weedfs.WeedFsMaster} object
     */
    @Bean
    @Profile("weedfs")
    @Order(40)
    public WeedFsMaster weedFsMaster() {
        return new WeedFsMaster();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.weedfs.WeedFsVolume} object responsible for starting and
     * stopping a Weed FS volume node
     * 
     * @return the {@link net.objecthunter.larch.service.backend.weedfs.WeedFsVolume} object
     */
    @Bean
    @Profile("weedfs")
    @Order(50)
    public WeedFsVolume weedfsVolume() {
        return new WeedFsVolume();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.weedfs.WeedFSBlobstoreService} implementation as the
     * {@link net.objecthunter.larch.service.backend.BackendBlobstoreService} fro the repository
     * 
     * @return the {@link net.objecthunter.larch.service.backend.weedfs.WeedFSBlobstoreService} object
     */
    @Bean
    @Profile("weedfs")
    public WeedFSBlobstoreService weedFSBlobstoreService() {
        return new WeedFSBlobstoreService();
    }

    /**
     * Get a Jackson {@link com.fasterxml.jackson.databind.ObjectMapper} Spring bean for JSON
     * serialization/deserialization
     * 
     * @return the {@link com.fasterxml.jackson.databind.ObjectMapper} implementation used by various services
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JSR310Module());
        return mapper;
    }

    /**
     * Get the {@link com.fasterxml.jackson.databind.SerializationConfig} Spring bean for the Jackson
     * {@link com.fasterxml.jackson.databind.ObjectMapper}
     * 
     * @return the {@link com.fasterxml.jackson.databind.SerializationConfig} that should be used by the Jackson
     *         mapper
     */
    @Bean
    public SerializationConfig serializationConfig() {
        return objectMapper().getSerializationConfig();
    }

    /**
     * A commons-multipart {@link org.springframework.web.multipart.commons.CommonsMultipartResolver} for resolving
     * files in a HTTP multipart request
     * 
     * @return a {@link org.springframework.web.multipart.commons.CommonsMultipartResolver} object used by Spring MVC
     */
    @Bean
    public CommonsMultipartResolver multipartResolver() {
        return new CommonsMultipartResolver();
    }

    /**
     * The Spring-security JavaConfig class containing the relevan AuthZ/AuthN definitions
     * 
     * @return the {@link LarchServerSecurityConfiguration} used
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 8)
    public LarchServerSecurityConfiguration larchServerSecurityConfiguration() {
        return new LarchServerSecurityConfiguration();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.BackendCredentialsService} implementation for use by the
     * repository
     * 
     * @return a {@link net.objecthunter.larch.service.backend.BackendCredentialsService} implementation
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 9)
    public ElasticSearchCredentialsService larchElasticSearchAuthenticationManager() {
        return new ElasticSearchCredentialsService();
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        final JmsTemplate templ = new JmsTemplate(activeMQConnectionFactory());
        templ.setReceiveTimeout(500);
        templ.setDefaultDestination(jmsQueue());
        return templ;
    }

    @Bean
    public BrokerService brokerService() throws Exception {
        final File dir =
                new File(env.getProperty("larch.messaging.path.data", System.getProperty("java.io.tmpdir")
                        + "/larch-jms-data"));
        FileSystemUtil.checkAndCreate(dir);
        final BrokerService broker = new BrokerService();
        broker.addConnector(brokerUri());
        broker.getPersistenceAdapter().setDirectory(dir);
        broker.start();
        return broker;
    }

    @Bean
    public Queue jmsQueue() {
        return new ActiveMQQueue("larch");
    }

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory() {
        return new ActiveMQConnectionFactory(brokerUri());
    }

    @Bean
    public String brokerUri() {
        return env.getProperty("larch.messaging.broker.uri", "vm://localhost");
    }

    @Bean
    public MessagingService messagingService() {
        return new DefaultMessagingService();
    }
}

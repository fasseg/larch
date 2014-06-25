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
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import net.objecthunter.larch.service.*;
import net.objecthunter.larch.service.elasticsearch.*;
import net.objecthunter.larch.service.fs.FilesystemBlobstoreService;
import net.objecthunter.larch.service.impl.*;
import net.objecthunter.larch.service.weedfs.WeedFSBlobstoreService;
import net.objecthunter.larch.service.weedfs.WeedFsMaster;
import net.objecthunter.larch.service.weedfs.WeedFsVolume;
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

import javax.jms.Queue;
import java.io.File;

/**
 * General JavaConfig class for the larch repository containing all the necessary beans for a larch
 * repository except the security context configuration
 */
@EnableAutoConfiguration
@ComponentScan(basePackages = "net.objecthunter.larch.controller")
@Configuration
public class LarchServerConfiguration {

    @Autowired
    public Environment env;

    /**
     * Get a {@link net.objecthunter.larch.service.AuditService} Spring bean
     *
     * @return the {@link net.objecthunter.larch.service.AuditService} implementation
     */
    @Bean
    public AuditService auditService() {
        return new ElasticSearchAuditService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.SchemaService} Spring bean
     *
     * @return the {@link net.objecthunter.larch.service.SchemaService} implementation
     */
    @Bean
    public SchemaService schemaService() {
        return new ElasticSearchSchemaService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.impl.DefaultEntityService} Spring bean
     *
     * @return the {@link net.objecthunter.larch.service.SchemaService} implementation
     */
    @Bean
    public EntityService defaultEntityService() {
        return new DefaultEntityService();
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

    /**
     * Get a {@link net.objecthunter.larch.service.SearchService} implementation Spring bean
     *
     * @return a {@link net.objecthunter.larch.service.elasticsearch.ElasticSearchSearchService} implementation
     */
    @Bean
    public IndexService elasticSearchIndexService() {
        return new ElasticSearchIndexService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.PublishService} implementation Spring bean
     *
     * @return a {@link net.objecthunter.larch.service.PublishService} implementation
     */
    @Bean
    public PublishService publishService() {
        return new ElasticSearchPublishService();
    }

    @Bean
    public MailService mailService() {
        return new DefaultMailService();
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
     * Get {@link net.objecthunter.larch.service.elasticsearch.ElasticSearchNode} Spring bean responsible for
     * starting and stopping the ElasticSearch services
     *
     * @return the {@link net.objecthunter.larch.service.elasticsearch.ElasticSearchNode} object
     */
    @Bean
    public ElasticSearchNode elasticSearchNode() {
        return new ElasticSearchNode();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.fs.FilesystemBlobstoreService} implementation for usage as a {@link net
     * .objecthunter.larch.service.BlobstoreService} in the repository
     *
     * @return the {@link net.objecthunter.larch.service.fs.FilesystemBlobstoreService} implementation
     */
    @Bean
    @Profile("fs")
    public FilesystemBlobstoreService filesystemBlobstoreService() {
        return new FilesystemBlobstoreService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.weedfs.WeedFsMaster} object responsible for starting and stopping the Weed
     * FS master node
     *
     * @return the {@link net.objecthunter.larch.service.weedfs.WeedFsMaster} object
     */
    @Bean
    @Profile("weedfs")
    @Order(40)
    public WeedFsMaster weedFsMaster() {
        return new WeedFsMaster();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.weedfs.WeedFsVolume} object responsible for starting and stopping a Weed
     * FS volume node
     *
     * @return the {@link net.objecthunter.larch.service.weedfs.WeedFsVolume} object
     */
    @Bean
    @Profile("weedfs")
    @Order(50)
    public WeedFsVolume weedfsVolume() {
        return new WeedFsVolume();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.weedfs.WeedFSBlobstoreService} implementation as the {@link net
     * .objecthunter.larch.service.BlobstoreService} fro the repository
     *
     * @return the {@link net.objecthunter.larch.service.weedfs.WeedFSBlobstoreService} object
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
     * Get the {@link com.fasterxml.jackson.databind.SerializationConfig} Spring bean for the Jackson {@link com.fasterxml.jackson.databind.ObjectMapper}
     *
     * @return the {@link com.fasterxml.jackson.databind.SerializationConfig} that should be used by the Jackson mapper
     */
    @Bean
    public SerializationConfig serializationConfig() {
        return objectMapper().getSerializationConfig();
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
     * Get a {@link net.objecthunter.larch.service.SearchService} Spring bean
     *
     * @return a {@link net.objecthunter.larch.service.SearchService} implementation
     */
    @Bean
    public SearchService searchService() {
        return new ElasticSearchSearchService();
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
     * Get a  {@link net.objecthunter.larch.service.VersionService} Spring bean
     *
     * @return a VersionService implementation
     */
    @Bean
    public VersionService versionService() {
        return new ElasticSearchVersionService();
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
     * Get a {@link net.objecthunter.larch.service.CredentialsService} implementation for use by the repository
     *
     * @return a {@link net.objecthunter.larch.service.CredentialsService} implementation
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
        final File dir = new File(env.getProperty("larch.messaging.path.data", System.getProperty("java.io.tmpdir") + "/larch-jms-data"));
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
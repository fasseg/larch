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

package net.objecthunter.larch.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Describe;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.state.LarchState;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Client implementation for the Larch server
 */
public class LarchClient {

    private static final Logger log = LoggerFactory.getLogger(LarchClient.class);

    private final URI larchUri;

    private final ObjectMapper mapper = new ObjectMapper();

    private final CloseableHttpClient httpClient;

    private final HttpClientContext httpContext;

    private final HttpHost targetHost;

    public LarchClient(URI larchUri, String username, String password) {
        this.httpClient = HttpClients.createDefault();
        this.larchUri = larchUri;
        this.httpContext = HttpClientContext.create();
        this.targetHost = new HttpHost(larchUri.getHost(), larchUri.getPort(), larchUri.getScheme());
        final CredentialsProvider httpCredsProvider = new BasicCredentialsProvider();
        httpCredsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(username, password)
                );
        final AuthCache httpAuthCache = new BasicAuthCache();
        final BasicScheme httpBasicScheme = new BasicScheme();
        httpAuthCache.put(targetHost, httpBasicScheme);
        this.httpContext.setCredentialsProvider(httpCredsProvider);
        this.httpContext.setAuthCache(httpAuthCache);
    }

    /**
     * Retrieve a {@link net.objecthunter.larch.model.state.LarchState} response from the repository containing
     * detailed state information
     * 
     * @return a LarchState POJO
     * @throws IOException
     */
    public LarchState retrieveState() throws IOException {
        final HttpGet get = new HttpGet(this.larchUri + "/state");
        final HttpResponse resp = this.httpClient.execute(targetHost, get, httpContext);
        if (resp.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Unable to retrieve LarchState response from the repository");
        }
        return mapper.readValue(resp.getEntity().getContent(), LarchState.class);
    }

    /**
     * Retrieve a {@link net.objecthunter.larch.model.Describe} response from the repository containing general state
     * information
     * 
     * @return a Describe POJO
     * @throws IOException
     */
    public Describe retrieveDescribe() throws IOException {
        final HttpGet get = new HttpGet(this.larchUri + "/describe");
        final HttpResponse resp = this.httpClient.execute(targetHost, get, httpContext);
        if (resp.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Unable to retrieve Describe response from the repository");
        }
        return mapper.readValue(resp.getEntity().getContent(), Describe.class);
    }

    /**
     * Retrieve a {@link net.objecthunter.larch.model.Metadata} of an Entity from the repository
     * 
     * @param entityId the entity's id
     * @param metadataName the meta data set's name
     * @return the Metadata as a POJO
     * @throws IOException if an error occurred while fetching the meta data
     */
    public Metadata retrieveMetadata(String entityId, String metadataName) throws IOException {
        final HttpGet get = new HttpGet(this.larchUri + "/entity/" + entityId + "/metadata/" + metadataName);
        final HttpResponse resp = this.httpClient.execute(targetHost, get, httpContext);
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to fetch meta data\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to fetch meta data " + metadataName + " from entity " + entityId);
        }
        return mapper.readValue(resp.getEntity().getContent(), Metadata.class);
    }

    /**
     * Retrieve a {@link net.objecthunter.larch.model.Metadata} of a Binary from the repository
     * 
     * @param entityId the entity's id
     * @param metadataName the meta data set's name
     * @return the Metadata as a POJO
     * @throws IOException if an error occurred while fetching the meta data
     */
    public Metadata retrieveBinaryMetadata(String entityId, String binaryName, String metadataName)
            throws IOException {
        final HttpGet get =
                new HttpGet(this.larchUri + "/entity/" + entityId + "/binary/" + binaryName + "/metadata/" +
                        metadataName);
        final HttpResponse resp = this.httpClient.execute(targetHost, get, httpContext);
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to fetch meta data\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to fetch meta data " + metadataName + " from binary " + binaryName +
                    " of entity " + entityId);
        }
        return mapper.readValue(resp.getEntity().getContent(), Metadata.class);
    }

    /**
     * Add {@link net.objecthunter.larch.model.Metadata} to an existing entity
     * 
     * @param entityId the entity's id
     * @param md the Metadata object
     * @throws IOException
     */
    public void postMetadata(String entityId, Metadata md) throws IOException {
        final HttpPost post = new HttpPost(this.larchUri + "/entity/" + entityId + "/metadata");
        post.setEntity(new StringEntity(mapper.writeValueAsString(md), ContentType.APPLICATION_JSON));
        final HttpResponse resp = this.httpClient.execute(targetHost, post, httpContext);
        if (resp.getStatusLine().getStatusCode() != 201) {
            log.error("Unable to add meta data\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to add meta data " + md.getName() + " from entity " + entityId);
        }
    }

    /**
     * Add {@link net.objecthunter.larch.model.Metadata} to an existing binary
     * 
     * @param entityId the entity's id
     * @param md the Metadata object
     * @throws IOException
     */
    public void postBinaryMetadata(String entityId, String binaryName, Metadata md) throws IOException {
        final HttpPost post = new HttpPost(this.larchUri + "/entity/" + entityId + "/binary/" + binaryName + "/metadata");
        post.setEntity(new StringEntity(mapper.writeValueAsString(md), ContentType.APPLICATION_JSON));
        final HttpResponse resp = this.httpClient.execute(targetHost, post, httpContext);
        if (resp.getStatusLine().getStatusCode() != 201) {
            log.error("Unable to add meta data to binary\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to add meta data " + md.getName() + " to binary " + binaryName +
                    " of entity " + entityId);
        }
    }

    /**
     * Delete the {@link net.objecthunter.larch.model.Metadata} of a {@link net.objecthunter.larch.model.Entity}
     * 
     * @param entityId the entity's id
     * @param metadataName the meta data set's name
     * @throws IOException
     */
    public void deleteMetadata(String entityId, String metadataName) throws IOException {
        final HttpDelete delete = new HttpDelete(this.larchUri + "/entity/" + entityId + "/metadata/" + metadataName);
        final HttpResponse resp = this.httpClient.execute(targetHost, delete, httpContext);
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to remove meta data from entity\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to remove meta data " + metadataName + "  of entity " + entityId);
        }

    }

    /**
     * Delete the {@link net.objecthunter.larch.model.Metadata} of a {@link net.objecthunter.larch.model.Binary}
     * 
     * @param entityId the entity's id
     * @param binaryName the binary's name
     * @param metadataName the meta data set's name
     * @throws IOException
     */
    public void deleteBinaryMetadata(String entityId, String binaryName, String metadataName) throws IOException {
        final HttpDelete delete = new HttpDelete(this.larchUri + "/entity/" + entityId + "/binary/" + binaryName + "/metadata/" +
                metadataName);
        final HttpResponse resp = this.httpClient.execute(targetHost, delete, httpContext);
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to remove meta data from binary\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to remove meta data " + metadataName + " from binary " + binaryName +
                    " of entity " + entityId);
        }

    }

    /**
     * Retrieve {@link net.objecthunter.larch.model.Binary} from the repository
     * 
     * @param entityId the entity's id
     * @param binaryName the binary's name
     * @return the Binary as a POJO
     * @throws IOException if an error occurred while fetching from the repository
     */
    public Binary retrieveBinary(String entityId, String binaryName) throws IOException {
        final HttpGet get = new HttpGet(this.larchUri + "/entity/" + entityId + "/binary/" + binaryName);
        final HttpResponse resp = this.httpClient.execute(targetHost, get, httpContext);
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to fetch binary\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to fetch binary" + binaryName + " from entity " + entityId);
        }
        return mapper.readValue(resp.getEntity().getContent(), Binary.class);
    }

    /**
     * Add a {@link net.objecthunter.larch.model.Binary} to an existing entity
     * 
     * @param entityId the entity's id
     * @param bin the binary object to add
     * @throws IOException
     */
    public void postBinary(String entityId, Binary bin) throws IOException {
        final HttpPost post = new HttpPost(this.larchUri +  "/entity/" + entityId + "/binary");
        post.setEntity(new StringEntity(mapper.writeValueAsString(bin), ContentType.APPLICATION_JSON));
        final HttpResponse resp = this.httpClient.execute(targetHost, post, httpContext);
        if (resp.getStatusLine().getStatusCode() != 201) {
            log.error("Unable to add binary. Server says:\n", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to add binary " + bin.getName() + " to entity " + entityId);
        }
    }

    public void postBinary(String entityId, String name, String mimeType, InputStream src) throws IOException {
        final HttpPost post = new HttpPost(this.larchUri +  "/entity/" + entityId + "/binary?name=\" + name\n" +
                "                + \"&mimetype=\" + mimeType");
        post.setEntity(new InputStreamEntity(src));
        final HttpResponse resp = this.httpClient.execute(targetHost, post, httpContext);
        if (resp.getStatusLine().getStatusCode() != 201) {
            log.error("Unable to add binary. Server says:\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to add binary " + name + " to entity " + entityId);
        }
    }

    /**
     * Delete a {@link net.objecthunter.larch.model.Binary} in the repository
     * 
     * @param entityId the entity's id
     * @param binaryName the binary's name
     * @throws IOException
     */
    public void deleteBinary(String entityId, String binaryName) throws IOException {
        final HttpDelete delete = new HttpDelete(this.larchUri + "/entity/" + entityId + "/binary/" + binaryName);
        final HttpResponse resp = this.httpClient.execute(targetHost, delete, httpContext);
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to delete binary. Server says:\n", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to delete binary " + binaryName + " of entity " + entityId);
        }
    }

    /**
     * Fetch the actual binary content from the repository
     * 
     * @param entityId the Id of the entity
     * @param binaryName the name of the binary to fetch
     * @return an InputStream containing the binary's data
     * @throws IOException
     */
    public InputStream retrieveBinaryContent(String entityId, String binaryName) throws IOException {
        final HttpGet get = new HttpGet(this.larchUri + "/entity/" + entityId + "/binary/" + binaryName + "/content");
        final HttpResponse resp = this.httpClient.execute(targetHost, get, httpContext);
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to fetch binary data\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to fetch binary data " + binaryName + " from entity " + entityId);
        }
        return resp.getEntity().getContent();
    }

    /**
     * Update an {@link net.objecthunter.larch.model.Entity} in the larch repository
     * 
     * @param e the updated entity object to be written to the repository
     * @throws IOException if an error occurred during update
     */
    public void updateEntity(Entity e) throws IOException {
        if (e.getId() == null || e.getId().isEmpty()) {
            throw new IOException("ID of the entity can not be empty when updating");
        }
        final HttpPut put = new HttpPut(this.larchUri +  "/entity/" + e.getId());
        put.setEntity(new StringEntity(mapper.writeValueAsString(e), ContentType.APPLICATION_JSON));
        final HttpResponse resp = this.httpClient.execute(targetHost, put, httpContext);
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to update entity\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to update entity " + e.getId());
        }
    }

    /**
     * Delete an entity in the larch repository
     * 
     * @param id the id of the entity to delete
     * @throws IOException
     */
    public void deleteEntity(String id) throws IOException {
        final HttpDelete delete = new HttpDelete(this.larchUri + "/entity/" + id);
        final HttpResponse resp = this.httpClient.execute(targetHost, delete, httpContext);
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to delete Entity\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to delete entity " + id);
        }
    }

    /**
     * Post an {@link net.objecthunter.larch.model.Entity} to the Larch server
     * 
     * @param e The entity to ingest
     * @return the entity's id
     * @throws IOException if an error occurred while ingesting
     */
    public String postEntity(Entity e) throws IOException {
        final HttpPost post = new HttpPost(this.larchUri +  "/entity/");
        post.setEntity(new StringEntity(mapper.writeValueAsString(e), ContentType.APPLICATION_JSON));
        final HttpResponse resp = this.httpClient.execute(targetHost, post, httpContext);
        if (resp.getStatusLine().getStatusCode() != 201) {
            log.error("Unable to post entity to Larch at {}\n{}", larchUri, EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to create Entity " + e.getId());
        }
        return EntityUtils.toString(resp.getEntity());
    }

    /**
     * Retrieve an entity using a HTTP GET from the Larch server
     * 
     * @param id the Id of the entity
     * @return An entity object
     */
    public Entity retrieveEntity(String id) throws IOException {
        final HttpGet get = new HttpGet(this.larchUri + "/entity" + id);
        final HttpResponse resp = this.httpClient.execute(targetHost, get, httpContext);
        return mapper.readValue(resp.getEntity().getContent(), Entity.class);
    }
}

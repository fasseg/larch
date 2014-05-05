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

import com.fasterxml.jackson.databind.ObjectMapper;
import net.objecthunter.larch.model.Entity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Client implementation for the Larch server
 */
public class LarchClient {
    private static final Logger log = LoggerFactory.getLogger(LarchClient.class);

    private ObjectMapper mapper = new ObjectMapper();

    private String larchUri = "http://localhost:8080";

    private HttpHost localhost = new HttpHost("localhost", 8080, "http");

    private Executor executor = Executor.newInstance()
            .auth(localhost, "admin", "admin")
            .authPreemptive(localhost);

    /**
     * Fetch the actual binary content from the repository
     * @param entityId the Id of the entity
     * @param binaryName the name of the binary to fetch
     * @return an InputStream containing the binary's data
     * @throws IOException
     */
    public InputStream retrieveBinaryContent(String entityId, String binaryName) throws  IOException {
        final HttpResponse resp = this.execute(Request.Get(larchUri + "/entity/" + entityId + "/binary/" + binaryName))
                .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to fetch binary data\n{}",EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to fetch binary data " + binaryName  + " from entity " + entityId);
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
        final HttpResponse resp = this.execute(Request.Put(larchUri + "/entity/" + e.getId())
                .bodyString(mapper.writeValueAsString(e), ContentType.APPLICATION_JSON))
                .returnResponse();
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
        final HttpResponse resp = this.execute(Request.Delete(larchUri + "/entity/" + id))
                .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to delete Entity\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to delete entity " + id);
        }
    }

    /**
     * Post an {@link net.objecthunter.larch.model.Entity} to the Larch server
     *
     * @param e The entity to ingest
     * @return The identifier of the created entity
     * @throws IOException if an error occurred while ingesting
     */
    public void postEntity(Entity e) throws IOException {
        final HttpResponse resp = this.execute(Request.Post(larchUri + "/entity")
                .bodyString(mapper.writeValueAsString(e), ContentType.APPLICATION_JSON))
                .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 201) {
            log.error("Unable to post entity to Larch\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to create Entity " + e.getId());
        }
    }

    /**
     * Retrieve an entity using a HTTP GET from the Larch server
     *
     * @param id the Id of the entity
     * @return An entity object
     */
    public Entity retrieveEntity(String id) throws IOException {
        final HttpResponse resp = this.execute(Request.Get(larchUri + "/entity/" + id)
                .addHeader("Accept", "application/json"))
                .returnResponse();
        return mapper.readValue(resp.getEntity().getContent(), Entity.class);
    }

    /**
     * Execute a HTTP request
     *
     * @param req the Request object to use for the execution
     * @return The HttpResponse containing the requested information
     * @throws IOException
     */
    protected Response execute(Request req) throws IOException {
        return this.executor.execute(req);
    }
}

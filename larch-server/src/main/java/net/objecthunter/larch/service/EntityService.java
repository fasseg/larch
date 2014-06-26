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
package net.objecthunter.larch.service;

import java.io.IOException;
import java.io.InputStream;

import net.objecthunter.larch.model.Entity;

import com.fasterxml.jackson.databind.JsonNode;

import net.objecthunter.larch.model.Entity;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Service definition for CRUD operations on {@link net.objecthunter.larch.model.Entity} objects
 */
public interface EntityService {
    String create(Entity e) throws IOException;

    void update(Entity e) throws IOException;

    Entity retrieve(String id) throws IOException;

    void delete(String id) throws IOException;

    InputStream getContent(String id, String name) throws IOException;

    Entity retrieve(String id, int i) throws IOException;

    void createBinary(String entityId, String name, String contentType, InputStream inputStream) throws IOException;

    void patch(String id, JsonNode node) throws IOException;

    void createRelation(String id, String predicate, String object) throws IOException;

    void deleteBinary(String entityId, String name) throws IOException;

    void deleteMetadata(String entityId, String mdName) throws IOException;

    void deleteBinaryMetadata(String entityId, String binaryName, String mdName) throws IOException;

    void createIdentifier(String entityId, String type, String value) throws IOException;

    void deleteIdentifier(String entityId, String type, String value) throws IOException;

    void publish(String id) throws IOException;
}

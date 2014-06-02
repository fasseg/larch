package net.objecthunter.larch.service;

import net.objecthunter.larch.model.Entity;

import java.io.IOException;

/**
 * Service definition for entity version storage
 */
public interface VersionService {
    /**
     * Add an old version of an entity to the old version storage and return the path
     * @param e the old version to store
     * @return the path to where the old version can be found
     */
    void addOldVersion(Entity e) throws IOException;

    /**
     * Retrieve an old version of an entity from the version storage
     * @param id the id of the entity to retrieve
     * @param versionNumber the number of the version
     * @return the requested old version of an entity
     */
    Entity getOldVersion(String id, int versionNumber) throws IOException;

}

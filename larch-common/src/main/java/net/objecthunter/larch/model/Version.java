
package net.objecthunter.larch.model;

/**
 * Model class to hold version information
 */
public class Version {

    private String entityId;

    private int versionNumber;

    private String path;

    public Version() {
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getPath() {
        return path;
    }
}

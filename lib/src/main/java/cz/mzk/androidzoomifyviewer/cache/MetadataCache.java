package cz.mzk.androidzoomifyviewer.cache;

/**
 * @author Martin Řehánek
 */

public interface MetadataCache {

    public String getMetadata(String metadataUrl);

    public void storeMetadata(String metadata, String metadataUrl);

}

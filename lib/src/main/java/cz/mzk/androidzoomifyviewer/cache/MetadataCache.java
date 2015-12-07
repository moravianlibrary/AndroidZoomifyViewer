package cz.mzk.androidzoomifyviewer.cache;

/**
 * @author Martin Řehánek
 */

public interface MetadataCache {

    public String getXml(String metadataUrl);

    public void storeXml(String metadata, String metadataUrl);

}

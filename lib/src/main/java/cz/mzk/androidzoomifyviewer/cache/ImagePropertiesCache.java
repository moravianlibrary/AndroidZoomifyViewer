package cz.mzk.androidzoomifyviewer.cache;

/**
 * @author Martin Řehánek
 */

// TODO: 7.12.15 Asi posilat rovnou "metadata url"
public interface ImagePropertiesCache {

    public String getXml(String zoomifyBaseUrl);

    public void storeXml(String xml, String zoomifyBaseUrl);

}

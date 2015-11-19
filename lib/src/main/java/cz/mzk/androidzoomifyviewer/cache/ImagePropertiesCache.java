package cz.mzk.androidzoomifyviewer.cache;

/**
 * @author Martin Řehánek
 */
public interface ImagePropertiesCache {

    public String getXml(String zoomifyBaseUrl);

    public void storeXml(String xml, String zoomifyBaseUrl);

}

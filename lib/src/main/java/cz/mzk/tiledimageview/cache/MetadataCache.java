package cz.mzk.tiledimageview.cache;

import android.support.annotation.WorkerThread;

/**
 * @author Martin Řehánek
 */

public interface MetadataCache {

    //MEMORY

    @WorkerThread
    public String getMetadataFromMemory(String key);

    // TODO: 10.12.15 poradi parametru
    @WorkerThread
    public void storeMetadataToMemory(String metadata, String key);


    //DISK

    @WorkerThread
    public boolean isDiskCacheEnabled();

    @WorkerThread
    public boolean isMetadataOnDisk(String key);

    @WorkerThread
    public String getMetadataFromDisk(String key);

    @WorkerThread
    public void storeMetadataIntoDisk(String key, String metadata);


}

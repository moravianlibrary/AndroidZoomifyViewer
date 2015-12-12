package cz.mzk.tiledimageview.images.cache;

import android.content.Context;

import cz.mzk.tiledimageview.Logger;

/**
 * Created by Martin Řehánek on 12.12.15.
 */
public class MetadataCache extends AbstractCache<String> {

    public static final String DISK_CACHE_SUBDIR = "imageProperties"; //legacy name for subdir. No need to change it
    public static final int DISK_CACHE_SIZE_BYTES = 1024 * 1024 * 10; // 10MB
    public static final int MEMORY_CACHE_SIZE_ITEMS = 100;


    public MetadataCache(Context context, boolean diskCacheEnabled, boolean clearDiskCache) {
        super(context, new Logger(MetadataCache.class), MEMORY_CACHE_SIZE_ITEMS, diskCacheEnabled, DISK_CACHE_SUBDIR, DISK_CACHE_SIZE_BYTES, clearDiskCache);
    }

    @Override
    String getItem(DiskLruCache diskCache, String key) throws DiskLruCache.DiskLruCacheException {
        DiskLruCache.Snapshot snapshot = diskCache.get(key);
        if (snapshot != null) {
            String result = snapshot.getString(0);
            if (result == null) {
                mLogger.w("item from disk cache was null, removing record");
                diskCache.remove(key);
                return null;
            } else {
                return result;
            }
        } else {
            return null;
        }
    }

    @Override
    void storeItem(DiskLruCache diskCache, String key, String matadata) throws DiskLruCache.DiskLruCacheException {
        diskCache.storeString(0, key, matadata);
    }
}

package cz.mzk.tiledimageview.images.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

import cz.mzk.tiledimageview.Logger;

/**
 * Created by Martin Řehánek on 11.12.15.
 */
public class TileCache extends AbstractCache<Bitmap> {

    private static final String DISK_CACHE_SUBDIR = "tiles";

    public TileCache(Context context, int memoryCacheSizeItems, boolean diskCacheEnabled, long diskCacheSizeBytes, boolean clearDiskCache) {
        super(context, new Logger(TileCache.class), memoryCacheSizeItems, diskCacheEnabled, DISK_CACHE_SUBDIR, diskCacheSizeBytes, clearDiskCache);
    }

    @Override
    Bitmap getItem(DiskLruCache diskCache, String key) throws DiskLruCache.DiskLruCacheException {
        DiskLruCache.Snapshot snapshot = diskCache.get(key);
        if (snapshot != null) {
            InputStream in = snapshot.getInputStream(0);
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            if (bitmap == null) {
                mLogger.w("item from disk cache was null, removing record");
                diskCache.remove(key);
            }
            return bitmap;
        } else {
            return null;
        }
    }

    @Override
    void storeItem(DiskLruCache diskCache, String key, Bitmap item) throws DiskLruCache.DiskLruCacheException {
        diskCache.storeBitmap(0, key, item);
    }

}

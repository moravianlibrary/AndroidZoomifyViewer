package cz.mzk.tiledimageview.cache;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;

import cz.mzk.tiledimageview.Logger;

/**
 * @author Martin Řehánek
 */
public abstract class AbstractTileCache {

    private static final Logger LOGGER = new Logger(AbstractTileCache.class);

    protected String buildKey(String tileUrl) {
        String key = CacheKeyBuilder.buildKeyFromUrl(tileUrl);
        if (key.length() > 127) {
            LOGGER.w("cache key is longer then 127 characters");
        }
        return key;
    }

    @SuppressLint("NewApi")
    protected int getBitmapSizeInKB(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount() / 1024;
        } else {
            return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
        }
    }

}
package cz.mzk.androidzoomifyviewer.cache;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;

/**
 * @author Martin Řehánek
 */
public abstract class AbstractTileCache {

    protected String buildKey(String tileUrl) {
        return CacheUtils.escapeSpecialChars(tileUrl);
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
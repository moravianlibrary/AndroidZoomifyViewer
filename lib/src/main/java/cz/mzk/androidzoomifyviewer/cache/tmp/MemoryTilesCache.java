package cz.mzk.androidzoomifyviewer.cache.tmp;

import android.graphics.Bitmap;
import android.util.LruCache;
import android.util.Log;

import cz.mzk.androidzoomifyviewer.tiles.TileId;

/**
 * This LRU cache stores tiles only in memory as bitmaps. Raw bitmaps occupy significant amount of memory (megabytes), so tiles
 * are typically often removed from cache and hit ratio is low. Size of available memory is 1/8 of total max memory dedicated to
 * app.
 *
 * @author Martin Řehánek
 */
public class MemoryTilesCache extends AbstractTileCache implements TilesCache {

    private static final String TAG = MemoryTilesCache.class.getSimpleName();

    private LruCache<String, Bitmap> mMemoryCache;
    private State state = State.INITIALIZING;

    public MemoryTilesCache() {
        this(getDefaultMemoryCacheSizeKB());
    }

    public MemoryTilesCache(int cacheSizeKB) {
        if (cacheSizeKB == 0) {
            state = State.DISABLED;
        } else {
            mMemoryCache = new LruCache<String, Bitmap>(cacheSizeKB) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than number of items.
                    return getBitmapSizeInKB(bitmap);
                }
            };
            Log.d(TAG, "Lru cache allocated with " + cacheSizeKB + " kB");
            state = State.READY;
        }
    }

    @Override
    public Bitmap getTile(String zoomifyBaseUrl, TileId tileId) {
        return mMemoryCache.get(buildKey(zoomifyBaseUrl, tileId));
    }

    @Override
    public void storeTile(Bitmap bmp, String zoomifyBaseUrl, TileId tileId) {
        String key = buildKey(zoomifyBaseUrl, tileId);
        synchronized (mMemoryCache) {
            if (mMemoryCache.get(key) == null) {
                Log.d(TAG, "storing " + key);
                mMemoryCache.put(key, bmp);
                logStatistics();
            }
        }
    }

    private void logStatistics() {
        int hitCount = mMemoryCache.hitCount();
        int missCount = mMemoryCache.missCount();
        float hitRatio = (float) hitCount / (float) (hitCount + missCount) * 100f;
        Log.d(TAG, String.format("hit ratio: %,.2f %%", hitRatio));
    }

    @Override
    public State getState() {
        return state;
    }

}

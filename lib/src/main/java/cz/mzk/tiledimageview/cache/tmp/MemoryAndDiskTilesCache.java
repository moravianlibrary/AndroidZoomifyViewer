package cz.mzk.tiledimageview.cache.tmp;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;

import java.io.File;
import java.io.InputStream;

import cz.mzk.tiledimageview.Logger;
import cz.mzk.tiledimageview.cache.DiskLruCache;
import cz.mzk.tiledimageview.cache.DiskLruCache.DiskLruCacheException;
import cz.mzk.tiledimageview.cache.DiskLruCache.Snapshot;
import cz.mzk.tiledimageview.cache.InitDiskCacheTask;
import cz.mzk.tiledimageview.tiles.TilePositionInPyramid;

/**
 * @author Martin Řehánek
 */
public class MemoryAndDiskTilesCache extends AbstractTileCache implements TilesCache {

    private static final Logger LOGGER = new Logger(MemoryAndDiskTilesCache.class);

    private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DEFAULT_DISK_CACHE_SUBDIR = "tiles";
    private final Object mDiskCacheInitializationLock = new Object();
    private final LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mDiskCache = null;
    private boolean mDiskCacheDisabled = false;

    private State state = State.INITIALIZING;

    public MemoryAndDiskTilesCache(Context context, int cacheSizeKB, int diskCacheSize, String diskCacheDir,
                                   boolean clearCache) {
        // if (cacheSizeKB != 0) {
        // mMemoryCache = new LruCache<String, Bitmap>(cacheSizeKB) {
        // @Override
        // protected int sizeOf(String key, Bitmap bitmap) {
        // // The cache size will be measured in kilobytes rather than number of items.
        // return getBitmapSizeInKB(bitmap);
        // }
        // };
        // Log.d(TAG, "Lru cache allocated with " + cacheSizeKB + " kB");
        // }
        // TODO
        int maxItems = 50;
        mMemoryCache = new LruCache<String, Bitmap>(maxItems);
        LOGGER.d("Lru cache allocated, max items: " + maxItems);
        // TODO: no need to do it asynchronously
        initDiskCacheAsync(context, diskCacheSize, diskCacheDir, clearCache);
    }

    public MemoryAndDiskTilesCache(Context context, boolean clearCache) {
        this(context, getDefaultMemoryCacheSizeKB(), DEFAULT_DISK_CACHE_SIZE, DEFAULT_DISK_CACHE_SUBDIR, clearCache);
    }

    private void initDiskCacheAsync(Context context, int diskCacheSize, String diskCacheDir, boolean clearCache) {
        try {
            File cacheDir = getDiskCacheDir(context, diskCacheDir);
            int appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            new InitDiskCacheTask(appVersion, diskCacheSize, clearCache, new InitDiskCacheTask.Listener() {

                @Override
                public void onFinished(DiskLruCache cache) {
                    mDiskCache = cache;
                    state = State.READY;
                }

                @Override
                public void onError() {
                    LOGGER.i("disabling disk cache");
                    mDiskCacheDisabled = true;
                    state = mMemoryCache != null ? State.READY : State.DISABLED;
                }
            }).execute(cacheDir);
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void storeTile(Bitmap bmp, String zoomifyBaseUrl, TilePositionInPyramid tilePositionInPyramid) {
        String key = buildKey(zoomifyBaseUrl, tilePositionInPyramid);
        storeTileToMemoryCache(key, bmp);
        storeTileToDiskCache(key, bmp);
    }

    private void storeTileToMemoryCache(String key, Bitmap bmp) {
        synchronized (mMemoryCache) {
            // Log.v(TAG, "assuming mMemoryCache lock: " + Thread.currentThread().toString());
            if (mMemoryCache.get(key) == null) {
                // Log.d(TAG, "storing to memory cache: " + key);
                mMemoryCache.put(key, bmp);
            } else {
                // Log.d(TAG, "already in memory cache: " + key);
            }
            // Log.v(TAG, "releasing mMemoryCache lock: " + Thread.currentThread().toString());
        }
    }

    // TODO: fix if needed
    private void storeTileToDiskCache(String key, Bitmap bmp) {
        // waitUntilDiskCacheInitializedOrDisabled();
        // Editor edit = null;
        // OutputStream out = null;
        // try {
        // if (mDiskCache != null && !mDiskCacheDisabled) {
        // Snapshot fromDiskCache = mDiskCache.get(key);
        // if (fromDiskCache != null) {
        // // Log.d(TAG, "already in disk cache: " + key);
        // } else {
        // // Log.d(TAG, "storing to disk cache: " + key);
        // edit = mDiskCache.edit(key);
        // if (edit != null) {
        // edit.hashCode();
        // out = edit.newOutputStream(0);
        // byte[] bytes = bitmapToByteArray(bmp);
        // out.write(bytes);
        // // int kB = bytes.length / 1024;
        // // Log.d(TestTags.CACHE, "bmp size: " + kB + " kB");
        // edit.commit();
        // } else {
        // // another thread trying to write, i.e. incorrectly implemented synchronization
        // Log.e(TAG, key + ": edit allready opened");
        // }
        // }
        // }
        // } catch (IOException e) {
        // Log.e(TAG, "failed to store tile to disk cache: " + e.getMessage());
        // try {
        // if (edit != null) {
        // edit.abort();
        // }
        // if (out != null) {
        // out.close();
        // }
        // } catch (IOException e1) {
        // Log.e(TAG, "failed to cleanup", e1);
        // }
        // }
    }

    @Override
    public Bitmap getTile(String zoomifyBaseUrl, TilePositionInPyramid tilePositionInPyramid) {
        String key = buildKey(zoomifyBaseUrl, tilePositionInPyramid);
        // long start = System.currentTimeMillis();
        Bitmap inMemoryCache = mMemoryCache.get(key);
        // long afterHitOrMiss = System.currentTimeMillis();
        if (inMemoryCache != null) {
            // Log.d(TAG, "memory cache hit: " + key);
            // Log.d(TAG, "memory cache hit, delay: " + (afterHitOrMiss - start) + " ms");
            return inMemoryCache;
        } else {
            // Log.d(TAG, "memory cache miss: " + key);
            // Log.d(TAG, "memory cache miss, delay: " + (afterHitOrMiss - start) + " ms");
            Bitmap fromDiskCache = getTileFromDiskCache(key);
            // store also to memory cache (nonblocking)
            if (fromDiskCache != null) {
                new StoreTileToMemoryCacheTask(key).execute(fromDiskCache);
            }
            return fromDiskCache;
        }
    }

    private void waitUntilDiskCacheInitializedOrDisabled() {
        try {
            synchronized (mDiskCacheInitializationLock) {
                // Log.v(TAG, "assuming disk cache initialization lock: " + Thread.currentThread().toString());
                // Wait until disk cache is initialized or disabled
                while (mDiskCache == null && !mDiskCacheDisabled) {
                    try {
                        mDiskCacheInitializationLock.wait();
                    } catch (InterruptedException e) {
                        LOGGER.e("waiting for disk cache lock interrupted", e);
                    }
                }
            }
        } finally {
            LOGGER.v("releasing disk cache initialization lock: " + Thread.currentThread().toString());
        }
    }

    private Bitmap getTileFromDiskCache(String key) {
        waitUntilDiskCacheInitializedOrDisabled();
        try {
            if (mDiskCache != null && !mDiskCacheDisabled) {
                // long start = System.currentTimeMillis();
                Snapshot snapshot = mDiskCache.get(key);
                if (snapshot != null) {
                    // long afterHit = System.currentTimeMillis();
                    InputStream in = snapshot.getInputStream(0);
                    Bitmap stream = BitmapFactory.decodeStream(in);
                    // long afterDecoding = System.currentTimeMillis();
                    // long retrieval = afterHit - start;
                    // long decoding = afterDecoding - afterHit;
                    // long total = retrieval + decoding;
                    // Log.d(TAG, "disk cache hit: " + key + ", delay: " + total + "ms (retrieval: " + retrieval +
                    // "ms, decoding: " + decoding + " ms)");
                    return stream;
                } else {
                    // long afterMiss = System.currentTimeMillis();
                    // Log.d(TAG, "disk cache miss: " + key + ", delay: " + (afterMiss - start) + " ms");
                    return null;
                }
            } else {
                return null;
            }
        } catch (DiskLruCacheException e) {
            LOGGER.e("error loading tile from disk cache: " + key, e);
            return null;
        }
    }

    @Override
    public State getState() {
        return state;
    }

    private class StoreTileToMemoryCacheTask extends AsyncTask<Bitmap, Void, Void> {
        private final String key;

        public StoreTileToMemoryCacheTask(String key) {
            this.key = key;
        }

        @Override
        protected Void doInBackground(Bitmap... params) {
            storeTileToMemoryCache(key, params[0]);
            return null;
        }
    }

}

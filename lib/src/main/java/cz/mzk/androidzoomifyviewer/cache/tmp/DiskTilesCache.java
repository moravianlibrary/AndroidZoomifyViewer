package cz.mzk.androidzoomifyviewer.cache.tmp;

import java.io.File;
import java.io.InputStream;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.cache.DiskLruCache;
import cz.mzk.androidzoomifyviewer.cache.DiskLruCache.DiskLruCacheException;
import cz.mzk.androidzoomifyviewer.cache.DiskLruCache.Snapshot;
import cz.mzk.androidzoomifyviewer.cache.InitDiskCacheTask;
import cz.mzk.androidzoomifyviewer.cache.InitDiskCacheTask.Listener;
import cz.mzk.androidzoomifyviewer.tiles.TileId;

public class DiskTilesCache extends AbstractTileCache implements TilesCache {

    private static final Logger logger = new Logger(DiskTilesCache.class);

    private DiskLruCache mCache = null;
    private State state = State.INITIALIZING;

    public DiskTilesCache(Context context, int diskCacheSize, String diskCacheDir, boolean clearCache) {
        initDiskCacheAsync(context, diskCacheSize, diskCacheDir, clearCache);
    }

    private void initDiskCacheAsync(Context context, int diskCacheSize, String diskCacheDir, boolean clearCache) {
        try {
            File cacheDir = getDiskCacheDir(context, diskCacheDir);
            int appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            new InitDiskCacheTask(appVersion, diskCacheSize, clearCache, new Listener() {

                @Override
                public void onFinished(DiskLruCache cache) {
                    mCache = cache;
                    state = State.READY;
                }

                @Override
                public void onError() {
                    logger.i("disabling cache");
                    state = State.DISABLED;
                }
            }).execute(cacheDir);
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // private void checkState() {
    // if (state == State.INITIALIZING) {
    // throw new IllegalStateException("not initialized yet");
    // }
    // if (state == State.DISABLED) {
    // throw new IllegalStateException("disabled");
    // }
    // }

    @Override
    public Bitmap getTile(String zoomifyBaseUrl, TileId tileId) {
        // checkState();
        String key = buildKey(zoomifyBaseUrl, tileId);
        return getTileFromDiskCache(key);
    }

    private Bitmap getTileFromDiskCache(String key) {
        // waitUntilDiskCacheInitializedOrDisabled();
        try {
            // long start = System.currentTimeMillis();
            Snapshot snapshot = mCache.get(key);
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
        } catch (DiskLruCacheException e) {
            logger.i("error loading tile from disk cache: " + key, e);
            return null;
        }
    }

    @Override
    public void storeTile(Bitmap tile, String zoomifyBaseUrl, TileId tileId) {
        // checkState();
        String key = buildKey(zoomifyBaseUrl, tileId);
        storeTileToDiskCache(key, tile);
    }

    // TODO: fix if needed
    private void storeTileToDiskCache(String key, Bitmap bmp) {
        // // waitUntilDiskCacheInitializedOrDisabled();
        // Editor edit = null;
        // OutputStream out = null;
        // try {
        // Snapshot fromDiskCache = mCache.get(key);
        // if (fromDiskCache != null) {
        // Log.d(TAG, "already in disk cache: " + key);
        // } else {
        // Log.d(TAG, "storing to disk cache: " + key);
        // edit = mCache.edit(key);
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
    public State getState() {
        return state;
    }

}

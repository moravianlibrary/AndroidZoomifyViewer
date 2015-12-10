package cz.mzk.tiledimageview.cache;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.annotation.WorkerThread;
import android.util.LruCache;

import java.io.File;
import java.io.IOException;

import cz.mzk.tiledimageview.Logger;
import cz.mzk.tiledimageview.Utils;
import cz.mzk.tiledimageview.cache.DiskLruCache.DiskLruCacheException;
import cz.mzk.tiledimageview.cache.DiskLruCache.Snapshot;

/**
 * @author Martin Řehánek
 */
// TODO: 9.12.15 Prejmenovat mozna
public class MemoryAndDiskMetadataCache implements MetadataCache {

    public static final String DISK_CACHE_SUBDIR = "imageProperties"; //legacy name for subdir. No need to change it
    public static final int DISK_CACHE_SIZE_B = 1024 * 1024 * 10; // 10MB
    public static final int MEMORY_CACHE_SIZE_ITEMS = 100;

    private static final Logger LOGGER = new Logger(MemoryAndDiskMetadataCache.class);

    private final Object mMemoryCacheLock = new Object();
    private final LruCache<String, String> mMemoryCache;
    private DiskLruCache mDiskCache = null;

    @WorkerThread
    public MemoryAndDiskMetadataCache(Context context, boolean diskCacheEnabled, boolean clearCache) {
        mMemoryCache = initMemoryCache();
        if (diskCacheEnabled) {
            mDiskCache = initDiskCache(context, clearCache);
        } else {
            mDiskCache = null;
        }
    }


    private LruCache<String, String> initMemoryCache() {
        LruCache<String, String> result;
        synchronized (mMemoryCacheLock) {
            LOGGER.v("assumed memory-cache lock (initializationd): " + Thread.currentThread().toString());
            result = new LruCache<String, String>(MEMORY_CACHE_SIZE_ITEMS);
            LOGGER.d("in-memory lru cache allocated for " + MEMORY_CACHE_SIZE_ITEMS + " items");
        }
        LOGGER.v("released memory-cache lock (initializationd): " + Thread.currentThread().toString());
        return result;
    }

    @Override
    public String getMetadataFromMemory(String key) {
        String result;
        synchronized (mMemoryCacheLock) {
            LOGGER.v("assumed memory-cache lock (get): " + Thread.currentThread().toString());
            result = mMemoryCache.get(key);
        }
        LOGGER.v("released memory-cache lock (get): " + Thread.currentThread().toString());
        return result;
    }

    @Override
    public void storeMetadataToMemory(String metadata, String key) {
        synchronized (mMemoryCacheLock) {
            LOGGER.v("assumed memory-cache lock (store): " + Thread.currentThread().toString());
            if (mMemoryCache.get(key) == null) {
                LOGGER.d("storing to memory cache: " + key);
                mMemoryCache.put(key, metadata);
            } else {
                LOGGER.d("already in memory cache: " + key);
            }
        }
        LOGGER.v("released memory-cache lock (store): " + Thread.currentThread().toString());
    }


    private DiskLruCache initDiskCache(Context context, boolean clearCache) {
        try {
            File cacheDir = getDiskCacheDir(context);
            int appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            if (cacheDir.exists()) {
                if (clearCache) {
                    LOGGER.i("clearing metadata disk cache");
                    boolean cleared = DiskUtils.deleteDirContent(cacheDir);
                    if (!cleared) {
                        LOGGER.w("failed to delete content of " + cacheDir.getAbsolutePath() + ", disabling disk cache");
                        return null;
                    }
                }
            } else {
                LOGGER.i("creating cache dir " + cacheDir);
                boolean created = cacheDir.mkdir();
                if (!created) {
                    LOGGER.w("failed to create cache dir " + cacheDir.getAbsolutePath() + ", disabling disk cache");
                    return null;
                }
            }
            LOGGER.d("disk cache dir: " + cacheDir.getAbsolutePath());
            DiskLruCache result = DiskLruCache.open(cacheDir, appVersion, 1, DISK_CACHE_SIZE_B);
            LOGGER.i("disk cache initialized; size: " + Utils.formatBytes(DISK_CACHE_SIZE_B));
            return result;
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        } catch (DiskLruCacheException e) {
            LOGGER.w("error initializing disk cache, disabling");
            return null;
        } finally {
            LOGGER.v("initDiskCache: releasing disk cache initialization lock: " + Thread.currentThread().toString());
        }
    }


    /**
     * Creates a unique subdirectory of the designated app cache directory. Tries to use external but if not mounted, falls back
     * on internal storage.
     *
     * @param context
     * @return
     */
    private File getDiskCacheDir(Context context) {
        // Check if media is mounted or storage is built-in, if so, try and use
        // external cache dir (and exteranlscache dir is not null}
        // otherwise use internal cache dir
        // TODO: rozmyslet velikost cache podle zvoleneho uloziste
        // FIXME: na S3 haze nullpointerexception
        // String cacheDirPath =
        // Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
        // || !Environment.isExternalStorageRemovable() ?
        // context.getExternalCacheDir().getPath() : context
        // .getCacheDir().getPath();

        String cacheDirPath = context.getCacheDir().getPath();
        return new File(cacheDirPath + File.separator + DISK_CACHE_SUBDIR);
    }


    @Override
    public boolean isDiskCacheEnabled() {
        return mDiskCache != null;
    }


    @Override
    public boolean isMetadataOnDisk(String key) {
        if (mDiskCache != null) {
            try {
                return mDiskCache.containsReadable(key);
            } catch (IOException e) {
                LOGGER.v("isMetadataOnDisk error: " + key, e);
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public String getMetadataFromDisk(String key) {
        if (mDiskCache != null) {
            try {
                Snapshot snapshot = mDiskCache.get(key);
                if (snapshot != null) {
                    String result = snapshot.getString(0);
                    return result;
                } else {
                    return null;
                }
            } catch (DiskLruCacheException e) {
                LOGGER.w("error loading from disk cache: " + key, e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void storeMetadataIntoDisk(String key, String metadata) {
        if (mDiskCache != null) {
            try {
                Snapshot fromDiskCache = mDiskCache.get(key);
                if (fromDiskCache != null) {
                    LOGGER.d("already in disk cache: " + key);
                } else {
                    LOGGER.d("storing into disk cache: " + key);
                    mDiskCache.storeString(0, key, metadata);
                }
            } catch (DiskLruCacheException e) {
                LOGGER.e("failed to store into disk cache: " + key, e);
            }
        }
    }

}

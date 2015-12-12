package cz.mzk.tiledimageview.images.cache;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.IOException;

import cz.mzk.tiledimageview.Logger;
import cz.mzk.tiledimageview.Utils;

/**
 * Created by Martin Řehánek on 11.12.15.
 */
public abstract class AbstractCache<Item> implements Cache<Item> {


    protected final Logger mLogger;
    private final InflatableLruCache<String, Item> mMemoryCache;
    private Object mMemoryCacheLock = new Object(); //not final, can be possibly replaced
    private DiskLruCache mDiskCache = null;

    public AbstractCache(Context context, Logger logger, int memoryCacheSizeItems, boolean diskCacheEnabled, String diskCacheSubdir, long diskCacheSizeBytes, boolean clearDiskCache) {
        mLogger = logger;
        mMemoryCache = initMemoryCache(memoryCacheSizeItems);
        if (diskCacheEnabled) {
            mDiskCache = initDiskCache(context, diskCacheSubdir, diskCacheSizeBytes, clearDiskCache);
        } else {
            mDiskCache = null;
        }
    }

    private InflatableLruCache<String, Item> initMemoryCache(int sizeItems) {
        InflatableLruCache<String, Item> result;
        synchronized (mMemoryCacheLock) {
            mLogger.v("assumed memory-cache lock (initialization): " + Thread.currentThread().toString());
            result = new InflatableLruCache<String, Item>(sizeItems, mLogger);
            mLogger.d("in-memory lru cache allocated for " + sizeItems + " items");
        }
        mLogger.v("released memory-cache lock (initialization): " + Thread.currentThread().toString());
        return result;
    }

    @Override
    public Item getItemFromMemoryCache(String key) {
        Item result;
        synchronized (mMemoryCacheLock) {
            mLogger.v("assumed memory-cache lock (get): " + Thread.currentThread().toString());
            result = mMemoryCache.get(key);
        }
        mLogger.v("released memory-cache lock (get): " + Thread.currentThread().toString());
        return result;
    }

    @Override
    public void storeItemToMemoryCache(String key, Item item) {
        synchronized (mMemoryCacheLock) {
            mLogger.v("assumed memory-cache lock (store): " + Thread.currentThread().toString());
            if (mMemoryCache.get(key) == null) {
                mLogger.d("storing to memory cache: " + key);
                mMemoryCache.put(key, item);
            } else {
                mLogger.d("already in memory cache: " + key);
            }
        }
        mLogger.v("released memory-cache lock (store): " + Thread.currentThread().toString());
    }


    @Override
    public void increasMemoryCacheSize(int newMaxItems) {
        synchronized (mMemoryCacheLock) {
            mMemoryCache.inflate(newMaxItems);
        }
    }

    private DiskLruCache initDiskCache(Context context, String subdir, long sizeBytes, boolean clearCache) {
        try {
            File cacheDir = getDiskCacheDir(context, subdir);
            int appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            if (cacheDir.exists()) {
                if (clearCache) {
                    mLogger.i("clearing disk cache");
                    boolean cleared = DiskUtils.deleteDirContent(cacheDir);
                    if (!cleared) {
                        mLogger.w("failed to delete content of " + cacheDir.getAbsolutePath() + ", disabling disk cache");
                        return null;
                    }
                }
            } else {
                mLogger.i("creating disk cache dir " + cacheDir);
                boolean created = cacheDir.mkdir();
                if (!created) {
                    mLogger.w("failed to create disk cache dir " + cacheDir.getAbsolutePath() + ", disabling disk cache");
                    return null;
                }
            }
            mLogger.d("disk cache dir: " + cacheDir.getAbsolutePath());
            DiskLruCache result = DiskLruCache.open(cacheDir, appVersion, 1, sizeBytes);
            mLogger.i("disk cache initialized; size: " + Utils.formatBytes(sizeBytes));
            return result;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        } catch (DiskLruCache.DiskLruCacheException e) {
            mLogger.w("error initializing disk cache, disabling");
            return null;
        }
    }


    /**
     * Creates a unique subdirectory of the designated app cache directory. Tries to use external but if not mounted, falls back
     * on internal storage.
     *
     * @param context
     * @return
     */
    private File getDiskCacheDir(Context context, String subdir) {
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
        return new File(cacheDirPath + File.separator + subdir);
    }


    @Override
    public boolean isDiskCacheEnabled() {
        return mDiskCache != null;
    }


    @Override
    public boolean isItemInDiskCache(String key) {
        if (mDiskCache != null) {
            try {
                return mDiskCache.containsReadable(key);
            } catch (IOException e) {
                mLogger.v("isItemInDiskCache error: " + key, e);
                return false;
            }
        } else {
            return false;
        }
    }


    @Override
    public void close() {
        try {
            mDiskCache.flush();
        } catch (DiskLruCache.DiskLruCacheException e) {
            mLogger.e("Error flushing disk cache");
        } finally {
            try {
                mDiskCache.close();
            } catch (IOException e) {
                mLogger.e("Error closing disk cache");
            }
        }
    }

    @Override
    public Item getItemFromDiskCache(String key) {
        if (mDiskCache != null) {
            try {
                return getItem(mDiskCache, key);
            } catch (DiskLruCache.DiskLruCacheException e) {
                mLogger.w("error loading from disk cache: " + key, e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void storeItemToDiskCache(String key, Item item) {
        if (mDiskCache != null) {
            try {
                DiskLruCache.Snapshot fromDiskCache = mDiskCache.get(key);
                if (fromDiskCache != null) {
                    mLogger.d("already in disk cache: " + key);
                } else {
                    mLogger.d("storing into disk cache: " + key);
                    storeItem(mDiskCache, key, item);
                }
            } catch (DiskLruCache.DiskLruCacheException e) {
                mLogger.e("failed to store into disk cache: " + key, e);
            }
        }
    }

    abstract Item getItem(DiskLruCache diskCache, String key) throws DiskLruCache.DiskLruCacheException;

    abstract void storeItem(DiskLruCache diskCache, String key, Item item) throws DiskLruCache.DiskLruCacheException;


}

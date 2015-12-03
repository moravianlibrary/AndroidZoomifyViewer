package cz.mzk.androidzoomifyviewer.cache;

import android.os.AsyncTask;

import java.io.File;

import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.cache.DiskLruCache.DiskLruCacheException;

public class InitDiskCacheTask extends AsyncTask<File, Void, DiskLruCache> {

    private static final Logger logger = new Logger(InitDiskCacheTask.class);

    private final Object mDiskCacheInitializationLock = new Object();
    private final int appVersion;
    private final boolean clearCache;
    private final int cacheSize;
    private final Listener listener;

    public InitDiskCacheTask(int appVersion, int cacheSize, boolean clearCache, Listener listener) {
        this.appVersion = appVersion;
        this.clearCache = clearCache;
        this.cacheSize = cacheSize;
        this.listener = listener;
    }

    @Override
    protected DiskLruCache doInBackground(File... params) {
        synchronized (mDiskCacheInitializationLock) {
            logger.v("assuming mDiskCacheLock: " + Thread.currentThread().toString());
            try {
                File cacheDir = params[0];
                if (cacheDir.exists()) {
                    if (clearCache) {
                        logger.i("clearing tiles disk cache");
                        boolean cleared = DiskUtils.deleteDirContent(cacheDir);
                        if (!cleared) {
                            logger.w("failed to delete content of " + cacheDir.getAbsolutePath());
                            return null;
                        }
                    }
                } else {
                    logger.i("creating cache dir " + cacheDir);
                    boolean created = cacheDir.mkdir();
                    if (!created) {
                        logger.e("failed to create cache dir " + cacheDir.getAbsolutePath());
                        return null;
                    }
                }
                return DiskLruCache.open(cacheDir, appVersion, 1, cacheSize);
            } catch (DiskLruCacheException e) {
                logger.e("error opening disk cache");
                return null;
            } finally {
                logger.v("releasing disk cache initialization lock: " + Thread.currentThread().toString());
                mDiskCacheInitializationLock.notifyAll();
            }
        }
    }

    @Override
    protected void onPostExecute(DiskLruCache result) {
        if (listener != null) {
            if (result != null) {
                listener.onFinished(result);
            } else {
                listener.onError();
            }
        }
    }

    public static interface Listener {
        public void onError();

        public void onFinished(DiskLruCache cache);
    }

}

package cz.mzk.androidzoomifyviewer.cache;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;

import cz.mzk.androidzoomifyviewer.ConcurrentAsyncTask;
import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.cache.TilesCache.FetchingBitmapFromDiskHandler;

public class BitmapFetchTaskRegistry {

    public static final int MAX_TASKS = 10;
    // private static final int THREAD_PRIORITY = Math.min(Thread.MAX_PRIORITY, Thread.MIN_PRIORITY + 2);

    private static final Logger logger = new Logger(BitmapFetchTaskRegistry.class);

    private final Map<String, FetchBitmapFromDiskAndStoreToMemoryCacheTask> tasks = new HashMap<String, FetchBitmapFromDiskAndStoreToMemoryCacheTask>();
    private final MemoryAndDiskTilesCache cache;

    public BitmapFetchTaskRegistry(MemoryAndDiskTilesCache cache) {
        this.cache = cache;
    }

    public void registerTask(String key, FetchingBitmapFromDiskHandler fetchedListener) {
        if (tasks.size() < MAX_TASKS) {
            boolean registered = tasks.containsKey(key);
            if (!registered) {
                FetchBitmapFromDiskAndStoreToMemoryCacheTask task = new FetchBitmapFromDiskAndStoreToMemoryCacheTask(
                        cache, key, fetchedListener);
                tasks.put(key, task);
                logger.v("registration performed: " + key + ": (total " + tasks.size() + ")");
                task.executeConcurrentIfPossible();
            } else {
                logger.v("registration ignored: already registered: " + key + ": (total " + tasks.size() + ")");
            }
        } else {
            logger.v("registration ignored: to many tasks: " + key + ": (total " + tasks.size() + ")");
        }
    }

    public void cancelAllTasks() {
        for (FetchBitmapFromDiskAndStoreToMemoryCacheTask task : tasks.values()) {
            task.cancel(false);
        }
    }

    private void unregisterTask(String key) {
        tasks.remove(key);
        // logger.v("unregistration performed: " + key + ": (total " + tasks.size() + ")");
    }

    private class FetchBitmapFromDiskAndStoreToMemoryCacheTask extends ConcurrentAsyncTask<Void, Void, Void> {

        private final MemoryAndDiskTilesCache cache;
        private final String key;
        private final FetchingBitmapFromDiskHandler onFinishedHandler;
        private boolean success = false;

        public FetchBitmapFromDiskAndStoreToMemoryCacheTask(MemoryAndDiskTilesCache cache, String key,
                                                            FetchingBitmapFromDiskHandler onFinishedHandler) {
            this.cache = cache;
            this.key = key;
            this.onFinishedHandler = onFinishedHandler;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Thread thread = Thread.currentThread();
            // thread.setPriority(THREAD_PRIORITY);
            // ThreadGroup group = thread.getThreadGroup();
            // int threadPriority = thread.getPriority();
            // TestLoggers.THREADS.d(String.format(
            // "bmp fetch: priority: %d, TG: name: %s, active: %d, max priority: %d, ", threadPriority,
            // group.getName(), group.activeCount(), group.getMaxPriority()));
            try {
                if (!isCancelled()) {
                    Bitmap fromDiskCache = cache.getTileFromDiskCache(key);
                    if (!isCancelled()) {
                        if (fromDiskCache != null) {
                            logger.v("storing to memory cache: " + key);
                            cache.storeTileToMemoryCache(key, fromDiskCache);
                            success = true;
                        } else {
                            logger.v("tile bitmap is null: " + key);
                            success = false;
                        }
                    }
                }
                return null;
            } catch (Throwable e) {
                logger.e("error fetching tile " + key, e);
                return null;
            }
        }

        @Override
        protected void onCancelled() {
            // logger.e("canceled: " + key);
            super.onCancelled();
            unregisterTask(key);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            unregisterTask(key);
            if (success && onFinishedHandler != null) {
                onFinishedHandler.onFetched();
            }
        }
    }

}

package cz.mzk.tiledimageview.images.tasks;

import cz.mzk.tiledimageview.images.cache.CacheManager;

/**
 * Created by Martin Řehánek on 11.12.15.
 */
public class InflateTileMemoryCache extends ConcurrentAsyncTask<Void, Void, Void> {

    private final int mNewMaxSize;
    private final TaskManager.TaskHandler mRegistryHandler;

    public InflateTileMemoryCache(int mNewMaxSize, TaskManager.TaskHandler registryHandler) {
        this.mNewMaxSize = mNewMaxSize;
        mRegistryHandler = registryHandler;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (!isCancelled()) {
            CacheManager.getTileCache().increasMemoryCacheSize(mNewMaxSize);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mRegistryHandler != null) {
            mRegistryHandler.onFinished();
        }
    }

    @Override
    protected void onCancelled(Void result) {
        if (mRegistryHandler != null) {
            mRegistryHandler.onCanceled();
        }
    }
}

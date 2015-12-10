package cz.mzk.tiledimageview.images.tasks;

import cz.mzk.tiledimageview.ConcurrentAsyncTask;
import cz.mzk.tiledimageview.cache.CacheManager;
import cz.mzk.tiledimageview.images.tasks.ImageManagerTaskRegistry.TaskHandler;

/**
 * Created by Martin Řehánek on 10.12.15.
 */
public class StoreMetadataIntoDiskCacheTask extends ConcurrentAsyncTask<Void, Void, Void> {

    private final String mKey;
    private final String mMetadata;
    private final TaskHandler mHandler;

    public StoreMetadataIntoDiskCacheTask(String key, String metadata, TaskHandler handler) {
        mKey = key;
        mMetadata = metadata;
        mHandler = handler;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (!isCancelled()) {
            CacheManager.getMetadataCache().storeMetadataIntoDisk(mMetadata, mKey);
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void result) {
        if (mHandler != null) {
            mHandler.onFinished();
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (mHandler != null) {
            mHandler.onCanceled();
        }
    }
}


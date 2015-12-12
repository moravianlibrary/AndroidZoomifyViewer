package cz.mzk.tiledimageview.images.tasks;

import cz.mzk.tiledimageview.images.cache.CacheManager;
import cz.mzk.tiledimageview.images.tasks.TaskManager.TaskListener;

/**
 * Created by Martin Řehánek on 10.12.15.
 */
public class StoreMetadataIntoDiskCacheTask extends ConcurrentAsyncTask<Void, Void, Void> {

    private final String mKey;
    private final String mMetadata;
    private final TaskListener mListener;

    public StoreMetadataIntoDiskCacheTask(String key, String metadata, TaskListener listener) {
        mKey = key;
        mMetadata = metadata;
        mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (!isCancelled()) {
            CacheManager.getMetadataCache().storeItemToDiskCache(mKey, mMetadata);
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void result) {
        if (mListener != null) {
            mListener.onFinished();
        }
    }

    @Override
    protected void onCancelled(Void result) {
        if (mListener != null) {
            mListener.onCanceled();
        }
    }
}


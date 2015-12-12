package cz.mzk.tiledimageview.images.tasks;

import cz.mzk.tiledimageview.images.cache.CacheManager;

/**
 * Created by Martin Řehánek on 11.12.15.
 */
public class InflateTileMemoryCache extends ConcurrentAsyncTask<Void, Void, Void> {

    private final int mNewMaxSize;
    private final TaskManager.TaskListener mListener;

    public InflateTileMemoryCache(int mNewMaxSize, TaskManager.TaskListener listener) {
        this.mNewMaxSize = mNewMaxSize;
        mListener = listener;
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

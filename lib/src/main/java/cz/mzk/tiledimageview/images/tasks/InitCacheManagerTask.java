package cz.mzk.tiledimageview.images.tasks;

import android.content.Context;

import cz.mzk.tiledimageview.images.cache.CacheManager;

/**
 * Created by Martin Řehánek on 10.12.15.
 */
public class InitCacheManagerTask extends ConcurrentAsyncTask<Void, Void, Void> {


    private final Context mContext;
    private final boolean mDiskCacheEnabled;
    private final boolean mClearDiskCache;
    private final long mTileDiskCacheBytes;
    private final TaskManager.TaskListener mListener;


    public InitCacheManagerTask(Context context, boolean diskCacheEnabled, boolean clearDiskCache, long tileDiskCacheBytes, TaskManager.TaskListener listener) {
        mContext = context;
        mDiskCacheEnabled = diskCacheEnabled;
        mClearDiskCache = clearDiskCache;
        mTileDiskCacheBytes = tileDiskCacheBytes;
        mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (!CacheManager.isInitialized()) {
            CacheManager.initialize(mContext, mDiskCacheEnabled, mClearDiskCache, mTileDiskCacheBytes);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
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

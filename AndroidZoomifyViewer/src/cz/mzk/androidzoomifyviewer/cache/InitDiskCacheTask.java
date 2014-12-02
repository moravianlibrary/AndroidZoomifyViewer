package cz.mzk.androidzoomifyviewer.cache;

import java.io.File;
import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

public class InitDiskCacheTask extends AsyncTask<File, Void, DiskLruCache> {
	private static final String TAG = InitDiskCacheTask.class.getSimpleName();

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
			Log.v(TAG, "assuming mDiskCacheLock: " + Thread.currentThread().toString());
			try {
				File cacheDir = params[0];
				if (cacheDir.exists()) {
					if (clearCache) {
						Log.i(TAG, "clearing tiles disk cache");
						boolean cleared = DiskUtils.deleteDirContent(cacheDir);
						if (!cleared) {
							Log.w(TAG, "failed to delete content of " + cacheDir.getAbsolutePath());
							return null;
						}
					}
				} else {
					Log.i(TAG, "creating cache dir " + cacheDir);
					boolean created = cacheDir.mkdir();
					if (!created) {
						Log.w(TAG, "failed to create cache dir " + cacheDir.getAbsolutePath());
						return null;
					}
				}
				return DiskLruCache.open(cacheDir, appVersion, 1, cacheSize);
			} catch (IOException e) {
				Log.w(TAG, "error opening disk cache");
				return null;
			} finally {
				Log.v(TAG, "releasing disk cache initialization lock: " + Thread.currentThread().toString());
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

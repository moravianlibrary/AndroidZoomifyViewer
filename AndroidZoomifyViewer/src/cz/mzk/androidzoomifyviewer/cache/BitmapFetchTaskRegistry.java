package cz.mzk.androidzoomifyviewer.cache;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.util.Log;
import cz.mzk.androidzoomifyviewer.ConcurrentAsyncTask;
import cz.mzk.androidzoomifyviewer.cache.TilesCache.FetchingBitmapFromDiskHandler;

public class BitmapFetchTaskRegistry {

	private static final String TAG = BitmapFetchTaskRegistry.class.getSimpleName();
	public static final int MAX_TASKS = 10;

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
				Log.v(TAG, "registration performed: " + key + ": (total " + tasks.size() + ")");
				task.executeConcurrentIfPossible();
			} else {
				Log.v(TAG, "registration ignored: already registered: " + key + ": (total " + tasks.size() + ")");
			}
		} else {
			Log.v(TAG, "registration ignored: to many tasks: " + key + ": (total " + tasks.size() + ")");
		}
	}

	public void cancelAllTasks() {
		for (FetchBitmapFromDiskAndStoreToMemoryCacheTask task : tasks.values()) {
			task.cancel(false);
		}
	}

	private void unregisterTask(String key) {
		tasks.remove(key);
		// Log.v(TAG, "unregistration performed: " + key + ": (total " + tasks.size() + ")");
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
			try {
				if (!isCancelled()) {
					Bitmap fromDiskCache = cache.getTileFromDiskCache(key);
					if (!isCancelled()) {
						if (fromDiskCache != null) {
							Log.v(TAG, "storing to memory cache: " + key);
							cache.storeTileToMemoryCache(key, fromDiskCache);
							success = true;
						} else {
							Log.e(TAG, "tile bitmap is null: " + key);
							success = false;
						}
					}
				}
				return null;
			} catch (Throwable e) {
				Log.e(TAG, "error fetching tile " + key, e);
				return null;
			}
		}

		@Override
		protected void onCancelled() {
			// Log.v(TAG, "canceled: " + key);
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

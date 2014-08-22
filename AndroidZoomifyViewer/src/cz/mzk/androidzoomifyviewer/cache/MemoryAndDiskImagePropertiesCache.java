package cz.mzk.androidzoomifyviewer.cache;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import cz.mzk.androidzoomifyviewer.cache.DiskLruCache.Editor;
import cz.mzk.androidzoomifyviewer.cache.DiskLruCache.Snapshot;

/**
 * @author Martin Řehánek
 * 
 */
public class MemoryAndDiskImagePropertiesCache extends AbstractImagePropertiesCache implements ImagePropertiesCache {

	private static final String TAG = MemoryAndDiskImagePropertiesCache.class.getSimpleName();
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 1MB
	private static final int MEMORY_CACHE_ITEM_SIZE = 100;
	private static final String DISK_CACHE_SUBDIR = "imageProperties";
	private final Object mDiskCacheInitializationLock = new Object();
	private DiskLruCache mDiskCache = null;
	private boolean mDiskCacheDisabled = false;
	private final LruCache<String, String> mMemoryCache;

	public MemoryAndDiskImagePropertiesCache(Context context, boolean clearCache) {
		mMemoryCache = initMemoryCache();
		initDiskCacheAsync(context, clearCache);
	}

	private LruCache<String, String> initMemoryCache() {
		LruCache<String, String> cache = new LruCache<String, String>(MEMORY_CACHE_ITEM_SIZE) {
		};
		Log.d(TAG, "in-memory lru cache allocated for " + MEMORY_CACHE_ITEM_SIZE + " items");
		return cache;
	}

	private void initDiskCacheAsync(Context context, boolean clearCache) {
		try {
			File cacheDir = getDiskCacheDir(context);
			int appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
			new InitDiskCacheTask(appVersion, clearCache).execute(cacheDir);
		} catch (NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a unique subdirectory of the designated app cache directory.
	 * Tries to use external but if not mounted, falls back on internal storage.
	 * 
	 * @param context
	 * @return
	 */
	private File getDiskCacheDir(Context context) {
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
		// context.getc
		String cacheDirPath = context.getCacheDir().getPath();
		return new File(cacheDirPath + File.separator + DISK_CACHE_SUBDIR);
	}

	private class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
		private final int appVersion;
		private final boolean clearCache;

		public InitDiskCacheTask(int appVersion, boolean clearCache) {
			this.appVersion = appVersion;
			this.clearCache = clearCache;
		}

		@Override
		protected Void doInBackground(File... params) {
			synchronized (mDiskCacheInitializationLock) {
				Log.d(TAG, "assuming disk cache initialization lock: " + Thread.currentThread().toString());
				try {
					File cacheDir = params[0];
					if (cacheDir.exists()) {
						if (clearCache) {
							Log.i(TAG, "clearing image-properties disk cache");
							boolean cleared = DiskUtils.deleteDirContent(cacheDir);
							if (!cleared) {
								Log.w(TAG, "failed to delete content of " + cacheDir.getAbsolutePath());
								disableDiskCache();
								return null;
							}
						}
					} else {
						Log.i(TAG, "creating cache dir " + cacheDir);
						boolean created = cacheDir.mkdir();
						if (!created) {
							Log.w(TAG, "failed to create cache dir " + cacheDir.getAbsolutePath());
							disableDiskCache();
							return null;
						}
					}
					mDiskCache = DiskLruCache.open(cacheDir, appVersion, 1, DISK_CACHE_SIZE);
					return null;
				} catch (IOException e) {
					Log.w(TAG, "error initializing disk cache, disabling");
					mDiskCacheDisabled = true;
					mDiskCacheInitializationLock.notifyAll();
					return null;
				} finally {
					mDiskCacheInitializationLock.notifyAll();
					Log.d(TAG, "releasing disk cache initialization lock: " + Thread.currentThread().toString());
				}
			}
		}
	}

	private void disableDiskCache() {
		Log.i(TAG, "disabling disk cache");
		mDiskCacheDisabled = true;
	}

	@Override
	public void storeXml(String xml, String zoomifyBaseUrl) {
		String key = buildKey(zoomifyBaseUrl);
		storeXmlToMemoryCache(key, xml);
		storeXmlToDiskCache(key, xml);
	}

	private void storeXmlToMemoryCache(String key, String xml) {
		synchronized (mMemoryCache) {
			Log.d(TAG, "assuming mMemoryCache lock: " + Thread.currentThread().toString());
			if (mMemoryCache.get(key) == null) {
				Log.d(TAG, "storing to memory cache: " + key);
				mMemoryCache.put(key, xml);
			} else {
				Log.d(TAG, "already in memory cache: " + key);
			}
			Log.d(TAG, "releasing mMemoryCache lock: " + Thread.currentThread().toString());
		}
	}

	private void waitUntilDiskCacheInitializedOrDisabled() {
		try {
			synchronized (mDiskCacheInitializationLock) {
				Log.d(TAG, "assuming disk cache lock: " + Thread.currentThread().toString());
				// Wait until disk cache is initialized or disabled
				while (mDiskCache == null && !mDiskCacheDisabled) {
					try {
						mDiskCacheInitializationLock.wait();
					} catch (InterruptedException e) {
						Log.e(TAG, "waiting for disk cache lock interrupted", e);
					}
				}
			}
		} finally {
			Log.d(TAG, "releasing disk cache lock: " + Thread.currentThread().toString());
		}
	}

	private void storeXmlToDiskCache(String key, String xml) {
		waitUntilDiskCacheInitializedOrDisabled();
		Editor edit = null;
		try {
			if (!mDiskCacheDisabled) {
				Snapshot fromDiskCache = mDiskCache.get(key);
				if (fromDiskCache != null) {
					Log.d(TAG, "already in disk cache: " + key);
				} else {
					Log.d(TAG, "storing to disk cache: " + key);
					edit = mDiskCache.edit(key);
					if (edit != null) {
						edit.set(0, xml);
						edit.commit();
					} else {
						// jine vlakno se pokousi zapisovat
						// tj. spatne implementovana synchronizace
						Log.e(TAG, key + ": edit allready opened");
					}
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "failed to store xml to disk cache: " + e.getMessage());
			try {
				if (edit != null) {
					edit.abort();
				}
			} catch (IOException e1) {
				Log.e(TAG, "failed to cleanup", e1);
			}
		}
	}

	@Override
	public String getXml(String zoomifyBaseUrl) {
		String key = buildKey(zoomifyBaseUrl);
		// long start = System.currentTimeMillis();
		String inMemoryCache = mMemoryCache.get(key);
		// long afterHitOrMiss = System.currentTimeMillis();
		if (inMemoryCache != null) {
			// Log.d(TAG, "memory cache hit: " + key);
			// Log.d(TAG, "memory cache hit, delay: " + (afterHitOrMiss - start)
			// + " ms");
			return inMemoryCache;
		} else {
			// Log.d(TAG, "memory cache miss: " + key);
			// Log.d(TAG, "memory cache miss, delay: " + (afterHitOrMiss -
			// start) + " ms");
			String fromDiskCache = getXmlFromDiskCache(key);
			// store also to memory cache (nonblocking)
			if (fromDiskCache != null) {
				new StoreXmlToMemoryCacheTask(key).execute(fromDiskCache);
			}
			return fromDiskCache;
		}
	}

	private String getXmlFromDiskCache(String key) {
		waitUntilDiskCacheInitializedOrDisabled();
		try {
			if (!mDiskCacheDisabled) {
				// long start = System.currentTimeMillis();
				Snapshot snapshot = mDiskCache.get(key);
				if (snapshot != null) {
					// long afterHit = System.currentTimeMillis();
					// InputStream in = snapshot.getInputStream(0);
					// String result = stringFromStream(in, bufferSize);
					String result = snapshot.getString(0);
					// long afterDecoding = System.currentTimeMillis();
					// long retrieval = afterHit - start;
					// long decoding = afterDecoding - afterHit;
					// Log.d(TAG, "disk cache hit, delay: " + (retrieval +
					// decoding) + "ms (retrieval: " + retrieval
					// + "ms, decoding: " + decoding + " ms)");
					// Log.d(TAG, "disk cache hit: " + key + ", delay: " +
					// (retrieval + decoding) + "ms (retrieval: "
					// + retrieval + ", decoding: " + decoding);
					return result;
				} else {
					// long afterMiss = System.currentTimeMillis();
					// Log.d(TAG, "disk cache miss:, delay: " + (afterMiss -
					// start) + " ms");
					// Log.d(TAG, "disk cache miss: " + key + ", delay: " +
					// (afterMiss - start) + " ms");
					return null;
				}
			} else {
				return null;
			}
		} catch (IOException e) {
			Log.i(TAG, "error loading xml from disk cache: " + key, e);
			return null;
		}
	}

	private class StoreXmlToMemoryCacheTask extends AsyncTask<String, Void, Void> {
		private final String key;

		public StoreXmlToMemoryCacheTask(String key) {
			this.key = key;
		}

		@Override
		protected Void doInBackground(String... params) {
			storeXmlToMemoryCache(key, params[0]);
			return null;
		}
	}

}

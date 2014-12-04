package cz.mzk.androidzoomifyviewer.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import cz.mzk.androidzoomifyviewer.ConcurrentAsyncTask;
import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.cache.DiskLruCache.DiskLruCacheException;
import cz.mzk.androidzoomifyviewer.cache.DiskLruCache.Snapshot;
import cz.mzk.androidzoomifyviewer.cache.TileBitmap.State;
import cz.mzk.androidzoomifyviewer.tiles.TileId;
import cz.mzk.androidzoomifyviewer.viewer.Utils;

/**
 * 
 * @author Martin Řehánek
 * 
 */
public class MemoryAndDiskTilesCache extends AbstractTileCache implements TilesCache {

	// public static final int DISK_CACHE_SIZE = 1024; // 100MB
	// public static final int DISK_CACHE_SIZE = 1024 * 1024 * 100; // 100MB
	public static final String DISK_CACHE_SUBDIR = "tiles";

	private static final Logger logger = new Logger(MemoryAndDiskTilesCache.class);

	private final LruCache<String, Bitmap> mMemoryCache;
	private final BitmapFetchTaskRegistry mBitmapFetchManager;
	private final Object mDiskCacheInitializationLock = new Object();

	private DiskLruCache mDiskCache = null;
	private boolean mDiskCacheEnabled = true;

	public MemoryAndDiskTilesCache(Context context, boolean clearCache, int memoryCacheMaxItems, long diskCacheBytes) {
		super();
		// mMemoryCache = initMemoryCacheFixedSize();
		mMemoryCache = new LruCache<String, Bitmap>(memoryCacheMaxItems);
		logger.i("memory cache initialized; max items: " + memoryCacheMaxItems);
		mBitmapFetchManager = new BitmapFetchTaskRegistry(this);
		initDiskCacheAsync(context, clearCache, diskCacheBytes);
	}

	private LruCache<String, Bitmap> initMemoryCacheFixedSize() {
		// Get max available VM memory, exceeding this amount will throw an OutOfMemory exception. Stored in kilobytes as LruCache
		// takes an int in its constructor.
		int maxMemoryKB = (int) (Runtime.getRuntime().maxMemory() / 1024);
		// Use 1/8th of the available memory for this memory cache.
		final int cacheSizeKB = maxMemoryKB / 8;
		LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(cacheSizeKB) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes rather than number of items.
				return getBitmapSizeInKB(bitmap);
			}
		};
		// logger.d("in-memory lru cache allocated with " + cacheSizeKB + " kB");
		return cache;
	}

	// TODO: no need to be async
	private void initDiskCacheAsync(Context context, boolean clearCache, long diskCacheBytes) {
		try {
			File cacheDir = getDiskCacheDir(context);
			int appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
			new InitDiskCacheTask(appVersion, clearCache, diskCacheBytes).execute(cacheDir);
		} catch (NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a unique subdirectory of the designated app cache directory. Tries to use external but if not mounted, falls back
	 * on internal storage.
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
		String cacheDirPath = context.getCacheDir().getPath();
		return new File(cacheDirPath + File.separator + DISK_CACHE_SUBDIR);
	}

	private class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
		private final int appVersion;
		private final boolean clearCache;
		private final long cacheSizeBytes;

		public InitDiskCacheTask(int appVersion, boolean clearCache, long diskCacheBytes) {
			this.appVersion = appVersion;
			this.clearCache = clearCache;
			this.cacheSizeBytes = diskCacheBytes;
		}

		@Override
		protected Void doInBackground(File... params) {
			synchronized (mDiskCacheInitializationLock) {
				// Log.v(TAG, "assuming mDiskCacheLock: " + Thread.currentThread().toString());
				try {
					File cacheDir = params[0];
					if (cacheDir.exists()) {
						if (clearCache) {
							logger.i("clearing disk cache");
							boolean cleared = DiskUtils.deleteDirContent(cacheDir);
							if (!cleared) {
								logger.w("failed to delete content of " + cacheDir.getAbsolutePath());
								disableDiskCache();
								return null;
							}
						}
					} else {
						// logger.i("creating cache dir " + cacheDir);
						boolean created = cacheDir.mkdir();
						if (!created) {
							logger.w("failed to create cache dir " + cacheDir.getAbsolutePath());
							disableDiskCache();
							return null;
						}
					}
					mDiskCache = DiskLruCache.open(cacheDir, appVersion, 1, cacheSizeBytes);
					logger.i("disk cache initialized; size: " + Utils.formatBytes(cacheSizeBytes));
					return null;
				} catch (DiskLruCacheException e) {
					logger.w("error opening disk cache, disabling");
					disableDiskCache();
					return null;
				} finally {
					// Log.v(TAG, "releasing disk cache initialization lock: " + Thread.currentThread().toString());
					mDiskCacheInitializationLock.notifyAll();
				}
			}
		}
	}

	private void disableDiskCache() {
		logger.i("disabling disk cache");
		mDiskCacheEnabled = false;
	}

	@Override
	public void storeTile(Bitmap bmp, String zoomifyBaseUrl, TileId tileId) {
		String key = buildKey(zoomifyBaseUrl, tileId);
		storeTileToMemoryCache(key, bmp);
		storeTileToDiskCache(key, bmp);
	}

	protected void storeTileToMemoryCache(String key, Bitmap bmp) {
		synchronized (mMemoryCache) {
			// Log.v(TAG, "assuming mMemoryCache lock: " + Thread.currentThread().toString());
			if (mMemoryCache.get(key) == null) {
				// logger.d("storing to memory cache: " + key);
				mMemoryCache.put(key, bmp);
			} else {
				// logger.d("already in memory cache: " + key);
			}
			// Log.v(TAG, "releasing mMemoryCache lock: " + Thread.currentThread().toString());
		}
	}

	private void storeTileToDiskCache(String key, Bitmap bmp) {
		waitUntilDiskCacheInitializedOrDisabled();
		try {
			if (mDiskCacheEnabled) {
				Snapshot fromDiskCache = mDiskCache.get(key);
				if (fromDiskCache != null) {
					logger.d("already in disk cache: " + key);
				} else {
					logger.d("storing to disk cache: " + key);
					mDiskCache.storeBitmap(0, key, bmp);
				}
			}
		} catch (DiskLruCacheException e) {
			logger.e("failed to store tile to disk cache: " + key, e);
		}
	}

	@Override
	public Bitmap getTile(String zoomifyBaseUrl, TileId tileId) {
		String key = buildKey(zoomifyBaseUrl, tileId);
		// long start = System.currentTimeMillis();
		// Bitmap inMemoryCache = mMemoryCache.get(key);
		Bitmap inMemoryCache = getTileFromMemoryCache(key);
		// long afterHitOrMiss = System.currentTimeMillis();
		if (inMemoryCache != null) {
			// logger.d("memory cache hit: " + key);
			// logger.d("memory cache hit, delay: " + (afterHitOrMiss - start) + " ms");
			// return new TileBitmap(State.IN_MEMORY, inMemoryCache);
			return inMemoryCache;
		} else {
			// logger.d("memory cache miss: " + key);
			// logger.d("memory cache miss, delay: " + (afterHitOrMiss - start) + " ms");
			Bitmap fromDiskCache = getTileFromDiskCache(key);
			// store also to memory cache (nonblocking)
			if (fromDiskCache != null) {
				new StoreTileToMemoryCacheTask(key).executeConcurrentIfPossible(fromDiskCache);
			}
			return fromDiskCache;
		}
	}

	private Bitmap getTileFromMemoryCache(String key) {
		synchronized (mMemoryCache) {
			return mMemoryCache.get(key);
		}
	}

	@Override
	public boolean containsTile(String zoomifyBaseUrl, TileId tileId) {
		String key = buildKey(zoomifyBaseUrl, tileId);
		// Bitmap inMemory = mMemoryCache.get(key);
		Bitmap inMemory = getTileFromMemoryCache(key);
		if (inMemory != null) {
			return true;
		} else {
			return diskCacheContainsTile(key);
		}
	}

	@Override
	public boolean containsTileInMemory(String zoomifyBaseUrl, TileId tileId) {
		String key = buildKey(zoomifyBaseUrl, tileId);
		// Bitmap inMemory = mMemoryCache.get(key);
		Bitmap inMemory = getTileFromMemoryCache(key);
		return inMemory != null;
	}

	private void waitUntilDiskCacheInitializedOrDisabled() {
		try {
			synchronized (mDiskCacheInitializationLock) {
				// Log.v(TAG, "assuming disk cache initialization lock: " + Thread.currentThread().toString());
				// Wait until disk cache is initialized or disabled
				while (mDiskCache == null && mDiskCacheEnabled) {
					try {
						mDiskCacheInitializationLock.wait();
					} catch (InterruptedException e) {
						logger.e("waiting for disk cache lock interrupted", e);
					}
				}
			}
		} finally {
			// Log.v(TAG, "releasing disk cache initialization lock: " + Thread.currentThread().toString());
		}
	}

	private boolean diskCacheContainsTile(String key) {
		waitUntilDiskCacheInitializedOrDisabled();
		try {
			if (mDiskCacheEnabled) {
				return mDiskCache.containsReadable(key);
			} else {
				return false;
			}
		} catch (IOException e) {
			logger.e("error loading tile from disk cache: " + key, e);
			return false;
		}
	}

	protected Bitmap getTileFromDiskCache(String key) {
		waitUntilDiskCacheInitializedOrDisabled();
		try {
			if (mDiskCacheEnabled) {
				// long start = System.currentTimeMillis();
				Snapshot snapshot = mDiskCache.get(key);
				if (snapshot != null) {
					// long afterHit = System.currentTimeMillis();
					InputStream in = snapshot.getInputStream(0);
					Bitmap stream = BitmapFactory.decodeStream(in);
					// long afterDecoding = System.currentTimeMillis();
					// long retrieval = afterHit - start;
					// long decoding = afterDecoding - afterHit;
					// long total = retrieval + decoding;
					// logger.d("disk cache hit: " + key + ", delay: " + total + "ms (retrieval: " + retrieval +
					// "ms, decoding: " + decoding + " ms)");
					if (stream == null) {
						logger.w("bitmap from disk cache was null, removing record");
						mDiskCache.remove(key);
					}
					return stream;
				} else {
					// long afterMiss = System.currentTimeMillis();
					// logger.d("disk cache miss: " + key + ", delay: " + (afterMiss - start) + " ms");
					return null;
				}
			} else {
				return null;
			}
		} catch (DiskLruCacheException e) {
			logger.e("error loading tile from disk cache: " + key, e);
			return null;
		}
	}

	private class StoreTileToMemoryCacheTask extends ConcurrentAsyncTask<Bitmap, Void, Void> {
		private final String key;

		public StoreTileToMemoryCacheTask(String key) {
			this.key = key;
		}

		@Override
		protected Void doInBackground(Bitmap... params) {
			storeTileToMemoryCache(key, params[0]);
			return null;
		}
	}

	@Override
	public TileBitmap getTileAsync(String zoomifyBaseUrl, TileId tileId, FetchingBitmapFromDiskHandler listener) {
		// Debug.startMethodTracing();
		String key = buildKey(zoomifyBaseUrl, tileId);
		// long start = System.currentTimeMillis();
		// Bitmap inMemoryCache = mMemoryCache.get(key);
		Bitmap inMemoryCache = getTileFromMemoryCache(key);
		// long afterHitOrMiss = System.currentTimeMillis();
		if (inMemoryCache != null) {
			// Log.v(TAG, "memory cache hit: " + key);
			// logger.d("memory cache hit, delay: " + (afterHitOrMiss - start) + " ms");
			return new TileBitmap(State.IN_MEMORY, inMemoryCache);
		} else {
			// Log.v(TAG, "memory cache miss: " + key);
			try {
				if (mDiskCache.containsReadable(key)) {
					mBitmapFetchManager.registerTask(key, listener);
					return new TileBitmap(State.IN_DISK, null);
				} else {
					return new TileBitmap(State.NOT_FOUND, null);
				}
			} catch (IOException e) {
				e.printStackTrace();
				return new TileBitmap(State.NOT_FOUND, null);
			}
		}
	}

	@Override
	public void cancelAllTasks() {
		mBitmapFetchManager.cancelAllTasks();
	}

	// @Override
	// public void updateMemoryCacheSizeInItems(int minSize, int maxSize) {
	// synchronized (mMemoryCache) {
	// int currentSize = mMemoryCache.maxSize();
	// if (currentSize < minSize) {
	// // logger.d("Increasing cache size " + currentSize + " -> " + minSize + " items");
	// Log.d("blabla", "Increasing cache size " + currentSize + " -> " + minSize + " items");
	// mMemoryCache.resize(minSize);
	// } else if (currentSize > maxSize) {
	// Log.d("blabla", "Decreasing cache size " + currentSize + " -> " + maxSize + " items");
	// mMemoryCache.trimToSize(maxSize);
	// mMemoryCache.resize(maxSize);
	// } else {
	// Log.d("blabla", "" + currentSize + " in <" + minSize + ", " + maxSize + ">");
	// }
	// }
	// }

	@Override
	public void updateMemoryCacheSizeInItems(int minSize) {
		synchronized (mMemoryCache) {
			int currentSize = mMemoryCache.maxSize();
			if (currentSize < minSize) {
				logger.d("Increasing cache size " + currentSize + " -> " + minSize + " items");
				mMemoryCache.resize(minSize);
			} else {
				// Log.v(TAG, "" + currentSize + " >= " + minSize);
			}
		}
	}

	@Override
	public void close() {
		try {
			mDiskCache.flush();
		} catch (DiskLruCacheException e) {
			logger.e("Error flushing disk cache");
		} finally {
			try {
				mDiskCache.close();
			} catch (IOException e) {
				logger.e("Error closing disk cache");
			}
		}
	}

}
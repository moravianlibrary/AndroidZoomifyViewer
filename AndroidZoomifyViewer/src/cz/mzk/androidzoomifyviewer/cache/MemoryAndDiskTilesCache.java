package cz.mzk.androidzoomifyviewer.cache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import cz.mzk.androidzoomifyviewer.cache.DiskLruCache.Editor;
import cz.mzk.androidzoomifyviewer.cache.DiskLruCache.Snapshot;
import cz.mzk.androidzoomifyviewer.tiles.TileId;

/**
 * 
 * @author Martin Řehánek
 * 
 */
public class MemoryAndDiskTilesCache extends AbstractTileCache implements TilesCache {

	private static final String TAG = MemoryAndDiskTilesCache.class.getSimpleName();
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	private static final String DISK_CACHE_SUBDIR = "tiles";
	private final Object mDiskCacheInitializationLock = new Object();
	private DiskLruCache mDiskCache = null;
	private boolean mDiskCacheDisabled = false;
	private final LruCache<String, Bitmap> mMemoryCache;

	public MemoryAndDiskTilesCache(Context context, boolean clearCache) {
		mMemoryCache = initMemoryCache();
		initDiskCacheAsync(context, clearCache);
	}

	private LruCache<String, Bitmap> initMemoryCache() {
		// Get max available VM memory, exceeding this amount will throw an
		// OutOfMemory exception. Stored in kilobytes as LruCache takes an
		// int in its constructor.
		int maxMemoryKB = (int) (Runtime.getRuntime().maxMemory() / 1024);
		// Use 1/8th of the available memory for this memory cache.
		final int cacheSizeKB = maxMemoryKB / 8;
		LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(cacheSizeKB) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
				return bitmap.getByteCount() / 1024;
			}
		};
		Log.d(TAG, "in-memory lru cache allocated with " + cacheSizeKB + " kB");
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
				// Log.d(TAG, "assuming mDiskCacheLock: " +
				// Thread.currentThread().toString());
				try {
					File cacheDir = params[0];
					if (cacheDir.exists()) {
						if (clearCache) {
							Log.i(TAG, "clearing tiles disk cache");
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
					Log.w(TAG, "error opening disk cache, disabling");
					disableDiskCache();
					return null;
				} finally {
					Log.d(TAG, "releasing disk cache initialization lock: " + Thread.currentThread().toString());
					mDiskCacheInitializationLock.notifyAll();
				}
			}
		}
	}

	private void disableDiskCache() {
		Log.i(TAG, "disabling disk cache");
		mDiskCacheDisabled = true;
	}

	@Override
	public void storeTile(Bitmap bmp, String zoomifyBaseUrl, TileId tileId) {
		String key = buildKey(zoomifyBaseUrl, tileId);
		storeTileToMemoryCache(key, bmp);
		storeTileToDiskCache(key, bmp);
	}

	private void storeTileToMemoryCache(String key, Bitmap bmp) {
		synchronized (mMemoryCache) {
			// Log.d(TAG, "assuming mMemoryCache lock: " +
			// Thread.currentThread().toString());
			if (mMemoryCache.get(key) == null) {
				Log.d(TAG, "storing to memory cache: " + key);
				mMemoryCache.put(key, bmp);
			} else {
				Log.d(TAG, "already in memory cache: " + key);
			}
			// Log.d(TAG, "releasing mMemoryCache lock: " +
			// Thread.currentThread().toString());
		}
	}

	private void storeTileToDiskCache(String key, Bitmap bmp) {
		waitUntilDiskCacheInitializedOrDisabled();
		Editor edit = null;
		OutputStream out = null;
		try {
			if (!mDiskCacheDisabled) {
				Snapshot fromDiskCache = mDiskCache.get(key);
				if (fromDiskCache != null) {
					Log.d(TAG, "already in disk cache: " + key);
				} else {
					Log.d(TAG, "storing to disk cache: " + key);
					edit = mDiskCache.edit(key);
					if (edit != null) {
						edit.hashCode();
						out = edit.newOutputStream(0);
						byte[] bytes = bitmapToByteArray(bmp);
						out.write(bytes);
						edit.commit();
					} else {
						// jine vlakno se pokousi zapisovat
						// tj. spatne implementovana synchronizace
						Log.e(TAG, key + ": edit allready opened");
					}
				}
			}
			// }
		} catch (IOException e) {
			Log.e(TAG, "failed to store tile to disk cache: " + e.getMessage());
			try {
				if (edit != null) {
					edit.abort();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException e1) {
				Log.e(TAG, "failed to cleanup", e1);
			}
		}
	}

	private byte[] bitmapToByteArray(Bitmap bitmap) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 100, bos);
		return bos.toByteArray();
	}

	@Override
	public Bitmap getTile(String zoomifyBaseUrl, TileId tileId) {
		String key = buildKey(zoomifyBaseUrl, tileId);
		// long start = System.currentTimeMillis();
		Bitmap inMemoryCache = mMemoryCache.get(key);
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
			Bitmap fromDiskCache = getTileFromDiskCache(key);
			// store also to memory cache (nonblocking)
			if (fromDiskCache != null) {
				new StoreTileToMemoryCacheTask(key).execute(fromDiskCache);
			}
			return fromDiskCache;
		}
	}

	private void waitUntilDiskCacheInitializedOrDisabled() {
		try {
			synchronized (mDiskCacheInitializationLock) {
				Log.d(TAG, "assuming disk cache initialization lock: " + Thread.currentThread().toString());
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
			Log.d(TAG, "releasing disk cache initialization lock: " + Thread.currentThread().toString());
		}
	}

	private Bitmap getTileFromDiskCache(String key) {
		waitUntilDiskCacheInitializedOrDisabled();
		try {
			if (!mDiskCacheDisabled) {
				// long start = System.currentTimeMillis();
				Snapshot snapshot = mDiskCache.get(key);
				if (snapshot != null) {
					// long afterHit = System.currentTimeMillis();
					InputStream in = snapshot.getInputStream(0);
					Bitmap stream = BitmapFactory.decodeStream(in);
					// long afterDecoding = System.currentTimeMillis();
					// long retrieval = afterHit - start;
					// long decoding = afterDecoding - afterHit;
					// Log.d(TAG, "disk cache hit, delay: " + (retrieval +
					// decoding) + "ms (retrieval: " + retrieval
					// + "ms, decoding: " + decoding + " ms)");
					// Log.d(TAG, "disk cache hit: " + key + ", delay: " +
					// (retrieval + decoding) + "ms (retrieval: "
					// + retrieval + ", decoding: " + decoding);
					return stream;
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
			Log.i(TAG, "error loading tile from disk cache: " + key, e);
			return null;
		}
	}

	private class StoreTileToMemoryCacheTask extends AsyncTask<Bitmap, Void, Void> {
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

}

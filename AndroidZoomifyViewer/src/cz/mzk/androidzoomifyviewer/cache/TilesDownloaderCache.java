package cz.mzk.androidzoomifyviewer.cache;

import android.util.Log;
import android.util.LruCache;
import cz.mzk.androidzoomifyviewer.tiles.TilesDownloader;

/**
 * @author Martin Řehánek
 * 
 */
public class TilesDownloaderCache {

	private static final String TAG = TilesDownloaderCache.class.getSimpleName();
	private static final int MAX_DOWNLOADERS_IN_CACHE = 10;

	private LruCache<String, TilesDownloader> memoryCache;

	public TilesDownloaderCache() {
		memoryCache = new LruCache<String, TilesDownloader>(MAX_DOWNLOADERS_IN_CACHE) {
			@Override
			protected int sizeOf(String key, TilesDownloader downloader) {
				return 1;
			}
		};
		Log.d(TAG, TilesDownloaderCache.class.getSimpleName() + " allocated for max " + MAX_DOWNLOADERS_IN_CACHE
				+ " objects");
	}

	public TilesDownloader get(String baseUrl) {
		return memoryCache.get(baseUrl);
	}

	public void put(String baseUrl, TilesDownloader downloader) {
		synchronized (memoryCache) {
			if (memoryCache.get(baseUrl) == null) {
				Log.d(TAG, "storing " + baseUrl);
				memoryCache.put(baseUrl, downloader);
			}
		}
	}

}

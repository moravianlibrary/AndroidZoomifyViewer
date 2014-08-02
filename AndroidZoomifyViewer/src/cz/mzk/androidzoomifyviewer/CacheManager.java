package cz.mzk.androidzoomifyviewer;

import android.content.Context;
import cz.mzk.androidzoomifyviewer.cache.MemoryAndDiskTilesCache;
import cz.mzk.androidzoomifyviewer.cache.TilesCache;
import cz.mzk.androidzoomifyviewer.cache.TilesDownloaderCache;

/**
 * @author Martin Řehánek
 * 
 */
public class CacheManager {
	// TODO: podobne dalsi konfiguraci, jako timouty (properties, dlazdice), devMode, 

	private static TilesCache tilesCache;
	private static TilesDownloaderCache downloaderCache;
	private static boolean initialized = false;

	public static void initialize(Context context) {
		if (initialized) {
			throw new IllegalStateException(CacheManager.class.getSimpleName() + " has been already initialized");
		}
		tilesCache = new MemoryAndDiskTilesCache(context);
		downloaderCache = new TilesDownloaderCache();
		initialized = true;
	}

	public static TilesCache getTilesCache() {
		if (!initialized) {
			throw new IllegalStateException(CacheManager.class.getSimpleName() + " has not been initialized");
		}
		return tilesCache;
	}

	public static TilesDownloaderCache getDownloaderCache() {
		if (!initialized) {
			throw new IllegalStateException(CacheManager.class.getSimpleName() + " has not been initialized");
		}
		return downloaderCache;
	}

}

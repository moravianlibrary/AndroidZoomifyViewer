package cz.mzk.androidzoomifyviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import cz.mzk.androidzoomifyviewer.cache.ImagePropertiesCache;
import cz.mzk.androidzoomifyviewer.cache.MemoryAndDiskImagePropertiesCache;
import cz.mzk.androidzoomifyviewer.cache.MemoryAndDiskTilesCache;
import cz.mzk.androidzoomifyviewer.cache.TilesCache;

/**
 * @author Martin Řehánek
 * 
 */
public class CacheManager {
	private static final String TAG = CacheManager.class.getSimpleName();
	public static final int TILE_SIZE_PX = 256;
	private static TilesCache tilesCache;
	private static ImagePropertiesCache imagePropertiesCache;
	private static boolean initialized = false;

	/**
	 * 
	 * @param context
	 * @param clearDiskCache
	 *            whether (disk) cache should be cleared
	 */
	public static void initialize(Context context, boolean clearDiskCache) {
		if (initialized) {
			throw new IllegalStateException(CacheManager.class.getSimpleName() + " has been already initialized");
		}
		int memoryCacheMaxItems = computeMaxTilesOnScreen(context);
		tilesCache = new MemoryAndDiskTilesCache(context, clearDiskCache, memoryCacheMaxItems);
		// tilesCache = new MemoryAndDiskTilesMulticache(context, clearDiskCache);
		// tilesCache = new MemoryTilesCache();
		imagePropertiesCache = new MemoryAndDiskImagePropertiesCache(context, clearDiskCache);
		initialized = true;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private static int computeMaxTilesOnScreen(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = 0;
		int height = 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			Rect rect = new Rect();
			display.getRectSize(rect);
			width = rect.width();
			height = rect.height();
		} else {
			width = display.getWidth();
			height = display.getHeight();
		}
		int columns = (int) Math.ceil(width / TILE_SIZE_PX);
		int rows = (int) Math.ceil(height / TILE_SIZE_PX);
		int result = rows * columns * 2;
		Log.d(TAG, "screen width: " + width + ", height: " + height + ", initial tiles cache size: " + result);
		return result;
	}

	public static TilesCache getTilesCache() {
		if (!initialized) {
			throw new IllegalStateException(CacheManager.class.getSimpleName() + " has not been initialized");
		}
		return tilesCache;
	}

	public static ImagePropertiesCache getImagePropertiesCache() {
		if (!initialized) {
			throw new IllegalStateException(CacheManager.class.getSimpleName() + " has not been initialized");
		}
		return imagePropertiesCache;
	}

}

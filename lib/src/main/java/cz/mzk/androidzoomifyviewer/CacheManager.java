package cz.mzk.androidzoomifyviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import cz.mzk.androidzoomifyviewer.cache.ImagePropertiesCache;
import cz.mzk.androidzoomifyviewer.cache.MemoryAndDiskImagePropertiesCache;
import cz.mzk.androidzoomifyviewer.cache.MemoryAndDiskTilesCache;
import cz.mzk.androidzoomifyviewer.cache.TilesCache;

/**
 * @author Martin Řehánek
 */
public class CacheManager {
    public static final int TILE_SIZE_PX = 256;

    private static final Logger logger = new Logger(CacheManager.class);

    private static TilesCache tilesCache;
    private static ImagePropertiesCache imagePropertiesCache;
    private static boolean initialized = false;

    /**
     * @param context
     * @param clearDiskCacheOnStart whether disk cache should be cleared when application starts
     */
    public static void initialize(Context context, boolean diskCacheEnabled, boolean clearDiskCacheOnStart,
                                  long tileDiskCacheBytes) {
        if (initialized) {
            throw new IllegalStateException(CacheManager.class.getSimpleName() + " has been initialized already");
        }
        logger.i("initializing");
        int memoryCacheMaxItems = computeMaxTilesOnScreen(context);
        tilesCache = new MemoryAndDiskTilesCache(context, memoryCacheMaxItems, diskCacheEnabled, clearDiskCacheOnStart,
                tileDiskCacheBytes);
        // tilesCache = new MemoryAndDiskTilesMulticache(context, clearDiskCache);
        // tilesCache = new MemoryTilesCache();
        imagePropertiesCache = new MemoryAndDiskImagePropertiesCache(context, diskCacheEnabled, clearDiskCacheOnStart);
        initialized = true;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private static int computeMaxTilesOnScreen(Context context) {
        try {
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
            logger.d("screen width: " + width + ", height: " + height + ", initial tiles cache size: " + result);
            return result;
        } catch (UnsupportedOperationException e) {
            //for unit testing only when this service is not available
            int result = 1;
            logger.w("WINDOW_SERVICE not available, returning arbitrary value " + result);
            return result;
        }
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

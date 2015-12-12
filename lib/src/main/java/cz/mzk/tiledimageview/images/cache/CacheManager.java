package cz.mzk.tiledimageview.images.cache;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.WorkerThread;
import android.view.Display;
import android.view.WindowManager;

import cz.mzk.tiledimageview.Logger;

/**
 * @author Martin Řehánek
 */
public class CacheManager {
    public static final int TILE_SIZE_PX = 256;

    private static final Logger LOGGER = new Logger(CacheManager.class);

    private static MetadataCache metadataCache;
    private static TileCache tileCache;
    private static boolean initialized = false;

    /**
     * @param context
     * @param clearDiskCache whether disk cache should be cleared when application starts
     */
    @WorkerThread
    public static void initialize(Context context, boolean diskCacheEnabled, boolean clearDiskCache, long tileDiskCacheBytes) {
        if (initialized) {
            LOGGER.w("already initialized");
        } else {
            LOGGER.i("initializing");
            int memoryCacheMaxItems = computeMaxTilesOnScreen(context) * 2;
            metadataCache = new MetadataCache(context, diskCacheEnabled, clearDiskCache);
            tileCache = new TileCache(context, memoryCacheMaxItems, diskCacheEnabled, tileDiskCacheBytes, clearDiskCache);
            initialized = true;
        }
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
            LOGGER.d("screen width: " + width + ", height: " + height + ", initial tiles cache size: " + result);
            return result;
        } catch (UnsupportedOperationException e) {
            //for unit testing only when this service is not available
            int result = 1;
            LOGGER.w("WINDOW_SERVICE not available, returning arbitrary value " + result);
            return result;
        }
    }

    public static TileCache getTileCache() {
        if (!initialized) {
            throw new IllegalStateException(CacheManager.class.getSimpleName() + " has not been initialized");
        }
        return tileCache;
    }

    public static MetadataCache getMetadataCache() {
        if (!initialized) {
            throw new IllegalStateException(CacheManager.class.getSimpleName() + " has not been initialized");
        }
        return metadataCache;
    }

}

package cz.mzk.androidzoomifyviewer.cache.tmp;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import cz.mzk.androidzoomifyviewer.cache.tmp.TilesCache.State;
import cz.mzk.androidzoomifyviewer.tiles.TileId;

/**
 * This implementation contains different caches for different layers.
 *
 * @author martin
 */
public class MemoryAndDiskTilesMulticache extends AbstractTileCache implements TilesCache {

    private static final String TAG = MemoryAndDiskTilesMulticache.class.getSimpleName();
    private static final int MIN_MEMORY_CACHE_SIZE_KB = 2 * 1024;// 2MB
    private static final int MIN_DISK_CACHE_SIZE_KB = 1024;// 1MB

    private static final String DISK_CACH_DIR_NAME_LEVEL_0 = "tiles0";
    private static final String DISK_CACH_DIR_NAME_LEVEL_1 = "tiles1";
    private static final String DISK_CACH_DIR_NAME_LEVEL_2 = "tiles2";
    private static final String DISK_CACH_DIR_NAME_LEVEL_OTHER = "tilesOther";

    private TilesCache level0;
    private TilesCache level1;
    private TilesCache level2;
    private TilesCache levelOther;

    private State state = State.INITIALIZING;

    public MemoryAndDiskTilesMulticache(Context context, boolean clearCache) {
        this(context, getDefaultMemoryCacheSizeKB(), DEFAULT_DISK_CACHE_SIZE, clearCache);
    }

    public MemoryAndDiskTilesMulticache(Context context, int memoryCacheSize, int diskCacheSize, boolean clearCache) {
        int[] diskSpace = distributeDiskSpace(diskCacheSize);
        int[] memorySpace = distributeMemorySpace(memoryCacheSize);
        level0 = initCache(context, DISK_CACH_DIR_NAME_LEVEL_0, memorySpace[0], diskSpace[0], clearCache);
        level1 = initCache(context, DISK_CACH_DIR_NAME_LEVEL_1, memorySpace[1], diskSpace[1], clearCache);
        level2 = initCache(context, DISK_CACH_DIR_NAME_LEVEL_2, memorySpace[2], diskSpace[2], clearCache);
        levelOther = initCache(context, DISK_CACH_DIR_NAME_LEVEL_OTHER, memorySpace[3], diskSpace[3], clearCache);
        this.state = State.READY;
    }

    private TilesCache initCache(Context context, String diskCachDir, int memoryCacheSize, int diskCacheSize,
                                 boolean clearCache) {
        Log.d(TAG, "initializing cache: memory: " + memoryCacheSize + " KB, disk: " + diskCacheSize + " KB ("
                + diskCachDir + ")");
        if (diskCacheSize != 0 && memoryCacheSize != 0) {// memory+disk
            return new MemoryAndDiskTilesCache(context, memoryCacheSize, diskCacheSize, diskCachDir, clearCache);
        } else if (diskCacheSize == 0 && memoryCacheSize != 0) {// memory only
            return new MemoryTilesCache(memoryCacheSize);
        } else if (diskCacheSize != 0 && memoryCacheSize == 0) {// disk only
            return new DiskTilesCache(context, diskCacheSize, diskCachDir, clearCache);
        } else {
            return null;
        }
    }

    private int[] distributeMemorySpace(int totalSize) {
        int minSize = MIN_MEMORY_CACHE_SIZE_KB;
        int level0 = 0;
        int level1 = 0;
        int level2 = 0;
        int levelOther = 0;

        if (totalSize < minSize) {
            return new int[]{0, 0, 0, 0};
        } else {
            int[] halfs = splitIfBigEnough(totalSize, minSize);
            levelOther = halfs[1];
            int secondHalf = halfs[1];
            if (secondHalf != 0) {
                int[] quarters = splitIfBigEnough(secondHalf, minSize);
                level2 = quarters[0];
                int secondQuarter = quarters[1];
                if (secondQuarter != 0) {
                    int eights[] = splitIfBigEnough(secondQuarter, minSize);
                    level1 = eights[0];
                    int secondEight = eights[1];
                    if (secondEight != 0) {
                        level0 = secondEight;
                    }
                }
            }
            return new int[]{level0, level1, level2, levelOther};
        }
    }

    private int[] splitIfBigEnough(int total, int minSize) {
        int half = total / 2;
        if (half < minSize) {
            return new int[]{total, 0};
        } else {
            return new int[]{half, total - half};
        }
    }

    private int[] distributeDiskSpace(int totalSize) {
        int minSize = MIN_DISK_CACHE_SIZE_KB;
        int level0 = 0;
        int level1 = 0;
        int level2 = 0;
        int levelOther = 0;

        if (totalSize < minSize) {
            return new int[]{0, 0, 0, 0};
        } else {
            int[] halfs = splitIfBigEnough(totalSize, minSize);
            level0 = halfs[1];
            int secondHalf = halfs[1];
            if (secondHalf != 0) {
                int[] quarters = splitIfBigEnough(secondHalf, minSize);
                level1 = quarters[0];
                int secondQuarter = quarters[1];
                if (secondQuarter != 0) {
                    int eights[] = splitIfBigEnough(secondQuarter, minSize);
                    level2 = eights[0];
                    int secondEight = eights[1];
                    if (secondEight != 0) {
                        levelOther = secondEight;
                    }
                }
            }
            return new int[]{level0, level1, level2, levelOther};
        }
    }

    @Override
    public Bitmap getTile(String zoomifyBaseUrl, TileId tileId) {
        switch (tileId.getLayer()) {
            case 0:
                return level0 != null && level0.getState() == State.READY ? level0.getTile(zoomifyBaseUrl, tileId) : null;
            case 1:
                return level1 != null && level1.getState() == State.READY ? level1.getTile(zoomifyBaseUrl, tileId) : null;
            case 2:
                return level2 != null && level2.getState() == State.READY ? level2.getTile(zoomifyBaseUrl, tileId) : null;
            default:
                return levelOther != null && levelOther.getState() == State.READY ? levelOther.getTile(zoomifyBaseUrl,
                        tileId) : null;
        }
    }

    @Override
    public void storeTile(Bitmap tile, String zoomifyBaseUrl, TileId tileId) {
        switch (tileId.getLayer()) {
            case 0:
                if (level0 != null && level0.getState() == State.READY) {
                    level0.storeTile(tile, zoomifyBaseUrl, tileId);
                }
                break;
            case 1:
                if (level1 != null && level1.getState() == State.READY) {
                    level1.storeTile(tile, zoomifyBaseUrl, tileId);
                }
                break;
            case 2:
                if (level2 != null && level2.getState() == State.READY) {
                    level2.storeTile(tile, zoomifyBaseUrl, tileId);
                }
                break;
            default:
                if (levelOther != null && levelOther.getState() == State.READY) {
                    levelOther.storeTile(tile, zoomifyBaseUrl, tileId);
                }
                break;
        }
    }

    @Override
    public State getState() {
        // TODO: wait for initialization
        return state;
    }
}

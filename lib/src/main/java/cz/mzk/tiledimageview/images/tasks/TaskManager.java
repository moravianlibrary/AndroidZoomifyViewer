package cz.mzk.tiledimageview.images.tasks;

import android.content.Context;
import android.support.annotation.UiThread;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import cz.mzk.tiledimageview.Logger;
import cz.mzk.tiledimageview.TiledImageView;
import cz.mzk.tiledimageview.images.ImageManager;
import cz.mzk.tiledimageview.images.TilePositionInPyramid;
import cz.mzk.tiledimageview.images.TiledImageProtocol;

/**
 * This class registers running AsyncTasks in which tiles for single image are downloaded and saved to cache. Also AsyncTask to initialize image metadata.
 * Purpose of this class is to prevent executing multiple task to download same tile. Methods of this class are allways accessed from UI thread,
 * so there's no need for synchronization of internal data (tiles map).
 *
 * @author Martin Řehánek
 */
public class TaskManager {

    public static final int MAX_TASKS_IN_POOL = 40; //for at most 3 TiledImageView instances visible at the same time

    private static final Logger LOGGER = new Logger(TaskManager.class);

    private final ImageManager mImgManager;
    private final Map<TilePositionInPyramid, DeliverTileIntoMemoryCacheTask> mDeliverTileTasks = new HashMap<>();

    private InitImageManagerTask mInitImageManagerTask;
    private StoreMetadataIntoDiskCacheTask mStoreMetadataToDiskCacheTask;
    private InflateTileMemoryCache mInflateTileMemoryCacheTask;
    private int lastITileMemoryCacheInflatedSize = 0;


    public TaskManager(ImageManager imgManager) {
        this.mImgManager = imgManager;
    }


    @UiThread
    public static void enqueueCacheManagerInitialization(Context context, boolean diskCacheEnabled, boolean clearDiskCache, long tileDiskCacheBytes, TaskListener listener) {
        InitCacheManagerTask task = new InitCacheManagerTask(context, diskCacheEnabled, clearDiskCache, tileDiskCacheBytes, listener);
        try {
            LOGGER.i("enqueuing init-cache-manager task");
            task.executeConcurrentIfPossible();
        } catch (RejectedExecutionException e) {
            LOGGER.w("init-cache-manager task task: to many threads in execution pool");
            listener.onCanceled();
        }
    }

    @UiThread
    public void enqueuDeliveringTileIntoMemoryCache(final TilePositionInPyramid tilePosition,
                                                    final String tileImageUrl,
                                                    String cacheKey,
                                                    TiledImageView.TileDownloadSuccessListener successListener,
                                                    TiledImageView.TileDownloadErrorListener errorListener
    ) {
        if (mDeliverTileTasks.size() < MAX_TASKS_IN_POOL) {
            if (!mDeliverTileTasks.containsKey(tilePosition)) {
                //if (true) {
                LOGGER.i(String.format("enqueuing deliver-tile-into-memory-cache task: %s, (total %d)", tileImageUrl, mDeliverTileTasks.size()));
                DeliverTileIntoMemoryCacheTask task = new DeliverTileIntoMemoryCacheTask(tileImageUrl, cacheKey, successListener, errorListener, new TaskListener() {

                    @Override
                    public void onFinished(Object... data) {
                        mDeliverTileTasks.remove(tilePosition);
                        LOGGER.d(String.format("deliver-tile-into-memory-cache task finished: %s", tileImageUrl));
                    }

                    @Override
                    public void onCanceled() {
                        LOGGER.d(String.format("deliver-tile-into-memory-cache task canceled: %s", tileImageUrl));
                        mDeliverTileTasks.remove(tilePosition);
                    }
                });
                mDeliverTileTasks.put(tilePosition, task);
                try {
                    task.executeConcurrentIfPossible();
                } catch (RejectedExecutionException e) {
                    LOGGER.w("deliver-tile-into-memory-cache task: to many threads in execution pool");
                    mDeliverTileTasks.remove(tilePosition);
                }
            } else {
                LOGGER.d(String.format("ignoring tile-download task for '%s' (already in queue)", tileImageUrl));
            }
        } else {
            LOGGER.w(String.format("ignoring tile-download task for '%s' (queue full - %d items)", tileImageUrl, mDeliverTileTasks.size()));
        }
    }

    @UiThread
    public void enqueueMetadataInitialization(TiledImageProtocol protocol, String metadataUrl, TiledImageView.MetadataInitializationListener listener, TiledImageView.MetadataInitializationSuccessListener successListener) {
        if (mInitImageManagerTask == null) {
            LOGGER.i("enqueuing metadata-initialization task");
            mInitImageManagerTask = new InitImageManagerTask(mImgManager, protocol, metadataUrl, listener, successListener, new TaskListener() {

                @Override
                public void onFinished(Object... data) {
                    mInitImageManagerTask = null;
                    LOGGER.d("metadata-initialization task finished");
                    boolean storeMetadataToDisk = (boolean) data[0];
                    if (storeMetadataToDisk) {
                        enqueueMetadataIntoDiskCacheStoreTask((String) data[1], (String) data[2]);
                    }
                }

                @Override
                public void onCanceled() {
                    mInitImageManagerTask = null;
                    LOGGER.d("metadata-initialization task canceled");
                }
            });
            try {
                mInitImageManagerTask.executeConcurrentIfPossible();
            } catch (RejectedExecutionException e) {
                LOGGER.w("to many threads in execution pool");
                mInitImageManagerTask = null;
                listener.onCannotExecuteMetadataInitialization(metadataUrl);
            }
        } else {
            LOGGER.d("ignoring metadata-initialization task - already in queue");
        }
    }

    private void enqueueMetadataIntoDiskCacheStoreTask(String cacheKey, String metadata) {
        if (mStoreMetadataToDiskCacheTask == null) {
            LOGGER.i("enqueuing store-metadata-into-disk-cache task");
            mStoreMetadataToDiskCacheTask = new StoreMetadataIntoDiskCacheTask(cacheKey, metadata, new TaskListener() {
                @Override
                public void onFinished(Object... data) {
                    mStoreMetadataToDiskCacheTask = null;
                    LOGGER.d("store-metadata-into-disk-cache task finished");
                }

                @Override
                public void onCanceled() {
                    LOGGER.d("store-metadata-into-disk-cache task canceled");
                    mStoreMetadataToDiskCacheTask = null;
                }
            });
            try {
                mStoreMetadataToDiskCacheTask.executeConcurrentIfPossible();
            } catch (RejectedExecutionException e) {
                LOGGER.w("to many threads in execution pool");
                mStoreMetadataToDiskCacheTask = null;
            }
        }
    }

    @UiThread
    public void cancelAllTasks() {
        LOGGER.d("canceling all tasks");
        if (mInitImageManagerTask != null) {
            mInitImageManagerTask.cancel(false);
        }
        if (mStoreMetadataToDiskCacheTask != null) {
            mStoreMetadataToDiskCacheTask.cancel(false);
        }
        if (mInflateTileMemoryCacheTask != null) {
            mInflateTileMemoryCacheTask.cancel(false);
        }
        for (DeliverTileIntoMemoryCacheTask task : mDeliverTileTasks.values()) {
            task.cancel(false);
        }
    }

    @UiThread
    public boolean cancel(TilePositionInPyramid tilePositionInPyramid) {
        DeliverTileIntoMemoryCacheTask task = mDeliverTileTasks.get(tilePositionInPyramid);
        if (task != null) {
            LOGGER.d(String.format("canceling tile-download task for %s", tilePositionInPyramid.toString()));
            task.cancel(false);
            return true;
        } else {
            return false;
        }
    }

    @UiThread
    public Set<TilePositionInPyramid> getAllDeliverTileTaksIds() {
        return mDeliverTileTasks.keySet();
    }

    public void enqueueTilesMemoryCacheInflation(final int newMaxSize) {
        if (lastITileMemoryCacheInflatedSize > newMaxSize) {
            //ignore
        } else {
            if (mInflateTileMemoryCacheTask == null) {
                //alright, enqueue
                InflateTileMemoryCache task = new InflateTileMemoryCache(newMaxSize, new TaskListener() {
                    @Override
                    public void onFinished(Object... data) {
                        lastITileMemoryCacheInflatedSize = newMaxSize;
                        mInflateTileMemoryCacheTask = null;
                    }

                    @Override
                    public void onCanceled() {
                        mInflateTileMemoryCacheTask = null;
                    }
                });
                mInflateTileMemoryCacheTask = task;
                try {
                    task.executeConcurrentIfPossible();
                } catch (RejectedExecutionException e) {
                    LOGGER.w("to many threads in execution pool");
                    mInflateTileMemoryCacheTask = null;
                }
            } else {
                //task is running or scheduled. Possibly lower maxSize, but doesn't matter, soon another task will appear
            }
        }
    }


    public static interface TaskListener {
        @UiThread
        void onFinished(Object... data);

        void onCanceled();

    }
}

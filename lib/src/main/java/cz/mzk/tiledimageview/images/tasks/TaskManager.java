package cz.mzk.tiledimageview.images.tasks;

import android.content.Context;
import android.support.annotation.UiThread;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import cz.mzk.tiledimageview.Logger;
import cz.mzk.tiledimageview.TiledImageView;
import cz.mzk.tiledimageview.TiledImageView.MetadataInitializationListener;
import cz.mzk.tiledimageview.TiledImageView.MetadataInitializationSuccessListener;
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

    //hight task pool size will cause taks from other TileImage instances (typically InitImageManagerTask) to wait to long
    public static final int MAX_TASKS_IN_POOL = 10;

    private static final Logger LOGGER = new Logger(TaskManager.class);

    private final Map<TilePositionInPyramid, DeliverTileIntoMemoryCacheTask> mDeliverTileTasks = new HashMap<>();

    private DeliverMetadataTask mDeliverMetadataTask;
    private InflateTileMemoryCache mInflateTileMemoryCacheTask;
    private int lastITileMemoryCacheInflatedSize = 0;


    public TaskManager() {
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
    public void enqueueMetadataDelivery(TiledImageProtocol protocol, String metadataUrl, String cacheKey,
                                        MetadataInitializationSuccessListener successListener, MetadataInitializationListener listener) {
        if (mDeliverMetadataTask == null) {
            LOGGER.i("enqueuing deliver-metadata task");
            mDeliverMetadataTask = new DeliverMetadataTask(protocol, metadataUrl, cacheKey, listener, successListener, new TaskListener() {

                @Override
                public void onFinished(Object... data) {
                    mDeliverMetadataTask = null;
                    LOGGER.d("deliver-metadata task finished");
                }

                @Override
                public void onCanceled() {
                    mDeliverMetadataTask = null;
                    LOGGER.d("deliver-metadata task canceled");
                }
            });
            try {
                mDeliverMetadataTask.executeConcurrentIfPossible();
            } catch (RejectedExecutionException e) {
                LOGGER.d("to many threads in execution pool");
                mDeliverMetadataTask = null;
                listener.onCannotExecuteMetadataInitialization(metadataUrl);
            }
        } else {
            LOGGER.d("ignoring deliver-metadata task - already in queue");
        }
    }


    @UiThread
    public void enqueueTileDeliveryIntoMemoryCache(final TilePositionInPyramid tilePosition, final String tileImageUrl, String cacheKey,
                                                   TiledImageView.TileDownloadSuccessListener successListener,
                                                   TiledImageView.TileDownloadErrorListener errorListener
    ) {
        if (mDeliverTileTasks.size() < MAX_TASKS_IN_POOL) {
            if (!mDeliverTileTasks.containsKey(tilePosition)) {
                //if (true) {
                LOGGER.i(String.format("enqueuing deliver-tile-into-memory-cache task: %s, (total %d)", tileImageUrl, mDeliverTileTasks.size() + 1));
                DeliverTileIntoMemoryCacheTask task = new DeliverTileIntoMemoryCacheTask(tileImageUrl, cacheKey, successListener, errorListener, new TaskListener() {

                    @Override
                    public void onFinished(Object... data) {
                        LOGGER.d(String.format("deliver-tile-into-memory-cache task finished: %s", tileImageUrl));
                        mDeliverTileTasks.remove(tilePosition);
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
            LOGGER.d(String.format("ignoring tile-download task for '%s' (queue full - %d items)", tileImageUrl, mDeliverTileTasks.size()));
        }
    }


    @UiThread
    public void cancelAllTasks() {
        LOGGER.d("canceling all tasks");
        if (mDeliverMetadataTask != null) {
            mDeliverMetadataTask.cancel(false);
        }
        if (mInflateTileMemoryCacheTask != null) {
            mInflateTileMemoryCacheTask.cancel(false);
        }
        for (DeliverTileIntoMemoryCacheTask task : mDeliverTileTasks.values()) {
            task.cancel(false);
        }
    }

    @UiThread
    public boolean cancelTileDelivery(TilePositionInPyramid tilePositionInPyramid) {
        DeliverTileIntoMemoryCacheTask task = mDeliverTileTasks.get(tilePositionInPyramid);
        if (task != null) {
            //LOGGER.d(String.format("canceling tile-download task for %s", tilePositionInPyramid.toString()));
            task.cancel(false);
            return true;
        } else {
            return false;
        }
    }

    @UiThread
    public Set<TilePositionInPyramid> getIdsOfAllTileDeliveryTasks() {
        return mDeliverTileTasks.keySet();
    }

    public void enqueueTilesMemoryCacheInflation(final int newMaxSize) {
        if (lastITileMemoryCacheInflatedSize >= newMaxSize) {
            //ignore
            //LOGGER.d(String.format("ignoring inflate-tiles-memory-cache task (%d>=%d)", lastITileMemoryCacheInflatedSize, newMaxSize));
        } else {
            if (mInflateTileMemoryCacheTask == null) {
                LOGGER.i(String.format("enqueuing inflate-tiles-memory-cache task (oldSize=%d,newSize=%d)", lastITileMemoryCacheInflatedSize, newMaxSize));
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
                    LOGGER.d("to many threads in execution pool");
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

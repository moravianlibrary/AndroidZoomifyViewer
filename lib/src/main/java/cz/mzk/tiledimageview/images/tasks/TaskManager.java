package cz.mzk.tiledimageview.images.tasks;

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

    public static final int MAX_TASKS_IN_POOL = 40;

    private static final Logger LOGGER = new Logger(TaskManager.class);

    private final ImageManager mImgManager;
    private final Map<TilePositionInPyramid, DeliverTileIntoMemoryCacheTask> mTileDownloadTasks = new HashMap<>();
    //private final Map<TilePositionInPyramid, FetchTileTask> mFetchTileTasks = new HashMap<>();

    private InitImageManagerTask mInitMetadataTask;
    private StoreMetadataIntoDiskCacheTask mStoreMetadataToDiskCacheTask;
    private InflateTileMemoryCache mInflateTileMemoryCacheTask;
    private int lastITileMemoryCacheInflated = 0;


    public TaskManager(ImageManager imgManager) {
        this.mImgManager = imgManager;
    }

    @UiThread
    public void enqueuDeliveringTileIntoMemoryCache(final TilePositionInPyramid tilePosition,
                                                    final String tileImageUrl,
                                                    String cacheKey,
                                                    TiledImageView.TileDownloadSuccessListener successListener,
                                                    TiledImageView.TileDownloadErrorListener errorListener
    ) {
        if (mTileDownloadTasks.size() < MAX_TASKS_IN_POOL) {
            if (!mTileDownloadTasks.containsKey(tilePosition)) {
                //if (true) {
                LOGGER.i(String.format("enqueuing deliver-tile-into-memory-cache task: %s, (total %d)", tileImageUrl, mTileDownloadTasks.size()));
                DeliverTileIntoMemoryCacheTask task = new DeliverTileIntoMemoryCacheTask(tileImageUrl, cacheKey, successListener, errorListener, new TaskHandler() {

                    @Override
                    public void onFinished(Object... data) {
                        mTileDownloadTasks.remove(tilePosition);
                        LOGGER.d(String.format("deliver-tile-into-memory-cache task finished: %s", tileImageUrl));
                    }

                    @Override
                    public void onCanceled() {
                        LOGGER.d(String.format("deliver-tile-into-memory-cache task canceled: %s", tileImageUrl));
                        mTileDownloadTasks.remove(tilePosition);
                    }
                });
                mTileDownloadTasks.put(tilePosition, task);
                try {
                    task.executeConcurrentIfPossible();
                } catch (RejectedExecutionException e) {
                    LOGGER.w("deliver-tile-into-memory-cache task: to many threads in execution pool");
                    // TODO: 10.12.15 dat vedet nahoru pres handlery?
                    mTileDownloadTasks.remove(tilePosition);
                }
            } else {
                LOGGER.d(String.format("ignoring tile-download task for '%s' (already in queue)", tileImageUrl));
            }
        } else {
            LOGGER.w(String.format("ignoring tile-download task for '%s' (queue full - %d items)", tileImageUrl, mTileDownloadTasks.size()));
        }
    }

    @UiThread
    public void enqueueMetadataInitializationTask(TiledImageProtocol protocol, String metadataUrl, TiledImageView.MetadataInitializationHandler handler, TiledImageView.MetadataInitializationSuccessListener successListener) {
        if (mInitMetadataTask == null) {
            LOGGER.i("enqueuing metadata-initialization task");
            mInitMetadataTask = new InitImageManagerTask(mImgManager, protocol, metadataUrl, handler, successListener, new TaskHandler() {

                @Override
                public void onFinished(Object... data) {
                    mInitMetadataTask = null;
                    LOGGER.d("metadata-initialization task finished");
                    boolean storeMetadataToDisk = (boolean) data[0];
                    if (storeMetadataToDisk) {
                        //todo: naplanuj task, uloz ho
                        enqueueMetadataIntoDiskCacheStoreTask((String) data[1], (String) data[2]);
                    }
                }

                @Override
                public void onCanceled() {
                    mInitMetadataTask = null;
                    LOGGER.d("metadata-initialization task canceled");
                }
            });
            try {
                mInitMetadataTask.executeConcurrentIfPossible();
            } catch (RejectedExecutionException e) {
                LOGGER.w("to many threads in execution pool");
                // TODO: 10.12.15 dat vedet nahoru pres handlery, nejspis novou metodou
                mInitMetadataTask = null;
            }
        } else {
            LOGGER.d("ignoring metadata-initialization task - already in queue");
        }
    }

    private void enqueueMetadataIntoDiskCacheStoreTask(String cacheKey, String metadata) {
        if (mStoreMetadataToDiskCacheTask == null) {
            LOGGER.i("enqueuing store-metadata-into-disk-cache task");
            mStoreMetadataToDiskCacheTask = new StoreMetadataIntoDiskCacheTask(cacheKey, metadata, new TaskHandler() {
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
        if (mInitMetadataTask != null) {
            mInitMetadataTask.cancel(false);
        }
        if (mStoreMetadataToDiskCacheTask != null) {
            mStoreMetadataToDiskCacheTask.cancel(false);
        }
        if (mInflateTileMemoryCacheTask != null) {
            mInflateTileMemoryCacheTask.cancel(false);
        }
        for (DeliverTileIntoMemoryCacheTask task : mTileDownloadTasks.values()) {
            task.cancel(false);
        }
    }

    @UiThread
    public boolean cancel(TilePositionInPyramid id) {
        DeliverTileIntoMemoryCacheTask task = mTileDownloadTasks.get(id);
        if (task != null) {
            LOGGER.d(String.format("canceling tile-download task for %s", id.toString()));
            task.cancel(false);
            return true;
        } else {
            return false;
        }
    }

    @UiThread
    public Set<TilePositionInPyramid> getAllTileDownloadTaskIds() {
        return mTileDownloadTasks.keySet();
    }

    public void enqueueTilesMemoryCacheInflation(final int newMaxSize) {
        if (lastITileMemoryCacheInflated > newMaxSize) {
            //ignore
        } else {
            if (mInflateTileMemoryCacheTask == null) {
                //alright, enqueue
                InflateTileMemoryCache task = new InflateTileMemoryCache(newMaxSize, new TaskHandler() {
                    @Override
                    public void onFinished(Object... data) {
                        lastITileMemoryCacheInflated = newMaxSize;
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


    public static interface TaskHandler {
        @UiThread
        void onFinished(Object... data);

        void onCanceled();

    }
}

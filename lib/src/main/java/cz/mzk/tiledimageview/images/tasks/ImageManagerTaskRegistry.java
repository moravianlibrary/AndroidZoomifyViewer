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

/**
 * This class registers running AsyncTasks in which tiles for single image are downloaded and saved to cache. Also AsyncTask to initialize image metadata.
 * Purpose of this class is to prevent executing multiple task to download same tile. Methods of this class are allways accessed from UI thread,
 * so there's no need for synchronization of internal data (tiles map).
 *
 * @author Martin Řehánek
 */
public class ImageManagerTaskRegistry {

    public static final int MAX_TASKS_IN_POOL = 30;

    private static final Logger LOGGER = new Logger(ImageManagerTaskRegistry.class);

    private final ImageManager mImgManager;
    private final Map<TilePositionInPyramid, DownloadAndSaveTileTask> mTileDownloadTasks = new HashMap<>();
    private InitImageManagerTask mInitMetadataTask;
    private StoreMetadataIntoDiskCacheTask mStoreToDiskCacheTask;


    public ImageManagerTaskRegistry(ImageManager imgManager) {
        this.mImgManager = imgManager;
    }

    @UiThread
    public void enqueueTileDownloadTask(final TilePositionInPyramid tilePosition,
                                        String tileImageUrl,
                                        TiledImageView.TileDownloadErrorListener errorListener,
                                        TiledImageView.TileDownloadSuccessListener successListener) {
        if (mTileDownloadTasks.size() < MAX_TASKS_IN_POOL) {
            if (!mTileDownloadTasks.containsKey(tilePosition)) {
                //if (true) {
                LOGGER.i(String.format("enqueuing tile-download task: %s, (total %d)", tileImageUrl, mTileDownloadTasks.size()));
                DownloadAndSaveTileTask task = new DownloadAndSaveTileTask(tileImageUrl, errorListener, successListener, new TaskHandler() {

                    @Override
                    public void onFinished(Object... data) {
                        mTileDownloadTasks.remove(tilePosition);
                    }

                    @Override
                    public void onCanceled() {
                        mTileDownloadTasks.remove(tilePosition);
                    }
                });
                mTileDownloadTasks.put(tilePosition, task);
                try {
                    task.executeConcurrentIfPossible();
                } catch (RejectedExecutionException e) {
                    LOGGER.w("to many threads in execution pool");
                    // TODO: 10.12.15 dat vedet nahoru pres handlery
                    mTileDownloadTasks.remove(tilePosition);
                }
            } else {
                LOGGER.d(String.format("ignoring tile-download task for '%s' (already in queue)", tileImageUrl));
            }
        } else {
            LOGGER.w(String.format("ignoring tile-download task for '%s' (queue full - %d items)", tileImageUrl, mTileDownloadTasks.size()));
        }
    }

    /*private boolean execute(ConcurrentAsyncTask<Object, Object, Object> task) {
        try {
            task.executeConcurrentIfPossible();
            return true;
        } catch (RejectedExecutionException e) {
            LOGGER.w("to many threads in execution pool");
            return false;
        }
    }*/

    @UiThread
    public void enqueueMetadataInitializationTask(String metadataUrl, TiledImageView.MetadataInitializationHandler handler, TiledImageView.MetadataInitializationSuccessListener successListener) {
        if (mInitMetadataTask == null) {
            LOGGER.i("enqueuing metadata-initialization task");
            mInitMetadataTask = new InitImageManagerTask(mImgManager, metadataUrl, handler, successListener, new TaskHandler() {

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
        if (mStoreToDiskCacheTask == null) {
            LOGGER.i("enqueuing store-metadata-into-disk-cache task");
            mStoreToDiskCacheTask = new StoreMetadataIntoDiskCacheTask(cacheKey, metadata, new TaskHandler() {
                @Override
                public void onFinished(Object... data) {
                    mStoreToDiskCacheTask = null;
                    LOGGER.d("store-metadata-into-disk-cache task finished");
                }

                @Override
                public void onCanceled() {
                    LOGGER.d("store-metadata-into-disk-cache task canceled");
                    mStoreToDiskCacheTask = null;
                }
            });
            try {
                mStoreToDiskCacheTask.executeConcurrentIfPossible();
            } catch (RejectedExecutionException e) {
                LOGGER.w("to many threads in execution pool");
                mStoreToDiskCacheTask = null;
            }
        }
    }

    @UiThread
    public void cancelAllTasks() {
        LOGGER.d("canceling all tasks");
        if (mInitMetadataTask != null) {
            mInitMetadataTask.cancel(false);
        }
        if (mStoreToDiskCacheTask != null) {
            mStoreToDiskCacheTask.cancel(false);
        }
        for (DownloadAndSaveTileTask task : mTileDownloadTasks.values()) {
            task.cancel(false);
        }
    }

    @UiThread
    public boolean cancel(TilePositionInPyramid id) {
        DownloadAndSaveTileTask task = mTileDownloadTasks.get(id);
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


    public static interface TaskHandler {
        @UiThread
        void onFinished(Object... data);

        void onCanceled();

    }
}

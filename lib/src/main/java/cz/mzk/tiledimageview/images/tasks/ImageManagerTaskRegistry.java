package cz.mzk.tiledimageview.images.tasks;

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

    public ImageManagerTaskRegistry(ImageManager imgManager) {
        this.mImgManager = imgManager;
    }

    public void enqueueTileDownloadTask(final TilePositionInPyramid tilePosition, String tileImageUrl, TiledImageView.TileDownloadErrorListener errorListener, TiledImageView.TileDownloadSuccessListener successListener) {
        if (mTileDownloadTasks.size() < MAX_TASKS_IN_POOL) {
            if (!mTileDownloadTasks.containsKey(tilePosition)) {
                //if (true) {
                LOGGER.d(String.format("enqueuing tile-download task: %s, (total %d)", tileImageUrl, mTileDownloadTasks.size()));
                DownloadAndSaveTileTask task = new DownloadAndSaveTileTask(tileImageUrl, errorListener, successListener, new TaskFinishedListener() {

                    @Override
                    public void onTaskFinished() {
                        mTileDownloadTasks.remove(tilePosition);
                    }
                });
                mTileDownloadTasks.put(tilePosition, task);
                try {
                    task.executeConcurrentIfPossible();
                } catch (RejectedExecutionException e) {
                    LOGGER.w("to many threads in execution pool");
                    mTileDownloadTasks.remove(tilePosition);
                }
            } else {
                LOGGER.d(String.format("ignoring tile-download task for '%s' (already in queue)", tileImageUrl));
            }
        } else {
            LOGGER.d(String.format("ignoring tile-download task for '%s' (queue full - %d items)", tileImageUrl, mTileDownloadTasks.size()));
        }
    }

    public void enqueueMetadataInitializationTask(String metadataUrl, TiledImageView.MetadataInitializationHandler handler, TiledImageView.MetadataInitializationSuccessListener successListener) {
        if (mInitMetadataTask == null) {
            LOGGER.d("enqueuing metadata-initialization task");
            mInitMetadataTask = new InitImageManagerTask(mImgManager, metadataUrl, handler, successListener, new TaskFinishedListener() {

                @Override
                public void onTaskFinished() {
                    mInitMetadataTask = null;
                }
            });
            try {
                mInitMetadataTask.executeConcurrentIfPossible();
            } catch (RejectedExecutionException e) {
                LOGGER.w("to many threads in execution pool");
                mInitMetadataTask = null;
            }
        } else {
            LOGGER.d("ignoring metadata-initialization task - already in queue");
        }
    }

    public void cancelAllTasks() {
        LOGGER.d("canceling all tasks");
        if (mInitMetadataTask != null) {
            mInitMetadataTask.cancel(false);
            mInitMetadataTask = null;
        }
        for (DownloadAndSaveTileTask task : mTileDownloadTasks.values()) {
            task.cancel(false);
        }
    }

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

    public Set<TilePositionInPyramid> getAllTileDownloadTaskIds() {
        return mTileDownloadTasks.keySet();
    }


    public static interface TaskFinishedListener {
        void onTaskFinished();
    }
}

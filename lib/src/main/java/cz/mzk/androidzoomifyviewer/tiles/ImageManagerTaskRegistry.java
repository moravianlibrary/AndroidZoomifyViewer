package cz.mzk.androidzoomifyviewer.tiles;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;

/**
 * This class registers running AsyncTasks in which tiles for single image are downloaded and saved to cache. Also AsyncTask to initialize image metadata.
 * Purpose of this class is to prevent executing multiple task to download same tile. Methods of this class are allways accessed from UI thread,
 * so there's no need for synchronization of internal data (tiles map).
 *
 * @author Martin Řehánek
 */
public class ImageManagerTaskRegistry {

    public static final int MAX_TASKS_IN_POOL = 50;

    private static final Logger LOGGER = new Logger(ImageManagerTaskRegistry.class);

    private final ImageManager mImgManager;
    private final Map<TilePositionInPyramid, DownloadAndSaveTileTask> mTileDownloadTasks = new HashMap<>();
    private InitImageManagerTask mInitMetadataTask;

    public ImageManagerTaskRegistry(ImageManager imgManager) {
        this.mImgManager = imgManager;
    }

    public void enqueueTileDownloadTask(final TilePositionInPyramid tilePositionInPyramid, String mZoomifyBaseUrl, TiledImageView.TileDownloadErrorListener errorListener, TiledImageView.TileDownloadSuccessListener successListener) {
        if (mTileDownloadTasks.size() < MAX_TASKS_IN_POOL) {
            if (!mTileDownloadTasks.containsKey(tilePositionInPyramid)) {
                LOGGER.d("enqueuing tile-download task for " + tilePositionInPyramid + ": (total " + mTileDownloadTasks.size() + ")");
                // TODO: 7.12.15 proc se posila zoomfyBaseUrl?
                DownloadAndSaveTileTask task = new DownloadAndSaveTileTask(mImgManager, mZoomifyBaseUrl, tilePositionInPyramid, errorListener, successListener, new TaskFinishedListener() {

                    @Override
                    public void onTaskFinished() {
                        mTileDownloadTasks.remove(tilePositionInPyramid);
                    }
                });
                mTileDownloadTasks.put(tilePositionInPyramid, task);
                task.executeConcurrentIfPossible();
            } else {
                LOGGER.d("ignoring tile-download task for " + tilePositionInPyramid + ", already in queue");
            }
        } else {
            LOGGER.d("ignoring tile-download task for " + tilePositionInPyramid + ", queue full (" + mTileDownloadTasks.size() + " mTileDownloadTasks)");
        }
    }

    public void enqueueMetadataInitializationTask(TiledImageView.MetadataInitializationHandler handler, TiledImageView.MetadataInitializationSuccessListener successListener) {
        if (mInitMetadataTask == null) {
            LOGGER.d("enqueuing metadata-initialization task");
            mInitMetadataTask = new InitImageManagerTask(mImgManager, handler, successListener, new TaskFinishedListener() {

                @Override
                public void onTaskFinished() {
                    mInitMetadataTask = null;
                }
            });
            mInitMetadataTask.executeConcurrentIfPossible();
        } else {
            LOGGER.d("ignoring metadata-initialization task - already in queue");
        }
    }

    public void cancelAllTasks() {
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

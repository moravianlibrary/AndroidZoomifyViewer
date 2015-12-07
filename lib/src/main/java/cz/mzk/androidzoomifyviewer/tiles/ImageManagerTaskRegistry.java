package cz.mzk.androidzoomifyviewer.tiles;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cz.mzk.androidzoomifyviewer.Logger;

/**
 * This class registers running AsynctTasks in which tiles for single image are downloaded and saved to cache. Purpose of this
 * class is to prevent executing multiple tasks to download same tile. Methods of this class are allways accessed from UI thread,
 * so there's no need for synchronization of internal data (tiles map).
 *
 * @author Martin Řehánek
 */
public class ImageManagerTaskRegistry {

    public static final int MAX_TASKS_IN_POOL = 50;

    private static final Logger logger = new Logger(ImageManagerTaskRegistry.class);

    private final Map<TilePositionInPyramid, DownloadAndSaveTileTask> tasks = new HashMap<TilePositionInPyramid, DownloadAndSaveTileTask>();
    private final ImageManager imgManager;
    private InitImageManagerTask mInitTask;

    public ImageManagerTaskRegistry(ImageManager imgManager) {
        this.imgManager = imgManager;
    }

    public void registerTask(final TilePositionInPyramid tilePositionInPyramid, String mZoomifyBaseUrl, TileDownloadHandler handler) {
        if (tasks.size() < MAX_TASKS_IN_POOL) {
            boolean registered = tasks.containsKey(tilePositionInPyramid);
            if (!registered) {
                // TODO: 7.12.15 proc se posila zoomfyBaseUrl?
                DownloadAndSaveTileTask task = new DownloadAndSaveTileTask(imgManager, mZoomifyBaseUrl, tilePositionInPyramid, handler, new TaskFinishedListener() {

                    @Override
                    public void onTaskFinished() {
                        tasks.remove(tilePositionInPyramid);
                    }
                });
                tasks.put(tilePositionInPyramid, task);
                logger.v("  registered task for " + tilePositionInPyramid + ": (total " + tasks.size() + ")");
                task.executeConcurrentIfPossible();
            } else {
                // logger.v( " ignored task registration task for " + tilePositionInPyramid + ": (total " + tasks.size() + ")");
            }
        } else {
            // logger.v( "registration ignored: to many tasks: " + tilePositionInPyramid + ": (total " + tasks.size() + ")");
        }
    }

    public void enqueueInitializationTask(MetadataInitializationHandler handler) {
        mInitTask = new InitImageManagerTask(imgManager, handler, new TaskFinishedListener() {

            @Override
            public void onTaskFinished() {
                mInitTask = null;
            }
        });
        mInitTask.executeConcurrentIfPossible();
    }


    public void cancelAllTasks() {
        if (mInitTask != null) {
            mInitTask.cancel(false);
            mInitTask = null;
        }
        for (DownloadAndSaveTileTask task : tasks.values()) {
            task.cancel(false);
        }
    }

    public void unregisterTask(TilePositionInPyramid tilePositionInPyramid) {
        tasks.remove(tilePositionInPyramid);
        logger.v("unregistration performed: " + tilePositionInPyramid + ": (total " + tasks.size() + ")");
    }

    public boolean cancel(TilePositionInPyramid id) {
        DownloadAndSaveTileTask task = tasks.get(id);
        if (task != null) {
            task.cancel(false);
            return true;
        } else {
            return false;
        }
    }

    public Set<TilePositionInPyramid> getAllTaskTileIds() {
        return tasks.keySet();
    }

    public static interface TaskFinishedListener {
        void onTaskFinished();
    }
}

package cz.mzk.androidzoomifyviewer.tiles;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.tiles.DownloadAndSaveTileTask.TileDownloadResultHandler;

/**
 * This class registers running AsynctTasks in which tiles for single image are downloaded and saved to cache. Purpose of this
 * class is to prevent executing multiple tasks to download same tile. Methods of this class are allways accessed from UI thread,
 * so there's no need for synchronization of internal data (tiles map).
 * 
 * @author Martin Řehánek
 * 
 */
public class DownloadAndSaveTileTasksRegistry {

	public static final int MAX_TASKS_IN_POOL = 10;

	private static final Logger logger = new Logger(DownloadAndSaveTileTasksRegistry.class);

	private final Map<TileId, DownloadAndSaveTileTask> tasks = new HashMap<TileId, DownloadAndSaveTileTask>();
	private final TilesDownloader downloader;

	public DownloadAndSaveTileTasksRegistry(TilesDownloader downloader) {
		this.downloader = downloader;
	}

	public void registerTask(TileId tileId, String mZoomifyBaseUrl, TileDownloadResultHandler handler) {
		if (tasks.size() < MAX_TASKS_IN_POOL) {
			boolean registered = tasks.containsKey(tileId);
			if (!registered) {
				DownloadAndSaveTileTask task = new DownloadAndSaveTileTask(downloader, mZoomifyBaseUrl, tileId, handler);
				tasks.put(tileId, task);
				logger.v("  registered task for " + tileId + ": (total " + tasks.size() + ")");
				task.executeConcurrentIfPossible();
			} else {
				// logger.v( " ignored task registration task for " + tileId + ": (total " + tasks.size() + ")");
			}
		} else {
			// logger.v( "registration ignored: to many tasks: " + tileId + ": (total " + tasks.size() + ")");
		}
	}

	public void cancelAllTasks() {
		for (DownloadAndSaveTileTask task : tasks.values()) {
			task.cancel(false);
		}
	}

	public void unregisterTask(TileId tileId) {
		tasks.remove(tileId);
		logger.v("unregistration performed: " + tileId + ": (total " + tasks.size() + ")");
	}

	public boolean cancel(TileId id) {
		DownloadAndSaveTileTask task = tasks.get(id);
		if (task != null) {
			task.cancel(false);
			return true;
		} else {
			return false;
		}
	}

	public Set<TileId> getAllTaskTileIds() {
		return tasks.keySet();
	}
}

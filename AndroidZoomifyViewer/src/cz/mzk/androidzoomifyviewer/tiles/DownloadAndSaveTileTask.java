package cz.mzk.androidzoomifyviewer.tiles;

import android.graphics.Bitmap;
import android.util.Log;
import cz.mzk.androidzoomifyviewer.ConcurrentAsyncTask;
import cz.mzk.androidzoomifyviewer.cache.TilesCache;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;

/**
 * @author Martin Řehánek
 * 
 */
public class DownloadAndSaveTileTask extends ConcurrentAsyncTask<Void, Void, Void> {

	private static final String TAG = DownloadAndSaveTileTask.class.getSimpleName();
	private final TilesDownloader downloader;
	private final String zoomifyBaseUrl;
	private final TileId tileId;
	private Exception error;
	private TilesCache tilesCache;
	private TiledImageView pageView;

	public DownloadAndSaveTileTask(TilesDownloader downloader, String zoomifyBaseUrl, TileId tileId,
			TilesCache tilesCache, TiledImageView pageView) {
		this.downloader = downloader;
		this.zoomifyBaseUrl = zoomifyBaseUrl;
		this.tileId = tileId;
		this.tilesCache = tilesCache;
		this.pageView = pageView;
	}

	@Override
	protected void onPreExecute() {
		downloader.getTaskRegistry().registerTask(this, tileId);
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			if (!isCancelled()) {
				Bitmap tile = downloader.downloadTile(tileId);
				if (!isCancelled()) {
					tilesCache.storeTile(tile, zoomifyBaseUrl, tileId);
				} else {
					Log.d(TAG, "tile downloading canceled before saving data");
				}
			} else {
				Log.d(TAG, "tile downloading canceled before download started");
			}
		} catch (Exception e) {
			error = e;
		} finally {
			Log.d(TAG, "ending downloader task");
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void params) {
		//TODO: callback
		Log.d(TAG, "finished, canceled=" + isCancelled());
		downloader.getTaskRegistry().unregisterTask(tileId);
		if (error != null) {
			Log.e(TAG, "error downloading tile", error);
			error.printStackTrace();
		} else {
			Log.d(TAG, "tile downloaded", error);
			pageView.invalidate();
		}
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		downloader.getTaskRegistry().unregisterTask(tileId);
	}
}

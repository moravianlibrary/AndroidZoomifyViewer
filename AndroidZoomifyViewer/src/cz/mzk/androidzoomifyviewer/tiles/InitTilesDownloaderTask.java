package cz.mzk.androidzoomifyviewer.tiles;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import cz.mzk.androidzoomifyviewer.ConcurrentAsyncTask;
import cz.mzk.androidzoomifyviewer.tiles.TilesDownloader.ImageServerResponseException;
import cz.mzk.androidzoomifyviewer.tiles.TilesDownloader.TooManyRedirectionsException;

/**
 * @author Martin Řehánek
 * 
 */
public class InitTilesDownloaderTask extends ConcurrentAsyncTask<Void, Void, TilesDownloader> {
	private static final String TAG = InitTilesDownloaderTask.class.getSimpleName();
	private final String zoomifyBaseUrl;
	private final TilesDownloaderInitializationHandler handler;

	private IOException ioException;
	private TooManyRedirectionsException tooManyRedirectionsException;
	private ImageServerResponseException imageServerResponseException;
	private XmlPullParserException xmlPullParserException;

	public InitTilesDownloaderTask(String zoomifyBaseUrl, TilesDownloaderInitializationHandler handler) {
		this.zoomifyBaseUrl = zoomifyBaseUrl;
		this.handler = handler;
	}

	@Override
	protected TilesDownloader doInBackground(Void... params) {
		try {
			Log.d(TAG, "downloading metadata from '" + zoomifyBaseUrl + "'");
			TilesDownloader downloader = new TilesDownloader(zoomifyBaseUrl);
			downloader.init();
			return downloader;
		} catch (IOException e) {
			ioException = e;
		} catch (TooManyRedirectionsException e) {
			tooManyRedirectionsException = e;
		} catch (ImageServerResponseException e) {
			imageServerResponseException = e;
		} catch (XmlPullParserException e) {
			xmlPullParserException = e;
		}
		return null;
	}

	@Override
	protected void onPostExecute(TilesDownloader downloader) {
		if (ioException != null) {
			handler.onDataTransferError(zoomifyBaseUrl, ioException.getMessage());
		} else if (tooManyRedirectionsException != null) {
			handler.onRedirectionLoop(zoomifyBaseUrl, tooManyRedirectionsException.getRedirections());
		} else if (imageServerResponseException != null) {
			handler.onInvalidImagePropertiesState(zoomifyBaseUrl, imageServerResponseException.getErrorCode());
		} else if (xmlPullParserException != null) {
			handler.onInvalidImagePropertiesData(zoomifyBaseUrl, xmlPullParserException.getMessage());
		} else {
			handler.onInitialized(zoomifyBaseUrl, downloader);
		}
	}

	public interface TilesDownloaderInitializationHandler {

		public void onInitialized(String imagePropertiesUrl, TilesDownloader downloader);

		public void onInvalidImagePropertiesState(String imagePropertiesUrl, int responseCode);

		public void onRedirectionLoop(String imagePropertiesUrl, int redirections);

		public void onDataTransferError(String imagePropertiesUrl, String errorMessage);

		public void onInvalidImagePropertiesData(String imagePropertiesUrl, String errorMessage);

	}

}

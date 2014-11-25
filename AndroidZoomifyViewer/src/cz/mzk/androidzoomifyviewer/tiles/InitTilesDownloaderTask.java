package cz.mzk.androidzoomifyviewer.tiles;

import android.util.Log;
import cz.mzk.androidzoomifyviewer.ConcurrentAsyncTask;
import cz.mzk.androidzoomifyviewer.tiles.TilesDownloader.ImageServerResponseException;
import cz.mzk.androidzoomifyviewer.tiles.TilesDownloader.InvalidDataException;
import cz.mzk.androidzoomifyviewer.tiles.TilesDownloader.OtherIOException;
import cz.mzk.androidzoomifyviewer.tiles.TilesDownloader.TooManyRedirectionsException;

/**
 * @author Martin Řehánek
 * 
 */
public class InitTilesDownloaderTask extends ConcurrentAsyncTask<Void, Void, TilesDownloader> {
	private static final String TAG = InitTilesDownloaderTask.class.getSimpleName();
	private final String zoomifyBaseUrl;
	private final double pxRatio;
	private final ImagePropertiesDownloadResultHandler handler;

	private OtherIOException otherIoException;
	private TooManyRedirectionsException tooManyRedirectionsException;
	private ImageServerResponseException imageServerResponseException;
	private InvalidDataException invalidXmlException;

	/**
	 * 
	 * @param zoomifyBaseUrl
	 *            Zoomify base url, not null
	 * @param pxRatio 
	 * @param handler
	 *            ImageProperties.xml download result handler, not null
	 */
	public InitTilesDownloaderTask(String zoomifyBaseUrl, double pxRatio, ImagePropertiesDownloadResultHandler handler) {
		this.zoomifyBaseUrl = zoomifyBaseUrl;
		this.handler = handler;
		this.pxRatio = pxRatio;
	}

	@Override
	protected TilesDownloader doInBackground(Void... params) {
		try {
			Log.d(TAG, "downloading metadata from '" + zoomifyBaseUrl + "'");
			TilesDownloader downloader = new TilesDownloader(zoomifyBaseUrl, pxRatio);
			if (!isCancelled()) {
				downloader.init();
				return downloader;
			}
		} catch (TooManyRedirectionsException e) {
			tooManyRedirectionsException = e;
		} catch (ImageServerResponseException e) {
			imageServerResponseException = e;
		} catch (InvalidDataException e) {
			invalidXmlException = e;
		} catch (OtherIOException e) {
			otherIoException = e;
		}
		return null;
	}

	@Override
	protected void onPostExecute(TilesDownloader downloader) {
		if (tooManyRedirectionsException != null) {
			handler.onRedirectionLoop(tooManyRedirectionsException.getUrl(),
					tooManyRedirectionsException.getRedirections());
		} else if (imageServerResponseException != null) {
			handler.onUnhandableResponseCode(imageServerResponseException.getUrl(),
					imageServerResponseException.getErrorCode());
		} else if (invalidXmlException != null) {
			handler.onInvalidData(invalidXmlException.getUrl(), invalidXmlException.getMessage());
		} else if (otherIoException != null) {
			handler.onDataTransferError(otherIoException.getUrl(), otherIoException.getMessage());
		} else {
			handler.onSuccess(downloader);
		}
	}

	public interface ImagePropertiesDownloadResultHandler {

		public void onSuccess(TilesDownloader downloader);

		public void onUnhandableResponseCode(String imagePropertiesUrl, int responseCode);

		public void onRedirectionLoop(String imagePropertiesUrl, int redirections);

		public void onDataTransferError(String imagePropertiesUrl, String errorMessage);

		public void onInvalidData(String imagePropertiesUrl, String errorMessage);

	}

}

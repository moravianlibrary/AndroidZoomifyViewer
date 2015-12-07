package cz.mzk.androidzoomifyviewer.tiles.zoomify;

import cz.mzk.androidzoomifyviewer.ConcurrentAsyncTask;
import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.tiles.MetadataInitializationHandler;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.ImageServerResponseException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.InvalidDataException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.OtherIOException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.TooManyRedirectionsException;

/**
 * @author Martin Řehánek
 */
public class InitTilesDownloaderTask extends ConcurrentAsyncTask<Void, Void, Void> {

    private static final Logger logger = new Logger(InitTilesDownloaderTask.class);


    private ZoomifyImageManager mDownloader;
    private final MetadataInitializationHandler mHandler;

    private OtherIOException otherIoException;
    private TooManyRedirectionsException tooManyRedirectionsException;
    private ImageServerResponseException imageServerResponseException;
    private InvalidDataException invalidXmlException;

    /**
     * @param handler
     */
    public InitTilesDownloaderTask(ZoomifyImageManager downloader, MetadataInitializationHandler handler) {
        mDownloader = downloader;
        mHandler = handler;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            //logger.d("downloading metadata from '" + zoomifyBaseUrl + "'");
            if (!isCancelled()) {
                mDownloader.initImageMetadata();
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
    protected void onPostExecute(Void result) {
        if (tooManyRedirectionsException != null) {
            mHandler.onRedirectionLoop(tooManyRedirectionsException.getUrl(), tooManyRedirectionsException.getRedirections());
        } else if (imageServerResponseException != null) {
            mHandler.onUnhandableResponseCode(imageServerResponseException.getUrl(), imageServerResponseException.getErrorCode());
        } else if (invalidXmlException != null) {
            mHandler.onInvalidData(invalidXmlException.getUrl(), invalidXmlException.getMessage());
        } else if (otherIoException != null) {
            mHandler.onDataTransferError(otherIoException.getUrl(), otherIoException.getMessage());
        } else {
            mHandler.onSuccess();
        }
    }

}

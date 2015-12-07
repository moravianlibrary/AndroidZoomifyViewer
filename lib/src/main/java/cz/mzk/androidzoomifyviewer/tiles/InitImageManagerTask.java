package cz.mzk.androidzoomifyviewer.tiles;

import cz.mzk.androidzoomifyviewer.ConcurrentAsyncTask;
import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.ImageServerResponseException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.InvalidDataException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.OtherIOException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.TooManyRedirectionsException;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;

/**
 * @author Martin Řehánek
 */
public class InitImageManagerTask extends ConcurrentAsyncTask<Void, Void, Void> {

    private static final Logger LOGGER = new Logger(InitImageManagerTask.class);
    private final TiledImageView.MetadataInitializationHandler mHandler;
    private final TiledImageView.MetadataInitializationSuccessListener mSuccessListener;
    private final ImageManagerTaskRegistry.TaskFinishedListener mRegistryListener;
    private ImageManager mImgManager;
    private OtherIOException mOtherIoException;
    private TooManyRedirectionsException mTooManyRedirectionsException;
    private ImageServerResponseException mImageServerResponseException;
    private InvalidDataException mInvalidXmlException;

    public InitImageManagerTask(ImageManager imgManager, TiledImageView.MetadataInitializationHandler handler, TiledImageView.MetadataInitializationSuccessListener successListener, ImageManagerTaskRegistry.TaskFinishedListener registryListener) {
        mImgManager = imgManager;
        mHandler = handler;
        mSuccessListener = successListener;
        mRegistryListener = registryListener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            if (!isCancelled()) {
                mImgManager.initImageMetadata();
            }
        } catch (TooManyRedirectionsException e) {
            mTooManyRedirectionsException = e;
        } catch (ImageServerResponseException e) {
            mImageServerResponseException = e;
        } catch (InvalidDataException e) {
            mInvalidXmlException = e;
        } catch (OtherIOException e) {
            mOtherIoException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mRegistryListener != null) {
            mRegistryListener.onTaskFinished();
        }
        if (mTooManyRedirectionsException != null) {
            if (mHandler != null) {
                mHandler.onMetadataRedirectionLoop(mTooManyRedirectionsException.getUrl(), mTooManyRedirectionsException.getRedirections());
            }
        } else if (mImageServerResponseException != null) {
            if (mHandler != null) {
                mHandler.onMetadataUnhandableResponseCode(mImageServerResponseException.getUrl(), mImageServerResponseException.getErrorCode());
            }
        } else if (mInvalidXmlException != null) {
            if (mHandler != null) {
                mHandler.onMetadataInvalidData(mInvalidXmlException.getUrl(), mInvalidXmlException.getMessage());
            }
        } else if (mOtherIoException != null) {
            if (mHandler != null) {
                mHandler.onMetadataDataTransferError(mOtherIoException.getUrl(), mOtherIoException.getMessage());
            }
        } else {
            if (mHandler != null) {
                mHandler.onMetadataInitialized();
            }
            if (mSuccessListener != null) {
                mSuccessListener.onMetadataDownloaded(mImgManager);
            }
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (mRegistryListener != null) {
            mRegistryListener.onTaskFinished();
        }
    }

}

package cz.mzk.tiledimageview.tiles.tasks;

import cz.mzk.tiledimageview.ConcurrentAsyncTask;
import cz.mzk.tiledimageview.Logger;
import cz.mzk.tiledimageview.cache.CacheManager;
import cz.mzk.tiledimageview.cache.MetadataCache;
import cz.mzk.tiledimageview.tiles.Downloader;
import cz.mzk.tiledimageview.tiles.ImageManager;
import cz.mzk.tiledimageview.tiles.exceptions.ImageServerResponseException;
import cz.mzk.tiledimageview.tiles.exceptions.InvalidDataException;
import cz.mzk.tiledimageview.tiles.exceptions.OtherIOException;
import cz.mzk.tiledimageview.tiles.exceptions.TooManyRedirectionsException;
import cz.mzk.tiledimageview.tiles.metadata.ImageMetadata;
import cz.mzk.tiledimageview.tiles.zoomify.ZoomifyMetadataParser;
import cz.mzk.tiledimageview.viewer.TiledImageView;

/**
 * @author Martin Řehánek
 */
public class InitImageManagerTask extends ConcurrentAsyncTask<Void, Void, ImageMetadata> {

    private static final Logger LOGGER = new Logger(InitImageManagerTask.class);

    private final TiledImageView.MetadataInitializationHandler mHandler;
    private final TiledImageView.MetadataInitializationSuccessListener mSuccessListener;
    private final ImageManagerTaskRegistry.TaskFinishedListener mRegistryListener;
    private final ImageManager mImgManager;
    private final String mMetadataUrl;
    private OtherIOException mOtherIoException;
    private TooManyRedirectionsException mTooManyRedirectionsException;
    private ImageServerResponseException mImageServerResponseException;
    private InvalidDataException mInvalidXmlException;

    public InitImageManagerTask(ImageManager imgManager, String metadataUrl, TiledImageView.MetadataInitializationHandler handler, TiledImageView.MetadataInitializationSuccessListener successListener, ImageManagerTaskRegistry.TaskFinishedListener registryListener) {
        mImgManager = imgManager;
        mMetadataUrl = metadataUrl;
        mHandler = handler;
        mSuccessListener = successListener;
        mRegistryListener = registryListener;
    }

    @Override
    protected ImageMetadata doInBackground(Void... params) {
        try {
            if (isCancelled()) {
                LOGGER.d("Task canceled before fetching metadata: " + mMetadataUrl);
            } else {
                String metadataStr = fetchMetadata();
                if (isCancelled()) {
                    LOGGER.d("Task canceled before parsing metadata: " + mMetadataUrl);
                } else {
                    if (metadataStr != null) {
                        switch (mImgManager.getTiledImageProtocol()) {
                            case ZOOMIFY:
                                return new ZoomifyMetadataParser().parse(metadataStr, mMetadataUrl);
                            default:
                                return null;
                        }
                    }
                }
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

    private String fetchMetadata() throws OtherIOException, TooManyRedirectionsException, ImageServerResponseException {
        MetadataCache cache = CacheManager.getMetadataCache();
        String fromCache = cache.getMetadata(mMetadataUrl);
        if (fromCache != null) {
            LOGGER.d("Metadata found in cache: " + mMetadataUrl);
            return fromCache;
        } else {
            LOGGER.d("Metadata not in cache: " + mMetadataUrl);
            if (isCancelled()) {
                LOGGER.d("Task canceled before downloading metadata: " + mMetadataUrl);
                return null;
            } else {
                LOGGER.d("Downloading metadata: " + mMetadataUrl);
                String downloaded = Downloader.downloadMetadata(mMetadataUrl);
                cache.storeMetadata(downloaded, mMetadataUrl);
                return downloaded;
            }
        }
    }

    @Override
    protected void onPostExecute(ImageMetadata result) {
        if (mRegistryListener != null) {
            mRegistryListener.onTaskFinished();
        }
        if (result != null) {
            if (mSuccessListener != null) {
                mImgManager.init(result);
                mSuccessListener.onMetadataDownloaded(mImgManager);
            }
            if (mHandler != null) {
                mHandler.onMetadataInitialized();
            }
        } else {
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

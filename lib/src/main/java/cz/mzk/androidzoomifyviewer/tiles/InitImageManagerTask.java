package cz.mzk.androidzoomifyviewer.tiles;

import cz.mzk.androidzoomifyviewer.ConcurrentAsyncTask;
import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.cache.CacheManager;
import cz.mzk.androidzoomifyviewer.cache.MetadataCache;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.ImageServerResponseException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.InvalidDataException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.OtherIOException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.TooManyRedirectionsException;
import cz.mzk.androidzoomifyviewer.tiles.zoomify.ImageProperties;
import cz.mzk.androidzoomifyviewer.tiles.zoomify.ImagePropertiesParser;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;

/**
 * @author Martin Řehánek
 */
// TODO: 8.12.15 Zobecnit na Metadata instead of ImageProperties
// TODO: 8.12.15 A taky dodavat v parametru Perser jako zobecneni ImagePropertiesParser
public class InitImageManagerTask extends ConcurrentAsyncTask<Void, Void, ImageProperties> {

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
    protected ImageProperties doInBackground(Void... params) {
        try {
            if (isCancelled()) {
                LOGGER.d("Task canceled before fetching metadata: " + mMetadataUrl);
            } else {
                String metadataStr = fetchMetadata();
                if (isCancelled()) {
                    LOGGER.d("Task canceled before parsing metadata: " + mMetadataUrl);
                } else {
                    if (metadataStr != null) {
                        return ImagePropertiesParser.parse(metadataStr, mMetadataUrl);
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
        // TODO: 8.12.15 proverit, jestli opravdu vzdy funguje i z worker threadu
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
                cache.storeXml(downloaded, mMetadataUrl);
                return downloaded;
            }
        }
    }

    @Override
    protected void onPostExecute(ImageProperties result) {
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

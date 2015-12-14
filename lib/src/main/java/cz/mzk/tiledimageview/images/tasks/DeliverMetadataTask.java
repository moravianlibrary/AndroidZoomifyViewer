package cz.mzk.tiledimageview.images.tasks;

import cz.mzk.tiledimageview.Logger;
import cz.mzk.tiledimageview.TiledImageView;
import cz.mzk.tiledimageview.images.Downloader;
import cz.mzk.tiledimageview.images.TiledImageProtocol;
import cz.mzk.tiledimageview.images.cache.CacheManager;
import cz.mzk.tiledimageview.images.cache.MetadataCache;
import cz.mzk.tiledimageview.images.exceptions.ImageServerResponseException;
import cz.mzk.tiledimageview.images.exceptions.InvalidDataException;
import cz.mzk.tiledimageview.images.exceptions.OtherIOException;
import cz.mzk.tiledimageview.images.exceptions.TooManyRedirectionsException;
import cz.mzk.tiledimageview.images.metadata.ImageMetadata;
import cz.mzk.tiledimageview.images.zoomify.ZoomifyMetadataParser;

/**
 * Created by Martin Řehánek on 14.12.15.
 */
public class DeliverMetadataTask extends ConcurrentAsyncTask<Void, Void, ImageMetadata> {

    private static final Logger LOGGER = new Logger(DeliverMetadataTask.class);

    private final TiledImageView.MetadataInitializationListener mListener;
    private final TiledImageView.MetadataInitializationSuccessListener mSuccessListener;
    private final TaskManager.TaskListener mTaskManagerListener;
    private final String mMetadataUrl;
    private final TiledImageProtocol mProtocol;
    private final String mCacheKey;

    private TooManyRedirectionsException mTooManyRedirectionsException;
    private ImageServerResponseException mImageServerResponseException;
    private InvalidDataException mInvalidXmlException;
    private OtherIOException mOtherIoException;


    public DeliverMetadataTask(TiledImageProtocol protocol,
                               String metadataUrl,
                               String cacheKey,
                               TiledImageView.MetadataInitializationListener listener,
                               TiledImageView.MetadataInitializationSuccessListener successListener,
                               TaskManager.TaskListener taskManagerListener) {
        mProtocol = protocol;
        mMetadataUrl = metadataUrl;
        mCacheKey = cacheKey;
        mListener = listener;
        mSuccessListener = successListener;
        mTaskManagerListener = taskManagerListener;
    }

    @Override
    protected ImageMetadata doInBackground(Void... params) {
        if (!isCancelled()) {
            MetadataCache tileCache = CacheManager.getMetadataCache();
            boolean diskCacheEnabled = tileCache.isDiskCacheEnabled();
            if (diskCacheEnabled) {
                boolean inDiskCache = tileCache.isItemInDiskCache(mCacheKey);
                LOGGER.d("is in disk cache: " + inDiskCache);
                if (!isCancelled()) {
                    if (inDiskCache) {
                        String fromDiskCache = tileCache.getItemFromDiskCache(mCacheKey);
                        if (!isCancelled()) {
                            if (fromDiskCache != null) {
                                LOGGER.d("disk cache returned metadata");
                                tileCache.storeItemToMemoryCache(mCacheKey, fromDiskCache);
                                LOGGER.d("metadata stored into memory cache");
                                return parse(fromDiskCache);
                            } else {
                                LOGGER.w("disk cache returned null");
                                // insonsistence between isItemInDiskCache() and subsequential getItemFromDiskCache()
                                // ignoring
                            }
                        }
                    } else { //not in disk cache
                        return fetchFromNetAndSave(tileCache, diskCacheEnabled);
                    }
                }
            } else {//disk cache disabled
                return fetchFromNetAndSave(tileCache, false);
            }
        }
        return null;
    }


    private ImageMetadata fetchFromNetAndSave(MetadataCache metadataCache, boolean diskCacheEnabled) {
        String metadataStr = downloadMetadata();
        if (metadataStr != null) {
            LOGGER.d("fetched from net");
            if (!isCancelled()) {
                //parse before storing to cache  - if there is incorrect metadata, we don't wanna cache them
                ImageMetadata metadata = parse(metadataStr);
                //memory
                if (!isCancelled()) {
                    metadataCache.storeItemToMemoryCache(mCacheKey, metadataStr);
                    LOGGER.d("metadata stored into memory cache");
                }
                //disk
                if (!isCancelled()) {
                    if (diskCacheEnabled) {
                        metadataCache.storeItemToDiskCache(mCacheKey, metadataStr);
                        LOGGER.d("metadata stored into disk cache");
                    }
                }
                if (!isCancelled()) {
                    return metadata;
                }
            }
        } else {
            LOGGER.d("fetched from net but null");
        }
        return null;
    }

    private ImageMetadata parse(String metadataStr) {
        switch (mProtocol) {
            case ZOOMIFY:
                try {
                    return new ZoomifyMetadataParser().parse(metadataStr, mMetadataUrl);
                } catch (InvalidDataException e) {
                    mInvalidXmlException = e;
                } catch (OtherIOException e) {
                    mOtherIoException = e;
                }
        }
        return null;
    }


    private String downloadMetadata() {
        try {
            return Downloader.downloadMetadata(mMetadataUrl);
        } catch (TooManyRedirectionsException e) {
            mTooManyRedirectionsException = e;
        } catch (ImageServerResponseException e) {
            mImageServerResponseException = e;
        } catch (OtherIOException e) {
            mOtherIoException = e;
        }
        /*finally {
            LOGGER.v("tile processing task finished");
        }*/
        return null;
    }

    @Override
    protected void onPostExecute(ImageMetadata result) {
        if (mTaskManagerListener != null) {
            mTaskManagerListener.onFinished();
        }
        if (result != null) {
            if (mSuccessListener != null) {
                mSuccessListener.onMetadataFetched(result);
            }
            if (mListener != null) {
                mListener.onMetadataInitialized();
            }
        } else if (mListener != null) {
            if (mTooManyRedirectionsException != null) {
                mListener.onMetadataRedirectionLoop(mTooManyRedirectionsException.getUrl(), mTooManyRedirectionsException.getRedirections());
            } else if (mImageServerResponseException != null) {
                mListener.onMetadataUnhandableResponseCode(mImageServerResponseException.getUrl(), mImageServerResponseException.getErrorCode());
            } else if (mInvalidXmlException != null) {
                mListener.onMetadataInvalidData(mInvalidXmlException.getUrl(), mInvalidXmlException.getMessage());
            } else if (mOtherIoException != null) {
                mListener.onMetadataDataTransferError(mOtherIoException.getUrl(), mOtherIoException.getMessage());
            }
        }
    }

    @Override
    protected void onCancelled(ImageMetadata result) {
        if (mTaskManagerListener != null) {
            mTaskManagerListener.onCanceled();
        }
    }
}

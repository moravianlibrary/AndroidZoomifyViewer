package cz.mzk.tiledimageview.images.tasks;

import cz.mzk.tiledimageview.Logger;
import cz.mzk.tiledimageview.TiledImageView;
import cz.mzk.tiledimageview.images.Downloader;
import cz.mzk.tiledimageview.images.ImageManager;
import cz.mzk.tiledimageview.images.TiledImageProtocol;
import cz.mzk.tiledimageview.images.cache.CacheKeyBuilder;
import cz.mzk.tiledimageview.images.cache.CacheManager;
import cz.mzk.tiledimageview.images.cache.MetadataCache;
import cz.mzk.tiledimageview.images.exceptions.ImageServerResponseException;
import cz.mzk.tiledimageview.images.exceptions.InvalidDataException;
import cz.mzk.tiledimageview.images.exceptions.OtherIOException;
import cz.mzk.tiledimageview.images.exceptions.TooManyRedirectionsException;
import cz.mzk.tiledimageview.images.metadata.ImageMetadata;
import cz.mzk.tiledimageview.images.zoomify.ZoomifyMetadataParser;

/**
 * @author Martin Řehánek
 */
public class InitImageManagerTask extends ConcurrentAsyncTask<Void, Void, InitImageManagerTask.Result> {

    private static final Logger LOGGER = new Logger(InitImageManagerTask.class);

    private final TiledImageView.MetadataInitializationListener mListener;
    private final TiledImageView.MetadataInitializationSuccessListener mSuccessListener;
    private final TaskManager.TaskListener mTaskManagerListener;
    private final ImageManager mImgManager;
    private final String mMetadataUrl;
    private final TiledImageProtocol mProtocol;

    public InitImageManagerTask(ImageManager imgManager,
                                TiledImageProtocol protocol,
                                String metadataUrl,
                                TiledImageView.MetadataInitializationListener listener,
                                TiledImageView.MetadataInitializationSuccessListener successListener,
                                TaskManager.TaskListener taskManagerListener) {
        mImgManager = imgManager;
        mProtocol = protocol;
        mMetadataUrl = metadataUrl;
        mListener = listener;
        mSuccessListener = successListener;
        mTaskManagerListener = taskManagerListener;
    }

    @Override
    protected Result doInBackground(Void... params) {
        if (!isCancelled()) {
            Result result = new Result();
            try {
                fetchMetadata(result);
                if (!isCancelled()) {
                    if (result.metadataStr != null) {
                        switch (mProtocol) {
                            case ZOOMIFY:
                                result.metadata = new ZoomifyMetadataParser().parse(result.metadataStr, mMetadataUrl);
                        }
                    }
                }
            } catch (TooManyRedirectionsException e) {
                result.mTooManyRedirectionsException = e;
            } catch (ImageServerResponseException e) {
                result.mImageServerResponseException = e;
            } catch (InvalidDataException e) {
                result.mInvalidXmlException = e;
            } catch (OtherIOException e) {
                result.mOtherIoException = e;
            }
            if (!isCancelled()) {
                return result;
            }
        }
        return null;
    }

    private Result fetchMetadata(Result result) throws OtherIOException, TooManyRedirectionsException, ImageServerResponseException {
        //LOGGER.v("Fetching metadata: " + mMetadataUrl);
        MetadataCache cache = CacheManager.getMetadataCache();
        String key = CacheKeyBuilder.buildKeyFromUrl(mMetadataUrl);
        if (!isCancelled()) {
            String fromMemory = cache.getItemFromMemoryCache(key);
            if (!isCancelled()) {
                if (fromMemory != null) {
                    result.metadataStr = fromMemory;
                } else {
                    if (cache.isDiskCacheEnabled()) {
                        boolean inDisk = cache.isItemInDiskCache(key);
                        if (!isCancelled()) {
                            if (inDisk) {
                                String fromDisk = cache.getItemFromDiskCache(key);
                                if (!isCancelled()) {
                                    cache.storeItemToMemoryCache(key, fromDisk);
                                    result.metadataStr = fromDisk;
                                }
                            } else {
                                String fromNet = Downloader.downloadMetadata(mMetadataUrl);
                                if (!isCancelled()) {
                                    cache.storeItemToMemoryCache(key, fromNet);
                                    result.storeToCacheDisk = true;
                                    result.cacheKey = key;
                                    result.metadataStr = fromNet;
                                }
                            }
                        }
                    } else {//disk cache disabled
                        String fromNet = Downloader.downloadMetadata(mMetadataUrl);
                        if (!isCancelled()) {
                            cache.storeItemToMemoryCache(key, fromNet);
                            result.metadataStr = fromNet;
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Result result) {
        if (mTaskManagerListener != null) {
            mTaskManagerListener.onFinished(result.storeToCacheDisk, result.cacheKey, result.metadataStr);
        }
        if (result.success) {
            if (mSuccessListener != null) {
                mImgManager.init(result.metadata);
                mSuccessListener.onMetadataDownloaded(mImgManager);
            }
            if (mListener != null) {
                mListener.onMetadataInitialized();
            }
        } else if (mListener != null) {
            if (result.mTooManyRedirectionsException != null) {
                mListener.onMetadataRedirectionLoop(result.mTooManyRedirectionsException.getUrl(), result.mTooManyRedirectionsException.getRedirections());
            } else if (result.mImageServerResponseException != null) {
                mListener.onMetadataUnhandableResponseCode(result.mImageServerResponseException.getUrl(), result.mImageServerResponseException.getErrorCode());
            } else if (result.mInvalidXmlException != null) {
                mListener.onMetadataInvalidData(result.mInvalidXmlException.getUrl(), result.mInvalidXmlException.getMessage());
            } else if (result.mOtherIoException != null) {
                mListener.onMetadataDataTransferError(result.mOtherIoException.getUrl(), result.mOtherIoException.getMessage());
            }
        }
    }

    @Override
    protected void onCancelled(Result result) {
        if (mTaskManagerListener != null) {
            mTaskManagerListener.onCanceled();
        }
    }

    static class Result {
        Boolean success = true;
        ImageMetadata metadata;
        Boolean storeToCacheDisk = false;
        String cacheKey;
        String metadataStr;
        OtherIOException mOtherIoException;
        TooManyRedirectionsException mTooManyRedirectionsException;
        ImageServerResponseException mImageServerResponseException;
        InvalidDataException mInvalidXmlException;

    }

}

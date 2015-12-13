package cz.mzk.tiledimageview.images.tasks;

import android.graphics.Bitmap;

import cz.mzk.tiledimageview.Logger;
import cz.mzk.tiledimageview.TiledImageView;
import cz.mzk.tiledimageview.images.Downloader;
import cz.mzk.tiledimageview.images.cache.CacheManager;
import cz.mzk.tiledimageview.images.cache.TileCache;
import cz.mzk.tiledimageview.images.exceptions.ImageServerResponseException;
import cz.mzk.tiledimageview.images.exceptions.OtherIOException;
import cz.mzk.tiledimageview.images.exceptions.TooManyRedirectionsException;

/**
 * @author Martin Řehánek
 */
public class DeliverTileIntoMemoryCacheTask extends ConcurrentAsyncTask<Void, Void, Boolean> {

    // private static final int THREAD_PRIORITY = Math.min(Thread.MAX_PRIORITY, Thread.MIN_PRIORITY + 1);
    private static final Logger LOGGER = new Logger(DeliverTileIntoMemoryCacheTask.class);

    private final String mTileImageUrl;
    private final String mCacheKey;
    private final TiledImageView.TileDownloadErrorListener mErrorListener;
    private final TiledImageView.TileDownloadSuccessListener mSuccessListener;
    private final TaskManager.TaskListener mTaskManagerListener;

    private OtherIOException otherIoException;
    private TooManyRedirectionsException tooManyRedirectionsException;
    private ImageServerResponseException imageServerResponseException;

    /**
     * @param tileImageUrl        Url of tile image (jpeg, tif, png, bmp, ...)
     * @param errorListener
     * @param successListener
     * @param taskManagerListener
     */
    public DeliverTileIntoMemoryCacheTask(String tileImageUrl,
                                          String cacheKey,
                                          TiledImageView.TileDownloadSuccessListener successListener,
                                          TiledImageView.TileDownloadErrorListener errorListener,
                                          TaskManager.TaskListener taskManagerListener) {
        mTileImageUrl = tileImageUrl;
        mCacheKey = cacheKey;
        mSuccessListener = successListener;
        mErrorListener = errorListener;
        mTaskManagerListener = taskManagerListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (!isCancelled()) {
            TileCache tileCache = CacheManager.getTileCache();
            boolean diskCacheEnabled = tileCache.isDiskCacheEnabled();
            if (diskCacheEnabled) {
                boolean inDiskCache = tileCache.isItemInDiskCache(mCacheKey);
                LOGGER.d("is in disk cache: " + inDiskCache);
                if (!isCancelled()) {
                    if (inDiskCache) {
                        Bitmap fromDiskCache = tileCache.getItemFromDiskCache(mCacheKey);
                        if (!isCancelled()) {
                            if (fromDiskCache != null) {
                                LOGGER.d("disk cache returned bitmap");
                                tileCache.storeItemToMemoryCache(mCacheKey, fromDiskCache);
                                LOGGER.d("bitmap stored into memory cache");
                                return true;
                            } else {
                                LOGGER.w("disk cache returned null");
                                // insonsistence between isItemInDiskCache() and subsequential getItemFromDiskCache()
                                // ignoring
                                return false;
                            }
                        }
                    } else { //not in disk cache
                        fetchFromNetAndSave(tileCache, diskCacheEnabled);
                    }
                }
            } else {//disk cache disabled
                fetchFromNetAndSave(tileCache, false);
            }
        }
        return false;
    }


    private boolean fetchFromNetAndSave(TileCache tileCache, boolean diskCacheEnabled) {
        Bitmap fromNet = downloadTile(mTileImageUrl);
        if (fromNet != null) {
            LOGGER.d("fetched from net");
            //memory
            if (!isCancelled()) {
                tileCache.storeItemToMemoryCache(mCacheKey, fromNet);
                LOGGER.d("bitmap stored into memory cache");
                return true;
            }
            //disk
            if (!isCancelled()) {
                if (diskCacheEnabled) {
                    tileCache.storeItemToDiskCache(mCacheKey, fromNet);
                    LOGGER.d("bitmap stored into disk cache");
                }
            }
        } else {
            LOGGER.d("fetched from net but null");
        }
        return false;
    }

    private Bitmap downloadTile(String mTileImageUrl) {
        try {
            Bitmap tile = Downloader.downloadTile(mTileImageUrl);
            return tile;
        } catch (TooManyRedirectionsException e) {
            tooManyRedirectionsException = e;
        } catch (ImageServerResponseException e) {
            imageServerResponseException = e;
        } catch (OtherIOException e) {
            otherIoException = e;
        } finally {
            LOGGER.v("tile processing task finished");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        //LOGGER.i("finished: " + mTileImageUrl);
        if (mTaskManagerListener != null) {
            mTaskManagerListener.onFinished();
        }
        if (success) {
            if (mSuccessListener != null) {
                mSuccessListener.onTileDelivered();
            }
        } else {
            if (mErrorListener != null) {
                if (tooManyRedirectionsException != null) {
                    mErrorListener.onTileRedirectionLoop(tooManyRedirectionsException.getUrl(), tooManyRedirectionsException.getRedirections());
                } else if (imageServerResponseException != null) {
                    mErrorListener.onTileUnhandableResponse(imageServerResponseException.getUrl(), imageServerResponseException.getErrorCode());
                } else if (otherIoException != null) {
                    mErrorListener.onTileDataTransferError(otherIoException.getUrl(), otherIoException.getMessage());
                }
            }
        }
    }

    @Override
    protected void onCancelled(Boolean succes) {
        //LOGGER.i("canceled: " + mTileImageUrl);
        if (mTaskManagerListener != null) {
            mTaskManagerListener.onCanceled();
        }
    }

}

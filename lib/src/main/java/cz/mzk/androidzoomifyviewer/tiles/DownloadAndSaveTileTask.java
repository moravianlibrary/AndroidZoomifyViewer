package cz.mzk.androidzoomifyviewer.tiles;

import android.graphics.Bitmap;

import cz.mzk.androidzoomifyviewer.CacheManager;
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
public class DownloadAndSaveTileTask extends ConcurrentAsyncTask<Void, Void, Boolean> {

    // private static final int THREAD_PRIORITY = Math.min(Thread.MAX_PRIORITY, Thread.MIN_PRIORITY + 1);
    private static final Logger LOGGER = new Logger(DownloadAndSaveTileTask.class);

    private final ImageManager mImgManager;// TODO: 7.12.15 Bude stacit jen downloader
    private final String zoomifyBaseUrl; // TODO: 7.12.15 Really needed here?
    private final TilePositionInPyramid mTilePositionInPyramid;
    private final TiledImageView.TileDownloadErrorListener mErrorListener;
    private final TiledImageView.TileDownloadSuccessListener mSuccessListener;
    private final ImageManagerTaskRegistry.TaskFinishedListener mRegistryListener;

    private OtherIOException otherIoException;
    private TooManyRedirectionsException tooManyRedirectionsException;
    private ImageServerResponseException imageServerResponseException;
    private InvalidDataException invalidXmlException;

    /**
     * @param imgManager             initialized ImageManager, not null
     * @param zoomifyBaseUrl         Zoomify base url, not null
     * @param mTilePositionInPyramid Tile id, not null
     * @param errorListener          Tile download result mErrorListener, not null
     * @param successListener
     * @param taskFinishedListener
     */
    public DownloadAndSaveTileTask(ImageManager imgManager, String zoomifyBaseUrl, TilePositionInPyramid mTilePositionInPyramid, TiledImageView.TileDownloadErrorListener errorListener, TiledImageView.TileDownloadSuccessListener successListener, ImageManagerTaskRegistry.TaskFinishedListener taskFinishedListener) {
        this.mImgManager = imgManager;
        this.zoomifyBaseUrl = zoomifyBaseUrl;
        this.mTilePositionInPyramid = mTilePositionInPyramid;
        mErrorListener = errorListener;
        mSuccessListener = successListener;
        mRegistryListener = taskFinishedListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // Thread thread = Thread.currentThread();
        // thread.setPriority(THREAD_PRIORITY);
        // ThreadGroup group = thread.getThreadGroup();
        // int threadPriority = thread.getPriority();
        // TestLoggers.THREADS.d(String.format("bmp download: priority: %d, TG: name: %s, active: %d, max priority: %d, ",
        // threadPriority, group.getName(), group.activeCount(), group.getMaxPriority()));
        try {
            if (!isCancelled()) {
                Bitmap tile = mImgManager.downloadTile(mTilePositionInPyramid);
                if (!isCancelled()) {
                    if (tile != null) {
                        CacheManager.getTilesCache().storeTile(tile, mImgManager.buildTileUrl(mTilePositionInPyramid));
                        LOGGER.v(String.format("tile downloaded and saved to disk cache: base url: '%s', tile: '%s'", zoomifyBaseUrl, mTilePositionInPyramid));
                        return true;
                    } else {
                        // TODO: examine this
                        LOGGER.w("tile is null");
                    }
                } else {
                    LOGGER.v(String
                            .format("tile processing canceled task after downloading and before saving data: base url: '%s', tile: '%s'", zoomifyBaseUrl, mTilePositionInPyramid));
                }
            } else {
                LOGGER.v(String.format("tile processing task canceled before download started: base url: '%s', tile: '%s'", zoomifyBaseUrl, mTilePositionInPyramid));
            }
        } catch (TooManyRedirectionsException e) {
            tooManyRedirectionsException = e;
        } catch (ImageServerResponseException e) {
            imageServerResponseException = e;
        } catch (OtherIOException e) {
            otherIoException = e;
        }
        // TODO
        // catch (InvalidDataException e) {
        // invalidXmlException = e;
        // }
        finally {
            LOGGER.v("tile processing task finished");
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (mRegistryListener != null) {
            mRegistryListener.onTaskFinished();
        }
        if (mSuccessListener != null && success) {
            mSuccessListener.onTileDownloaded();
        } else {
            if (mErrorListener != null) {
                if (tooManyRedirectionsException != null) {
                    mErrorListener.onRedirectionLoop(mTilePositionInPyramid, tooManyRedirectionsException.getUrl(), tooManyRedirectionsException.getRedirections());
                } else if (imageServerResponseException != null) {
                    mErrorListener.onUnhandableResponse(mTilePositionInPyramid, imageServerResponseException.getUrl(), imageServerResponseException.getErrorCode());
                } else if (invalidXmlException != null) {
                    mErrorListener.onInvalidDataError(mTilePositionInPyramid, invalidXmlException.getUrl(), invalidXmlException.getMessage());
                } else if (otherIoException != null) {
                    mErrorListener.onDataTransferError(mTilePositionInPyramid, otherIoException.getUrl(), otherIoException.getMessage());
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

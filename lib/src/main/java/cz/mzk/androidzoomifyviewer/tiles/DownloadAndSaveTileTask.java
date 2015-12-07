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
public class DownloadAndSaveTileTask extends ConcurrentAsyncTask<Void, Void, Bitmap> {

    // private static final int THREAD_PRIORITY = Math.min(Thread.MAX_PRIORITY, Thread.MIN_PRIORITY + 1);
    private static final Logger LOGGER = new Logger(DownloadAndSaveTileTask.class);

    private final ImageManager mImgManager;// TODO: 7.12.15 Bude stacit jen mImgManager
    private final String zoomifyBaseUrl;
    private final TilePositionInPyramid tilePositionInPyramid;
    private final TiledImageView.TileDownloadErrorListener mErrorListener;
    private final TiledImageView.TileDownloadSuccessListener mSuccessListener;
    private final ImageManagerTaskRegistry.TaskFinishedListener mRegistryListener;

    private OtherIOException otherIoException;
    private TooManyRedirectionsException tooManyRedirectionsException;
    private ImageServerResponseException imageServerResponseException;
    private InvalidDataException invalidXmlException;

    /**
     * @param imgManager            initialized ImageManager, not null
     * @param zoomifyBaseUrl        Zoomify base url, not null
     * @param tilePositionInPyramid Tile id, not null
     * @param errorListener         Tile download result mErrorListener, not null
     * @param successListener
     * @param taskFinishedListener
     */
    public DownloadAndSaveTileTask(ImageManager imgManager, String zoomifyBaseUrl, TilePositionInPyramid tilePositionInPyramid, TiledImageView.TileDownloadErrorListener errorListener, TiledImageView.TileDownloadSuccessListener successListener, ImageManagerTaskRegistry.TaskFinishedListener taskFinishedListener) {
        this.mImgManager = imgManager;
        this.zoomifyBaseUrl = zoomifyBaseUrl;
        this.tilePositionInPyramid = tilePositionInPyramid;
        mErrorListener = errorListener;
        mSuccessListener = successListener;
        mRegistryListener = taskFinishedListener;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        // Thread thread = Thread.currentThread();
        // thread.setPriority(THREAD_PRIORITY);
        // ThreadGroup group = thread.getThreadGroup();
        // int threadPriority = thread.getPriority();
        // TestLoggers.THREADS.d(String.format("bmp download: priority: %d, TG: name: %s, active: %d, max priority: %d, ",
        // threadPriority, group.getName(), group.activeCount(), group.getMaxPriority()));
        try {
            if (!isCancelled()) {
                Bitmap tile = mImgManager.downloadTile(tilePositionInPyramid);
                if (!isCancelled()) {
                    if (tile != null) {
                        CacheManager.getTilesCache().storeTile(tile, zoomifyBaseUrl, tilePositionInPyramid);
                        LOGGER.v(String.format("tile downloaded and saved to disk cache: base url: '%s', tile: '%s'",
                                zoomifyBaseUrl, tilePositionInPyramid));
                    } else {
                        // TODO: examine this
                        LOGGER.w("tile is null");
                    }
                } else {
                    LOGGER.v(String
                            .format("tile processing canceled task after downloading and before saving data: base url: '%s', tile: '%s'",
                                    zoomifyBaseUrl, tilePositionInPyramid));
                }
            } else {
                LOGGER.v(String.format(
                        "tile processing task canceled before download started: base url: '%s', tile: '%s'",
                        zoomifyBaseUrl, tilePositionInPyramid));
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
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (mRegistryListener != null) {
            mRegistryListener.onTaskFinished();
        }
        if (mSuccessListener != null && bitmap != null) {
            mSuccessListener.onTileDownloaded();
        }
        if (mErrorListener != null) {
            if (tooManyRedirectionsException != null) {
                mErrorListener.onRedirectionLoop(tilePositionInPyramid, tooManyRedirectionsException.getUrl(), tooManyRedirectionsException.getRedirections());
            } else if (imageServerResponseException != null) {
                mErrorListener.onUnhandableResponse(tilePositionInPyramid, imageServerResponseException.getUrl(), imageServerResponseException.getErrorCode());
            } else if (invalidXmlException != null) {
                mErrorListener.onInvalidDataError(tilePositionInPyramid, invalidXmlException.getUrl(), invalidXmlException.getMessage());
            } else if (otherIoException != null) {
                mErrorListener.onDataTransferError(tilePositionInPyramid, otherIoException.getUrl(), otherIoException.getMessage());
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

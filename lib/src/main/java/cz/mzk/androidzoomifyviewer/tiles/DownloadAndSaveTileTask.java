package cz.mzk.androidzoomifyviewer.tiles;

import android.graphics.Bitmap;

import cz.mzk.androidzoomifyviewer.CacheManager;
import cz.mzk.androidzoomifyviewer.ConcurrentAsyncTask;
import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.ImageServerResponseException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.InvalidDataException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.OtherIOException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.TooManyRedirectionsException;

/**
 * @author Martin Řehánek
 */
public class DownloadAndSaveTileTask extends ConcurrentAsyncTask<Void, Void, Bitmap> {

    // private static final int THREAD_PRIORITY = Math.min(Thread.MAX_PRIORITY, Thread.MIN_PRIORITY + 1);
    private static final Logger LOGGER = new Logger(DownloadAndSaveTileTask.class);

    private final ImageManager mImgManager;// TODO: 7.12.15 Bude stacit jen mImgManager
    private final String zoomifyBaseUrl;
    private final TilePositionInPyramid tilePositionInPyramid;
    private final TileDownloadHandler handler;
    private final ImageManagerTaskRegistry.TaskFinishedListener mRegistryListener;

    private OtherIOException otherIoException;
    private TooManyRedirectionsException tooManyRedirectionsException;
    private ImageServerResponseException imageServerResponseException;
    private InvalidDataException invalidXmlException;

    /**
     * @param imgManager            initialized Tiles mImgManager, not null
     * @param zoomifyBaseUrl        Zoomify base url, not null
     * @param tilePositionInPyramid Tile id, not null
     * @param handler               Tile download result handler, not null
     * @param taskFinishedListener
     */
    public DownloadAndSaveTileTask(ImageManager imgManager, String zoomifyBaseUrl, TilePositionInPyramid tilePositionInPyramid, TileDownloadHandler handler, ImageManagerTaskRegistry.TaskFinishedListener taskFinishedListener) {
        this.mImgManager = imgManager;
        this.zoomifyBaseUrl = zoomifyBaseUrl;
        this.tilePositionInPyramid = tilePositionInPyramid;
        this.handler = handler;
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
        if (handler != null) {
            if (tooManyRedirectionsException != null) {
                handler.onRedirectionLoop(tilePositionInPyramid, tooManyRedirectionsException.getUrl(),
                        tooManyRedirectionsException.getRedirections());
            } else if (imageServerResponseException != null) {
                handler.onUnhandableResponseCode(tilePositionInPyramid, imageServerResponseException.getUrl(),
                        imageServerResponseException.getErrorCode());
            } else if (invalidXmlException != null) {
                handler.onInvalidData(tilePositionInPyramid, invalidXmlException.getUrl(), invalidXmlException.getMessage());
            } else if (otherIoException != null) {
                handler.onDataTransferError(tilePositionInPyramid, otherIoException.getUrl(), otherIoException.getMessage());
            } else {
                handler.onSuccess(tilePositionInPyramid, bitmap);
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

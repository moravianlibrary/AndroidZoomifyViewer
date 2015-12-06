package cz.mzk.androidzoomifyviewer.tiles.zoomify;

import android.graphics.Bitmap;

import cz.mzk.androidzoomifyviewer.CacheManager;
import cz.mzk.androidzoomifyviewer.ConcurrentAsyncTask;
import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.tiles.TileDownloadHandler;
import cz.mzk.androidzoomifyviewer.tiles.TilePositionInPyramid;
import cz.mzk.androidzoomifyviewer.tiles.TilesDownloader;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.ImageServerResponseException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.InvalidDataException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.OtherIOException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.TooManyRedirectionsException;

/**
 * @author Martin Řehánek
 */
public class DownloadAndSaveTileTask extends ConcurrentAsyncTask<Void, Void, Bitmap> {

    // private static final int THREAD_PRIORITY = Math.min(Thread.MAX_PRIORITY, Thread.MIN_PRIORITY + 1);
    private static final Logger logger = new Logger(DownloadAndSaveTileTask.class);

    private final TilesDownloader downloader;
    private final String zoomifyBaseUrl;
    private final TilePositionInPyramid tilePositionInPyramid;
    private final TileDownloadHandler handler;

    private OtherIOException otherIoException;
    private TooManyRedirectionsException tooManyRedirectionsException;
    private ImageServerResponseException imageServerResponseException;
    private InvalidDataException invalidXmlException;

    /**
     * @param downloader            initialized Tiles downloader, not null
     * @param zoomifyBaseUrl        Zoomify base url, not null
     * @param tilePositionInPyramid Tile id, not null
     * @param handler               Tile download result handler, not null
     */
    public DownloadAndSaveTileTask(TilesDownloader downloader, String zoomifyBaseUrl, TilePositionInPyramid tilePositionInPyramid, TileDownloadHandler handler) {
        this.downloader = downloader;
        this.zoomifyBaseUrl = zoomifyBaseUrl;
        this.tilePositionInPyramid = tilePositionInPyramid;
        this.handler = handler;
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
                Bitmap tile = downloader.downloadTile(tilePositionInPyramid);
                if (!isCancelled()) {
                    if (tile != null) {
                        CacheManager.getTilesCache().storeTile(tile, zoomifyBaseUrl, tilePositionInPyramid);
                        logger.v(String.format("tile downloaded and saved to disk cache: base url: '%s', tile: '%s'",
                                zoomifyBaseUrl, tilePositionInPyramid));
                    } else {
                        // TODO: examine this
                        logger.w("tile is null");
                    }
                } else {
                    logger.v(String
                            .format("tile processing canceled task after downloading and before saving data: base url: '%s', tile: '%s'",
                                    zoomifyBaseUrl, tilePositionInPyramid));
                }
            } else {
                logger.v(String.format(
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
            logger.v("tile processing task finished");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        downloader.unregisterFinishedOrCanceledTask(tilePositionInPyramid);
        //downloader.getTaskRegistry().unregisterTask(tilePositionInPyramid);
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
        //downloader.getTaskRegistry().unregisterTask(tilePositionInPyramid);
        downloader.unregisterFinishedOrCanceledTask(tilePositionInPyramid);
    }

}

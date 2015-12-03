package cz.mzk.androidzoomifyviewer.tiles.zoomify;

import android.graphics.Bitmap;

import cz.mzk.androidzoomifyviewer.CacheManager;
import cz.mzk.androidzoomifyviewer.ConcurrentAsyncTask;
import cz.mzk.androidzoomifyviewer.Logger;
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
    private final ZoomifyTileId zoomifyTileId;
    private final TileDownloadResultHandler handler;

    private OtherIOException otherIoException;
    private TooManyRedirectionsException tooManyRedirectionsException;
    private ImageServerResponseException imageServerResponseException;
    private InvalidDataException invalidXmlException;

    /**
     * @param downloader     initialized Tiles downloader, not null
     * @param zoomifyBaseUrl Zoomify base url, not null
     * @param zoomifyTileId  Tile id, not null
     * @param handler        Tile download result handler, not null
     */
    public DownloadAndSaveTileTask(TilesDownloader downloader, String zoomifyBaseUrl, ZoomifyTileId zoomifyTileId,
                                   TileDownloadResultHandler handler) {
        this.downloader = downloader;
        this.zoomifyBaseUrl = zoomifyBaseUrl;
        this.zoomifyTileId = zoomifyTileId;
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
                Bitmap tile = downloader.downloadTile(zoomifyTileId);
                if (!isCancelled()) {
                    if (tile != null) {
                        CacheManager.getTilesCache().storeTile(tile, zoomifyBaseUrl, zoomifyTileId);
                        logger.v(String.format("tile downloaded and saved to disk cache: base url: '%s', tile: '%s'",
                                zoomifyBaseUrl, zoomifyTileId));
                    } else {
                        // TODO: examine this
                        logger.w("tile is null");
                    }
                } else {
                    logger.v(String
                            .format("tile processing canceled task after downloading and before saving data: base url: '%s', tile: '%s'",
                                    zoomifyBaseUrl, zoomifyTileId));
                }
            } else {
                logger.v(String.format(
                        "tile processing task canceled before download started: base url: '%s', tile: '%s'",
                        zoomifyBaseUrl, zoomifyTileId));
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
        downloader.unregisterFinishedOrCanceledTask(zoomifyTileId);
        //downloader.getTaskRegistry().unregisterTask(zoomifyTileId);
        if (tooManyRedirectionsException != null) {
            handler.onRedirectionLoop(zoomifyTileId, tooManyRedirectionsException.getUrl(),
                    tooManyRedirectionsException.getRedirections());
        } else if (imageServerResponseException != null) {
            handler.onUnhandableResponseCode(zoomifyTileId, imageServerResponseException.getUrl(),
                    imageServerResponseException.getErrorCode());
        } else if (invalidXmlException != null) {
            handler.onInvalidData(zoomifyTileId, invalidXmlException.getUrl(), invalidXmlException.getMessage());
        } else if (otherIoException != null) {
            handler.onDataTransferError(zoomifyTileId, otherIoException.getUrl(), otherIoException.getMessage());
        } else {
            handler.onSuccess(zoomifyTileId, bitmap);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        //downloader.getTaskRegistry().unregisterTask(zoomifyTileId);
        downloader.unregisterFinishedOrCanceledTask(zoomifyTileId);
    }

    public interface TileDownloadResultHandler {

        public void onSuccess(ZoomifyTileId zoomifyTileId, Bitmap bitmap);

        public void onUnhandableResponseCode(ZoomifyTileId zoomifyTileId, String tileUrl, int responseCode);

        public void onRedirectionLoop(ZoomifyTileId zoomifyTileId, String tileUrl, int redirections);

        public void onDataTransferError(ZoomifyTileId zoomifyTileId, String tileUrl, String errorMessage);

        public void onInvalidData(ZoomifyTileId zoomifyTileId, String tileUrl, String errorMessage);

    }

}

package cz.mzk.tiledimageview.images;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.UiThread;

import java.util.List;

import cz.mzk.tiledimageview.TiledImageView;
import cz.mzk.tiledimageview.TiledImageView.TileDownloadErrorListener;
import cz.mzk.tiledimageview.TiledImageView.TileDownloadSuccessListener;
import cz.mzk.tiledimageview.images.metadata.ImageMetadata;

/**
 * Created by Martin Řehánek on 3.12.15.
 */
@UiThread
public interface ImageManager {

    //TASK MANAGEMENT

    public void init(ImageMetadata imageMetadata);

    public void enqueueMetadataInitialization(TiledImageView.MetadataInitializationHandler handler, TiledImageView.MetadataInitializationSuccessListener successListener);

    //public void enqueTileDownload(TilePositionInPyramid tilePositionInPyramid, TiledImageView.TileDownloadErrorListener errorListener, TiledImageView.TileDownloadSuccessListener successListener);

    public void cancelFetchingATilesForLayerExeptForThese(int layerId, List<TilePositionInPyramid> visibleTiles);

    public void inflateTilesMemoryCache(int newMaxSize);

    public void cancelAllTasks();


    //STATE & IMAGE METADATA

    public boolean isInitialized();

    public String getImageBaseUrl();

    public int getImageWidth();

    public int getImageHeight();

    public int getTileTypicalSize();

    public TiledImageProtocol getTiledImageProtocol();


    //CORE - computations with tiles

    public int computeBestLayerId(Rect wholeImageInCanvasCoords);

    public List<TilePositionInPyramid> getVisibleTilesForLayer(int layerId, Rect visibleAreaInImageCoords);

    public Rect getTileAreaInImageCoords(TilePositionInPyramid tilePositionInPyramid);

    public String buildTileUrl(TilePositionInPyramid tilePositionInPyramid);

    //TILE ACCCESS

    /**
     * Retuns tile or null if tile is no longer accessible from memory cache.In this case schedules task to fetch bitmap either from disk or network.
     * After this task is finished (and if not canceled) one of sucessListener or errorListener's method is called.
     *
     * @param tilePositionInPyramid tile id
     * @param successListener
     * @param errorListener
     * @return
     */
    public Bitmap getTile(TilePositionInPyramid tilePositionInPyramid, TileDownloadSuccessListener successListener, TileDownloadErrorListener errorListener);

    /**
     * @param tilePositionInPyramid
     * @return true if tile was found in memory cache. Subsequent call to getTile() can still return null becuse bitmap could be removed from cache as a result of another thread accessing the cache.
     */
    public boolean tileIsAvailableNow(TilePositionInPyramid tilePositionInPyramid);


}

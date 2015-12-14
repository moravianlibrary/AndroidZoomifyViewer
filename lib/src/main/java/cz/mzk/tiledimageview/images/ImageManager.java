package cz.mzk.tiledimageview.images;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.UiThread;

import java.util.List;

import cz.mzk.tiledimageview.TiledImageView.MetadataInitializationListener;
import cz.mzk.tiledimageview.TiledImageView.MetadataInitializationSuccessListener;
import cz.mzk.tiledimageview.TiledImageView.TileDownloadErrorListener;
import cz.mzk.tiledimageview.TiledImageView.TileDownloadSuccessListener;
import cz.mzk.tiledimageview.images.metadata.ImageMetadata;

/**
 * Created by Martin Řehánek on 3.12.15.
 */
@UiThread
public interface ImageManager {

    // INITIALIZATION

    public void init(ImageMetadata imageMetadata);


    // METADATA/TILE ACCESS

    /**
     * Returns metadata if found in member variable or memory cache. Otherwise asyncTask is scheduled to fetch metadata from either disk cache or network.
     *
     * @param successListener internal TiledImageView listener
     * @param listener        client listener
     * @return
     */
    public ImageMetadata getMetadata(MetadataInitializationSuccessListener successListener, MetadataInitializationListener listener);

    /**
     * @param tilePositionInPyramid
     * @return true if tile was found in memory cache. Subsequent call to getTile() can still return null becuse bitmap could be removed from cache as a result of another thread accessing the cache.
     */
    public boolean tileIsAvailableNow(TilePositionInPyramid tilePositionInPyramid);

    /**
     * Retuns tile's bitmap if it is found in memory cache. If not, async task to  fetch bitmap from eitger disk cache or network is scheduled.
     * After this task is finished (and if not canceled) one of sucessListener or errorListener's method is called.
     *
     * @param tilePositionInPyramid tile id
     * @param successListener       internal TiledImageView listener
     * @param errorListener         client listener
     * @return
     */
    public Bitmap getTile(TilePositionInPyramid tilePositionInPyramid, TileDownloadSuccessListener successListener, TileDownloadErrorListener errorListener);


    //CANCELING RUNNING/SCHEDULED TASKS

    public void cancelFetchingTilesForLayerExeptForThese(int layerId, List<TilePositionInPyramid> visibleTiles);

    public void cancelFetchingAllTilesForLayersSmallerThan(int layer);

    public void cancelFetchingAllTilesForLayersBiggerThan(int layer);

    public void inflateTilesMemoryCache(int newMaxSize);

    public void cancelAllTasks();


    //STATE & IMAGE METADATA ACCESS

    public boolean isInitialized();

    public String getImageBaseUrl();

    public int getImageWidth();

    public int getImageHeight();

    public int getTileTypicalSize();

    public TiledImageProtocol getTiledImageProtocol();


    //COMPUTATIONS WITH IMAGE METADATA

    public int computeBestLayerId(Rect wholeImageInCanvasCoords);

    public List<TilePositionInPyramid> getVisibleTilesForLayer(int layerId, Rect visibleAreaInImageCoords);

    public Rect getTileAreaInImageCoords(TilePositionInPyramid tilePositionInPyramid);

    public String buildTileUrl(TilePositionInPyramid tilePositionInPyramid);


}

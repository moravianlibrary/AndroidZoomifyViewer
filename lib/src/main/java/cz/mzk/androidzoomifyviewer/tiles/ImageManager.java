package cz.mzk.androidzoomifyviewer.tiles;

import android.graphics.Rect;

import java.util.List;

import cz.mzk.androidzoomifyviewer.tiles.metadata.ImageMetadata;
import cz.mzk.androidzoomifyviewer.tiles.zoomify.Layer;
import cz.mzk.androidzoomifyviewer.viewer.RectD;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;

/**
 * Created by Martin Řehánek on 3.12.15.
 */
public interface ImageManager {

    //TASK MANAGEMENT

    public void init(ImageMetadata imageMetadata);

    public void enqueueMetadataInitialization(TiledImageView.MetadataInitializationHandler handler, TiledImageView.MetadataInitializationSuccessListener successListener);

    public void enqueTileDownload(TilePositionInPyramid tilePositionInPyramid, TiledImageView.TileDownloadErrorListener errorListener, TiledImageView.TileDownloadSuccessListener successListener);

    public void cancelFetchingATilesForLayerExeptForThese(int layerId, List<TilePositionInPyramid> visibleTiles);

    public void cancelAllTasks();


    //STATE & IMAGE METADATA

    public boolean isInitialized();

    public int getImageWidth();

    public int getImageHeight();

    public int getTileTypicalSize();

    public List<Layer> getLayers();


    //CORE - computations with tiles

    public int computeBestLayerId(Rect wholeImageInCanvasCoords);

    public List<TilePositionInPyramid> getVisibleTilesForLayer(int layerId, RectD visibleAreaInImageCoords);

    public Rect getTileAreaInImageCoords(TilePositionInPyramid tilePositionInPyramid);

    public String buildTileUrl(TilePositionInPyramid tilePositionInPyramid);


    // TODO: 8.12.15
    public TilesFormat getTilesFormat();

    //TMP

    //jenom na testy, TODO: odstranit odsud
    @Deprecated
    public int[] calculateTileCoordsFromPointInImageCoords(int layerId, int pixelX, int pixelY);


}

package cz.mzk.androidzoomifyviewer.tiles;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.List;

import cz.mzk.androidzoomifyviewer.tiles.exceptions.ImageServerResponseException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.InvalidDataException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.OtherIOException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.TooManyRedirectionsException;
import cz.mzk.androidzoomifyviewer.tiles.zoomify.Layer;
import cz.mzk.androidzoomifyviewer.viewer.RectD;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;

/**
 * Created by Martin Řehánek on 3.12.15.
 */
public interface ImageManager {

    //METADATA INITIALIZATION

    //public void initImageMetadataAsync(MetadataInitializationHandler handler);

    public void initImageMetadataAsync(TiledImageView.MetadataInitializationHandler handler, TiledImageView.MetadataInitializationSuccessListener successListener);


    public boolean isInitialized();


    //TASK MANAGEMENT

    public void enqueTileDownload(TilePositionInPyramid tilePositionInPyramid, TiledImageView.TileDownloadErrorListener handler, TiledImageView.TileDownloadSuccessListener tileDownloadSuccessListener);

    public void unregisterFinishedOrCanceledTask(TilePositionInPyramid tilePositionInPyramid);

    public void cancelFetchingATilesForLayerExeptForThese(int layerId, List<TilePositionInPyramid> visibleTiles);

    // TODO: 7.12.15 also task to initialize metadata

    public void cancelAllTasks();

    //IMAGE METADATA

    public int getImageWidth();

    public int getImageHeight();

    public int getTileTypicalSize();

    public List<Layer> getLayers();


    //CORE - computations with tiles

    public int computeBestLayerId(Rect wholeImageInCanvasCoords);

    public List<TilePositionInPyramid> getVisibleTilesForLayer(int layerId, RectD visibleAreaInImageCoords);

    public Rect getTileAreaInImageCoords(TilePositionInPyramid tilePositionInPyramid);

    public String buildTileUrl(TilePositionInPyramid tilePositionInPyramid);


    //A CO TOHLE? Vola zase jenom task. Mela by se lip oddelit sprava tasku od samotneho stahovani


    //TMP

    //jenom na testy, TODO: odstranit odsud
    @Deprecated
    public int[] calculateTileCoordsFromPointInImageCoords(int layerId, int pixelX, int pixelY);

    //TODO: presunout do budouciho Downloaderu

    public Bitmap downloadTile(TilePositionInPyramid tilePositionInPyramid) throws OtherIOException, TooManyRedirectionsException, ImageServerResponseException;

    public void initImageMetadata() throws OtherIOException, TooManyRedirectionsException, ImageServerResponseException, InvalidDataException;


}

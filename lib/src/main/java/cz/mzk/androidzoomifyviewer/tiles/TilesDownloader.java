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

/**
 * Created by Martin Řehánek on 3.12.15.
 */
public interface TilesDownloader {

    //METADATA INITIALIZATION

    public void initImageMetadataAsync(MetadataInitializationHandler handler);

    public boolean isInitialized();


    //TASK MANAGEMENT

    public void enqueTileDownload(TilePositionInPyramid tilePositionInPyramid, TileDownloadHandler handler);

    public void unregisterFinishedOrCanceledTask(TilePositionInPyramid tilePositionInPyramid);

    public void cancelFetchingATilesForLayerExeptForThese(int layerId, List<TilePositionInPyramid> visibleTiles);

    //vsechno, krome samotne inicializace metadat

    public void cancelAllTasks();

    // TODO: 3.12.15 pouzivat
    // zabije uplne vsechny tasky a uvolni pripadne dalsi zdroje
    public void destroy();


    //IMAGE METADATA

    public int getImageWidth();

    public int getImageHeight();

    public int getTileTypicalSize();

    public List<Layer> getLayers();


    //CORE - computations with tiles

    public int computeBestLayerId(Rect wholeImageInCanvasCoords);

    public List<TilePositionInPyramid> getVisibleTilesForLayer(int layerId, RectD visibleAreaInImageCoords);

    public Rect getTileAreaInImageCoords(TilePositionInPyramid tilePositionInPyramid);


    //A CO TOHLE? Vola zase jenom task. Mela by se lip oddelit sprava tasku od samotneho stahovani


    //TMP

    //jenom na testy, TODO: odstranit odsud
    @Deprecated
    public int[] calculateTileCoordsFromPointInImageCoords(int layerId, int pixelX, int pixelY);

    //TODO: presunout do budouciho Downloaderu

    public Bitmap downloadTile(TilePositionInPyramid tilePositionInPyramid) throws OtherIOException, TooManyRedirectionsException, ImageServerResponseException;

    public void initImageMetadata() throws OtherIOException, TooManyRedirectionsException, ImageServerResponseException, InvalidDataException;

}

package cz.mzk.androidzoomifyviewer.tiles;

import android.graphics.Bitmap;

import java.util.List;

import cz.mzk.androidzoomifyviewer.tiles.exceptions.ImageServerResponseException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.InvalidDataException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.OtherIOException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.TooManyRedirectionsException;
import cz.mzk.androidzoomifyviewer.tiles.zoomify.DownloadAndSaveTileTask;
import cz.mzk.androidzoomifyviewer.tiles.zoomify.ImageProperties;
import cz.mzk.androidzoomifyviewer.tiles.zoomify.Layer;
import cz.mzk.androidzoomifyviewer.tiles.zoomify.ZoomifyTileId;
import cz.mzk.androidzoomifyviewer.viewer.Point;

/**
 * Created by Martin Řehánek on 3.12.15.
 */
public interface TilesDownloader {

    //METADATA INITIALIZATION

    public void initializeWithImageProperties() throws OtherIOException, TooManyRedirectionsException, ImageServerResponseException, InvalidDataException;


    //TASK MANAGEMENT

    public void enqueTileFetching(ZoomifyTileId zoomifyTileId, DownloadAndSaveTileTask.TileDownloadResultHandler handler);

    public void unregisterFinishedOrCanceledTask(ZoomifyTileId zoomifyTileId);

    public void cancelFetchingTilesOutOfSight(int layerId, ZoomifyTileId.TileCoords bottomRightVisibleTileCoords, ZoomifyTileId.TileCoords topLeftVisibleTileCoords);

    //vsechno, krome samotne inicializace metadat

    public void cancelAllTasks();

    // TODO: 3.12.15 pouzivat
    // zabije uplne vsechny tasky a uvolni pripadne dalsi zdroje
    public void destroy();


    //GETTING METADATA
    //todo: spoutu veci nebude asi potreba, vybery vrstev apod by se mely delat v implementaci downloaderu, pro iif to bude absolutne jinak

    public ImageProperties getImageProperties();

    // TODO: 3.12.15 to patri do pravomoci downloaderu
    //public int[] calculateTileCoordsFromPointInImageCoords(int layerId, int pixelX, int pixelY);

    public int[] calculateTileCoordsFromPointInImageCoords(int layerId, int pixelX, int pixelY);

    public int computeBestLayerId(int imageInCanvasWidthPx, int imageInCanvasHeightPx);

    public int[] getTileSizesInImageCoords(ZoomifyTileId zoomifyTileId);

    public ZoomifyTileId.TileCoords calculateTileCoordsFromPointInImageCoords(int layerId, Point pointInMageCoords);

    public double getLayerWidth(int layerId);

    public double getLayerHeight(int layerId);

    public List<Layer> getLayers();


    //A CO TOHLE? Vola zase jenom task. Mela by se lip oddelit sprava tasku od samotneho stahovani

    public Bitmap downloadTile(ZoomifyTileId zoomifyTileId) throws OtherIOException, TooManyRedirectionsException, ImageServerResponseException;


}

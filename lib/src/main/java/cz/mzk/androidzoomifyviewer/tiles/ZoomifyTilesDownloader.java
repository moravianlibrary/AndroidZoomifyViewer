package cz.mzk.androidzoomifyviewer.tiles;

import android.graphics.Bitmap;

import java.util.List;

import cz.mzk.androidzoomifyviewer.tiles.exceptions.ImageServerResponseException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.InvalidDataException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.OtherIOException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.TooManyRedirectionsException;

/**
 * Created by Martin Řehánek on 3.12.15.
 */
public interface ZoomifyTilesDownloader {
    public ImageProperties getImageProperties();

    public void initializeWithImageProperties() throws OtherIOException, TooManyRedirectionsException, ImageServerResponseException,
            InvalidDataException;

    public DownloadAndSaveTileTasksRegistry getTaskRegistry();

    public Bitmap downloadTile(ZoomifyTileId zoomifyTileId) throws OtherIOException, TooManyRedirectionsException,
            ImageServerResponseException;

    public int[] getTileCoordsFromPointCoords(int layerId, int pixelX, int pixelY);

    public int computeBestLayerId(int imageInCanvasWidthPx, int imageInCanvasHeightPx);

    public int[] getTileSizesInImageCoords(ZoomifyTileId zoomifyTileId);

    public double getLayerWidth(int layerId);

    public double getLayerHeight(int layerId);

    public List<Layer> getLayers();
}

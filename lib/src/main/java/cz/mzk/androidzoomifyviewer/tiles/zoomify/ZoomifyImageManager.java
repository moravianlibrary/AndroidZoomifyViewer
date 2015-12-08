package cz.mzk.androidzoomifyviewer.tiles.zoomify;

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.tiles.ImageManager;
import cz.mzk.androidzoomifyviewer.tiles.TileDimensionsInImage;
import cz.mzk.androidzoomifyviewer.tiles.TilePositionInPyramid;
import cz.mzk.androidzoomifyviewer.tiles.TiledImageProtocol;
import cz.mzk.androidzoomifyviewer.tiles.metadata.ImageMetadata;
import cz.mzk.androidzoomifyviewer.tiles.tasks.ImageManagerTaskRegistry;
import cz.mzk.androidzoomifyviewer.viewer.Point;
import cz.mzk.androidzoomifyviewer.viewer.RectD;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;
import cz.mzk.androidzoomifyviewer.viewer.Utils;

/**
 * This class encapsulates image metadata from ZoomifyImageMetadata.xml and provides method for downloading tiles (bitmaps) for given
 * image.
 *
 * @author Martin Řehánek
 */
public class ZoomifyImageManager implements ImageManager {

    /**
     * @link https://github.com/moravianlibrary/AndroidZoomifyViewer/issues/25
     */
    public static final boolean COMPUTE_NUMBER_OF_LAYERS_ROUND_CALCULATION = true;

    private static final Logger LOGGER = new Logger(ZoomifyImageManager.class);

    private final ImageManagerTaskRegistry taskRegistry = new ImageManagerTaskRegistry(this);

    private final String mBaseUrl;
    private final double mPxRatio;
    private final String mImagePropertiesUrl; // TODO: 8.12.15 lazy initialization?

    private ImageMetadata mImageMetadata;
    private List<Layer> layers;


    /**
     * @param zoomifyBaseUrl Zoomify base url.
     * @param pxRatio        Ratio between pixels and density-independent pixels for computing image_size_in_canvas. Must be between 0 and 1.
     *                       dpRatio = (1-pxRatio)
     */
    public ZoomifyImageManager(String zoomifyBaseUrl, double pxRatio) {
        if (pxRatio < 0 || pxRatio > 1) {
            throw new IllegalArgumentException("pxRation not in <0;1> interval");
        } else {
            mPxRatio = pxRatio;
        }
        if (zoomifyBaseUrl == null || zoomifyBaseUrl.isEmpty()) {
            throw new IllegalArgumentException("baseUrl is null or empty");
        } else {
            mBaseUrl = zoomifyBaseUrl.endsWith("/") ? zoomifyBaseUrl : zoomifyBaseUrl + '/';
        }
        mImagePropertiesUrl = mBaseUrl + "ImageProperties.xml";
    }

    private void checkInitialized() {
        if (mImageMetadata != null) {
            throw new IllegalStateException("not initialized (" + mBaseUrl + ")");
        }
    }

    @Override
    public int getImageWidth() {
        checkInitialized();
        return mImageMetadata.getWidth();
    }

    @Override
    public int getImageHeight() {
        checkInitialized();
        return mImageMetadata.getHeight();
    }

    @Override
    public int getTileTypicalSize() {
        checkInitialized();
        return mImageMetadata.getTileSize();
    }

    @Override
    public void init(ImageMetadata imageMetadata) {
        if (mImageMetadata != null) {
            throw new IllegalStateException("already initialized (" + mImagePropertiesUrl + ")");
        } else {
            LOGGER.d("initImageMetadata: " + mImagePropertiesUrl);
        }
        mImageMetadata = imageMetadata;
        LOGGER.d(mImageMetadata.toString());
        layers = initLayers();
    }

    @Override
    public void enqueueMetadataInitialization(TiledImageView.MetadataInitializationHandler handler, TiledImageView.MetadataInitializationSuccessListener successListener) {
        taskRegistry.enqueueMetadataInitializationTask(mImagePropertiesUrl, handler, successListener);
    }

    @Override
    public boolean isInitialized() {
        return mImageMetadata != null;
    }

    private List<Layer> initLayers() {
        int numberOfLayers = computeNumberOfLayers();
        // LOGGER.d( "layers #: " + numberOfLayers);
        List<Layer> result = new ArrayList<Layer>(numberOfLayers);
        double width = mImageMetadata.getWidth();
        double height = mImageMetadata.getHeight();
        double tileSize = mImageMetadata.getTileSize();
        for (int layer = 0; layer < numberOfLayers; layer++) {
            double powerOf2 = Utils.pow(2, numberOfLayers - layer - 1);
            int tilesHorizontal = (int) Math.ceil(Math.floor(width / powerOf2) / tileSize);
            int tilesVertical = (int) Math.ceil(Math.floor(height / powerOf2) / tileSize);
            result.add(new Layer(tilesVertical, tilesHorizontal));
        }
        return result;
    }

    private int computeNumberOfLayers() {
        float tilesInLayer = -1f;
        int tilesInLayerInt = -1;
        float maxDimension = Math.max(mImageMetadata.getWidth(), mImageMetadata.getHeight());
        float tileSize = mImageMetadata.getTileSize();
        int i = 0;
        do {
            tilesInLayer = (maxDimension / (tileSize * Utils.pow(2, i)));
            i++;
            tilesInLayerInt = (int) Math.ceil(COMPUTE_NUMBER_OF_LAYERS_ROUND_CALCULATION ? Utils.round(tilesInLayer, 3)
                    : tilesInLayer);
        } while (tilesInLayerInt != 1);
        return i;
    }

    @Override
    public List<TilePositionInPyramid> getVisibleTilesForLayer(int layerId, RectD visibleAreaInImageCoords) {
        TilePositionInPyramid.TilePositionInLayer[] corners = getCornerVisibleTilesCoords(layerId, visibleAreaInImageCoords);
        TilePositionInPyramid.TilePositionInLayer topLeftVisibleTilePositionInLayer = corners[0];
        TilePositionInPyramid.TilePositionInLayer bottomRightVisibleTilePositionInLayer = corners[1];

        List<TilePositionInPyramid> visibleTiles = new ArrayList<>();
        for (int y = topLeftVisibleTilePositionInLayer.row; y <= bottomRightVisibleTilePositionInLayer.row; y++) {
            for (int x = topLeftVisibleTilePositionInLayer.column; x <= bottomRightVisibleTilePositionInLayer.column; x++) {
                visibleTiles.add(new TilePositionInPyramid(layerId, x, y));
            }
        }
        return visibleTiles;

    }

    private TilePositionInPyramid.TilePositionInLayer[] getCornerVisibleTilesCoords(int layerId, RectD visibleAreaInImageCoords) {
        int imageWidthMinusOne = mImageMetadata.getWidth() - 1;
        int imageHeightMinusOne = mImageMetadata.getHeight() - 1;

        int topLeftVisibleX = Utils.collapseToInterval((int) visibleAreaInImageCoords.left, 0, imageWidthMinusOne);
        int topLeftVisibleY = Utils.collapseToInterval((int) visibleAreaInImageCoords.top, 0, imageHeightMinusOne);
        int bottomRightVisibleX = Utils.collapseToInterval((int) visibleAreaInImageCoords.right, 0, imageWidthMinusOne);
        int bottomRightVisibleY = Utils.collapseToInterval((int) visibleAreaInImageCoords.bottom, 0, imageHeightMinusOne);
        Point topLeftVisibleInImageCoords = new Point(topLeftVisibleX, topLeftVisibleY);
        Point bottomRightVisibleInImageCoords = new Point(bottomRightVisibleX, bottomRightVisibleY);

        // TestTags.TILES.d( "top left: [" + topLeftVisibleX + "," + topLeftVisibleY + "]");
        // TestTags.TILES.d( "bottom right: [" + bottomRightVisibleX + "," + bottomRightVisibleY + "]");
        // TODO: 4.12.15 udelat private metodu calculateTileCoordsFromPointInImageCoords, pokud se vola jen odsud
        TilePositionInPyramid.TilePositionInLayer topLeftVisibleTile = calculateTileCoordsFromPointInImageCoords(layerId, topLeftVisibleInImageCoords);
        TilePositionInPyramid.TilePositionInLayer bottomRightVisibleTile = calculateTileCoordsFromPointInImageCoords(layerId, bottomRightVisibleInImageCoords);
        // TestTags.TILES.d( "top_left:     " + Utils.toString(topLeftVisibleTileCoords));
        // TestTags.TILES.d( "bottom_right: " + Utils.toString(bottomRightVisibleTileCoords));
        return new TilePositionInPyramid.TilePositionInLayer[]{topLeftVisibleTile, bottomRightVisibleTile};
    }

    /**
     * @link http://www.staremapy.cz/zoomify-analyza/
     */
    private int computeTileGroup(TilePositionInPyramid tilePositionInPyramid) {
        int column = tilePositionInPyramid.getPositionInLayer().column;
        int row = tilePositionInPyramid.getPositionInLayer().row;
        int level = tilePositionInPyramid.getLayer();
        double tileSize = mImageMetadata.getTileSize();
        double width = mImageMetadata.getWidth();
        double height = mImageMetadata.getHeight();
        double depth = layers.size();
        // LOGGER.d( tilePositionInPyramid.toString());
        // LOGGER.d( "column: " + column + ", row: " + row + ", d: " + depth + ", l: " + level);
        // LOGGER.d( "width: " + width + ", height: " + height + ", tileSize: " + tileSize);

        double first = Math.ceil(Math.floor(width / Math.pow(2, depth - level - 1)) / tileSize);
        double index = column + row * first;
        for (int i = 1; i <= level; i++) {
            index += Math.ceil(Math.floor(width / Math.pow(2, depth - i)) / tileSize)
                    * Math.ceil(Math.floor(height / Math.pow(2, depth - i)) / tileSize);
        }
        // LOGGER.d( "index: " + index);
        int result = (int) (index / tileSize);
        // LOGGER.d( "tile group: " + result);
        return result;
    }

    private String buildTileUrl(int tileGroup, TilePositionInPyramid tilePositionInPyramid) {
        StringBuilder builder = new StringBuilder();
        builder.append(mBaseUrl).append("TileGroup").append(tileGroup).append('/');
        builder.append(tilePositionInPyramid.getLayer()).append('-')
                .append(tilePositionInPyramid.getPositionInLayer().column).append('-')
                .append(tilePositionInPyramid.getPositionInLayer().row)
                .append(".jpg");
        return builder.toString();
    }

    @Override
    public int[] calculateTileCoordsFromPointInImageCoords(int layerId, int pixelX, int pixelY) {
        int[] result = new int[2];
        TilePositionInPyramid.TilePositionInLayer tilePositionInLayer = calculateTileCoordsFromPointInImageCoords(layerId, new Point(pixelX, pixelY));
        result[0] = tilePositionInLayer.column;
        result[1] = tilePositionInLayer.row;
        return result;
    }

    //@Override
    private TilePositionInPyramid.TilePositionInLayer calculateTileCoordsFromPointInImageCoords(int layerId, Point pointInMageCoords) {
        checkInitialized();
        if (layerId < 0 || layerId >= layers.size()) {
            throw new IllegalArgumentException("layer out of range: " + layerId);
        }

        if (pointInMageCoords.x < 0 || pointInMageCoords.x >= mImageMetadata.getWidth()) {
            throw new IllegalArgumentException("x coord out of range: " + pointInMageCoords.x);
        }
        if (pointInMageCoords.y < 0 || pointInMageCoords.y >= mImageMetadata.getHeight()) {
            throw new IllegalArgumentException("y coord out of range: " + pointInMageCoords.y);
        }

        // optimization, zero layer is whole image with coords 0,0
        if (layerId == 0) {
            return new TilePositionInPyramid.TilePositionInLayer(0, 0);
        }
        // LOGGER.d( "getting picture for layer=" + layerId + ", x=" + pixelX +
        // ", y=" + pixelY);
        // Log.d(TestTags.TILES, "layers: " + layers.size() + ", layer: " + layerId);
        double step = mImageMetadata.getTileSize() * Math.pow(2, layers.size() - layerId - 1);
        // Log.d(TestTags.TILES, "step: " + step);
        // x
        double cx_step = pointInMageCoords.x / step;
        // Log.d(TestTags.TILES, (cx_step - 1) + " < x <= " + cx_step);
        int x = (int) Math.floor(cx_step);
        // y
        double cy_step = pointInMageCoords.y / step;
        // Log.d(TestTags.TILES, (cy_step - 1) + " < y <= " + cy_step);
        int y = (int) Math.floor(cy_step);
        TilePositionInPyramid.TilePositionInLayer result = new TilePositionInPyramid.TilePositionInLayer(x, y);
        // Log.d(TestTags.TILES, "px: [" + pixelX + "," + pixelY + "] -> " + Utils.toString(result));
        return result;

    }

    /**
     * Selects highest layer, tiles of which would all fit into the image area in canvas (with exception of border tiles
     * partially overflowing).
     * <p/>
     * For determining this, canvas width/height can be taken into account either in pixels or density independent pixels or
     * combination of both (weighted arithemtic mean). Parameter mPxRatio is used for this. For example height of image area in
     * canvas is being computed this way:
     * <p/>
     * height = mPxRatio heightPx + (1-mPxRatio) * heightDp
     * <p/>
     * So to use px only, mPxRatio should be 1.0. To use dp, mPxRatio should be 0.0.
     * <p/>
     * Be aware that for devices with big displays and high display density putting big weight to px might caus extensive number
     * of tiles needed. That would lead to lots of parallel tasks for fetching tiles and hence decreased ui responsivness due to
     * network/disk (cache) access and thread synchronization. Also possible app crashes.
     * <p/>
     * On the other hand to much weight to dp could cause demanding not-deep-enought tile layer and as a consequence image would
     * seem blurry. Also devices with small displays and very high resolution would with great weight on px require unneccessary
     * number of tiles which most of people would not appreciate anyway because of limitations of human eyes.
     *
     * @param wholeImageInCanvasCoords
     * @return id of layer, that would best fill image are in canvas with only border tiles overflowing that area
     */
    @Override
    public int computeBestLayerId(Rect wholeImageInCanvasCoords) {
        checkInitialized();
        double dpRatio = 1.0 - mPxRatio;
        if (mPxRatio < 0.0) {
            throw new IllegalArgumentException("px ratio must be >= 0");
        } else if (mPxRatio > 1.0) {
            throw new IllegalArgumentException("px ratio must be <= 1");
        }

        int imageInCanvasWidthDp = 0;
        int imageInCanvasHeightDp = 0;
        if (mPxRatio < 1.0) {// optimization: initCache only if will be used
            imageInCanvasWidthDp = Utils.pxToDp(wholeImageInCanvasCoords.width());
            imageInCanvasHeightDp = Utils.pxToDp(wholeImageInCanvasCoords.height());
        }
        int imgInCanvasWidth = (int) (imageInCanvasWidthDp * dpRatio + wholeImageInCanvasCoords.width() * mPxRatio);
        int imgInCanvasHeight = (int) (imageInCanvasHeightDp * dpRatio + wholeImageInCanvasCoords.height() * mPxRatio);
        // int layersNum = layers.size();
        // if (true) {
        // // if (layersNum>=3){
        // // return 2;
        // // }else
        // if (layersNum >= 2) {
        // return 1;
        // } else {
        // return 0;
        // }
        // }
        if (true) {
            return bestLayerAtLeastAsBigAs(imgInCanvasWidth, imgInCanvasHeight);
        }

        int topLayer = layers.size() - 1;
        // Log.d(TestTags.TEST, "imgInCanvas: width: " + imgInCanvasWidth + ", height: " + imgInCanvasHeight);
        for (int layerId = topLayer; layerId >= 0; layerId--) {
            int horizontalTiles = layers.get(layerId).getTilesHorizontal();
            int layerWidthWithoutLastTile = mImageMetadata.getTileSize() * (horizontalTiles - 1);
            // int testWidth = mImageMetadata.getTileSize() * horizontalTiles;

            int verticalTiles = layers.get(layerId).getTilesVertical();
            int layerHeightWithoutLastTile = mImageMetadata.getTileSize() * (verticalTiles - 1);
            // int testHeight = mImageMetadata.getTileSize() * verticalTiles;
            double layerWidth = getLayerWidth(layerId);
            // double result = mImageMetadata.getWidth() / Utils.pow(2, layers.size() - layerId - 1);
            double layerHeight = getLayerHeight(layerId);
            // Log.d(TestTags.TEST, "layer " + layerId + ": width: " + layerWidth + ", height: " + layerHeight);
            if (layerWidth <= imgInCanvasWidth && layerHeight <= imgInCanvasHeight) {
                // Log.d(TestTags.TEST, "selected layer: " + layerId);
                return layerId;
                // return layerId == topLayer ? topLayer : layerId + 1;
            }

            // if (testWidth <= imgInCanvasWidth && testHeight <= imgInCanvasHeight) {
            // if (layerWidthWithoutLastTile <= imgInCanvasWidth && layerHeightWithoutLastTile <= imgInCanvasHeight) {
            // return layerId;
            // }
        }
        int layerId = 0;
        // int layerId = layers.size() - 1;
        // Log.d(TestTags.TEST, "selected layer: " + layerId);
        // return layers.size() - 1;
        return layerId;
    }

    private int bestLayerAtLeastAsBigAs(int imgInCanvasWidth, int imageInCanvasHeight) {
        // Log.d(TestTags.TEST, "imgInCanvas: width: " + imgInCanvasWidth + ", height: " + imageInCanvasHeight);
        for (int layerId = 0; layerId < layers.size(); layerId++) {
            double layerWidth = getLayerWidth(layerId);
            double layerHeight = getLayerHeight(layerId);
            if (layerWidth >= imgInCanvasWidth && layerHeight >= imageInCanvasHeight) {
                return layerId;
            }
        }
        return layers.size() - 1;
    }


    @Override
    public Rect getTileAreaInImageCoords(TilePositionInPyramid tilePositionInPyramid) {
        TileDimensionsInImage tileSizesInImage = calculateTileDimensionsInImageCoords(tilePositionInPyramid);
        int left = tileSizesInImage.basicSize * tilePositionInPyramid.getPositionInLayer().column;
        int right = left + tileSizesInImage.actualWidth;
        int top = tileSizesInImage.basicSize * tilePositionInPyramid.getPositionInLayer().row;
        int bottom = top + tileSizesInImage.actualHeight;
        return new Rect(left, top, right, bottom);
    }

    @Override
    public String buildTileUrl(TilePositionInPyramid tilePositionInPyramid) {
        // TODO: 7.12.15 definitively cache this
        int tileGroup = computeTileGroup(tilePositionInPyramid);
        String tileUrl = buildTileUrl(tileGroup, tilePositionInPyramid);
        LOGGER.v("TILE URL: " + tileUrl);
        return tileUrl;
    }

    @Override
    public TiledImageProtocol getTiledImageProtocol() {
        return TiledImageProtocol.ZOOMIFY;
    }

    private TileDimensionsInImage calculateTileDimensionsInImageCoords(TilePositionInPyramid tilePositionInPyramid) {
        checkInitialized();
        int basicSize = getTilesBasicSizeInImageCoordsForGivenLayer(tilePositionInPyramid.getLayer());
        int width = getTileWidthInImageCoords(tilePositionInPyramid.getLayer(), tilePositionInPyramid.getPositionInLayer().column, basicSize);
        int height = getTileHeightInImageCoords(tilePositionInPyramid.getLayer(), tilePositionInPyramid.getPositionInLayer().row, basicSize);
        return new TileDimensionsInImage(basicSize, width, height);
    }

    private int getTilesBasicSizeInImageCoordsForGivenLayer(int layerId) {
        // TODO: 4.12.15 cachovat
        return mImageMetadata.getTileSize() * (int) (Math.pow(2, layers.size() - layerId - 1));
    }

    // TODO: sjednotit slovnik, tomuhle obcas rikam 'step'
    private int getTileWidthInImageCoords(int layerId, int tileHorizontalIndex, int basicSize) {
        if (tileHorizontalIndex == layers.get(layerId).getTilesHorizontal() - 1) {
            int result = mImageMetadata.getWidth() - basicSize * (layers.get(layerId).getTilesHorizontal() - 1);
            // LOGGER.d( "TILE FAR RIGHT WIDTH: " + result);
            return result;
        } else {
            return basicSize;
        }
    }

    private int getTileHeightInImageCoords(int layerId, int tileVerticalIndex, int basicSize) {
        // Log.d(TestTags.TILES, "tileVerticalIndex:" + tileVerticalIndex);
        int verticalTilesForLayer = layers.get(layerId).getTilesVertical();
        // LOGGER.d( "vertical tiles for layer " + layerId + ": " + verticalTilesForLayer);
        int lastTilesIndex = verticalTilesForLayer - 1;
        // Log.d(TestTags.TILES, "tiles vertical for layer: " + layerId + ": " + tilesVerticalForLayer);
        // Log.d(TestTags.TILES, "last tile's index: " + layerId + ": " + lastTilesIndex);
        // Log.d(TestTags.TEST, "tileVerticalI: " + tileVerticalIndex + ", lastTilesI: " + lastTilesIndex);
        if (tileVerticalIndex == lastTilesIndex) {
            return mImageMetadata.getHeight() - basicSize * (lastTilesIndex);
        } else {
            return basicSize;
        }
    }

    private double getLayerWidth(int layerId) {
        checkInitialized();
        // TODO: 4.12.15 possibly cache this if it's being called frequently
        double result = mImageMetadata.getWidth() / Utils.pow(2, layers.size() - layerId - 1);
        // LOGGER.d( "layer " + layerId + ", width=" + result + " px");
        return result;
    }

    private double getLayerHeight(int layerId) {
        checkInitialized();
        // TODO: 4.12.15 possibly cache this if it's being called frequently
        return mImageMetadata.getHeight() / Utils.pow(2, layers.size() - layerId - 1);
    }

    @Override
    public List<Layer> getLayers() {
        checkInitialized();
        return layers;
    }


    @Override
    public void cancelAllTasks() {
        taskRegistry.cancelAllTasks();
    }

    @Override
    public void enqueTileDownload(TilePositionInPyramid tilePositionInPyramid, TiledImageView.TileDownloadErrorListener errorListener, TiledImageView.TileDownloadSuccessListener successListener) {
        String tileImageUrl = buildTileUrl(tilePositionInPyramid);
        taskRegistry.enqueueTileDownloadTask(tilePositionInPyramid, tileImageUrl, errorListener, successListener);
    }

    @Override
    public void cancelFetchingATilesForLayerExeptForThese(int layerId, List<TilePositionInPyramid> visibleTiles) {
        checkInitialized();
        for (TilePositionInPyramid runningTilePositionInPyramid : taskRegistry.getAllTileDownloadTaskIds()) {
            if (runningTilePositionInPyramid.getLayer() == layerId) {
                if (!visibleTiles.contains(runningTilePositionInPyramid)) {
                    boolean wasCanceled = taskRegistry.cancel(runningTilePositionInPyramid);
                    // if (wasCanceled) {
                    // canceled++;
                    // }
                }
            }
        }
    }

}

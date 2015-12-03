package cz.mzk.androidzoomifyviewer.tiles;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.mzk.androidzoomifyviewer.CacheManager;
import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.cache.ImagePropertiesCache;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.ImageServerResponseException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.InvalidDataException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.OtherIOException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.TooManyRedirectionsException;
import cz.mzk.androidzoomifyviewer.viewer.Utils;

/**
 * This class encapsulates image metadata from ImageProperties.xml and provides method for downloading tiles (bitmaps) for given
 * image.
 *
 * @author Martin Řehánek
 */
public class TilesDownloader {

    /**
     * @link https://github.com/moravianlibrary/AndroidZoomifyViewer/issues/25
     */
    public static final boolean COMPUTE_NUMBER_OF_LAYERS_ROUND_CALCULATION = true;
    public static final int MAX_REDIRECTIONS = 5;
    public static final int IMAGE_PROPERTIES_TIMEOUT = 3000;
    public static final int TILES_TIMEOUT = 5000;

    private static final Logger logger = new Logger(TilesDownloader.class);

    private final DownloadAndSaveTileTasksRegistry taskRegistry = new DownloadAndSaveTileTasksRegistry(this);

    private final String mBaseUrl;
    private final double mPxRatio;

    private String mImagePropertiesUrl;
    private boolean initialized = false;
    private ImageProperties imageProperties;
    private List<Layer> layers;


    /**
     * @param baseUrl Zoomify base url.
     * @param pxRatio Ratio between pixels and density-independent pixels for computing image_size_in_canvas. Must be between 0 and 1.
     *                dpRatio = (1-pxRatio)
     */
    public TilesDownloader(String baseUrl, double pxRatio) {
        if (pxRatio < 0 || pxRatio > 1) {
            throw new IllegalArgumentException("pxRation not in <0;1> interval");
        } else {
            mPxRatio = pxRatio;
        }
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("baseUrl is null or empty");
        } else {
            mBaseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + '/';
        }
        mImagePropertiesUrl = mBaseUrl + "ImageProperties.xml";
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("not initialized (" + mBaseUrl + ")");
        }
    }

    public ImageProperties getImageProperties() {
        checkInitialized();
        return imageProperties;
    }

    /**
     * Initializes TilesDownloader by downloading and processing ImageProperties.xml. Instead of downloading, ImageProperties.xml
     * may be loaded from cache. Also ImageProperties.xml is saved to cache after being downloaded.
     *
     * @throws IllegalStateException        If this method had already been called
     * @throws TooManyRedirectionsException If max. number of redirections exceeded before downloading ImageProperties.xml. This probably means redirection
     *                                      loop.
     * @throws ImageServerResponseException If zoomify server response code for ImageProperties.xml cannot be handled here (everything apart from OK and
     *                                      3xx redirections).
     * @throws InvalidDataException         If ImageProperties.xml contains invalid data - empty content, not well formed xml, missing required attributes,
     *                                      etc.
     * @throws OtherIOException             In case of other error (invalid URL, error transfering data, ...)
     */
    public void init() throws OtherIOException, TooManyRedirectionsException, ImageServerResponseException,
            InvalidDataException {
        if (initialized) {
            throw new IllegalStateException("already initialized (" + mBaseUrl + ")");
        } else {
            logger.d("initializing: " + mBaseUrl);
        }
        HttpURLConnection.setFollowRedirects(false);
        String propertiesXml = getImagePropertiesXml();
        imageProperties = ImagePropertiesParser.parse(propertiesXml, mImagePropertiesUrl);
        logger.d(imageProperties.toString());
        layers = initLayers();
        initialized = true;
    }

    private String getImagePropertiesXml() throws OtherIOException, TooManyRedirectionsException,
            ImageServerResponseException {
        ImagePropertiesCache cache = CacheManager.getImagePropertiesCache();
        String fromCache = cache.getXml(mBaseUrl);
        if (fromCache != null) {
            return fromCache;
        } else {
            String downloaded = downloadPropertiesXml(mImagePropertiesUrl, MAX_REDIRECTIONS);
            cache.storeXml(downloaded, mBaseUrl);
            return downloaded;
        }
    }

    private String downloadPropertiesXml(String urlString, int remainingRedirections)
            throws TooManyRedirectionsException, ImageServerResponseException, OtherIOException {
        logger.d("downloading metadata from " + urlString);
        if (remainingRedirections == 0) {
            throw new TooManyRedirectionsException(urlString, MAX_REDIRECTIONS);
        }
        HttpURLConnection urlConnection = null;
        // logger.d( urlString + " remaining redirections: " +
        // remainingRedirections);
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(IMAGE_PROPERTIES_TIMEOUT);
            int responseCode = urlConnection.getResponseCode();
            // logger.d( "http code: " + responseCode);
            String location = urlConnection.getHeaderField("Location");
            switch (responseCode) {
                case 200:
                    return stringFromUrlConnection(urlConnection);
                case 300:
                    if (location == null || location.isEmpty()) {
                        throw new ImageServerResponseException(urlString, responseCode);
                    }
                    urlConnection.disconnect();
                    return downloadPropertiesXml(location, remainingRedirections - 1);
                case 301:
                    if (location == null || location.isEmpty()) {
                        throw new ImageServerResponseException(urlString, responseCode);
                    }
                    mImagePropertiesUrl = location;
                    urlConnection.disconnect();
                    return downloadPropertiesXml(location, remainingRedirections - 1);
                case 302:
                case 303:
                case 305:
                case 307:
                    if (location == null || location.isEmpty()) {
                        throw new ImageServerResponseException(urlString, responseCode);
                    }
                    urlConnection.disconnect();
                    return downloadPropertiesXml(location, remainingRedirections - 1);
                default:
                    throw new ImageServerResponseException(urlString, responseCode);
            }
        } catch (IOException e) {
            throw new OtherIOException(e.getMessage(), mImagePropertiesUrl);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private String stringFromUrlConnection(HttpURLConnection urlConnection) throws IOException {
        InputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            in = new BufferedInputStream(urlConnection.getInputStream());
            byte[] buffer = new byte[8 * 1024];
            out = new ByteArrayOutputStream();
            int readBytes = 0;
            while ((readBytes = in.read(buffer)) != -1) {
                out.write(buffer, 0, readBytes);
            }
            return out.toString();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    private List<Layer> initLayers() {
        int numberOfLayers = computeNumberOfLayers();
        // logger.d( "layers #: " + numberOfLayers);
        List<Layer> result = new ArrayList<Layer>(numberOfLayers);
        double width = imageProperties.getWidth();
        double height = imageProperties.getHeight();
        double tileSize = imageProperties.getTileSize();
        for (int layer = 0; layer < numberOfLayers; layer++) {
            double powerOf2 = Utils.pow(2, numberOfLayers - layer - 1);
            int tilesHorizontal = (int) Math.ceil(Math.floor(width / powerOf2) / tileSize);
            int tilesVertical = (int) Math.ceil(Math.floor(height / powerOf2) / tileSize);
            result.add(new Layer(tilesVertical, tilesHorizontal));
        }
        return result;
    }

    private int computeNumberOfLayers() {
        // double ai = -1;
        float tilesInLayer = -1f;
        int tilesInLayerInt = -1;
        // Log.d("blabla", "width: " + imageProperties.getWidth() + ", height: " + imageProperties.getHeight());
        float maxDimension = Math.max(imageProperties.getWidth(), imageProperties.getHeight());
        float tileSize = imageProperties.getTileSize();
        int i = 0;
        do {
            // if (i == 0) {
            // ai = Math.ceil(maxDimension / tileSize);
            // // ai = maxDimension / tileSize;
            // } else {
            // ai = Math.ceil(ai / 2.0);
            // // ai = ai / 2.0;
            // }
            tilesInLayer = (maxDimension / (tileSize * Utils.pow(2, i)));
            // Log.d("blabla", "a" + i + " : b" + i + " = " + ai + " : " + bi);
            // Log.d("blabla", "testI: " + tilesInLayer);
            i++;
            tilesInLayerInt = (int) Math.ceil(COMPUTE_NUMBER_OF_LAYERS_ROUND_CALCULATION ? Utils.round(tilesInLayer, 3)
                    : tilesInLayer);
        } while (tilesInLayerInt != 1);
        // } while (ai != 1);
        // float diff = tilesInLayer - 1.0f;
        // Log.d("blabla", "diff: " + diff);
        // Log.d("blabla", "layers: " + i);
        // double testD = Math.ceil(Utils.logarithm(maxMeasurement, 2) / ((double) imageProperties.getTileSize()));
        // Log.d("blabla", "d!=" + testD);
        return i;
    }

    public DownloadAndSaveTileTasksRegistry getTaskRegistry() {
        checkInitialized();
        return taskRegistry;
    }

    /**
     * Downloads tile from zoomify server. TODO: InvalidDataException
     *
     * @param tileId Tile id.
     * @return
     * @throws IllegalStateException        If methodi init had not been called yet.
     * @throws TooManyRedirectionsException If max. number of redirections exceeded before downloading tile. This probably means redirection loop.
     * @throws ImageServerResponseException If zoomify server response code for tile cannot be handled here (everything apart from OK and 3xx
     *                                      redirections).
     * @throws InvalidDataException         If tile contains invalid data.
     * @throws OtherIOException             In case of other IO error (invalid URL, error transfering data, ...)
     */
    public Bitmap downloadTile(TileId tileId) throws OtherIOException, TooManyRedirectionsException,
            ImageServerResponseException {
        checkInitialized();
        int tileGroup = computeTileGroup(tileId);
        String tileUrl = buildTileUrl(tileGroup, tileId);
        logger.v("TILE URL: " + tileUrl);
        return downloadTile(tileUrl, MAX_REDIRECTIONS);
    }

    private Bitmap downloadTile(String tileUrl, int remainingRedirections) throws TooManyRedirectionsException,
            ImageServerResponseException, OtherIOException {
        logger.d("downloading tile from " + tileUrl);
        if (remainingRedirections == 0) {
            throw new TooManyRedirectionsException(tileUrl, MAX_REDIRECTIONS);
        }
        // logger.d( tileUrl + " remaining redirections: " +
        // remainingRedirections);
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(tileUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(TILES_TIMEOUT);
            int responseCode = urlConnection.getResponseCode();
            switch (responseCode) {
                case 200:
                    return bitmapFromUrlConnection(urlConnection);
                case 300:
                case 301:
                case 302:
                case 303:
                case 305:
                case 307:
                    String location = urlConnection.getHeaderField("Location");
                    if (location == null || location.isEmpty()) {
                        throw new ImageServerResponseException(tileUrl, responseCode);
                    } else {
                        urlConnection.disconnect();
                        return downloadTile(location, remainingRedirections - 1);
                    }
                default:
                    throw new ImageServerResponseException(tileUrl, responseCode);
            }
        } catch (IOException e) {
            throw new OtherIOException(e.getMessage(), tileUrl);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private Bitmap bitmapFromUrlConnection(HttpURLConnection urlConnection) throws IOException {
        InputStream in = null;
        try {
            in = new BufferedInputStream(urlConnection.getInputStream());
            return BitmapFactory.decodeStream(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * @link http://www.staremapy.cz/zoomify-analyza/
     */
    private int computeTileGroup(TileId tileId) {
        int x = tileId.getX();
        int y = tileId.getY();
        int level = tileId.getLayer();
        double tileSize = imageProperties.getTileSize();
        double width = imageProperties.getWidth();
        double height = imageProperties.getHeight();
        double depth = layers.size();

        // logger.d( tileId.toString());
        // logger.d( "x: " + x + ", y: " + y + ", d: " + depth + ", l: " +
        // level);
        // logger.d( "width: " + width + ", height: " + height + ", tileSize: "
        // + tileSize);

        double first = Math.ceil(Math.floor(width / Math.pow(2, depth - level - 1)) / tileSize);
        double index = x + y * first;
        for (int i = 1; i <= level; i++) {
            index += Math.ceil(Math.floor(width / Math.pow(2, depth - i)) / tileSize)
                    * Math.ceil(Math.floor(height / Math.pow(2, depth - i)) / tileSize);
        }
        // logger.d( "index: " + index);
        int result = (int) (index / tileSize);
        // logger.d( "tile group: " + result);
        return result;
    }

    private String buildTileUrl(int tileGroup, TileId tileId) {
        StringBuilder builder = new StringBuilder();
        builder.append(mBaseUrl).append("TileGroup").append(tileGroup).append('/');
        builder.append(tileId.getLayer()).append('-').append(tileId.getX()).append('-').append(tileId.getY())
                .append(".jpg");
        return builder.toString();
    }

    public List<Layer> getLayers() {
        checkInitialized();
        return layers;
    }

    public int[] getTileCoords(int layerId, int pixelX, int pixelY) {
        checkInitialized();
        if (layerId < 0 || layerId >= layers.size()) {
            throw new IllegalArgumentException("layer out of range: " + layerId);
        }

        if (pixelX < 0 || pixelX >= imageProperties.getWidth()) {
            throw new IllegalArgumentException("x coord out of range: " + pixelX);
        }
        if (pixelY < 0 || pixelY >= imageProperties.getHeight()) {
            throw new IllegalArgumentException("y coord out of range: " + pixelY);
        }

        // optimization
        if (layerId == 0) {
            return new int[]{0, 0};
        }
        // logger.d( "getting picture for layer=" + layerId + ", x=" + pixelX +
        // ", y=" + pixelY);
        // Log.d(TestTags.TILES, "layers: " + layers.size() + ", layer: " + layerId);
        double step = imageProperties.getTileSize() * Math.pow(2, layers.size() - layerId - 1);
        // Log.d(TestTags.TILES, "step: " + step);
        // x
        double cx_step = pixelX / step;
        // Log.d(TestTags.TILES, (cx_step - 1) + " < x <= " + cx_step);
        int x = (int) Math.floor(cx_step);
        // y
        double cy_step = pixelY / step;
        // Log.d(TestTags.TILES, (cy_step - 1) + " < y <= " + cy_step);
        int y = (int) Math.floor(cy_step);
        int[] result = new int[]{x, y};
        // Log.d(TestTags.TILES, "px: [" + pixelX + "," + pixelY + "] -> " + Utils.toString(result));
        return result;
    }

    /**
     * Selects highest layer, tiles of which whould all fit into the image area in canvas (with exception of border tiles
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
     * @param imageInCanvasWidthPx
     * @param imageInCanvasHeightPx
     * @return id of layer, that would best fill image are in canvas with only border tiles overflowing that area
     */
    public int computeBestLayerId(int imageInCanvasWidthPx, int imageInCanvasHeightPx) {
        checkInitialized();
        double dpRatio = 1.0 - mPxRatio;
        if (mPxRatio < 0.0) {
            throw new IllegalArgumentException("px ratio must be >= 0");
        } else if (mPxRatio > 1.0) {
            throw new IllegalArgumentException("px ratio must be <= 1");
        }

        int imageInCanvasWidthDp = 0;
        int imageInCanvasHeightDp = 0;
        if (mPxRatio < 1.0) {// optimization: initialize only if will be used
            imageInCanvasWidthDp = Utils.pxToDp(imageInCanvasWidthPx);
            imageInCanvasHeightDp = Utils.pxToDp(imageInCanvasHeightPx);
        }
        int imgInCanvasWidth = (int) (imageInCanvasWidthDp * dpRatio + imageInCanvasWidthPx * mPxRatio);
        int imgInCanvasHeight = (int) (imageInCanvasHeightDp * dpRatio + imageInCanvasHeightPx * mPxRatio);
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
            int layerWidthWithoutLastTile = imageProperties.getTileSize() * (horizontalTiles - 1);
            // int testWidth = imageProperties.getTileSize() * horizontalTiles;

            int verticalTiles = layers.get(layerId).getTilesVertical();
            int layerHeightWithoutLastTile = imageProperties.getTileSize() * (verticalTiles - 1);
            // int testHeight = imageProperties.getTileSize() * verticalTiles;
            double layerWidth = getLayerWidth(layerId);
            // double result = imageProperties.getWidth() / Utils.pow(2, layers.size() - layerId - 1);
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

    /**
     * @param tileId
     * @return int array of size 3 containing dimensions in image coordinates for given tile. First item is basic size, that is
     * width/hight of typical tile (i.e. every tile except the border ones - unless there are only border ones). Second
     * item is tile's width, third is it's height.
     */
    public int[] getTileSizesInImageCoords(TileId tileId) {
        checkInitialized();
        int basicSize = getTilesBasicSizeInImageCoords(tileId.getLayer());
        int width = getTileWidthInImageCoords(tileId.getLayer(), tileId.getX(), basicSize);
        int height = getTileHeightInImageCoords(tileId.getLayer(), tileId.getY(), basicSize);
        return new int[]{basicSize, width, height};
    }

    private int getTilesBasicSizeInImageCoords(int layerId) {
        return imageProperties.getTileSize() * (int) (Math.pow(2, layers.size() - layerId - 1));
    }

    // TODO: sjednotit slovnik, tomuhle obcas rikam 'step'
    private int getTileWidthInImageCoords(int layerId, int tileHorizontalIndex, int basicSize) {
        if (tileHorizontalIndex == layers.get(layerId).getTilesHorizontal() - 1) {
            int result = imageProperties.getWidth() - basicSize * (layers.get(layerId).getTilesHorizontal() - 1);
            // logger.d( "TILE FAR RIGHT WIDTH: " + result);
            return result;
        } else {
            return basicSize;
        }
    }

    private int getTileHeightInImageCoords(int layerId, int tileVerticalIndex, int basicSize) {
        // Log.d(TestTags.TILES, "tileVerticalIndex:" + tileVerticalIndex);
        int verticalTilesForLayer = layers.get(layerId).getTilesVertical();
        // logger.d( "vertical tiles for layer " + layerId + ": " + verticalTilesForLayer);
        int lastTilesIndex = verticalTilesForLayer - 1;
        // Log.d(TestTags.TILES, "tiles vertical for layer: " + layerId + ": " + tilesVerticalForLayer);
        // Log.d(TestTags.TILES, "last tile's index: " + layerId + ": " + lastTilesIndex);
        // Log.d(TestTags.TEST, "tileVerticalI: " + tileVerticalIndex + ", lastTilesI: " + lastTilesIndex);
        if (tileVerticalIndex == lastTilesIndex) {
            return imageProperties.getHeight() - basicSize * (lastTilesIndex);
        } else {
            return basicSize;
        }
    }

    public double getLayerWidth(int layerId) {
        checkInitialized();
        double result = imageProperties.getWidth() / Utils.pow(2, layers.size() - layerId - 1);
        // logger.d( "layer " + layerId + ", width=" + result + " px");
        return result;
    }

    public double getLayerHeight(int layerId) {
        checkInitialized();
        return imageProperties.getHeight() / Utils.pow(2, layers.size() - layerId - 1);
    }

    public String getImagePropertiesUrl() {
        checkInitialized();
        return mImagePropertiesUrl;
    }

}

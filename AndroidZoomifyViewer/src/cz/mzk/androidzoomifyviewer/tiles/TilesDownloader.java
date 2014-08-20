package cz.mzk.androidzoomifyviewer.tiles;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import cz.mzk.androidzoomifyviewer.CacheManager;
import cz.mzk.androidzoomifyviewer.cache.ImagePropertiesCache;

/**
 * This class encapsulates image metadata from ImageProperties.xml and provides
 * method for downloading tiles (bitmaps) for given image.
 * 
 * @author Martin Řehánek
 * 
 */
public class TilesDownloader {

	private static final String TAG = TilesDownloader.class.getSimpleName();
	private static final int MAX_REDIRECTIONS = 5;
	private static final int IMAGE_PROPERTIES_TIMEOUT = 3000;
	private static final int TILES_TIMEOUT = 5000;
	private final DownloadAndSaveTileTasksRegistry taskRegistry = new DownloadAndSaveTileTasksRegistry();
	private String baseUrl;
	private String imagePropertiesUrl;
	private boolean initialized = false;
	private ImageProperties imageProperties;
	private List<Layer> layers;

	public TilesDownloader(String baseUrl) {
		this.baseUrl = baseUrl;
		this.imagePropertiesUrl = toImagePropertiesUrl(baseUrl);
	};

	private String toImagePropertiesUrl(String baseUrl) {
		return baseUrl + "ImageProperties.xml";
	}

	public ImageProperties getImageProperties() {
		if (!initialized) {
			throw new IllegalStateException("not initialized (" + baseUrl + ")");
		}
		return imageProperties;
	}

	/**
	 * Initializes TilesDownloader by downloading and processing
	 * ImageProperties.xml. Instead of downloading, ImageProperties.xml may be
	 * loaded from cache. Also ImageProperties.xml is saved to cache after being
	 * downloaded.
	 * 
	 * @throws IllegalStateException
	 *             If this method had already been called
	 * @throws TooManyRedirectionsException
	 *             If max. number of redirections exceeded before downloading
	 *             ImageProperties.xml. This probably means redirection loop.
	 * @throws ImageServerResponseException
	 *             If zoomify server response code for ImageProperties.xml
	 *             cannot be handled here (everything apart from OK and 3xx
	 *             redirections).
	 * @throws InvalidDataException
	 *             If ImageProperties.xml contains invalid data - empty content,
	 *             not well formed xml, missing required attributes, etc.
	 * @throws OtherIOException
	 *             In case of other error (invalid URL, error transfering data,
	 *             ...)
	 */
	public void init() throws OtherIOException, TooManyRedirectionsException, ImageServerResponseException,
			InvalidDataException {
		if (initialized) {
			throw new IllegalStateException("already initialized (" + baseUrl + ")");
		} else {
			Log.d(TAG, "initializing: " + baseUrl);
		}
		HttpURLConnection.setFollowRedirects(false);
		// String imagePropertiesUrl = baseUrl + "ImageProperties.xml";
		String propertiesXml = getImagePropertiesXml();
		imageProperties = loadFromXml(propertiesXml);
		Log.d(TAG, imageProperties.toString());
		layers = initLayers();
		initialized = true;
	}

	private String getImagePropertiesXml() throws OtherIOException, TooManyRedirectionsException,
			ImageServerResponseException {
		ImagePropertiesCache cache = CacheManager.getImagePropertiesCache();
		String fromCache = cache.getXml(baseUrl);
		if (fromCache != null) {
			return fromCache;
		} else {
			String downloaded = downloadPropertiesXml(imagePropertiesUrl, MAX_REDIRECTIONS);
			cache.storeXml(downloaded, baseUrl);
			return downloaded;
		}
	}

	private String downloadPropertiesXml(String urlString, int remainingRedirections)
			throws TooManyRedirectionsException, ImageServerResponseException, OtherIOException {
		if (remainingRedirections == 0) {
			throw new TooManyRedirectionsException(urlString, MAX_REDIRECTIONS);
		}
		HttpURLConnection urlConnection = null;
		// Log.d(TAG, urlString + " remaining redirections: " +
		// remainingRedirections);
		try {
			URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setReadTimeout(IMAGE_PROPERTIES_TIMEOUT);
			int responseCode = urlConnection.getResponseCode();
			// Log.d(TAG, "http code: " + responseCode);
			String location = urlConnection.getHeaderField("Location");
			switch (responseCode) {
			case 200:
				return stringFromUrlConnection(urlConnection);
			case 300:
				if (location == null || location.isEmpty()) {
					throw new ImageServerResponseException(urlString, responseCode);
				}
				return downloadPropertiesXml(location, remainingRedirections - 1);
			case 301:
				if (location == null || location.isEmpty()) {
					throw new ImageServerResponseException(urlString, responseCode);
				}
				imagePropertiesUrl = toImagePropertiesUrl(location);
				return downloadPropertiesXml(location, remainingRedirections - 1);
			case 302:
			case 303:
			case 305:
			case 307:
				if (location == null || location.isEmpty()) {
					throw new ImageServerResponseException(urlString, responseCode);
				}
				return downloadPropertiesXml(location, remainingRedirections - 1);
			default:
				throw new ImageServerResponseException(urlString, responseCode);
			}
		} catch (IOException e) {
			throw new OtherIOException(e.getMessage(), imagePropertiesUrl);
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

	private ImageProperties loadFromXml(String propertiesXml) throws InvalidDataException, OtherIOException {
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(false);
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(new StringReader(propertiesXml));
			boolean elementImagePropertiesStarted = false;
			boolean elementImagePropertiesEnded = false;
			int width = -1;
			int height = -1;
			int numtiles = -1; // pro kontrolu
			int numimages = -1;
			double version = 0.0;
			int tileSize = -1;
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					if (!xpp.getName().equals("IMAGE_PROPERTIES")) {
						throw new InvalidDataException(imagePropertiesUrl, "Unexpected element " + xpp.getName());
					} else {
						elementImagePropertiesStarted = true;
						width = getAttributeIntegerValue(xpp, "WIDTH");
						height = getAttributeIntegerValue(xpp, "HEIGHT");
						numtiles = getAttributeIntegerValue(xpp, "NUMTILES");
						numimages = getAttributeIntegerValue(xpp, "NUMIMAGES");
						tileSize = getAttributeIntegerValue(xpp, "TILESIZE");
						version = getAttributeDoubleValue(xpp, "VERSION");
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					if (!xpp.getName().equals("IMAGE_PROPERTIES")) {
						throw new InvalidDataException(imagePropertiesUrl, "Unexpected element " + xpp.getName());
					} else {
						elementImagePropertiesEnded = true;
					}
				}
				eventType = xpp.next();
			}
			if (!elementImagePropertiesStarted) {
				throw new InvalidDataException(imagePropertiesUrl, "Element IMAGE_PROPERTIES not found");
			}
			if (!elementImagePropertiesEnded) {
				throw new InvalidDataException(imagePropertiesUrl, "Element IMAGE_PROPERTIES not closed");
			}
			if (version != 1.8) {
				throw new InvalidDataException(imagePropertiesUrl, "Unsupported tiles version: " + version);
			}
			if (numimages != 1) {
				throw new InvalidDataException(imagePropertiesUrl, "Unsupported number of images: " + numimages);
			}
			// TODO: check numTiles
			return new ImageProperties(width, height, numtiles, tileSize);
		} catch (XmlPullParserException e1) {
			throw new InvalidDataException(imagePropertiesUrl, e1.getMessage());
		} catch (IOException e) {
			throw new OtherIOException(e.getMessage(), imagePropertiesUrl);
		}
	}

	private Double getAttributeDoubleValue(XmlPullParser xpp, String attrName) throws InvalidDataException {
		String attrValue = getAttributeStringValue(xpp, attrName);
		try {
			return Double.valueOf(attrValue);
		} catch (NumberFormatException e) {
			throw new InvalidDataException(imagePropertiesUrl, "invalid content of attribute " + attrName + ": '"
					+ attrValue + "'");
		}
	}

	private int getAttributeIntegerValue(XmlPullParser xpp, String attrName) throws InvalidDataException {
		String attrValue = getAttributeStringValue(xpp, attrName);
		try {
			return Integer.valueOf(attrValue);
		} catch (NumberFormatException e) {
			throw new InvalidDataException(imagePropertiesUrl, "invalid content of attribute " + attrName + ": '"
					+ attrValue + "'");
		}
	}

	private String getAttributeStringValue(XmlPullParser xpp, String attrName) throws InvalidDataException {
		String attrValue = xpp.getAttributeValue(null, attrName);
		if (attrValue == null) {
			throw new InvalidDataException(imagePropertiesUrl, "missing attribute " + attrName);
		}
		if (attrValue.isEmpty()) {
			throw new InvalidDataException(imagePropertiesUrl, "empty attribute " + attrName);
		}
		return attrValue;
	}

	private List<Layer> initLayers() {
		int numberOfLayers = computeNumberOfLayers();
		// Log.d(TAG, "layers: " + numberOfLayers);
		List<Layer> result = new ArrayList<Layer>(numberOfLayers);
		double width = imageProperties.getWidth();
		double height = imageProperties.getHeight();
		double tileSize = imageProperties.getTileSize();
		for (int layer = 0; layer < numberOfLayers; layer++) {
			int sizeHorizontal = (int) Math
					.ceil(Math.floor(width / Math.pow(2, numberOfLayers - layer - 1)) / tileSize);
			int sizeVertical = (int) Math.ceil(Math.floor(height / Math.pow(2, numberOfLayers - layer - 1)) / tileSize);
			// Log.d(TAG, "layer=" + layer + ": horizontal=" + sizeHorizontal +
			// ", vertical=" + sizeVertical);
			result.add(new Layer(sizeVertical, sizeHorizontal));
		}
		return result;
	}

	private int computeNumberOfLayers() {
		double ai = (int) Math.ceil(Math.max(imageProperties.getWidth(), imageProperties.getHeight())
				/ imageProperties.getTileSize());
		int i = 0;
		do {
			i++;
			ai = Math.ceil(ai / 2.0);
			// System.out.println("a" + i + ": " + ai);
		} while (ai != 1);
		return i + 1;
	}

	public DownloadAndSaveTileTasksRegistry getTaskRegistry() {
		if (!initialized) {
			throw new IllegalStateException("not initialized (" + baseUrl + ")");
		}
		return taskRegistry;
	}

	/**
	 * Downloads tile from zoomify server. TODO: InvalidDataException
	 * 
	 * @param tileId
	 *            Tile id.
	 * @return
	 * @throws IllegalStateException
	 *             If methodi init had not been called yet.
	 * @throws TooManyRedirectionsException
	 *             If max. number of redirections exceeded before downloading
	 *             tile. This probably means redirection loop.
	 * @throws ImageServerResponseException
	 *             If zoomify server response code for tile cannot be handled
	 *             here (everything apart from OK and 3xx redirections).
	 * @throws InvalidDataException
	 *             If tile contains invalid data.
	 * @throws OtherIOException
	 *             In case of other IO error (invalid URL, error transfering
	 *             data, ...)
	 */
	public Bitmap downloadTile(TileId tileId) throws OtherIOException, TooManyRedirectionsException,
			ImageServerResponseException {
		int tileGroup = computeTileGroup(tileId);
		//int tileGroup = 0;
		String tileUrl = buildTileUrl(tileGroup, tileId);
		Log.d(TAG, "TILE URL: " + tileUrl);
		return downloadTile(tileUrl, MAX_REDIRECTIONS);
	}

	private Bitmap downloadTile(String tileUrl, int remainingRedirections) throws TooManyRedirectionsException,
			ImageServerResponseException, OtherIOException {
		if (remainingRedirections == 0) {
			throw new TooManyRedirectionsException(tileUrl, MAX_REDIRECTIONS);
		}
		// Log.d(TAG, tileUrl + " remaining redirections: " +
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
	 * @see http://www.staremapy.cz/zoomify-analyza/
	 */
	private int computeTileGroup(TileId tileId) {
		int x = tileId.getX();
		int y = tileId.getY();
		double tileSize = imageProperties.getTileSize();
		double width = imageProperties.getWidth();
		double height = imageProperties.getHeight();
		double layersNum = layers.size();
		int level = imageProperties.getLevel();
		// Log.d(TAG, "x: " + x + ", y: " + y + ", d: " + layersNum + ", l: " +
		// level);
		// Log.d(TAG, "width: " + width + ", height: " + height + ", tileSize: "
		// + tileSize);
		double first = Math.ceil(Math.floor(width / Math.pow(2, layersNum - level - 1)) / tileSize);
		double index = x + y * first;
		for (int i = 1; i <= level; i++) {
			index += Math.ceil(Math.floor(width / Math.pow(2, layersNum - i)) / tileSize)
					* Math.ceil(Math.floor(height / Math.pow(2, layersNum - i)) / tileSize);
		}
		// Log.d(TAG, "index: " + index);
		int result = (int) (index % tileSize);
		// Log.d(TAG, "tile group: " + result);
		return result;
	}

	private String buildTileUrl(int tileGroup, TileId tileId) {
		StringBuilder builder = new StringBuilder();
		builder.append(baseUrl).append("TileGroup").append(tileGroup).append('/');
		builder.append(tileId.getLayer()).append('-').append(tileId.getX()).append('-').append(tileId.getY())
				.append(".jpg");
		return builder.toString();
	}

	public List<Layer> getLayers() {
		if (!initialized) {
			throw new IllegalStateException("not initialized (" + baseUrl + ")");
		}
		return layers;
	}

	public int[] getTileCoords(int layerId, int pixelX, int pixelY) {
		if (!initialized) {
			throw new IllegalStateException("not initialized (" + baseUrl + ")");
		}
		if (layerId < 0 || layerId >= layers.size()) {
			throw new IllegalArgumentException("layer out of range: " + layerId);
		}

		if (pixelX < 0 || pixelX >= imageProperties.getWidth()) {
			throw new IllegalArgumentException("x coord out of range: " + pixelX);
		}
		if (pixelY < 0 || pixelY >= imageProperties.getHeight()) {
			throw new IllegalArgumentException("y coord out of range: " + pixelY);
		}
		if (layerId == 0) {
			return new int[] { 0, 0 };
		}
		// Log.d(TAG, "getting picture for layer=" + layerId + ", x=" + pixelX +
		// ", y=" + pixelY);
		double step = imageProperties.getTileSize() * Math.pow(2, layers.size() - layerId - 1);
		// Log.d(TAG, "step:" + step);
		// x
		double cx_step = pixelX / step;
		// Log.d(TAG, (cx_step - 1) + " < x <= " + cx_step);
		int x = (int) Math.floor(cx_step);
		// y
		double cy_step = pixelY / step;
		// Log.d(TAG, (cy_step - 1) + " < y <= " + cy_step);
		int y = (int) Math.floor(cy_step);
		return new int[] { x, y };
	}

	public int getBestLayerId(int canvasImageWidthDp, int canvasImageHeightDp) {
		if (!initialized) {
			throw new IllegalStateException("not initialized (" + baseUrl + ")");
		}
		for (int layerId = layers.size() - 1; layerId >= 0; layerId--) {
			int horizontalTiles = layers.get(layerId).getTilesHorizontal();
			int layerWidthWithoutLastTile = imageProperties.getTileSize() * (horizontalTiles - 1);

			int verticalTiles = layers.get(layerId).getTilesVertical();
			int layerHeightWithoutLastTile = imageProperties.getTileSize() * (verticalTiles - 1);
			// Log.d(TAG, "layer=" + layerId + ", totalWidthMinusOneTile=" +
			// widthInLayerWithoutLastTile);

			// tj. vrstva, ktera by pri vyskladani zabrala cely obrazek v canvas
			// v dp
			if (layerWidthWithoutLastTile <= canvasImageHeightDp && layerHeightWithoutLastTile <= canvasImageHeightDp) {
				return layerId;
			}
		}
		return layers.size() - 1;
	}

	/**
	 * 
	 * @param layerId
	 * @return size (width and height) of all tiles except the last one in each
	 *         row or column since these are not allways squares.
	 */
	public int getTilesSizeInImageCoords(int layerId) {
		if (!initialized) {
			throw new IllegalStateException("not initialized (" + baseUrl + ")");
		}
		return imageProperties.getTileSize() * (int) (Math.pow(2, layers.size() - layerId - 1));
	}

	// TODO: sjednotit slovnik, tomuhle obcas rikam 'step'
	public int getTileWidthInImage(int layerId, int tileHorizontalIndex) {
		if (!initialized) {
			throw new IllegalStateException("not initialized (" + baseUrl + ")");
		}
		int basicSize = imageProperties.getTileSize() * (int) (Math.pow(2, layers.size() - layerId - 1));
		if (tileHorizontalIndex == layers.get(layerId).getTilesHorizontal() - 1) {
			int result = imageProperties.getWidth() - basicSize * (layers.get(layerId).getTilesHorizontal() - 1);
			// Log.d(TAG, "TILE FAR RIGHT WIDTH: " + result);
			return result;
		} else {
			return basicSize;
		}
	}

	public int getTileHeightInImage(int layerId, int tileVerticalIndex) {
		if (!initialized) {
			throw new IllegalStateException("not initialized (" + baseUrl + ")");
		}
		int basicSize = imageProperties.getTileSize() * (int) (Math.pow(2, layers.size() - layerId - 1));
		if (tileVerticalIndex == layers.get(layerId).getTilesVertical() - 1) {
			return imageProperties.getHeight() - basicSize * (layers.get(layerId).getTilesVertical() - 1);
		} else {
			return basicSize;
		}
	}

	public double getLayerWidth(int layerId) {
		if (!initialized) {
			throw new IllegalStateException("not initialized (" + baseUrl + ")");
		}
		double result = (double) imageProperties.getWidth() / Math.pow(2, layers.size() - layerId - 1);
		// Log.d(TAG, "layer " + layerId + ", width=" + result + " px");
		return result;
	}

	public double getLayerHeight(int layerId) {
		if (!initialized) {
			throw new IllegalStateException("not initialized (" + baseUrl + ")");
		}
		return (double) imageProperties.getHeight() / Math.pow(2, layers.size() - layerId - 1);
	}

	public class TooManyRedirectionsException extends Exception {
		private static final long serialVersionUID = -6653657291115225081L;
		private final String url;
		private final int redirections;

		public TooManyRedirectionsException(String url, int redirections) {
			super();
			this.url = url;
			this.redirections = redirections;
		}

		public String getUrl() {
			return url;
		}

		public int getRedirections() {
			return redirections;
		}
	}

	public class ImageServerResponseException extends Exception {
		private static final long serialVersionUID = -9136216127329035507L;
		private final int errorCode;
		private final String url;

		public ImageServerResponseException(String url, int errorCode) {
			super();
			this.url = url;
			this.errorCode = errorCode;
		}

		public int getErrorCode() {
			return errorCode;
		}

		public String getUrl() {
			return url;
		}
	}

	public class InvalidDataException extends Exception {
		private static final long serialVersionUID = -6344968475737321154L;
		private final String url;

		public InvalidDataException(String url, String message) {
			super(message);
			this.url = url;
		}

		public String getUrl() {
			return url;
		}
	}

	public String getImagePropertiesUrl() {
		return imagePropertiesUrl;
	}

	public class OtherIOException extends Exception {
		private static final long serialVersionUID = 8890317963393224790L;
		private final String url;

		public OtherIOException(String message, String url) {
			super(message);
			this.url = url;
		}

		public String getUrl() {
			return url;
		}
	}

}

package cz.mzk.androidzoomifyviewer.viewer;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import cz.mzk.androidzoomifyviewer.CacheManager;
import cz.mzk.androidzoomifyviewer.cache.TilesCache;
import cz.mzk.androidzoomifyviewer.tiles.DownloadAndCacheTileTask;
import cz.mzk.androidzoomifyviewer.tiles.DownloadAndCacheTileTask.TileDownloadResultHandler;
import cz.mzk.androidzoomifyviewer.tiles.DownloadAndSaveTileTasksRegistry;
import cz.mzk.androidzoomifyviewer.tiles.ImageProperties;
import cz.mzk.androidzoomifyviewer.tiles.InitTilesDownloaderTask;
import cz.mzk.androidzoomifyviewer.tiles.TileId;
import cz.mzk.androidzoomifyviewer.tiles.TilesDownloader;

/**
 * @author Martin Řehánek
 * 
 */
public class TiledImageView extends View implements OnGestureListener, OnDoubleTapListener {

	private static final String TAG = TiledImageView.class.getSimpleName();

	private static final boolean DEV_MODE = false;
	private DevTools devTools = null;
	private ImageCoordsPoints testPoints = null;

	private boolean resizeFactorsInitialized = false;
	private double mInitialResizingFactor = 0.0;
	private double mMinResizingFactor = 0.0;

	private String mZoomifyBaseUrl;

	private SingleTapListener mSingleTapListener;

	private boolean pageInitialized = false;

	// SHIFTS
	private VectorD mInitialShift = VectorD.ZERO_VECTOR;
	// private VectorD mUserShift = VectorD.ZERO_VECTOR;
	// private int mInitialShiftX = 0;
	// private int mInitialShiftY = 0;

	// private int mUserShiftX = 0;
	// private int mUserShiftY = 0;

	// pan/shift limitations
	private int maxShiftUp;
	private int maxShiftDown;
	private int maxShiftLeft;
	private int maxShiftRight;
	// next/previous image when finished

	private boolean mDrawLayerWithWorseResolution = true;

	private ViewMode mViewMode = ViewMode.FIT_TO_SCREEN;

	// test stuff

	private TilesCache mTilesCache;
	private TilesDownloader mActiveImageDownloader;

	// za hranice canvas cela oblast s obrazkem
	private Rect mImageInCanvas = null;
	// jen viditelna cast stranky
	private Rect mVisibleImageInCanvas = null;

	private PinchZoomManager mPinchZoomManager;
	private DoubleTapZoomManager mDoubleTapZoomManager;
	private SwipeShiftManager mSwipeShiftManager;
	private GestureDetector mGestureDetector;

	private PointD visibleImageCenter;

	private ImageInitializationHandler mImageInitializationHandler;
	private TileDownloadHandler mTileDownloadHandler;

	public TiledImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TiledImageView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		if (DEV_MODE) {
			devTools = new DevTools(context);
		}
		mTilesCache = CacheManager.getTilesCache();
		mGestureDetector = new GestureDetector(context, this);
		mGestureDetector.setOnDoubleTapListener(this);
	}

	public ViewMode getViewMode() {
		return mViewMode;
	}

	public void setImageInitializationHandler(ImageInitializationHandler imageInitializationHandler) {
		this.mImageInitializationHandler = imageInitializationHandler;
	}

	public void setTileDownloadHandler(TileDownloadHandler tileDownloadHandler) {
		this.mTileDownloadHandler = tileDownloadHandler;
	}

	public void setViewMode(ViewMode viewMode) {
		if (viewMode == null) {
			throw new NullPointerException();
		}
		this.mViewMode = viewMode;
	}

	public void cancelUnnecessaryTasks() {
		if (mActiveImageDownloader != null) {
			for (DownloadAndCacheTileTask task : mActiveImageDownloader.getTaskRegistry().getAllTasks()) {
				if (task != null) {
					task.cancel(false);
				}
			}
		}
		if (mDoubleTapZoomManager != null) {
			mDoubleTapZoomManager.cancelZoomingAnimation();
		}
	}

	public void loadImage(String zoomifyBaseUrl) {
		Log.d(TAG, "loading new image, base url: " + zoomifyBaseUrl);
		cancelUnnecessaryTasks();
		pageInitialized = false;
		mPinchZoomManager = new PinchZoomManager(this, 1.0f);
		mDoubleTapZoomManager = new DoubleTapZoomManager(this);
		mSwipeShiftManager = new SwipeShiftManager();
		resizeFactorsInitialized = false;
		mZoomifyBaseUrl = zoomifyBaseUrl;
		mActiveImageDownloader = null;
		initTilesDownloaderAsync();
	}

	private void initTilesDownloaderAsync() {
		new InitTilesDownloaderTask(mZoomifyBaseUrl,
				new InitTilesDownloaderTask.ImagePropertiesDownloadResultHandler() {

					@Override
					public void onSuccess(TilesDownloader downloader) {
						Log.d(TAG, "downloader initialized");
						mActiveImageDownloader = downloader;
						if (DEV_MODE) {
							ImageProperties imageProperties = downloader.getImageProperties();
							testPoints = new ImageCoordsPoints(imageProperties.getWidth(), imageProperties.getHeight());
						}

						if (mImageInitializationHandler != null) {
							mImageInitializationHandler.onImagePropertiesProcessed();
						}
						pageInitialized = true;
						invalidate();
					}

					@Override
					public void onUnhandableResponseCode(String imagePropertiesUrl, int responseCode) {
						if (mImageInitializationHandler != null) {
							mImageInitializationHandler.onImagePropertiesUnhandableResponseCodeError(
									imagePropertiesUrl, responseCode);
						}
					}

					@Override
					public void onRedirectionLoop(String imagePropertiesUrl, int redirections) {
						if (mImageInitializationHandler != null) {
							mImageInitializationHandler.onImagePropertiesRedirectionLoopError(imagePropertiesUrl,
									redirections);
						}
					}

					@Override
					public void onDataTransferError(String imagePropertiesUrl, String errorMessage) {
						if (mImageInitializationHandler != null) {
							mImageInitializationHandler.onImagePropertiesDataTransferError(imagePropertiesUrl,
									errorMessage);
						}
					}

					@Override
					public void onInvalidData(String imagePropertiesUrl, String errorMessage) {
						if (mImageInitializationHandler != null) {
							mImageInitializationHandler.onImagePropertiesInvalidDataError(imagePropertiesUrl,
									errorMessage);
						}
					}
				}).executeConcurrentIfPossible();
	}

	@Override
	public void onDraw(final Canvas canv) {
		// long start = System.currentTimeMillis();
		int canvWidth = canv.getWidth();
		int canvHeight = canv.getHeight();
		if (DEV_MODE) {
			devTools.drawCanvasYellow(canv);
		}
		// Log.d(TAG, "canvas(px): width=" + canvWidth + ", height=" +
		// canvHeight);
		double canvWidthDp = pxToDp(canvWidth);
		double canvHeightDp = pxToDp(canvHeight);
		// Log.d(TAG, "canvas(dp): width=" + canvWidthDp + ", height=" +
		// canvHeightDp);

		if (mActiveImageDownloader != null) {
			if (DEV_MODE) {
				devTools.drawCanvasBlue(canv);
			}
			if (!resizeFactorsInitialized) {
				initResizeFactors(canv);
				resizeFactorsInitialized = true;
			}
			if (mPinchZoomManager.getState() == PinchZoomManager.State.READY_TO_PINCH
					|| mPinchZoomManager.getState() == PinchZoomManager.State.PINCHING) {
				double minZoomLevel = mMinResizingFactor / mInitialResizingFactor
						/ mDoubleTapZoomManager.getCurrentZoomLevel();
				Log.d(TAG_STATES, "minZoomLevel: " + minZoomLevel);
				mPinchZoomManager.setMinZoomLevel(minZoomLevel);
			}

			// za hranice canvas cela oblast s obrazkem
			mImageInCanvas = computeImageInCanvasCordsPossibly(canv, mActiveImageDownloader);
			// cast obrazku jen v canvas
			if (DEV_MODE) {
				devTools.drawWholeImageRed(canv, mImageInCanvas);
			}

			// Rect imageInCanvasDp = new Rect(pxToDp(imageInCanvas));
			// Log.d(TAG, "(dp) img dest: " + toString(imagesInCanvasDp, "dp"));

			// px
			int bestLayerId = mActiveImageDownloader.getBestLayerId(mImageInCanvas.width(), mImageInCanvas.height());
			// int bestLayerId = 5;
			// Log.d(TAG, "best layer id: " + bestLayerId);

			mVisibleImageInCanvas = computeVisibleInCanvas(canv);
			if (DEV_MODE) {
				devTools.drawImageVisiblePartGreen(canv, mVisibleImageInCanvas);
			}

			// Log.d("canv", "   image: " + toString(mImageInCanvas));
			// Log.d("canv", "   image: " + Utils.toString(mImageInCanvas));
			// Log.d("canv", "visible: " +
			// Utils.toString(mImageInCanvasVisible));
			maxShiftUp = mImageInCanvas.top >= 0 ? 0 : -mImageInCanvas.top;
			maxShiftDown = mImageInCanvas.bottom <= canv.getHeight() ? 0 : mImageInCanvas.bottom - canv.getHeight();
			maxShiftLeft = mImageInCanvas.left >= 0 ? 0 : -mImageInCanvas.left;
			maxShiftRight = mImageInCanvas.right <= canv.getWidth() ? 0 : mImageInCanvas.right - canv.getWidth();

			// Log.d(TAG, "IMAGE canv:     " + Utils.toString(mImageInCanvas));
			// Log.d(TAG, "IMAGE canv vis: " +
			// Utils.toString(mImageInCanvasVisible));

			// TODO: pokud je mid ve viditelne strance, posunout canvas tim
			// smerem

			visibleImageCenter = computeVisibleImageCenter();

			// visibleImageCenterX = (int) (mImageInCanvasVisible.width() / 2 +
			// mImageInCanvasVisible.left);
			// visibleImageCenterY = (int) (mImageInCanvasVisible.height() / 2 +
			// mImageInCanvasVisible.top);
			// if (zoomCenter != null && isInVisibleImage(imageInCanvasVisible,
			// zoomCenter)) {

			// Rect imageInImageCoordsVisible =
			// toVisibleImageAreaInImageCoords(imageInCanvasVisible,
			// imageInCanvasVisible);
			// Log.d(TAG, "visible image coords: " +
			// toString(imageInImageCoordsVisible));
			drawLayers(canv, mActiveImageDownloader, bestLayerId);
			PinchZoomManager.State zoomState = mPinchZoomManager.getState();
			SwipeShiftManager.State shiftState = mSwipeShiftManager.getState();

			if (DEV_MODE) {
				// && (zoomState == ZoomManager.State.READY_TO_PINCH ||
				// zoomState == ZoomManager.State.PINCHING || shiftState ==
				// SwipeShiftManager.State.IDLE)) {
				PointD initialZoomCenterInImageCoords = null;
				PointD currentZoomCenter = null;
				if (mDoubleTapZoomManager.getState() == DoubleTapZoomManager.State.ZOOMING) {
					initialZoomCenterInImageCoords = mDoubleTapZoomManager.getInitialZoomCenterInImageCoords();
					currentZoomCenter = mDoubleTapZoomManager.getCurrentZoomCenter();
				} else {
					initialZoomCenterInImageCoords = mPinchZoomManager.getInitialZoomCenterInImageCoords();
					currentZoomCenter = mPinchZoomManager.getCurrentZoomCenter();
				}
				if (initialZoomCenterInImageCoords != null && currentZoomCenter != null) {
					PointD initialZoomCenterCanvas = Utils.toCanvasCoords(initialZoomCenterInImageCoords,
							getTotalResizeFactor(), getTotalShift());
					devTools.drawZoomCenters(canv, currentZoomCenter, initialZoomCenterCanvas, getTotalResizeFactor(),
							getTotalShift());
				}
			}
			// long end = System.currentTimeMillis();
			// Log.d("timing", "onDraw: " + (end - start) + " ms");
			if (DEV_MODE && devTools != null && testPoints != null) {
				double resizeFactor = getTotalResizeFactor();
				devTools.drawImageCoordPoints(canv, testPoints, resizeFactor, getTotalShift());
			}
		}
	}

	private void initResizeFactors(Canvas canv) {
		ImageProperties imageProperties = mActiveImageDownloader.getImageProperties();
		double resizingFactorFitToScreen = computeResizingFactorFitToScreen(canv.getWidth(), canv.getHeight(),
				imageProperties.getWidth(), imageProperties.getHeight());
		double resizingFactorNoFreeSpace = computeResizingFactorNoFreeSpace(canv.getWidth(), canv.getHeight(),
				imageProperties.getWidth(), imageProperties.getHeight());
		switch (mViewMode) {
		case FIT_TO_SCREEN:
			mInitialResizingFactor = resizingFactorFitToScreen;
			break;
		default:
			mInitialResizingFactor = resizingFactorNoFreeSpace;
			break;
		}
		// Log.d(TAG, "fit to screen factor: " + mInitialResizeFactor);
		mMinResizingFactor = Math.min(resizingFactorFitToScreen, resizingFactorNoFreeSpace);
	}

	public DevTools getDevTools() {
		return devTools;
	}

	private PointD computeVisibleImageCenter() {
		// visibleImageCenterX = (int) (mImageInCanvasVisible.width() / 2 +
		// mImageInCanvasVisible.left);
		// visibleImageCenterY = (int) (mImageInCanvasVisible.height() / 2 +
		// mImageInCanvasVisible.top);
		float x = (mVisibleImageInCanvas.width() / 2 + mVisibleImageInCanvas.left);
		float y = (mVisibleImageInCanvas.height() / 2 + mVisibleImageInCanvas.top);
		return new PointD(x, y);
	}

	public double getTotalResizeFactor() {
		return mInitialResizingFactor * mPinchZoomManager.getCurrentZoomLevel()
				* mDoubleTapZoomManager.getCurrentZoomLevel();
	}

	private boolean isInVisibleImage(Rect imageInCanvasVisible, Point point) {
		return true; // TODO
	}

	// TODO: stejnou jmenovou konvenci, jako getActualResizeFactor
	// public Vector getTotalShift() {
	// // float[] zoomShift = mZoomManager.getActiveZoomShift();
	// Vector zoomShift = mZoomManager.getActualZoomShift().toVector();
	// // int shiftX = (int) (mUserShiftX + mInitialShiftX + zoomShift.x);
	// // int shiftY = (int) (mUserShiftY + mInitialShiftY + zoomShift.y);
	// // return new Vector(shiftX, shiftY);
	// return Vector.sum(mUserShift, mInitialShift, zoomShift);
	// }

	public VectorD getTotalShift() {
		VectorD swipeShift = mSwipeShiftManager.getSwipeShift();
		VectorD pinchZoomShift = mPinchZoomManager.getCurrentZoomShift();
		VectorD doubleTapZoomShift = mDoubleTapZoomManager.getCurrentZoomShift();
		// return VectorD.sum(mUserShift, mInitialShift, zoomShift);
		return VectorD.sum(mInitialShift, swipeShift, pinchZoomShift, doubleTapZoomShift);
	}

	private void drawLayers(Canvas canv, TilesDownloader downloader, int layerId) {
		// long start = System.currentTimeMillis();
		int[][] corners = getCornerVisibleTilesCoords(downloader, layerId);
		int[] topLeftVisibleTileCoords = corners[0];
		int[] bottomRightVisibleTileCoords = corners[1];
		// cancel downloading/saving of not visible tiles
		cancelDownloadersForTilesOutOfScreen(layerId, downloader.getTaskRegistry(), bottomRightVisibleTileCoords,
				topLeftVisibleTileCoords);

		// find visible tiles
		List<int[]> visibleTiles = new ArrayList<int[]>();
		for (int y = topLeftVisibleTileCoords[1]; y <= bottomRightVisibleTileCoords[1]; y++) {
			for (int x = topLeftVisibleTileCoords[0]; x <= bottomRightVisibleTileCoords[0]; x++) {
				int[] visibleTile = { x, y };
				// Log.d(TAG, "visible: " + toString(tile));
				visibleTiles.add(visibleTile);
			}
		}
		// check if all visible tiles are available
		boolean allTilesAvailable = true;
		for (int[] visibleTile : visibleTiles) {
			TileId visiblePicId = new TileId(layerId, visibleTile[0], visibleTile[1]);
			Bitmap tile = mTilesCache.getTile(mZoomifyBaseUrl, visiblePicId);
			if (tile == null) {
				allTilesAvailable = false;
				break;
			}
		}
		// if not all visible tiles available,
		// draw under layer with worse resolution
		if (!allTilesAvailable && layerId != 0 && mDrawLayerWithWorseResolution) {
			drawLayers(canv, downloader, layerId - 1);
		}
		// draw visible tiles if available, start downloading otherwise
		for (int[] visiblePic : visibleTiles) {
			TileId visibleTileId = new TileId(layerId, visiblePic[0], visiblePic[1]);
			Bitmap tile = mTilesCache.getTile(mZoomifyBaseUrl, visibleTileId);
			if (tile != null) {
				Rect tileInCanvas = toTileInCanvas(layerId, visiblePic, tile, downloader);
				canv.drawBitmap(tile, null, tileInCanvas, null);
			} else {
				if (!downloader.getTaskRegistry().isRunning(visibleTileId)) {
					new DownloadAndCacheTileTask(downloader, mZoomifyBaseUrl, visibleTileId,
							new TileDownloadResultHandler() {

								@Override
								public void onUnhandableResponseCode(TileId tileId, String tileUrl, int responseCode) {
									if (mTileDownloadHandler != null) {
										mTileDownloadHandler.onTileUnhandableResponseError(tileId, tileUrl,
												responseCode);
									}
								}

								@Override
								public void onSuccess(TileId tileId, Bitmap bitmap) {
									invalidate();
									if (mTileDownloadHandler != null) {
										mTileDownloadHandler.onTileProcessed(tileId);
									}
								}

								@Override
								public void onRedirectionLoop(TileId tileId, String tileUrl, int redirections) {
									if (mTileDownloadHandler != null) {
										mTileDownloadHandler.onTileRedirectionLoopError(tileId, tileUrl, redirections);
									}
								}

								@Override
								public void onInvalidData(TileId tileId, String tileUrl, String errorMessage) {
									if (mTileDownloadHandler != null) {
										mTileDownloadHandler.onTileInvalidDataError(tileId, tileUrl, errorMessage);
									}
								}

								@Override
								public void onDataTransferError(TileId tileId, String tileUrl, String errorMessage) {
									if (mTileDownloadHandler != null) {
										mTileDownloadHandler.onTileDataTransferError(tileId, tileUrl, errorMessage);
									}
								}
							}).executeConcurrentIfPossible();

				}
			}
		}
		// long end = System.currentTimeMillis();
		// Log.d(TAG, "drawLayers (layer=" + layerId + "): " + (end - start) +
		// " ms");

	}

	private int[][] getCornerVisibleTilesCoords(TilesDownloader downloader, int layerId) {
		// double resizeFactor = mZoomManager.getActualZoomLevel() *
		// mInitialResizeFactor;
		double resizeFactor = getTotalResizeFactor();
		// int shiftX = mUserShiftX + mCenterInScreenShiftX;
		// int shiftY = mUserShiftY + mCenterInScreenShiftY;
		int imageWidthMinusOne = downloader.getImageProperties().getWidth() - 1;
		int imageHeightMinusOne = downloader.getImageProperties().getHeight() - 1;

		VectorD totalShift = getTotalShift();
		// TODO
		// int topLeftVisibleX = collapseToInterval((int)
		// ((mImageInCanvasVisible.left - totalShift.x) / resizeFactor), 0,
		// imageWidthMinusOne);
		int topLeftVisibleX = collapseToInterval(
				(int) Utils.toImageX(mVisibleImageInCanvas.left, resizeFactor, totalShift.x), 0, imageWidthMinusOne);
		int topLeftVisibleY = collapseToInterval((int) ((mVisibleImageInCanvas.top - totalShift.y) / resizeFactor), 0,
				imageHeightMinusOne);
		int bottomRightVisibleX = collapseToInterval(
				(int) ((mVisibleImageInCanvas.right - totalShift.x) / resizeFactor), 0, imageWidthMinusOne);
		int bottomRightVisibleY = collapseToInterval(
				(int) ((mVisibleImageInCanvas.bottom - totalShift.y) / resizeFactor), 0, imageHeightMinusOne);

		int[] topLeftVisibleTileCoords = downloader.getTileCoords(layerId, topLeftVisibleX, topLeftVisibleY);
		int[] bottomRightVisibleTileCoords = downloader
				.getTileCoords(layerId, bottomRightVisibleX, bottomRightVisibleY);
		// Log.d(TAG, "top_left:     " + toString(topLeftVisibleTileCoords));
		// Log.d(TAG, "bottom_right: " +
		// toString(bottomRightVisibleTileCoords));
		return new int[][] { topLeftVisibleTileCoords, bottomRightVisibleTileCoords };
	}

	private void cancelDownloadersForTilesOutOfScreen(int layerId,
			DownloadAndSaveTileTasksRegistry tileDownloaderTaskRegister, int[] bottomRightVisibleTileCoords,
			int[] topLeftVisibleTileCoords) {
		// No longer visible pics (within this layer) but still running.
		// Will be stopped (perhpas except of those closest to screen)
		// int canceled = 0;
		for (String running : tileDownloaderTaskRegister.getAllTaskTileIds()) {
			TileId runningTileId = TileId.valueOf(running);
			if (runningTileId.getLayer() == layerId) {
				if (runningTileId.getX() < topLeftVisibleTileCoords[0]
						|| runningTileId.getX() > bottomRightVisibleTileCoords[0]
						|| runningTileId.getY() < topLeftVisibleTileCoords[1]
						|| runningTileId.getY() > bottomRightVisibleTileCoords[1]) {
					DownloadAndCacheTileTask task = tileDownloaderTaskRegister.getTask(runningTileId);
					if (task != null) {
						task.cancel(false);
						// canceled++;
					}
				}
			}
		}
		// Log.d(TAG, "canceled: " + canceled);
	}

	public void setDrawLayerWithWorseResolution(boolean show) {
		mDrawLayerWithWorseResolution = show;
	}

	private int collapseToInterval(int x, int min, int max) {
		if (x < min) {
			return min;
		} else if (x > max) {
			return max;
		} else {
			return x;
		}
	}

	private Rect toTileInCanvas(int layerId, int[] pic, Bitmap tile, TilesDownloader downloader) {
		// double resizeFactor = mZoomManager.getActualZoomLevel() *
		// mInitialResizeFactor;
		double resizeFactor = getTotalResizeFactor();

		int tileBasicSize = (int) ((double) downloader.getTilesSizeInImageCoords(layerId) * resizeFactor);
		int tileWidth = (int) ((double) downloader.getTileWidthInImage(layerId, pic[0]) * resizeFactor);
		int tileHeight = (int) ((double) downloader.getTileHeightInImage(layerId, pic[1]) * resizeFactor);

		int left = pic[0] * tileBasicSize + mImageInCanvas.left;
		int right = left + tileWidth;
		int top = pic[1] * tileBasicSize + mImageInCanvas.top;
		int bottom = top + tileHeight;
		Rect result = new Rect(left, top, right, bottom);
		return result;
	}

	private double computeResizingFactorFitToScreen(double canvasWidth, double canvasHeight, double imgOriginalWidth,
			double imgOriginalHeight) {
		double widthRatio = canvasWidth / imgOriginalWidth;
		double heightRatio = canvasHeight / imgOriginalHeight;
		// Log.d(TAG, "widthRatio=" + widthRatio + ", heightRatio=" +
		// heightRatio);
		// preferuj zmenseni
		if (widthRatio < 1 && heightRatio < 1) {
			return widthRatio < heightRatio ? widthRatio : heightRatio;
			// return widthRatio < heightRatio ? heightRatio : widthRatio;
		} else if (widthRatio < 1) {// heightRatio > 1
			return widthRatio;
		} else if (heightRatio < 1) {// widthRatio > 1
			return heightRatio;
		} else { // widthRatio > 1 && heightRatio > 1
			// mensi zvetseni
			return widthRatio < heightRatio ? widthRatio : heightRatio;
		}
	}

	private double computeResizingFactorNoFreeSpace(double canvasWidth, double canvasHeight, double imgOriginalWidth,
			double imgOriginalHeight) {
		double widthRatio = canvasWidth / imgOriginalWidth;
		double heightRatio = canvasHeight / imgOriginalHeight;
		// Log.d(TAG, "widthRatio=" + widthRatio + ", heightRatio=" +
		// heightRatio);

		// preferuj zmenseni
		if (widthRatio < 1 && heightRatio < 1) {
			// mensi zmenseni
			return widthRatio < heightRatio ? heightRatio : widthRatio;
		} else if (heightRatio > 1) {// widthRatio < 1
			return heightRatio;
		} else if (widthRatio > 1) {// heightRatio < 1
			return widthRatio;
		} else { // widthRatio > 1 && heightRatio > 1
			// vetsi zvetseni
			return widthRatio > heightRatio ? widthRatio : heightRatio;
		}
	}

	private Rect computeImageInCanvasCordsPossibly(Canvas canv, TilesDownloader downloader) {
		double canvasWidth = canv.getWidth();
		double canvasHeight = canv.getHeight();
		double imageOriginalWidth = downloader.getImageProperties().getWidth();
		double imageOriginalHeight = downloader.getImageProperties().getHeight();
		// double resizeFactor = mZoomManager.getActualZoomLevel() *
		// mInitialResizeFactor;
		// TODO; use Utils.toCanvasCoords here
		double resizeFactor = getTotalResizeFactor();
		int actualWidth = (int) (imageOriginalWidth * resizeFactor);
		int actualHeight = (int) (imageOriginalHeight * resizeFactor);
		// Log.d(TAG, "newImg: width=" + actualWidth + ", height=" +
		// actualHeight);

		// TODO: pocitat znovu jen pri zmene

		double extraWidth = canvasWidth - actualWidth;
		double extraHeight = canvasHeight - actualHeight;

		double xLeft = 0;
		double xCenter = extraWidth / 2.0;
		double xRight = extraWidth;
		double yTop = 0;
		double yCenter = extraHeight / 2.0;
		double yBottom = extraHeight;

		switch (mViewMode) {
		case FIT_TO_SCREEN:
			mInitialShift = new VectorD(xCenter, yCenter);
			break;
		case NO_FREE_SPACE_ALIGN_HORIZONTAL_LEFT_VERTICAL_TOP:
			mInitialShift = new VectorD(xLeft, yTop);
			break;
		case NO_FREE_SPACE_ALIGN_HORIZONTAL_LEFT_VERTICAL_CENTER:
			mInitialShift = new VectorD(xLeft, yCenter);
			break;
		case NO_FREE_SPACE_ALIGN_HORIZONTAL_LEFT_VERTICAL_BOTTOM:
			mInitialShift = new VectorD(xLeft, yBottom);
			break;
		case NO_FREE_SPACE_ALIGN_HORIZONTAL_CENTER_VERTICAL_TOP:
			mInitialShift = new VectorD(xCenter, yTop);
			break;
		case NO_FREE_SPACE_ALIGN_HORIZONTAL_CENTER_VERTICAL_CENTER:
			mInitialShift = new VectorD(xCenter, yCenter);
			break;
		case NO_FREE_SPACE_ALIGN_HORIZONTAL_CENTER_VERTICAL_BOTTOM:
			mInitialShift = new VectorD(xCenter, yBottom);
			break;
		case NO_FREE_SPACE_ALIGN_HORIZONTAL_RIGHT_VERTICAL_TOP:
			mInitialShift = new VectorD(xRight, yTop);
			break;
		case NO_FREE_SPACE_ALIGN_HORIZONTAL_RIGHT_VERTICAL_CENTER:
			mInitialShift = new VectorD(xRight, yCenter);
			break;
		case NO_FREE_SPACE_ALIGN_HORIZONTAL_RIGHT_VERTICAL_BOTTOM:
			mInitialShift = new VectorD(xRight, yBottom);
			break;
		}
		VectorD totalShift = getTotalShift();
		return new Rect((int) (0 + totalShift.x), (int) (0 + totalShift.y), (int) (actualWidth + totalShift.x),
				(int) (actualHeight + totalShift.y));
	}

	private Rect pxToDp(Rect rectPx) {
		int top = (int) (rectPx.top / this.getContext().getResources().getDisplayMetrics().density);
		int bottom = (int) (rectPx.bottom / this.getContext().getResources().getDisplayMetrics().density);
		int left = (int) (rectPx.left / this.getContext().getResources().getDisplayMetrics().density);
		int right = (int) (rectPx.right / this.getContext().getResources().getDisplayMetrics().density);
		return new Rect(left, top, right, bottom);
	}

	private Rect computeVisibleInCanvas(Canvas canv) {
		int left = mapNumberToInterval(mImageInCanvas.left, 0, canv.getWidth());
		int right = mapNumberToInterval(mImageInCanvas.right, 0, canv.getWidth());
		int top = mapNumberToInterval(mImageInCanvas.top, 0, canv.getHeight());
		int bottom = mapNumberToInterval(mImageInCanvas.bottom, 0, canv.getHeight());
		return new Rect(left, top, right, bottom);
	}

	private int mapNumberToInterval(int number, int min, int max) {
		if (number <= min) {
			return min;
		} else if (number >= max) {
			return max;
		} else {
			return number;
		}
	}

	@Override
	@SuppressLint("NewApi")
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		if (pageInitialized) {
			SwipeShiftManager.State swipeShiftManagerState = mSwipeShiftManager.getState();
			PinchZoomManager.State zoomManagerState = mPinchZoomManager.getState();
			DoubleTapZoomManager.State doubleTapZoomManagerState = mDoubleTapZoomManager.getState();
			if (doubleTapZoomManagerState == DoubleTapZoomManager.State.IDLE) {
				int actionIndex = event.getActionIndex();
				switch (action) {
				case (MotionEvent.ACTION_DOWN):
					mSwipeShiftManager.notifyReadyToDrag(event.getX(), event.getY());
					mGestureDetector.onTouchEvent(event);
					return true;
				case (MotionEvent.ACTION_POINTER_DOWN):
					if (actionIndex == 1) {
						// just so that click is not recognized by
						// GestureDetector
						mGestureDetector.onTouchEvent(event);
						if (swipeShiftManagerState == SwipeShiftManager.State.READY_TO_DRAG) {
							mSwipeShiftManager.notifyCanceled();
						} else if (swipeShiftManagerState == SwipeShiftManager.State.DRAGGING) {
							mSwipeShiftManager.notifyDraggingFinished(false);
						}
						if (zoomManagerState == PinchZoomManager.State.IDLE) {
							mPinchZoomManager.notifyReadyToPinch(event, getTotalResizeFactor(), getTotalShift());
							return true;
						} else {
							Log.w(TAG, "unexpected ACTION_POINTER_DOWN");
							return true;
						}
					} else {
						// ignore third and following fingers
						return true;
					}
				case (MotionEvent.ACTION_UP):
					if (swipeShiftManagerState == SwipeShiftManager.State.DRAGGING) {
						mSwipeShiftManager.notifyDraggingFinished(true);
						return true;
					} else if (swipeShiftManagerState == SwipeShiftManager.State.READY_TO_DRAG) {
						mSwipeShiftManager.notifyCanceled();
						mGestureDetector.onTouchEvent(event);
						return true;
					} else {
						mGestureDetector.onTouchEvent(event);
						return true;
					}
				case (MotionEvent.ACTION_POINTER_UP):
					if (actionIndex == 0 || actionIndex == 1) {
						// just so that click is not recognized by
						// GestureDetector
						mGestureDetector.onTouchEvent(event);
						if (zoomManagerState == PinchZoomManager.State.PINCHING) {
							mPinchZoomManager.notifyPinchingFinished();
						}
						if (zoomManagerState == PinchZoomManager.State.READY_TO_PINCH) {
							mPinchZoomManager.notifyCanceled();
						} else {
							Log.w(TAG, "unexpected ACTION_POINTER_UP");
						}
						if (swipeShiftManagerState == SwipeShiftManager.State.IDLE) {
							// TODO: enable
							// mSwipeShiftManager.notifyReadyToDrag(event);

							int remainingFingerIndex = actionIndex == 0 ? 1 : 0;
							mSwipeShiftManager.notifyReadyToDrag(event.getX(remainingFingerIndex),
									event.getY(remainingFingerIndex));
						} else {
							Log.w(TAG, "unexpected " + SwipeShiftManager.class.getSimpleName() + " state: "
									+ swipeShiftManagerState.name());
						}
						return true;
					} else {
						// ignore third and following fingers
						return true;
					}
				case (MotionEvent.ACTION_MOVE):
					if (swipeShiftManagerState == SwipeShiftManager.State.READY_TO_DRAG
							|| swipeShiftManagerState == SwipeShiftManager.State.DRAGGING) {
						boolean swiped = mSwipeShiftManager.notifyDragging(event.getX(), event.getY(), maxShiftUp,
								maxShiftDown, maxShiftLeft, maxShiftRight);
						if (!swiped) {
							mGestureDetector.onTouchEvent(event);
						} else {
							invalidate();
						}
						return true;
					} else {
						int fingers = event.getPointerCount();
						if (fingers == 2) {
							boolean refresh = mPinchZoomManager.notifyPinchingContinues(event, visibleImageCenter,
									getTotalResizeFactor(), getTotalShift());
							if (refresh) {
								invalidate();
							}
						} else {
							// should not ever happen
							Log.w(TAG, "unexpected ACTION_MOVE");
							return true;
						}
					}
				default:
					if (Build.VERSION.SDK_INT >= 19) {
						Log.w(TAG, "unexpected event: " + MotionEvent.actionToString(action));
					} else {
						Log.w(TAG, "unexpected event");
					}
					return super.onTouchEvent(event);
				}
			} else {
				Log.w(TAG_STATES, "ignoring event while double tap zooming animation in progress");
				return false;

			}
		} else {
			Log.d(TAG_STATES, "not initialized yet");
			mGestureDetector.onTouchEvent(event);
			return true;
		}
	}

	// private int[] toimageCoords(float x, float y) {
	// double resizeFactor = getActualResizeFactor();
	// int totalShift[] = getTotalShift();
	// int newX = (int) ((x - totalShift[0]) / resizeFactor);
	// int newY = (int) ((y - totalShift[1]) / resizeFactor);
	// return new int[] { newX, newY };
	// }

	private double pxToDp(double px) {
		return px / this.getContext().getResources().getDisplayMetrics().density;
	}

	public PinchZoomManager getZoomManager() {
		return mPinchZoomManager;
	}

	public enum ViewMode {
		FIT_TO_SCREEN, //

		NO_FREE_SPACE_ALIGN_HORIZONTAL_LEFT_VERTICAL_TOP, //
		NO_FREE_SPACE_ALIGN_HORIZONTAL_LEFT_VERTICAL_CENTER, //
		NO_FREE_SPACE_ALIGN_HORIZONTAL_LEFT_VERTICAL_BOTTOM, //

		NO_FREE_SPACE_ALIGN_HORIZONTAL_CENTER_VERTICAL_TOP, //
		NO_FREE_SPACE_ALIGN_HORIZONTAL_CENTER_VERTICAL_CENTER, //
		NO_FREE_SPACE_ALIGN_HORIZONTAL_CENTER_VERTICAL_BOTTOM, //

		NO_FREE_SPACE_ALIGN_HORIZONTAL_RIGHT_VERTICAL_TOP, //
		NO_FREE_SPACE_ALIGN_HORIZONTAL_RIGHT_VERTICAL_CENTER, //
		NO_FREE_SPACE_ALIGN_HORIZONTAL_RIGHT_VERTICAL_BOTTOM, //
	}

	private static final String TAG_STATES = "state";

	@Override
	@SuppressLint("NewApi")
	public boolean onSingleTapConfirmed(MotionEvent e) {
		PointD point = new PointD(e.getX(), e.getY());
		if (Build.VERSION.SDK_INT >= 19) {
			Log.d(TAG_STATES, "imgView: onSingleTapConfirmed: " + MotionEvent.actionToString(e.getAction()) + ": "
					+ point.toString());
		} else {
			Log.d(TAG_STATES, "imgView: onSingleTapConfirmed: " + point.toString());
		}
		if (mSingleTapListener != null) {
			mSingleTapListener.onSingleTap(e.getX(), e.getY());
		} else {
			Log.d(TAG_STATES, "imgView: SingleTapListener not initialized");
		}
		return true;
	}

	@Override
	@SuppressLint("NewApi")
	public boolean onDoubleTap(MotionEvent e) {
		PointD point = new PointD(e.getX(), e.getY());
		if (Build.VERSION.SDK_INT >= 19) {
			Log.d(TAG_STATES,
					"imgView: onDoubleTap: " + MotionEvent.actionToString(e.getAction()) + ": " + point.toString());
		} else {
			Log.d(TAG_STATES, "imgView: onDoubleTap: " + point.toString());
		}
		mDoubleTapZoomManager.startZoomingAnimation(point);

		return true;
	}

	@Override
	@SuppressLint("NewApi")
	public boolean onDoubleTapEvent(MotionEvent e) {
		// if (Build.VERSION.SDK_INT >= 19) {
		// Log.d(TAG_STATES, "imgView: onDoubleTapEvent: " +
		// MotionEvent.actionToString(e.getAction()));
		// } else {
		// Log.d(TAG_STATES, "imgView: onDoubleTapEvent");
		// }
		// return true;
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// Log.d(TAG_STATES, "imgView: onDown");
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// Log.d(TAG_STATES, "imgView: onShowPress");
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// Log.d(TAG_STATES, "imgView: onSingleTapUp");
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// Log.d(TAG_STATES, "imgView: onScroll");
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// Log.d(TAG_STATES, "imgView: onLongPress");
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// Log.d(TAG_STATES, "imgView: onFling");
		return false;
	}

	public void setSingleTapListener(SingleTapListener singleTapListener) {
		this.mSingleTapListener = singleTapListener;
	}

	public interface SingleTapListener {
		/**
		 * This method is called after single tap, that is confirmed not to be
		 * double tap and also has not been used internally by this view. I.e.
		 * for zooming, swiping etc.
		 * 
		 * @param x
		 *            x coordinate of the tap
		 * @param y
		 *            y coordinate of the tap
		 */
		public void onSingleTap(float x, float y);
	}

	/**
	 * Exactly one of these methods is called eventually after loadImage().
	 * Either onImagePropertiesProcessed() if ImageProperties.xml is found,
	 * downloaded and processed or one of the other methods in case of some
	 * error.
	 * 
	 * @author martin
	 * 
	 */
	public interface ImageInitializationHandler {

		/**
		 * ImageProperties.xml downloaded and processed properly.
		 * 
		 */
		public void onImagePropertiesProcessed();

		/**
		 * Response to HTTP request for ImageProperties.xml returned code that
		 * cannot be handled here. That means almost everything except for some
		 * 2xx codes and some 3xx codes for which redirection is applied.
		 * 
		 * @param imagePropertiesUrl
		 * @param responseCode
		 */
		public void onImagePropertiesUnhandableResponseCodeError(String imagePropertiesUrl, int responseCode);

		/**
		 * Too many redirections to ImageProperties.xml, probably loop.
		 * 
		 * @param imagePropertiesUrl
		 * @param redirections
		 */
		public void onImagePropertiesRedirectionLoopError(String imagePropertiesUrl, int redirections);

		/**
		 * Other errors in transfering ImageProperties.xml - timeouts etc.
		 * 
		 * @param imagePropertiesUrl
		 * @param errorMessage
		 */
		public void onImagePropertiesDataTransferError(String imagePropertiesUrl, String errorMessage);

		/**
		 * Invalid content in ImageProperties.xml. Particulary erroneous xml.
		 * 
		 * @param imagePropertiesUrl
		 * @param errorMessage
		 */
		public void onImagePropertiesInvalidDataError(String imagePropertiesUrl, String errorMessage);
	}

	/**
	 * Exactly one of these methods is called after tile is downloaded and
	 * stored to cache or something goes wrong in this process.
	 * 
	 * @author martin
	 * 
	 */
	public interface TileDownloadHandler {

		/**
		 * Tile downloaded and processed properly.
		 * 
		 * @param tileId
		 *            Tile id.
		 */
		public void onTileProcessed(TileId tileId);

		/**
		 * 
		 * Response to HTTP request for tile returned code that cannot be
		 * handled here. That means almost everything except for some 2xx codes
		 * and some 3xx codes for which redirection is applied.
		 * 
		 * @param tileId
		 *            Tile id.
		 * @param tileUrl
		 *            Tile jpeg url.
		 * @param errorMessage
		 *            Error message.
		 */

		public void onTileUnhandableResponseError(TileId tileId, String tileUrl, int responseCode);

		/**
		 * Too many redirections for tile, probably loop.
		 * 
		 * @param tileId
		 *            Tile id.
		 * @param tileUrl
		 *            Tile jpeg url.
		 * @param errorMessage
		 *            Error message.
		 */
		public void onTileRedirectionLoopError(TileId tileId, String tileUrl, int redirections);

		/**
		 * Other errors in transfering tile - timeouts etc.
		 * 
		 * @param tileId
		 *            Tile id.
		 * @param tileUrl
		 *            Tile jpeg url.
		 * @param errorMessage
		 *            Error message.
		 */
		public void onTileDataTransferError(TileId tileId, String tileUrl, String errorMessage);

		/**
		 * Invalid tile content.
		 * 
		 * @param tileId
		 *            Tile id.
		 * @param tileUrl
		 *            Tile jpeg url.
		 * @param errorMessage
		 *            Error message.
		 */
		public void onTileInvalidDataError(TileId tileId, String tileUrl, String errorMessage);
	}

}

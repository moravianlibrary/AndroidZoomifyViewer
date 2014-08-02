package cz.mzk.androidzoomifyviewer.viewer;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import cz.mzk.androidzoomifyviewer.CacheManager;
import cz.mzk.androidzoomifyviewer.cache.TilesCache;
import cz.mzk.androidzoomifyviewer.cache.TilesDownloaderCache;
import cz.mzk.androidzoomifyviewer.tiles.DownloadAndSaveTileTask;
import cz.mzk.androidzoomifyviewer.tiles.DownloadAndSaveTileTasksRegistry;
import cz.mzk.androidzoomifyviewer.tiles.ImageProperties;
import cz.mzk.androidzoomifyviewer.tiles.InitTilesDownloaderTask;
import cz.mzk.androidzoomifyviewer.tiles.TileId;
import cz.mzk.androidzoomifyviewer.tiles.TilesDownloader;

/**
 * @author Martin Řehánek
 * 
 */
public class TiledImageView extends View {

	private static final String TAG = TiledImageView.class.getSimpleName();

	private static final boolean DEV_MODE = false;
	private DevTools devTools = null;
	private PageCoordsPoints testPoints = null;

	private Double mInitialResizeFactor = null;

	private String mZoomifyBaseUrl;

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
	// next/previous page when finished
	// private boolean moveToPreviousPageWhenMoveFinished = false;
	// private boolean moveToNextPageWhenMoveFinished = false;

	// private String mPagePid;

	private boolean mDrawLayerWithWorseResolution = true;

	// private ViewMode mViewMode = ViewMode.FIT_TO_SCREEN;
	private ViewMode mViewMode = ViewMode.NO_FREE_SPACE_ALIGN_CENTER;

	// test stuff

	private TilesCache mTilesCache;
	private TilesDownloader mActivePageDownloader;

	// za hranice canvas cela oblast s obrazkem
	private Rect mPageInCanvas = null;
	// jen viditelna cast stranky
	private Rect mPageInCanvasVisible = null;

	private PinchZoomManager mPinchZoomManager;
	private SwipeShiftManager mSwipeShiftManager;

	private PointD visiblePageCenter;
	// int visiblePageCenterX;
	// int visiblePageCenterY;

	// pageCanvas(visible)

	private LoadingHandler loadingHandler;

	public TiledImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (DEV_MODE) {
			devTools = new DevTools(context);
		}
		mTilesCache = CacheManager.getTilesCache();
	}

	public TiledImageView(Context context) {
		super(context);
		if (DEV_MODE) {
			devTools = new DevTools(context);
		}
		mTilesCache = CacheManager.getTilesCache();
	}

	public ViewMode getViewMode() {
		return mViewMode;
	}

	public void setLoadingHandler(LoadingHandler handler) {
		this.loadingHandler = handler;
	}

	public void setViewMode(ViewMode viewMode) {
		if (viewMode == null) {
			throw new NullPointerException();
		}
		this.mViewMode = viewMode;
	}

	public void loadPage(final String zoomifyBaseUrl) {
		Log.d(TAG, "loading new page");
		if (mActivePageDownloader != null) {
			for (DownloadAndSaveTileTask task : mActivePageDownloader.getTaskRegistry().getAllTasks()) {
				if (task != null) {
					task.cancel(false);
				}
			}
		}
		// mZoomLevel = 1.0f;
		mPinchZoomManager = new PinchZoomManager(this, 1.0f);// DEFAULT ZOOM
																// LEVEL
		mInitialResizeFactor = null;
		// mUserShift = VectorD.ZERO_VECTOR;
		mSwipeShiftManager = new SwipeShiftManager();
		mZoomifyBaseUrl = zoomifyBaseUrl;

		final TilesDownloaderCache downloaderCache = CacheManager.getDownloaderCache();
		mActivePageDownloader = downloaderCache.get(zoomifyBaseUrl);

		if (mActivePageDownloader == null) {
			new InitTilesDownloaderTask(mZoomifyBaseUrl,
					new InitTilesDownloaderTask.TilesDownloaderInitializationHandler() {

						@Override
						public void onInitialized(String zoomifyBaseUrl, TilesDownloader downloader) {
							Log.d(TAG, "downloader initialized");
							downloaderCache.put(zoomifyBaseUrl, downloader);
							mActivePageDownloader = downloader;
							if (DEV_MODE) {
								ImageProperties imageProperties = downloader.getImageProperties();
								testPoints = new PageCoordsPoints(imageProperties.getWidth(),
										imageProperties.getHeight());
							}
							if (loadingHandler != null) {
								loadingHandler.onImagePropertiesProcessed(zoomifyBaseUrl);
							}
							invalidate();
						}

						@Override
						public void onInvalidImagePropertiesState(String zoomifyBaseUrl, int responseCode) {
							if (loadingHandler != null) {
								loadingHandler.onImagePropertiesInvalidStateError(zoomifyBaseUrl, responseCode);
							}
						}

						@Override
						public void onRedirectionLoop(String zoomifyBaseUrl, int redirections) {
							if (loadingHandler != null) {
								loadingHandler.onImagePropertiesRedirectionLoopError(zoomifyBaseUrl, redirections);
							}
						}

						@Override
						public void onDataTransferError(String zoomifyBaseUrl, String errorMessage) {
							if (loadingHandler != null) {
								loadingHandler.onImagePropertiesDataTransferError(zoomifyBaseUrl, errorMessage);
							}
						}

						@Override
						public void onInvalidImagePropertiesData(String zoomifyBaseUrl, String errorMessage) {
							if (loadingHandler != null) {
								loadingHandler.onImagePropertiesInvalidDataError(zoomifyBaseUrl, errorMessage);
							}
						}
					}).executeConcurrentIfPossible();
		} else {
			invalidate();
		}
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

		if (mActivePageDownloader != null) {
			if (DEV_MODE) {
				devTools.drawCanvasBlue(canv);
			}
			if (mInitialResizeFactor == null) {
				double resizingFactorFitToScreen = computeResizingFactorFitToScreen(canv.getWidth(), canv.getHeight(),
						mActivePageDownloader.getImageProperties().getWidth(), mActivePageDownloader
								.getImageProperties().getHeight());
				double resizingFactorNoFreeSpace = computeResizingFactorNoFreeSpace(canv.getWidth(), canv.getHeight(),
						mActivePageDownloader.getImageProperties().getWidth(), mActivePageDownloader
								.getImageProperties().getHeight());
				switch (mViewMode) {
				case FIT_TO_SCREEN:
					mInitialResizeFactor = resizingFactorFitToScreen;
					break;
				case NO_FREE_SPACE_ALIGN_CENTER:
				case NO_FREE_SPACE_ALIGN_TOP_LEFT:
					mInitialResizeFactor = resizingFactorNoFreeSpace;
					break;
				}
				// Log.d(TAG, "fit to screen factor: " + mInitialResizeFactor);
				double minResizingFactor = Math.min(resizingFactorFitToScreen, resizingFactorNoFreeSpace);
				mPinchZoomManager.setMinZoomLevel(minResizingFactor / mInitialResizeFactor);
			}

			// za hranice canvas cela oblast s obrazkem
			mPageInCanvas = computePageInCanvasCordsPossibly(canv, mActivePageDownloader);
			// cast obrazku jen v canvas
			if (DEV_MODE) {
				devTools.drawWholePageRed(canv, mPageInCanvas);
			}
			// Bitmap topLevelTile = mTilesCache.getTile(mPagePid, 0, 0, 0);

			// Rect pageInCanvasDp = new Rect(pxToDp(pageInCanvas));
			// Log.d(TAG, "(dp) img dest: " + toString(pageInCanvasDp, "dp"));

			// px
			int bestLayerId = mActivePageDownloader.getBestLayerId(mPageInCanvas.width(), mPageInCanvas.height());
			// Log.d(TAG, "best layer id: " + bestLayerId);

			mPageInCanvasVisible = computeVisibleInCanvas(canv);
			if (DEV_MODE) {
				devTools.drawPageVisiblePartGreen(canv, mPageInCanvasVisible);
			}

			// Log.d("canv", "   page: " + toString(mPageInCanvas));
			// Log.d("canv", "   page: " + Utils.toString(mPageInCanvas));
			// Log.d("canv", "visible: " +
			// Utils.toString(mPageInCanvasVisible));
			maxShiftUp = mPageInCanvas.top >= 0 ? 0 : -mPageInCanvas.top;
			maxShiftDown = mPageInCanvas.bottom <= canv.getHeight() ? 0 : mPageInCanvas.bottom - canv.getHeight();
			maxShiftLeft = mPageInCanvas.left >= 0 ? 0 : -mPageInCanvas.left;
			maxShiftRight = mPageInCanvas.right <= canv.getWidth() ? 0 : mPageInCanvas.right - canv.getWidth();

			// Log.d(TAG, "PAGE canv:     " + Utils.toString(mPageInCanvas));
			// Log.d(TAG, "PAGE canv vis: " +
			// Utils.toString(mPageInCanvasVisible));

			// TODO: pokud je mid ve viditelne strance, posunout canvas tim
			// smerem

			visiblePageCenter = computeVisiblePageCenter();

			// visiblePageCenterX = (int) (mPageInCanvasVisible.width() / 2 +
			// mPageInCanvasVisible.left);
			// visiblePageCenterY = (int) (mPageInCanvasVisible.height() / 2 +
			// mPageInCanvasVisible.top);
			// if (zoomCenter != null && isInVisiblePage(pageInCanvasVisible,
			// zoomCenter)) {

			// Rect pageInImageCoordsVisible =
			// toVisibleImageAreaInImageCoords(pageInCanvasVisible,
			// pageInCanvasVisible);
			// Log.d(TAG, "visible page coords: " +
			// toString(pageInImageCoordsVisible));
			drawLayers(canv, mActivePageDownloader, bestLayerId);
			PinchZoomManager.State zoomState = mPinchZoomManager.getState();
			SwipeShiftManager.State shiftState = mSwipeShiftManager.getState();

			if (DEV_MODE) {
				// && (zoomState == ZoomManager.State.READY_TO_PINCH ||
				// zoomState == ZoomManager.State.PINCHING || shiftState ==
				// SwipeShiftManager.State.IDLE)) {
				PointD currentZoomCenter = mPinchZoomManager.getCurrentZoomCenter();
				PointD initialZoomCenterInPageCoords = mPinchZoomManager.getInitialZoomCenterInPageCoords();
				if (initialZoomCenterInPageCoords != null && currentZoomCenter != null) {
					PointD initialZoomCenterCanvas = Utils.toCanvasCoords(initialZoomCenterInPageCoords,
							getTotalResizeFactor(), getTotalShift());
					devTools.drawZoomCenters(canv, currentZoomCenter, initialZoomCenterCanvas, getTotalResizeFactor(),
							getTotalShift());
				}
			}
			// long end = System.currentTimeMillis();
			// Log.d("timing", "onDraw: " + (end - start) + " ms");
			if (DEV_MODE && devTools != null && testPoints != null) {
				double resizeFactor = getTotalResizeFactor();
				devTools.drawPageCoordPoints(canv, testPoints, resizeFactor, getTotalShift());
			}
		}
	}

	public DevTools getDevTools() {
		return devTools;
	}

	private PointD computeVisiblePageCenter() {
		// visiblePageCenterX = (int) (mPageInCanvasVisible.width() / 2 +
		// mPageInCanvasVisible.left);
		// visiblePageCenterY = (int) (mPageInCanvasVisible.height() / 2 +
		// mPageInCanvasVisible.top);
		float x = (mPageInCanvasVisible.width() / 2 + mPageInCanvasVisible.left);
		float y = (mPageInCanvasVisible.height() / 2 + mPageInCanvasVisible.top);
		return new PointD(x, y);
	}

	public double getTotalResizeFactor() {
		return mPinchZoomManager.getCurrentZoomLevel() * mInitialResizeFactor;
	}

	private boolean isInVisiblePage(Rect pageInCanvasVisible, Point point) {
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
		VectorD zoomShift = mPinchZoomManager.getCurrentZoomShift();
		VectorD swipeShift = mSwipeShiftManager.getSwipeShift();
		// return VectorD.sum(mUserShift, mInitialShift, zoomShift);
		return VectorD.sum(mInitialShift, swipeShift, zoomShift);
	}

	/**
	 * Page pid se musi predavat, protoze behem provadeni drawLayers muze byt
	 * member zmenen pomoci loadPage()
	 * 
	 * @param canv
	 * @param pagePid
	 * @param layerId
	 * @param pageInCanvas
	 * @param pageInCanvasVisible
	 */
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
					new DownloadAndSaveTileTask(downloader, mZoomifyBaseUrl, visibleTileId, mTilesCache, this)
							.executeConcurrentIfPossible();
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
		// ((mPageInCanvasVisible.left - totalShift.x) / resizeFactor), 0,
		// imageWidthMinusOne);
		int topLeftVisibleX = collapseToInterval(
				(int) Utils.toPageX(mPageInCanvasVisible.left, resizeFactor, totalShift.x), 0, imageWidthMinusOne);
		int topLeftVisibleY = collapseToInterval((int) ((mPageInCanvasVisible.top - totalShift.y) / resizeFactor), 0,
				imageHeightMinusOne);
		int bottomRightVisibleX = collapseToInterval(
				(int) ((mPageInCanvasVisible.right - totalShift.x) / resizeFactor), 0, imageWidthMinusOne);
		int bottomRightVisibleY = collapseToInterval(
				(int) ((mPageInCanvasVisible.bottom - totalShift.y) / resizeFactor), 0, imageHeightMinusOne);

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
					DownloadAndSaveTileTask task = tileDownloaderTaskRegister.getTask(runningTileId);
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

		int tileBasicSize = (int) ((double) downloader.getTileBasicSizeInPage(layerId) * resizeFactor);
		int tileWidth = (int) ((double) downloader.getTileWidthInPage(layerId, pic[0]) * resizeFactor);
		int tileHeight = (int) ((double) downloader.getTileHeightInPage(layerId, pic[1]) * resizeFactor);

		int left = pic[0] * tileBasicSize + mPageInCanvas.left;
		int right = left + tileWidth;
		int top = pic[1] * tileBasicSize + mPageInCanvas.top;
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

	private Rect computePageInCanvasCordsPossibly(Canvas canv, TilesDownloader downloader) {
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
		switch (mViewMode) {
		case FIT_TO_SCREEN:
		case NO_FREE_SPACE_ALIGN_CENTER:
			// zarovnat na stred
			int x = computeOffsetCenter(canvasWidth, actualWidth);
			int y = computeOffsetCenter(canvasHeight, actualHeight);
			mInitialShift = new VectorD(x, y);
			break;
		case NO_FREE_SPACE_ALIGN_TOP_LEFT:
			// zarovnat k levemu hornimu rohu
			mInitialShift = VectorD.ZERO_VECTOR;
			break;
		}
		VectorD totalShift = getTotalShift();
		return new Rect((int) (0 + totalShift.x), (int) (0 + totalShift.y), (int) (actualWidth + totalShift.x),
				(int) (actualHeight + totalShift.y));
	}

	private int computeOffsetCenter(double canvasWidth, double imageInCanvasWidth) {
		double freeSpace = canvasWidth - imageInCanvasWidth;
		// Log.d(TAG, "free width=" + freeSpace);
		return (int) (freeSpace / 2.0);
	}

	// private int computeOffsetAlign(double canvasWidth, double
	// imageInCanvasWidth) {
	// double freeSpace = canvasWidth - imageInCanvasWidth;
	// // Log.d(TAG, "free width=" + freeSpace);
	// return (int) (freeSpace / 2.0);
	// }

	private Rect pxToDp(Rect rectPx) {
		int top = (int) (rectPx.top / this.getContext().getResources().getDisplayMetrics().density);
		int bottom = (int) (rectPx.bottom / this.getContext().getResources().getDisplayMetrics().density);
		int left = (int) (rectPx.left / this.getContext().getResources().getDisplayMetrics().density);
		int right = (int) (rectPx.right / this.getContext().getResources().getDisplayMetrics().density);
		return new Rect(left, top, right, bottom);
	}

	private Rect computeVisibleInCanvas(Canvas canv) {
		int left = mapNumberToInterval(mPageInCanvas.left, 0, canv.getWidth());
		int right = mapNumberToInterval(mPageInCanvas.right, 0, canv.getWidth());
		int top = mapNumberToInterval(mPageInCanvas.top, 0, canv.getHeight());
		int bottom = mapNumberToInterval(mPageInCanvas.bottom, 0, canv.getHeight());
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
		int action = MotionEventCompat.getActionMasked(event);
		if (Build.VERSION.SDK_INT >= 19) {
			// Log.d("Motion", MotionEvent.actionToString(action));
		}
		// Log.d("Motion", "zooming: " + mZoomManager.isZoomingNow() +
		// ", panning: " + mPanningNow);
		if (mSwipeShiftManager != null && mPinchZoomManager != null) {
			SwipeShiftManager.State swipeShiftManagerState = mSwipeShiftManager.getState();
			PinchZoomManager.State zoomManagerState = mPinchZoomManager.getState();
			int actionIndex = event.getActionIndex();
			switch (action) {
			case (MotionEvent.ACTION_MASK):
				// TODO: proc tohle?
				return true;
			case (MotionEvent.ACTION_DOWN):
				mSwipeShiftManager.notifyReadyToDrag(event.getX(), event.getY());
				return true;
			case (MotionEvent.ACTION_POINTER_DOWN):
				if (actionIndex == 1) {
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
					return true;
				}
			case (MotionEvent.ACTION_POINTER_UP):
				if (actionIndex == 0 || actionIndex == 1) {
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
					mSwipeShiftManager.notifyDragging(event.getX(), event.getY(), maxShiftUp, maxShiftDown,
							maxShiftLeft, maxShiftRight);
					invalidate();
					return true;
				} else {
					int fingers = event.getPointerCount();
					if (fingers == 2) {
						boolean refresh = mPinchZoomManager.notifyPinchingContinues(event, visiblePageCenter,
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
			Log.w(TAG, "not initialized");
			return false;
		}
	}

	// private int[] toPageCoords(float x, float y) {
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
		FIT_TO_SCREEN, NO_FREE_SPACE_ALIGN_TOP_LEFT, NO_FREE_SPACE_ALIGN_CENTER
	}

	public interface LoadingHandler {

		public void onImagePropertiesProcessed(String imagePropertiesUrl);

		public void onImagePropertiesInvalidStateError(String imagePropertiesUrl, int responseCode);

		public void onImagePropertiesRedirectionLoopError(String imagePropertiesUrl, int redirections);

		public void onImagePropertiesDataTransferError(String imagePropertiesUrl, String errorMessage);

		public void onImagePropertiesInvalidDataError(String imagePropertiesUrl, String errorMessage);
	}

}

package cz.mzk.androidzoomifyviewer.viewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import cz.mzk.androidzoomifyviewer.CacheManager;
import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.R;
import cz.mzk.androidzoomifyviewer.cache.TileBitmap;
import cz.mzk.androidzoomifyviewer.cache.TilesCache;
import cz.mzk.androidzoomifyviewer.cache.TilesCache.FetchingBitmapFromDiskHandler;
import cz.mzk.androidzoomifyviewer.gestures.MyGestureListener;
import cz.mzk.androidzoomifyviewer.rectangles.FramingRectangle;
import cz.mzk.androidzoomifyviewer.rectangles.FramingRectangleDrawer;
import cz.mzk.androidzoomifyviewer.tiles.MetadataInitializationHandler;
import cz.mzk.androidzoomifyviewer.tiles.TilePositionInPyramid;
import cz.mzk.androidzoomifyviewer.tiles.TilesDownloader;
import cz.mzk.androidzoomifyviewer.tiles.zoomify.ZoomifyTilesDownloader;

/**
 * @author Martin Řehánek
 */
public class TiledImageView extends View {
    public static final boolean FETCHING_BITMAP_FROM_DISK_CACHE_BLOCKING = false;
    public static final boolean DEV_MODE = true;

    private static final Logger logger = new Logger(TiledImageView.class);

    private static boolean initialized = false;
    boolean mMinZoomCanvasImagePaddingInitialized = false;
    double mCanvasImagePaddingHorizontal = -1;
    double mCanvasImagePaddingVertical = -1;
    int mCanvWidth;
    int mCanvHeight;
    private DevTools devTools = null;
    private ImageCoordsPoints testPoints = null;
    private boolean mViewmodeScaleFactorsInitialized = false;
    private double mInitialScaleFactor = -1.0;
    private double mMinScaleFactor = -1.0;
    private double mMaxScaleFactor = -1.0;
    private String mZoomifyBaseUrl; //todo: vyhledove odstranit, tady to nepatri
    private SingleTapListener mSingleTapListener;
    private boolean pageInitialized = false;
    // SHIFTS
    private boolean mViewmodeShiftInitialized = false;
    private VectorD mViewmodeShift = VectorD.ZERO_VECTOR;
    private boolean mDrawLayerWithWorseResolution = true;

    private ViewMode mViewMode = ViewMode.FIT_TO_SCREEN;
    // private ViewMode mViewMode =
    // ViewMode.NO_FREE_SPACE_ALIGN_HORIZONTAL_LEFT_VERTICAL_TOP;

    private TilesCache mTilesCache;
    private TilesDownloader mActiveImageDownloader;

    // za hranice canvas cela oblast s obrazkem
    private Rect mWholeImageInCanvasCoords = null;
    // jen viditelna cast stranky
    private Rect mVisibleImageInCanvas = null;

    private ImageInitializationHandler mImageInitializationHandler;
    private TileDownloadHandler mTileDownloadHandler;
    private MyGestureListener mGestureListener;

    private FramingRectangleDrawer mFramingRectDrawer;

    public TiledImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TiledImageView(Context context) {
        super(context);
        init(context);
    }

    public static void initialize(Context context) {
        if (initialized) {
            logger.w("initialized already");
        } else {
            Resources res = context.getResources();
            boolean diskCacheEnabled = res.getBoolean(R.bool.androidzoomifyviewer_disk_cache_enabled);
            boolean clearDiskCacheOnStart = res.getBoolean(R.bool.androidzoomifyviewer_disk_cache_clear_on_startup);
            long tileDiskCacheBytes = res.getInteger(R.integer.androidzoomifyviewer_tile_disk_cache_size_kb) * 1024;
            CacheManager.initialize(context, diskCacheEnabled, clearDiskCacheOnStart, tileDiskCacheBytes);
            initialized = true;
        }
    }

    private void init(Context context) {
        if (DEV_MODE) {
            devTools = new DevTools(context);
            logDeviceScreenCategory();
            logHwAcceleration();
        }
        mTilesCache = CacheManager.getTilesCache();
        mGestureListener = new MyGestureListener(this);
        mFramingRectDrawer = new FramingRectangleDrawer(context);
    }

    private void logDeviceScreenCategory() {
        // int size = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        // String category = size == Configuration.SCREENLAYOUT_SIZE_SMALL ? "small"
        // : size == Configuration.SCREENLAYOUT_SIZE_NORMAL ? "normal"
        // : size == Configuration.SCREENLAYOUT_SIZE_LARGE ? "large" : "xlarge";
        // Log.d(TestTags.DISPLAY, "display size: " + category);
        String screenType = getResources().getString(R.string.androidzoomifyviewer_screen_type);
        TestLoggers.DISPLAY.d("screen type: " + screenType);
        double pixelRatio = getResources().getInteger(R.integer.androidzoomifyviewer_pxRatio) / 100.0;
        TestLoggers.DISPLAY.d(String.format("pxRatio: %.2f", pixelRatio));
    }

    @SuppressLint("NewApi")
    private void logHwAcceleration() {
        if (Build.VERSION.SDK_INT >= 11) {
            TestLoggers.DISPLAY.d("(Window) HW accelerated: " + isHardwareAccelerated());
        }
    }

    public ViewMode getViewMode() {
        return mViewMode;
    }

    public void setViewMode(ViewMode viewMode) {
        if (viewMode == null) {
            throw new NullPointerException();
        }
        this.mViewMode = viewMode;
    }

    public void setImageInitializationHandler(ImageInitializationHandler imageInitializationHandler) {
        this.mImageInitializationHandler = imageInitializationHandler;
    }

    public void setTileDownloadHandler(TileDownloadHandler tileDownloadHandler) {
        this.mTileDownloadHandler = tileDownloadHandler;
    }

    public void cancelAllTasks() {
        if (mActiveImageDownloader != null) {
            //mActiveImageDownloader.getTaskRegistry().cancelAllTasks();
            mActiveImageDownloader.cancelAllTasks();
        }
        if (CacheManager.getTilesCache() != null) {
            CacheManager.getTilesCache().cancelAllTasks();
        }
        mGestureListener.stopAllAnimations();
    }

    public void loadImage(String zoomifyBaseUrl) {
        logger.d("loading new image, base url: " + zoomifyBaseUrl);
        pageInitialized = false;
        mViewmodeScaleFactorsInitialized = false;
        mViewmodeShiftInitialized = false;
        mMinZoomCanvasImagePaddingInitialized = false;
        mZoomifyBaseUrl = zoomifyBaseUrl;
        //mActiveImageDownloader = null;
        cancelAllTasks();
        mGestureListener.reset();
        if (mActiveImageDownloader != null) {
            mActiveImageDownloader.destroy();
        }
        double pxRatio = getResources().getInteger(R.integer.androidzoomifyviewer_pxRatio) / 100.0;
        mActiveImageDownloader = new ZoomifyTilesDownloader(mZoomifyBaseUrl, pxRatio);
        initTilesDownloaderAsync();
    }

    public void setFramingRectangles(List<FramingRectangle> framingRectangles) {
        mFramingRectDrawer.setFrameRectangles(framingRectangles);
        invalidate();
    }

    private void initTilesDownloaderAsync() {
        mActiveImageDownloader.initImageMetadataAsync(new MetadataInitializationHandler() {

            @Override
            //public void onSuccess(TilesDownloader downloader) {
            public void onSuccess() {
                logger.d("downloader initialized");
                //mActiveImageDownloader = downloader;
                if (DEV_MODE) {
                    testPoints = new ImageCoordsPoints(mActiveImageDownloader.getImageWidth(), mActiveImageDownloader.getImageHeight());
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
        });
    }

    @Override
    public void onDraw(final Canvas canv) {
        // TestLoggers.THREADS.d("ui: " + Thread.currentThread().getPriority());

        // Debug.startMethodTracing("default");
        // long start = System.currentTimeMillis();
        mCanvWidth = canv.getWidth();
        mCanvHeight = canv.getHeight();

        if (devTools != null) {
            devTools.setCanvas(canv);
            devTools.fillWholeCanvasWithColor(devTools.getPaintYellow());
            // Log.d(TestTags.CENTERS, "canvas(px): width=" + canvWidth +
            // ", height=" + canvHeight);
            // double canvWidthDp = pxToDp(canvWidth);
            // double canvHeightDp = pxToDp(canvHeight);
            // logger.d( "canvas(dp): width=" + canvWidthDp + ", height=" +
            // canvHeightDp);
        }
        if (mActiveImageDownloader.isInitialized()) {
            if (devTools != null) {
                devTools.fillWholeCanvasWithColor(devTools.getPaintBlue());
            }
            if (!mViewmodeScaleFactorsInitialized) {
                initViewmodeScaleFactors(canv);
                mViewmodeScaleFactorsInitialized = true;
            }
            if (!mViewmodeShiftInitialized) {
                initViewmodeShift(canv);
                mViewmodeShiftInitialized = true;
            }
            if (!mMinZoomCanvasImagePaddingInitialized) {
                initMinZoomPadding(canv);
                mMinZoomCanvasImagePaddingInitialized = true;
            }

            // za hranice canvas cela oblast s obrazkem
            mWholeImageInCanvasCoords = computeWholeImageAreaInCanvasCoords(getTotalScaleFactor(), getTotalShift());

            if (devTools != null) {
                devTools.fillRectAreaWithColor(mWholeImageInCanvasCoords, devTools.getPaintRedTrans());
            }

            mVisibleImageInCanvas = computeVisibleInCanvas(canv);
            // Log.d(TestTags.TEST, "canvas width: " + canv.getWidth() + ", height: " + canv.getHeight());
            // Log.d(TestTags.TEST, "canvas: width: " + canv.getWidth() + ", height: " + canv.getHeight());
            // Log.d(TestTags.TEST, "whole   image in canvas: " + mWholeImageInCanvasCoords.toShortString());
            // Log.d(TestTags.TEST, "visible image in canvas: " + mVisibleImageInCanvas.toShortString());
            // Log.d(TestTags.TEST, "visible image in canvas: width: " + mVisibleImageInCanvas.width() + ", height: "
            // + mVisibleImageInCanvas.height());
            if (devTools != null) {
                devTools.fillRectAreaWithColor(mVisibleImageInCanvas, devTools.getPaintGreenTrans());
            }

            int bestLayerId = mActiveImageDownloader.computeBestLayerId(mWholeImageInCanvasCoords);
            // Log.d(TestTags.TEST, "best layer: " + bestLayerId);

            drawLayers(canv, bestLayerId, true, calculateVisibleAreaInImageCoords());

            if (mFramingRectDrawer != null) {
                mFramingRectDrawer.setCanvas(canv);
                mFramingRectDrawer.draw(getTotalScaleFactor(), getTotalShift());
            }

            if (DEV_MODE) {
                if (devTools != null) {
                    double totalScaleFactor = getTotalScaleFactor();
                    VectorD totalShift = getTotalShift();
                    // test points
                    devTools.drawImageCoordPoints(testPoints, totalScaleFactor, totalShift);
                    devTools.drawTileRectStack();
                    // zoom centers
                    // devTools.drawDoubletapZoomCenters(getTotalScaleFactor(), getTotalShift());
                    // devTools.drawPinchZoomCenters(getTotalScaleFactor(), getTotalShift());
                }
            }
        }
        // Debug.stopMethodTracing();
    }

    private void initViewmodeScaleFactors(Canvas canv) {
        int imgWidth = mActiveImageDownloader.getImageWidth();
        int imgHeight = mActiveImageDownloader.getImageHeight();
        double scaleFactorFitToScreen = computeScaleFactorFitToScreen(canv.getWidth(), canv.getHeight(), imgWidth, imgHeight);
        double scaleFactorNoFreeSpace = computeScaleFactorNoFreeSpace(canv.getWidth(), canv.getHeight(), imgWidth, imgHeight);
        switch (mViewMode) {
            case FIT_TO_SCREEN:
                mInitialScaleFactor = scaleFactorFitToScreen;
                break;
            default:
                mInitialScaleFactor = scaleFactorNoFreeSpace;
                break;
        }
        // logger.d( "fit to screen factor: " + mInitialResizeFactor);
        mMinScaleFactor = Math.min(scaleFactorFitToScreen, scaleFactorNoFreeSpace);
        // TestLoggers.PINCH_ZOOM.d("minScale: " + mMinScaleFactor);
        // TODO: spis DP, nez PX
        // double maxWidthScale = (double) imageProperties.getWidth() / (double)
        // canv.getWidth();
        // double maxHeightScale = (double) imageProperties.getHeight() /
        // (double) canv.getHeight();

        // imageProperties.getNumtiles()*imageProperties.get

        // int necoWidthPx = imageProperties.getWidth();
        // int necoHeightPx = imageProperties.getHeight();

        int mustFitInCanvasObjectWidthPx = mActiveImageDownloader.getTileTypicalSize();
        int mustFitInCanvasObjectHeightPx = mActiveImageDownloader.getTileTypicalSize();

        // TestLoggers.PINCH_ZOOM.d("canvas px: [" + canv.getWidth() + "," + canv.getHeight() + "]");
        // TestLoggers.PINCH_ZOOM.d("canvas dp: [" + Utils.pxToDp(canv.getWidth()) + "," + Utils.pxToDp(canv.getHeight())
        // + "]");
        // TestLoggers.PINCH_ZOOM.d("image px: [" + imageProperties.getWidth() + "," + imageProperties.getHeight() + "]");
        // TestLoggers.PINCH_ZOOM.d("tile size: " + imageProperties.getTileSize());

        double maxWidthScalePx = (double) canv.getWidth() / (double) mustFitInCanvasObjectWidthPx;
        double maxHeightScalePx = (double) canv.getHeight() / (double) mustFitInCanvasObjectHeightPx;
        // double maxWidthScaleDp = (double) Utils.pxToDp(canv.getWidth()) / (double) mustFitInCanvasObjectWidthPx;
        // double maxHeightScaleDp = (double) Utils.pxToDp(canv.getHeight()) / (double) mustFitInCanvasObjectHeightPx;
        // TestLoggers.PINCH_ZOOM.d("px: maxWidthScale: " + maxWidthScalePx + ", maxHeightScale: " + maxHeightScalePx);
        // TestLoggers.PINCH_ZOOM.d("dp: maxWidthScale: " + maxWidthScaleDp + ", maxHeightScale: " + maxHeightScaleDp);
        mMaxScaleFactor = Math.min(maxWidthScalePx, maxHeightScalePx);
    }

    public DevTools getDevTools() {
        return devTools;
    }

    private PointD computeVisibleImageCenter() {
        float x = (mVisibleImageInCanvas.width() / 2 + mVisibleImageInCanvas.left);
        float y = (mVisibleImageInCanvas.height() / 2 + mVisibleImageInCanvas.top);
        return new PointD(x, y);
    }

    public double getCanvasImagePaddingHorizontal() {
        return mCanvasImagePaddingHorizontal;
    }

    public double getCanvasImagePaddingVertical() {
        return mCanvasImagePaddingVertical;
    }

    public VectorD getTotalShift() {
        VectorD gestureShifts = mGestureListener.getTotalShift();
        return mViewmodeShift.plus(gestureShifts);
    }

    public double getTotalScaleFactor() {
        return mInitialScaleFactor * mGestureListener.getTotalScaleFactor();
    }

    public double getMinScaleFactor() {
        return mMinScaleFactor;
    }

    public double getMaxScaleFactor() {
        return mMaxScaleFactor;
    }

    private void drawLayers(Canvas canv, int layerId, boolean isIdealLayer, RectD visibleAreaInImageCoords) {
        // long start = System.currentTimeMillis();
        List<TilePositionInPyramid> visibleTiles = mActiveImageDownloader.getVisibleTilesForLayer(layerId, visibleAreaInImageCoords);
        // cancel downloading/saving of not visible tiles within layer
        mActiveImageDownloader.cancelFetchingATilesForLayerExeptForThese(layerId, visibleTiles);
        // TODO: 4.12.15 Vyresit zruseni stahovani pro ty vrstvy, ktere uz nejsou relevantni. Treba kdyz rychle odzumuju
        if (isIdealLayer) {
            // possibly increase memory cache
            if (CacheManager.getTilesCache() != null) {
                // TODO: 4.12.15 jeste projit zvetsovani cache. Nemela by se treba zmensovat a i nad tim faktorem pouvazovat
                int minCacheSize = (visibleTiles.size() * 2);
                // int maxCacheSize = (int) (visibleTiles.size() * 5.5);
                // CacheManager.getTilesCache().updateMemoryCacheSizeInItems(minCacheSize, maxCacheSize);
                CacheManager.getTilesCache().updateMemoryCacheSizeInItems(minCacheSize);
            }
        }
        // check if all visible tiles within layer are available
        boolean allTilesAvailable = true;
        for (TilePositionInPyramid visibleTile : visibleTiles) {
            boolean tileAccessible = FETCHING_BITMAP_FROM_DISK_CACHE_BLOCKING ? CacheManager.getTilesCache()
                    .containsTile(mZoomifyBaseUrl, visibleTile) : CacheManager.getTilesCache().containsTileInMemory(mZoomifyBaseUrl, visibleTile);
            if (!tileAccessible) {
                allTilesAvailable = false;
                break;
            }
        }
        // if not all visible tiles available, draw smaller layer with worse resolution under it
        // TODO: disable, just for testing
        // mDrawLayerWithWorseResolution = false;
        if (!allTilesAvailable && layerId != 0 && mDrawLayerWithWorseResolution) {
            drawLayers(canv, layerId - 1, false, visibleAreaInImageCoords);
        }
        // draw visible tiles if available, start downloading otherwise
        for (TilePositionInPyramid visibleTile : visibleTiles) {
            if (FETCHING_BITMAP_FROM_DISK_CACHE_BLOCKING) {
                fetchTileBlocking(canv, visibleTile);
            } else {
                fetchTileNonblocking(canv, visibleTile);
            }
        }
        // long end = System.currentTimeMillis();
        // logger.d( "drawLayers (layer=" + layerId + "): " + (end - start) +
        // " ms");
    }

    private void fetchTileBlocking(Canvas canv, TilePositionInPyramid visibleTileId) {
        Bitmap tile = mTilesCache.getTile(mZoomifyBaseUrl, visibleTileId);
        if (tile != null) {
            drawTile(canv, visibleTileId, tile);
        } else {
            enqueTileDownload(visibleTileId);
        }
    }

    private void fetchTileNonblocking(Canvas canv, TilePositionInPyramid visibleTileId) {
        TileBitmap tile = mTilesCache.getTileAsync(mZoomifyBaseUrl, visibleTileId, new FetchingBitmapFromDiskHandler() {

            @Override
            public void onFetched() {
                invalidate();
            }
        });
        switch (tile.getState()) {
            case IN_MEMORY:
                drawTile(canv, visibleTileId, tile.getBitmap());
                break;
            case IN_DISK:
                // nothing, wait for fetch
                break;
            case NOT_FOUND:
                enqueTileDownload(visibleTileId);
        }
    }

    private void enqueTileDownload(TilePositionInPyramid visibleTileId) {
        mActiveImageDownloader.enqueTileFetching(visibleTileId, new cz.mzk.androidzoomifyviewer.tiles.TileDownloadHandler() {

            @Override
            public void onUnhandableResponseCode(TilePositionInPyramid tilePositionInPyramid, String tileUrl, int responseCode) {
                if (mTileDownloadHandler != null) {
                    mTileDownloadHandler.onTileUnhandableResponseError(tilePositionInPyramid, tileUrl, responseCode);
                }
            }

            @Override
            public void onSuccess(TilePositionInPyramid tilePositionInPyramid, Bitmap bitmap) {
                invalidate();
                if (mTileDownloadHandler != null) {
                    mTileDownloadHandler.onTileProcessed(tilePositionInPyramid);
                }
            }

            @Override
            public void onRedirectionLoop(TilePositionInPyramid tilePositionInPyramid, String tileUrl, int redirections) {
                if (mTileDownloadHandler != null) {
                    mTileDownloadHandler.onTileRedirectionLoopError(tilePositionInPyramid, tileUrl, redirections);
                }
            }

            @Override
            public void onInvalidData(TilePositionInPyramid tilePositionInPyramid, String tileUrl, String errorMessage) {
                if (mTileDownloadHandler != null) {
                    mTileDownloadHandler.onTileInvalidDataError(tilePositionInPyramid, tileUrl, errorMessage);
                }
            }

            @Override
            public void onDataTransferError(TilePositionInPyramid tilePositionInPyramid, String tileUrl, String errorMessage) {
                if (mTileDownloadHandler != null) {
                    mTileDownloadHandler.onTileDataTransferError(tilePositionInPyramid, tileUrl, errorMessage);
                }
            }
        });
    }

    private void drawTile(Canvas canv, TilePositionInPyramid tileId, Bitmap tileBmp) {
        Rect tileInCanvas = toTileAreaInCanvas(tileId, tileBmp).toRect();
        // Log.d(TestTags.TEST, "drawing tile: " + tileId + " to: " + tileInCanvas.toShortString());
        canv.drawBitmap(tileBmp, null, tileInCanvas, null);
        if (devTools != null) {
            // devTools.highlightTile(tileInCanvas, devTools.getPaintBlack());
            // devTools.highlightTile(tileInCanvas, devTools.getPaintWhiteTrans());
            devTools.highlightTile(tileInCanvas, devTools.getPaintRed());
        }
    }

    public int getCanvWidth() {
        return mCanvWidth;
    }

    public int getCanvHeight() {
        return mCanvHeight;
    }

    private RectD calculateVisibleAreaInImageCoords() {
        double resizeFactor = getTotalScaleFactor();
        VectorD totalShift = getTotalShift();
        return Utils.toImageCoords(new RectD(mVisibleImageInCanvas), resizeFactor, totalShift);
    }


    private void setDrawLayerWithWorseResolution(boolean show) {
        mDrawLayerWithWorseResolution = show;
    }


    private RectD toTileAreaInCanvas(TilePositionInPyramid tilePositionInPyramid, Bitmap tile) {
        Rect tileAreaInImageCoords = mActiveImageDownloader.getTileAreaInImageCoords(tilePositionInPyramid);
        return Utils.toCanvasCoords(tileAreaInImageCoords, getTotalScaleFactor(), getTotalShift());
    }

    private double computeScaleFactorFitToScreen(double canvasWidth, double canvasHeight, double imgOriginalWidth,
                                                 double imgOriginalHeight) {
        double widthRatio = canvasWidth / imgOriginalWidth;
        double heightRatio = canvasHeight / imgOriginalHeight;
        // logger.d( "widthRatio=" + widthRatio + ", heightRatio=" +
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

    private double computeScaleFactorNoFreeSpace(double canvasWidth, double canvasHeight, double imgOriginalWidth,
                                                 double imgOriginalHeight) {
        double widthRatio = canvasWidth / imgOriginalWidth;
        double heightRatio = canvasHeight / imgOriginalHeight;
        // logger.d( "widthRatio=" + widthRatio + ", heightRatio=" +
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

    private void initViewmodeShift(Canvas canv) {
        double canvasWidth = canv.getWidth();
        double canvasHeight = canv.getHeight();
        double imageOriginalWidth = mActiveImageDownloader.getImageWidth();
        double imageOriginalHeight = mActiveImageDownloader.getImageHeight();
        double actualWidth = imageOriginalWidth * mInitialScaleFactor;
        double actualHeight = imageOriginalHeight * mInitialScaleFactor;
        double extraSpaceWidthCanv = canvasWidth - actualWidth;
        double extraSpaceHeightCanv = canvasHeight - actualHeight;

        double xLeft = 0;
        double xCenter = extraSpaceWidthCanv / 2.0;
        double xRight = extraSpaceWidthCanv;
        double yTop = 0;
        double yCenter = extraSpaceHeightCanv / 2.0;
        double yBottom = extraSpaceHeightCanv;

        switch (mViewMode) {
            case FIT_TO_SCREEN:
                mViewmodeShift = new VectorD(xCenter, yCenter);
                break;
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_LEFT_VERTICAL_TOP:
                mViewmodeShift = new VectorD(xLeft, yTop);
                break;
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_LEFT_VERTICAL_CENTER:
                mViewmodeShift = new VectorD(xLeft, yCenter);
                break;
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_LEFT_VERTICAL_BOTTOM:
                mViewmodeShift = new VectorD(xLeft, yBottom);
                break;
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_CENTER_VERTICAL_TOP:
                mViewmodeShift = new VectorD(xCenter, yTop);
                break;
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_CENTER_VERTICAL_CENTER:
                mViewmodeShift = new VectorD(xCenter, yCenter);
                break;
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_CENTER_VERTICAL_BOTTOM:
                mViewmodeShift = new VectorD(xCenter, yBottom);
                break;
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_RIGHT_VERTICAL_TOP:
                mViewmodeShift = new VectorD(xRight, yTop);
                break;
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_RIGHT_VERTICAL_CENTER:
                mViewmodeShift = new VectorD(xRight, yCenter);
                break;
            case NO_FREE_SPACE_ALIGN_HORIZONTAL_RIGHT_VERTICAL_BOTTOM:
                mViewmodeShift = new VectorD(xRight, yBottom);
                break;
        }
        TestLoggers.CENTERS.d("initial shift:" + mViewmodeShift);
    }

    private void initMinZoomPadding(Canvas canv) {
        PointD imgBottomRight = new PointD(mActiveImageDownloader.getImageWidth(), mActiveImageDownloader.getImageHeight());
        PointD imgInCanvasBottomRight = Utils.toCanvasCoords(imgBottomRight, mMinScaleFactor, VectorD.ZERO_VECTOR);
        double freeWidth = (canv.getWidth() - imgInCanvasBottomRight.x) * 0.5;
        double freeHeight = (canv.getHeight() - imgInCanvasBottomRight.y) * 0.5;
        mCanvasImagePaddingHorizontal = Utils.toXInImageCoords(freeWidth, mMinScaleFactor, 0);
        mCanvasImagePaddingVertical = Utils.toYInImageCoords(freeHeight, mMinScaleFactor, 0);
        // Log.d(TestTags.CORNERS, "initMinZoomBorders: width: " +
        // mCanvasImagePaddingHorizontal + ", height: "
        // + mCanvasImagePaddingVertical);
    }

    public Rect computeWholeImageAreaInCanvasCoords(double scaleFactor, VectorD shift) {
        double imgWidth = mActiveImageDownloader.getImageWidth();
        double imgHeight = mActiveImageDownloader.getImageHeight();
        PointD imgTopLeft = new PointD(0, 0);
        PointD imgBottomRight = new PointD(imgWidth, imgHeight);
        PointD imgInCanvasTopLeft = Utils.toCanvasCoords(imgTopLeft, scaleFactor, shift);
        PointD imgInCanvasBottomRight = Utils.toCanvasCoords(imgBottomRight, scaleFactor, shift);
        Rect result = new Rect((int) imgInCanvasTopLeft.x, (int) imgInCanvasTopLeft.y, (int) imgInCanvasBottomRight.x,
                (int) imgInCanvasBottomRight.y);
        // TestTags.TILES.d( "computeAreaInCanvas result: " + result.toShortString());
        return result;
    }

    private Rect computeVisibleInCanvas(Canvas canv) {
        int left = mapNumberToInterval(mWholeImageInCanvasCoords.left, 0, canv.getWidth());
        int right = mapNumberToInterval(mWholeImageInCanvasCoords.right, 0, canv.getWidth());
        int top = mapNumberToInterval(mWholeImageInCanvasCoords.top, 0, canv.getHeight());
        int bottom = mapNumberToInterval(mWholeImageInCanvasCoords.bottom, 0, canv.getHeight());
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
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureListener.onTouchEvent(event);
    }

    public SingleTapListener getSingleTapListener() {
        return mSingleTapListener;
    }

    public void setSingleTapListener(SingleTapListener singleTapListener) {
        this.mSingleTapListener = singleTapListener;
    }

    public Rect getVisibleImageInCanvas() {
        return mVisibleImageInCanvas;
    }

    public double getInitialScaleFactor() {
        return mInitialScaleFactor;
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

    public interface SingleTapListener {
        /**
         * This method is called after single tap, that is confirmed not to be double tap and also has not been used internally by
         * this view. I.e. for zooming, swiping etc.
         *
         * @param x           x coordinate of the tap
         * @param y           y coordinate of the tap
         * @param boundingBox area containing the image
         */
        public void onSingleTap(float x, float y, Rect boundingBox);
    }

    /**
     * Exactly one of these methods is called eventually after loadImage(). Either onImagePropertiesProcessed() if
     * ImageProperties.xml is found, downloaded and processed or one of the other methods in case of some error.
     *
     * @author martin
     */
    public interface ImageInitializationHandler {

        /**
         * ImageProperties.xml downloaded and processed properly.
         */
        public void onImagePropertiesProcessed();

        /**
         * Response to HTTP request for ImageProperties.xml returned code that cannot be handled here. That means almost
         * everything except for some 2xx codes and some 3xx codes for which redirection is applied.
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
     * Exactly one of these methods is called after tile is downloaded and stored to cache or something goes wrong in this
     * process.
     *
     * @author martin
     */
    public interface TileDownloadHandler {

        /**
         * Tile downloaded and processed properly.
         *
         * @param tilePositionInPyramid Tile id.
         */
        public void onTileProcessed(TilePositionInPyramid tilePositionInPyramid);

        /**
         * Response to HTTP request for tile returned code that cannot be handled here. That means almost everything except for
         * some 2xx codes and some 3xx codes for which redirection is applied.
         *
         * @param tilePositionInPyramid Tile id.
         * @param tileUrl               Tile jpeg url.
         * @param responseCode          Http response code recieved.
         */

        public void onTileUnhandableResponseError(TilePositionInPyramid tilePositionInPyramid, String tileUrl, int responseCode);

        /**
         * Too many redirections for tile, probably loop.
         *
         * @param tilePositionInPyramid Tile id.
         * @param tileUrl               Tile jpeg url.
         * @param redirections          Total redirections.
         */
        public void onTileRedirectionLoopError(TilePositionInPyramid tilePositionInPyramid, String tileUrl, int redirections);

        /**
         * Other errors in transfering tile - timeouts etc.
         *
         * @param tilePositionInPyramid Tile id.
         * @param tileUrl               Tile jpeg url.
         * @param errorMessage          Error message.
         */
        public void onTileDataTransferError(TilePositionInPyramid tilePositionInPyramid, String tileUrl, String errorMessage);

        /**
         * Invalid tile content.
         *
         * @param tilePositionInPyramid Tile id.
         * @param tileUrl               Tile jpeg url.
         * @param errorMessage          Error message.
         */
        public void onTileInvalidDataError(TilePositionInPyramid tilePositionInPyramid, String tileUrl, String errorMessage);
    }

}

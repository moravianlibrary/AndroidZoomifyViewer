package cz.mzk.tiledimageview;

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

import cz.mzk.tiledimageview.dev.DevLoggers;
import cz.mzk.tiledimageview.dev.DevPoints;
import cz.mzk.tiledimageview.dev.DevTools;
import cz.mzk.tiledimageview.gestures.MyGestureListener;
import cz.mzk.tiledimageview.images.ImageManager;
import cz.mzk.tiledimageview.images.TilePositionInPyramid;
import cz.mzk.tiledimageview.images.TiledImageProtocol;
import cz.mzk.tiledimageview.images.cache.CacheManager;
import cz.mzk.tiledimageview.images.metadata.ImageMetadata;
import cz.mzk.tiledimageview.images.tasks.TaskManager;
import cz.mzk.tiledimageview.images.zoomify.ZoomifyImageManager;
import cz.mzk.tiledimageview.rectangles.FramingRectangle;
import cz.mzk.tiledimageview.rectangles.FramingRectangleDrawer;

/**
 * @author Martin Řehánek
 */
public class TiledImageView extends View implements TiledImageViewApi {
    public static final boolean DEV_MODE = false;// TODO: 7.12.15 configurable
    private static final Logger LOGGER = new Logger(TiledImageView.class);
    //STATE
    private boolean mAttachedToWindow = false;
    private boolean mVisible = false;
    private boolean mLowerQuality = false;
    private boolean mMinZoomCanvasImagePaddingInitialized = false;
    private boolean mHelpersInitialized = false;

    //CANVAS
    private double mCanvasImagePaddingHorizontal = -1;
    private double mCanvasImagePaddingVertical = -1;
    private Rect mWholeImageAreaInCanvasCoords = null; // whole image area in canvas coords, even from invisible canvas part (i.e. top and left can be negative)
    private Rect mVisibleImageAreaInCanvas = null;     // only part of image (in canvas coords) that is in visible part of canvas
    private boolean mViewmodeScaleFactorsInitialized = false;
    private double mPxRatio;

    // SHIFT
    private boolean mViewmodeShiftInitialized = false;
    private VectorD mViewmodeShift = VectorD.ZERO_VECTOR;

    //SCALE
    private double mInitialScaleFactor = -1.0;
    private double mMinScaleFactor = -1.0;
    private double mMaxScaleFactor = -1.0;

    //VIEW MODE
    private ViewMode mViewMode = ViewMode.FIT_TO_SCREEN;

    // TILES ACCESS
    private TiledImageProtocol mtiledImageProtocol;
    // TODO: 14.12.15 mozna baseUrl a protocol zabalit do objektu
    private String mImageBaseUrl;
    private ImageManager mImageManager;

    //EVENT LISTENERS
    private MetadataInitializationListener mMetadataInitializationListener;
    private TileDownloadErrorListener mTileDownloadErrorListener;

    //GESTURES
    private MyGestureListener mGestureListener;
    private SingleTapListener mSingleTapListener;

    //FRAMING RECTANGLES
    private FramingRectangleDrawer mFramingRectDrawer;

    //DEV
    private DevTools mDevTools = null;
    private DevPoints mTestPoints = null;


    public TiledImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LOGGER.i(buildMethodLog("constructor(Context,AttributeSet)"));
        initHelpers();
        logDeviceScreenCategory();
        logHwAcceleration();
    }

    public TiledImageView(Context context) {
        super(context);
        LOGGER.i(buildMethodLog("constructor(Context)"));
        initHelpers();
        logDeviceScreenCategory();
        logHwAcceleration();
    }

    private String buildMethodLog(String method) {
        if (DEV_MODE) {
            int instanceId = hashCode();
            return String.valueOf(instanceId) + ": " + method;
        } else {
            return method;
        }
    }

    private void init(Context context) {
        LOGGER.i(buildMethodLog("init"));
        mPxRatio = getResources().getInteger(R.integer.tiledimageview_pxRatio) / 100.0;
        if (mImageBaseUrl != null && mImageManager != null) {
            initImageManager();
        } else {
            if (mImageBaseUrl == null) {
                LOGGER.d(buildMethodLog("init: mImageBaseUrl is null"));
            }
            if (mImageManager == null) {
                LOGGER.d(buildMethodLog("init: mImageManager is null"));
            }
        }
    }


    /**
     * Must be called at least once so that cache can be initialized. Typically in Application.onCreate()
     *
     * @param context
     */
    private void initCache(final Context context) {
        LOGGER.i(buildMethodLog("initCache"));
        if (!CacheManager.isInitialized()) {
            Resources res = context.getResources();
            boolean diskCacheEnabled = res.getBoolean(R.bool.tiledimageview_disk_cache_enabled);
            boolean clearDiskCacheOnStart = res.getBoolean(R.bool.tiledimageview_disk_cache_clear_in_initialization);
            long tileDiskCacheBytes = res.getInteger(R.integer.tiledimageview_tile_disk_cache_size_kb) * 1024;
            TaskManager.enqueueCacheManagerInitialization(context, diskCacheEnabled, clearDiskCacheOnStart, tileDiskCacheBytes, new TaskManager.TaskListener() {
                @Override
                public void onFinished(Object... data) {
                    if (mAttachedToWindow) {
                        init(context);
                    }
                }

                @Override
                public void onCanceled() {
                    if (mAttachedToWindow) {
                        initCache(context);//retrying
                    }
                }
            });
        } else {
            init(context);
        }
    }

    private void logDeviceScreenCategory() {
        // int size = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        // String category = size == Configuration.SCREENLAYOUT_SIZE_SMALL ? "small"
        // : size == Configuration.SCREENLAYOUT_SIZE_NORMAL ? "normal"
        // : size == Configuration.SCREENLAYOUT_SIZE_LARGE ? "large" : "xlarge";
        // Log.d(TestTags.DISPLAY, "display size: " + category);
        String screenType = getResources().getString(R.string.tiledimageview_screen_type);
        DevLoggers.DISPLAY.d("screen type: " + screenType);
        double pixelRatio = getResources().getInteger(R.integer.tiledimageview_pxRatio) / 100.0;
        DevLoggers.DISPLAY.d(String.format("pxRatio: %.2f", pixelRatio));
    }

    @SuppressLint("NewApi")
    private void logHwAcceleration() {
        if (Build.VERSION.SDK_INT >= 11) {
            DevLoggers.DISPLAY.d("(Window) HW accelerated: " + isHardwareAccelerated());
        }
    }

    @Override
    public ViewMode getViewMode() {
        return mViewMode;
    }

    @Override
    public void setViewMode(ViewMode viewMode) {
        if (viewMode == null) {
            throw new NullPointerException();
        }
        mViewMode = viewMode;
    }

    @Override
    public void setMetadataInitializationListener(MetadataInitializationListener listener) {
        LOGGER.d(buildMethodLog("setMetadataInitializationListener"));
        mMetadataInitializationListener = listener;
    }

    @Override
    public void setTileDownloadErrorListener(TileDownloadErrorListener errorListener) {
        mTileDownloadErrorListener = errorListener;
    }

    @Override
    public void setLowerQuality(boolean lowerQuality) {
        mLowerQuality = lowerQuality;
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initHelpers();
        mAttachedToWindow = true;
        LOGGER.i(buildMethodLog("onAttachedToWindow"));
        mVisible = getVisibility() == VISIBLE;
        if (mImageBaseUrl != null) {
            mImageManager = constructImageManager();
        }
        initCache(getContext());
    }

    private void initHelpers() {
        LOGGER.d(buildMethodLog("initHelpers"));
        if (!mHelpersInitialized) {
            if (DEV_MODE) {
                mDevTools = new DevTools(getContext());
            }
            mGestureListener = new MyGestureListener(getContext(), this, mDevTools);
            mFramingRectDrawer = new FramingRectangleDrawer(getContext());
            mHelpersInitialized = true;
        }
    }

    private void clearHelpers() {
        LOGGER.d(buildMethodLog("clearHelpers"));
        mHelpersInitialized = false;
        if (mGestureListener != null) {
            mGestureListener.stopAllAnimations();
            mGestureListener = null;
        }
        mFramingRectDrawer = null;
        mDevTools = null;
    }

    @Override
    protected void onDetachedFromWindow() {
        LOGGER.i(buildMethodLog("ondDetachedFromWindow"));
        mAttachedToWindow = false;
        if (mImageManager != null) {
            mImageManager.cancelAllTasks();
            mImageManager = null;
        }
        clearHelpers();
        //clear client listeners
        mSingleTapListener = null;
        mMetadataInitializationListener = null;
        mTileDownloadErrorListener = null;

        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        LOGGER.i(buildMethodLog("onVisibilityChanged: " + Utils.visibilityToString(visibility)));
        mVisible = visibility == View.VISIBLE;
        /*if (!mVisible && mImageManager != null) {
            mImageManager.cancelAllTasks();
        }*/
        invalidate();
    }


    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        LOGGER.i(buildMethodLog("onWindowVisibilityChanged: " + Utils.visibilityToString(visibility)));
        boolean visible = visibility == View.VISIBLE;
        if (!visible) {
            mVisible = false;
        }
        /*if (!mVisible && mImageManager != null) {
            mImageManager.cancelAllTasks();
        }*/
        invalidate();
    }


    @Override
    public void loadImage(TiledImageProtocol tiledImageProtocol, String baseUrl) {
        //LOGGER.d("loading new image, base url: " + baseUrl);
        LOGGER.d(buildMethodLog("loadImage: " + baseUrl));
        mViewmodeScaleFactorsInitialized = false;
        mViewmodeShiftInitialized = false;
        mMinZoomCanvasImagePaddingInitialized = false;
        mImageBaseUrl = baseUrl;
        mtiledImageProtocol = tiledImageProtocol;
        if (mGestureListener != null) {
            mGestureListener.reset();
        }
        if (CacheManager.isInitialized()) {
            if (mImageManager != null) {
                mImageManager.cancelAllTasks();
            }
            mImageManager = constructImageManager();
            initImageManager();
        }
    }

    private ImageManager constructImageManager() {
        // TODO: 8.12.15 use tiledImageProtocol when other implementation is available
        switch (mtiledImageProtocol) {
            case ZOOMIFY:
                return new ZoomifyImageManager(mImageBaseUrl, mPxRatio);
            default:
                throw new RuntimeException("unknown protocol " + mtiledImageProtocol.name());
        }
    }

    private void initImageManager() {
        LOGGER.d(buildMethodLog("initImageManager"));
        final ImageManager imgManagerUsed = mImageManager;
        ImageMetadata metadata = mImageManager.getMetadata(new MetadataInitializationSuccessListener() {

            @Override
            public void onMetadataFetched(ImageMetadata metadata) {
                LOGGER.i(buildMethodLog("initImageManager: metadata fetched"));
                if (imgManagerUsed.equals(mImageManager)) {
                    initImageManager(metadata);
                } else {
                    LOGGER.d(buildMethodLog("initImageManager: ImageManager instance changed, ignoring"));
                }
            }
        }, mMetadataInitializationListener);
        if (metadata != null) {
            LOGGER.d(buildMethodLog("initImageManager: metadata!=null"));
            initImageManager(metadata);
            mMetadataInitializationListener.onMetadataInitialized();
        } else {
            LOGGER.d(buildMethodLog("initImageManager: metadata==null"));
        }
    }

    private void initImageManager(ImageMetadata metadata) {
        LOGGER.d(buildMethodLog("initImageManager (with metadata)"));
        mImageManager.init(metadata);
        if (DEV_MODE) {
            mTestPoints = new DevPoints(mImageManager.getImageWidth(), mImageManager.getImageHeight());
        }
    }

    @Override
    public void setFramingRectangles(List<FramingRectangle> framingRectangles) {
        mFramingRectDrawer.setFrameRectangles(framingRectangles);
        invalidate();
    }

    @Override
    public void onDraw(final Canvas canv) {
        //LOGGER.i("onDraw");
        if (mDevTools != null) {
            mDevTools.setCanvas(canv);
            mDevTools.fillWholeCanvasWithColor(mDevTools.getPaintYellow());
        }
        if (mImageManager != null && mImageManager.isInitialized()) {
            if (mDevTools != null) {
                mDevTools.fillWholeCanvasWithColor(mDevTools.getPaintBlue());
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

            // whole image area
            mWholeImageAreaInCanvasCoords = computeWholeImageAreaInCanvasCoords(getTotalScaleFactor(), getTotalShift());
            if (mDevTools != null) {
                mDevTools.fillRectAreaWithColor(mWholeImageAreaInCanvasCoords, mDevTools.getPaintRedTrans());
            }
            //visible image area
            mVisibleImageAreaInCanvas = computeVisibleImageAreaInCanvas(canv);
            if (mDevTools != null) {
                mDevTools.fillRectAreaWithColor(mVisibleImageAreaInCanvas, mDevTools.getPaintGreenTrans());
            }

            //determining hihest level to be drawn
            int bestLayerId;
            if (!mVisible) {
                bestLayerId = 0;
            } else {
                bestLayerId = mImageManager.computeBestLayerId(mWholeImageAreaInCanvasCoords);
                if (mLowerQuality && bestLayerId > 1) {
                    bestLayerId -= 1;
                }
            }
            //cancel fetchnig data for layers no longer needed to be drawn now
            mImageManager.cancelFetchingAllTilesForLayersBiggerThan(bestLayerId);

            //draw tiles
            drawTiles(canv, bestLayerId, true, calculateVisibleAreaInImageCoords());

            //draw framing rectangles
            if (mFramingRectDrawer != null) {
                mFramingRectDrawer.setCanvas(canv);
                mFramingRectDrawer.draw(getTotalScaleFactor(), getTotalShift());
            }

            //draw dev rectangles, points
            if (mDevTools != null) {
                double totalScaleFactor = getTotalScaleFactor();
                VectorD totalShift = getTotalShift();
                // test points
                mDevTools.drawImageCoordPoints(mTestPoints, totalScaleFactor, totalShift);
                mDevTools.drawTileRectStack();
                // zoom centers
                //mDevTools.drawDoubletapZoomCenters(getTotalScaleFactor(), getTotalShift());
                //mDevTools.drawPinchZoomCenters(getTotalScaleFactor(), getTotalShift());
            }
        }
        // Debug.stopMethodTracing();
    }

    private void initViewmodeScaleFactors(Canvas canv) {
        int imgWidth = mImageManager.getImageWidth();
        int imgHeight = mImageManager.getImageHeight();
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
        // LOGGER.d( "fit to screen factor: " + mInitialResizeFactor);
        mMinScaleFactor = Math.min(scaleFactorFitToScreen, scaleFactorNoFreeSpace);
        // DevLoggers.PINCH_ZOOM.d("minScale: " + mMinScaleFactor);
        // TODO: spis DP, nez PX
        // double maxWidthScale = (double) imageProperties.getWidth() / (double)
        // canv.getWidth();
        // double maxHeightScale = (double) imageProperties.getHeight() /
        // (double) canv.getHeight();

        // imageProperties.getNumtiles()*imageProperties.get

        // int necoWidthPx = imageProperties.getWidth();
        // int necoHeightPx = imageProperties.getHeight();

        int mustFitInCanvasObjectWidthPx = mImageManager.getTileTypicalSize();
        int mustFitInCanvasObjectHeightPx = mImageManager.getTileTypicalSize();

        // DevLoggers.PINCH_ZOOM.d("canvas px: [" + canv.getWidth() + "," + canv.getHeight() + "]");
        // DevLoggers.PINCH_ZOOM.d("canvas dp: [" + Utils.pxToDp(canv.getWidth()) + "," + Utils.pxToDp(canv.getHeight())
        // + "]");
        // DevLoggers.PINCH_ZOOM.d("image px: [" + imageProperties.getWidth() + "," + imageProperties.getHeight() + "]");
        // DevLoggers.PINCH_ZOOM.d("tile size: " + imageProperties.getTileSize());

        double maxWidthScalePx = (double) canv.getWidth() / (double) mustFitInCanvasObjectWidthPx;
        double maxHeightScalePx = (double) canv.getHeight() / (double) mustFitInCanvasObjectHeightPx;
        // double maxWidthScaleDp = (double) Utils.pxToDp(canv.getWidth()) / (double) mustFitInCanvasObjectWidthPx;
        // double maxHeightScaleDp = (double) Utils.pxToDp(canv.getHeight()) / (double) mustFitInCanvasObjectHeightPx;
        // DevLoggers.PINCH_ZOOM.d("px: maxWidthScale: " + maxWidthScalePx + ", maxHeightScale: " + maxHeightScalePx);
        // DevLoggers.PINCH_ZOOM.d("dp: maxWidthScale: " + maxWidthScaleDp + ", maxHeightScale: " + maxHeightScaleDp);
        mMaxScaleFactor = Math.min(maxWidthScalePx, maxHeightScalePx);
    }

    private PointD computeVisibleImageCenter() {
        float x = (mVisibleImageAreaInCanvas.width() / 2 + mVisibleImageAreaInCanvas.left);
        float y = (mVisibleImageAreaInCanvas.height() / 2 + mVisibleImageAreaInCanvas.top);
        return new PointD(x, y);
    }

    @Override
    public double getCanvasImagePaddingHorizontal() {
        return mCanvasImagePaddingHorizontal;
    }

    @Override
    public double getCanvasImagePaddingVertical() {
        return mCanvasImagePaddingVertical;
    }

    @Override
    public VectorD getTotalShift() {
        VectorD gestureShifts = mGestureListener.getTotalShift();
        return mViewmodeShift.plus(gestureShifts);
    }

    @Override
    public double getTotalScaleFactor() {
        return mInitialScaleFactor * mGestureListener.getTotalScaleFactor();
    }

    @Override
    public double getMinScaleFactor() {
        return mMinScaleFactor;
    }

    @Override
    public double getMaxScaleFactor() {
        return mMaxScaleFactor;
    }


    private void drawTiles(Canvas canv, int layer, boolean isIdealLayer, Rect visibleAreaInImageCoords) {
        //LOGGER.i("drawTiles " + layer);
        List<TilePositionInPyramid> visibleTilesInThisLayer = mImageManager.getVisibleTilesForLayer(layer, visibleAreaInImageCoords);
        // cancel fetching of not-visible-now tiles within layer
        mImageManager.cancelFetchingTilesForLayerExeptForThese(layer, visibleTilesInThisLayer);
        if (isIdealLayer) {
            // possibly increase memory cache
            mImageManager.inflateTilesMemoryCache(visibleTilesInThisLayer.size() * 2);
        }
        // check if all visible tiles within layer are available
        boolean allTilesAvailable = true;
        for (TilePositionInPyramid visibleTile : visibleTilesInThisLayer) {
            if (!mImageManager.tileIsAvailableNow(visibleTile)) {
                allTilesAvailable = false;
                break;
            }
        }
        // if not all visible tiles available, draw lower layer with worse resolution first
        if (!allTilesAvailable && layer != 0) {
            drawTiles(canv, layer - 1, false, visibleAreaInImageCoords);
        }
        //actually draw this layer
        //LOGGER.i("actually drawing layer " + layer);
        boolean allTilesDrawn = true;
        for (TilePositionInPyramid visibleTile : visibleTilesInThisLayer) {
            Bitmap bitmap = mImageManager.getTile(visibleTile, new TileDownloadSuccessListener() {
                @Override
                public void onTileDelivered() {
                    // tile not available yet, but when it's fetched it should be drawn probably
                    invalidate();
                }
            }, mTileDownloadErrorListener);
            if (bitmap != null) {
                drawTile(canv, visibleTile, bitmap);
            } else {
                allTilesDrawn = false;
            }
        }
        if (allTilesDrawn) {
            if (layer != 0) {
                mImageManager.cancelFetchingAllTilesForLayersSmallerThan(layer);
            }
        } else { //make sure it will be redrawn
            invalidate();
        }

    }

    private void drawTile(Canvas canv, TilePositionInPyramid tileId, Bitmap tileBmp) {
        Rect tileInCanvas = toTileAreaInCanvas(tileId, tileBmp);
        // Log.d(TestTags.TEST, "drawing tile: " + tileId + " to: " + tileInCanvas.toShortString());
        canv.drawBitmap(tileBmp, null, tileInCanvas, null);
        if (mDevTools != null) {
            // mDevTools.highlightTile(tileInCanvas, mDevTools.getPaintBlack());
            // mDevTools.highlightTile(tileInCanvas, mDevTools.getPaintWhiteTrans());
            mDevTools.highlightTile(tileInCanvas, mDevTools.getPaintRed());
        }
    }

    private Rect calculateVisibleAreaInImageCoords() {
        double resizeFactor = getTotalScaleFactor();
        VectorD totalShift = getTotalShift();
        return Utils.toImageCoords(mVisibleImageAreaInCanvas, resizeFactor, totalShift);
    }

    private Rect toTileAreaInCanvas(TilePositionInPyramid tilePositionInPyramid, Bitmap tile) {
        Rect tileAreaInImageCoords = mImageManager.getTileAreaInImageCoords(tilePositionInPyramid);
        return Utils.toCanvasCoords(tileAreaInImageCoords, getTotalScaleFactor(), getTotalShift());
    }

    private double computeScaleFactorFitToScreen(double canvasWidth, double canvasHeight, double imgOriginalWidth,
                                                 double imgOriginalHeight) {
        double widthRatio = canvasWidth / imgOriginalWidth;
        double heightRatio = canvasHeight / imgOriginalHeight;
        // LOGGER.d( "widthRatio=" + widthRatio + ", heightRatio=" +
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
        // LOGGER.d( "widthRatio=" + widthRatio + ", heightRatio=" +
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
        double imageOriginalWidth = mImageManager.getImageWidth();
        double imageOriginalHeight = mImageManager.getImageHeight();
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
        DevLoggers.CENTERS.d("initial shift:" + mViewmodeShift);
    }

    private void initMinZoomPadding(Canvas canv) {
        PointD imgBottomRight = new PointD(mImageManager.getImageWidth(), mImageManager.getImageHeight());
        PointD imgInCanvasBottomRight = Utils.toCanvasCoords(imgBottomRight, mMinScaleFactor, VectorD.ZERO_VECTOR);
        double freeWidth = (canv.getWidth() - imgInCanvasBottomRight.x) * 0.5;
        double freeHeight = (canv.getHeight() - imgInCanvasBottomRight.y) * 0.5;
        mCanvasImagePaddingHorizontal = Utils.toXInImageCoords(freeWidth, mMinScaleFactor, 0);
        mCanvasImagePaddingVertical = Utils.toYInImageCoords(freeHeight, mMinScaleFactor, 0);
        // Log.d(TestTags.CORNERS, "initMinZoomBorders: width: " +
        // mCanvasImagePaddingHorizontal + ", height: "
        // + mCanvasImagePaddingVertical);
    }

    private Rect computeWholeImageAreaInCanvasCoords(double scaleFactor, VectorD shift) {
        Rect imgArea = new Rect(0, 0, mImageManager.getImageWidth(), mImageManager.getImageHeight());
        return Utils.toCanvasCoords(imgArea, scaleFactor, shift);
    }

    private Rect computeVisibleImageAreaInCanvas(Canvas canv) {
        int left = mapNumberToInterval(mWholeImageAreaInCanvasCoords.left, 0, canv.getWidth());
        int right = mapNumberToInterval(mWholeImageAreaInCanvasCoords.right, 0, canv.getWidth());
        int top = mapNumberToInterval(mWholeImageAreaInCanvasCoords.top, 0, canv.getHeight());
        int bottom = mapNumberToInterval(mWholeImageAreaInCanvasCoords.bottom, 0, canv.getHeight());
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

    @Override
    public SingleTapListener getSingleTapListener() {
        return mSingleTapListener;
    }

    @Override
    public void setSingleTapListener(SingleTapListener singleTapListener) {
        mSingleTapListener = singleTapListener;
    }

    @Override
    public Rect getVisibleImageAreaInCanvas() {
        return mVisibleImageAreaInCanvas;
    }

    @Override
    public double getInitialScaleFactor() {
        return mInitialScaleFactor;
    }


    @Override
    public int getImageWidth() {
        if (mImageManager != null && mImageManager.isInitialized()) {
            return mImageManager.getImageWidth();
        } else {
            return 0;
        }
    }

    @Override
    public int getImageHeight() {
        if (mImageManager != null && mImageManager.isInitialized()) {
            return mImageManager.getImageHeight();
        } else {
            return 0;
        }
    }

    //TODO: posibly simplify names, @see cz.mzk.tiledimageview.demonstration.Utils.toSimplerString(ViewMode)
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
     * Exactly one of these methods is called eventually unless async task to initialize metadata is canceled prematurely.
     *
     * @author martin
     */
    public interface MetadataInitializationListener {

        /**
         * Metadata downloaded and processed properly.
         */
        public void onMetadataInitialized();

        /**
         * Response to HTTP request for ZoomifyImageMetadata.xml returned code that cannot be handled here. That means almost
         * everything except for some 2xx codes and some 3xx codes for which redirection is applied.
         *
         * @param imageMetadataUrl
         * @param responseCode
         */
        public void onMetadataUnhandableResponseCode(String imageMetadataUrl, int responseCode);

        /**
         * Too many redirections to ZoomifyImageMetadata.xml, probably loop.
         *
         * @param imageMetadataUrl
         * @param redirections
         */
        public void onMetadataRedirectionLoop(String imageMetadataUrl, int redirections);

        /**
         * Other errors in transfering ZoomifyImageMetadata.xml - timeouts etc.
         *
         * @param imageMetadataUrl
         * @param errorMessage
         */
        public void onMetadataDataTransferError(String imageMetadataUrl, String errorMessage);

        /**
         * Invalid content in ZoomifyImageMetadata.xml. Particulary erroneous xml.
         *
         * @param imageMetadataUrl
         * @param errorMessage
         */
        public void onMetadataInvalidData(String imageMetadataUrl, String errorMessage);

        /**
         * If worker thread to initialize metadata cannot be scheduled. This could happen if more than 3 instances of TiledImageView are visible at same time.
         *
         * @param imageMetadataUrl
         */
        public void onCannotExecuteMetadataInitialization(String imageMetadataUrl);
    }

    public interface TileDownloadErrorListener {

        /**
         * HTTP response code that could not be handled here. That means almost everything except for
         * some 2xx codes and some 3xx codes for which redirection is applied.
         *
         * @param tileImageUrl Url of tile image (jpeg, tif, png, bmp, ...).
         * @param responseCode Http response code recieved.
         */

        public void onTileUnhandableResponse(String tileImageUrl, int responseCode);

        /**
         * Too many redirections for tile, probably loop redirection loop.
         *
         * @param tileImageUrl Url of tile image (jpeg, tif, png, bmp, ...)
         * @param redirections Total redirections.
         */
        public void onTileRedirectionLoop(String tileImageUrl, int redirections);

        /**
         * Other errors in transfering tile - timeouts etc.
         *
         * @param tileImageUrl Url of tile image (jpeg, tif, png, bmp, ...)
         * @param errorMessage Error message.
         */
        public void onTileDataTransferError(String tileImageUrl, String errorMessage);

        /**
         * Invalid tile content.
         *
         * @param tileImageUrl Url of tile image (jpeg, tif, png, bmp, ...)
         * @param errorMessage Error message.
         */
        public void onTileInvalidDataError(String tileImageUrl, String errorMessage);
    }

    public static interface TileDownloadSuccessListener {
        public void onTileDelivered();
    }

    public interface MetadataInitializationSuccessListener {
        public void onMetadataFetched(ImageMetadata imageManager);
    }

}

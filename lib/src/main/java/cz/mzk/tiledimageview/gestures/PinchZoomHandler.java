package cz.mzk.tiledimageview.gestures;

import cz.mzk.tiledimageview.Logger;
import cz.mzk.tiledimageview.PointD;
import cz.mzk.tiledimageview.TiledImageViewApi;
import cz.mzk.tiledimageview.Utils;
import cz.mzk.tiledimageview.VectorD;
import cz.mzk.tiledimageview.dev.DevTools;

/**
 * @author Martin Řehánek
 */
public class PinchZoomHandler extends GestureHandler {

    //private static final Logger LOGGER = new Logger("GST: pinch zoom");
    private static final Logger LOGGER = new Logger(PinchZoomHandler.class);

    private State mState = State.IDLE;
    private double mInitialSpan;
    private PointD mInitialFocusInImageCoords;
    // shift
    private VectorD mAccumalatedShift = VectorD.ZERO_VECTOR;
    private VectorD mActiveShift = VectorD.ZERO_VECTOR;
    // scale
    private double mAccumulatedScaleFactor = 1.0;
    private double mActiveScaleFactor = 1.0;

    public PinchZoomHandler(TiledImageViewApi imageView, DevTools devTools) {
        super(imageView, devTools);
    }

    public State getmState() {
        return mState;
    }

    public double getCurrentScaleFactor() {
        return mAccumulatedScaleFactor * mActiveScaleFactor;
    }

    public VectorD getCurrentShift() {
        return VectorD.sum(mAccumalatedShift, mActiveShift);
    }

    public void startZooming(double span, PointD focus) {
        mInitialSpan = span;
        mInitialFocusInImageCoords = Utils.toImageCoords(focus, mImageViewApi.getTotalScaleFactor(),
                mImageViewApi.getTotalShift());
        devUpdateZoomCenters(focus);
        mState = State.READY_TO_PINCH;
        LOGGER.i(mState.name());
    }

    public void zoom(PointD currentFocusInCanvas, double currentSpan) {
        mState = State.PINCHING;
        LOGGER.i(mState.name());
        // scale
        mActiveScaleFactor = 1.0;
        double currentScaleFactor = currentSpan / mInitialSpan;
        LOGGER.d("scale factor: " + currentScaleFactor);
        double totalScaleFactorWithoutActive = mImageViewApi.getTotalScaleFactor();
        double maxTotalScaleFactor = mImageViewApi.getMaxScaleFactor();
        double maxActiveScaleFactor = maxTotalScaleFactor / totalScaleFactorWithoutActive;
        double minTotalScaleFactor = mImageViewApi.getMinScaleFactor();
        double minActiveScaleFactor = minTotalScaleFactor / totalScaleFactorWithoutActive;
        if (currentScaleFactor >= maxActiveScaleFactor) {
            LOGGER.d("max scale reached");
            mActiveScaleFactor = maxActiveScaleFactor;
        } else if (currentScaleFactor <= minActiveScaleFactor) {
            mActiveScaleFactor = minActiveScaleFactor;
        } else {
            mActiveScaleFactor = currentScaleFactor;
        }
        if (mActiveScaleFactor > 1) {
            LOGGER.d("zooming in");
        } else {// zooming out
            LOGGER.d("zooming out");
        }
        // shift
        mActiveShift = VectorD.ZERO_VECTOR;
        PointD initialFocusToBeShiftedInCanvasCoords = Utils.toCanvasCoords(mInitialFocusInImageCoords,
                mImageViewApi.getTotalScaleFactor(), mImageViewApi.getTotalShift());
        VectorD newShift = currentFocusInCanvas.minus(initialFocusToBeShiftedInCanvasCoords);
        mActiveShift = limitNewShift(newShift);
        devUpdateZoomCenters(currentFocusInCanvas);
        mImageViewApi.invalidate();
    }

    private void devUpdateZoomCenters(PointD currentFocusInCanvas) {
        if (mDevTools != null) {
            mDevTools.setPinchZoomCenters(currentFocusInCanvas, mInitialFocusInImageCoords);
        }
    }

    public void finishZooming() {
        mAccumulatedScaleFactor *= mActiveScaleFactor;
        mActiveScaleFactor = 1.0;
        mAccumalatedShift = VectorD.sum(mAccumalatedShift, mActiveShift);
        mActiveShift = VectorD.ZERO_VECTOR;
        mInitialSpan = 0.0;
        mInitialFocusInImageCoords = null;
        mState = State.IDLE;
        LOGGER.i(mState.name());
    }

    public void reset() {
        mAccumalatedShift = VectorD.ZERO_VECTOR;
        mActiveShift = VectorD.ZERO_VECTOR;
        mAccumulatedScaleFactor = 1.0;
        mActiveScaleFactor = 1.0;
    }

    public enum State {
        IDLE, READY_TO_PINCH, PINCHING
    }

}
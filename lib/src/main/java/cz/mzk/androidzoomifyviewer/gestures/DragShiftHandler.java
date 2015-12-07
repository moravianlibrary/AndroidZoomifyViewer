package cz.mzk.androidzoomifyviewer.gestures;

import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.viewer.DevTools;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageViewApi;
import cz.mzk.androidzoomifyviewer.viewer.VectorD;

/**
 * @author Martin Řehánek
 */
public class DragShiftHandler extends GestureHandler {

    // private static final Logger LOGGER = new Logger("GST: drag shift");
    private static final Logger LOGGER = new Logger(DragShiftHandler.class);

    public static final double MIN_DRAG_DISTANCE_TO_RECOGNIZE_SHIFT_DP = 1.0;
    public static final long MAX_DRAG_TIME_TO_BE_CONSIDERED_SINGLE_TAP_NS = 200000000;
    public static final double MAX_DRAG_DISTANCE_TO_BE_CONSIDERED_SINGLE_TAP_DP = 10.0;
    private VectorD mAccumulatedShift = VectorD.ZERO_VECTOR;
    private State mState = State.IDLE;

    public DragShiftHandler(TiledImageViewApi imageViewApi, DevTools devTools) {
        super(imageViewApi, devTools);
    }

    public void drag(float shiftX, float shiftY) {
        mState = State.SHIFTING;
        LOGGER.i(mState.name());
        VectorD shift = limitNewShift(new VectorD(shiftX, shiftY));
        mAccumulatedShift = mAccumulatedShift.plus(shift);
        mState = State.IDLE;
        LOGGER.i(mState.name());
        mImageViewApi.invalidate();
    }

    public State getmState() {
        return mState;
    }

    public VectorD getShift() {
        return mAccumulatedShift;
    }

    public void reset() {
        LOGGER.d("resetting");
        mAccumulatedShift = VectorD.ZERO_VECTOR;
    }

    public enum State {
        IDLE, SHIFTING;
    }

}

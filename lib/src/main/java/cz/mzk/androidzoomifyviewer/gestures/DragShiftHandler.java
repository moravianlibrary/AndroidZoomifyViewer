package cz.mzk.androidzoomifyviewer.gestures;

import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;
import cz.mzk.androidzoomifyviewer.viewer.VectorD;

/**
 * @author Martin Řehánek
 */
public class DragShiftHandler extends GestureHandler {

    public static final double MIN_DRAG_DISTANCE_TO_RECOGNIZE_SHIFT_DP = 1.0;
    public static final long MAX_DRAG_TIME_TO_BE_CONSIDERED_SINGLE_TAP_NS = 200000000;
    public static final double MAX_DRAG_DISTANCE_TO_BE_CONSIDERED_SINGLE_TAP_DP = 10.0;
    private static final Logger logger = new Logger(DragShiftHandler.class);
    private VectorD accumulatedShift = VectorD.ZERO_VECTOR;
    // private static final Logger logger = new Logger("GST: drag shift");
    private State state = State.IDLE;

    public DragShiftHandler(TiledImageView imageView) {
        super(imageView);
    }

    public void drag(float shiftX, float shiftY) {
        state = State.SHIFTING;
        logger.i(state.name());
        VectorD shift = limitNewShift(new VectorD(shiftX, shiftY));
        accumulatedShift = accumulatedShift.plus(shift);
        state = State.IDLE;
        logger.i(state.name());
        imageView.invalidate();
    }

    public State getState() {
        return state;
    }

    public VectorD getShift() {
        return accumulatedShift;
    }

    public void reset() {
        logger.d("resetting");
        accumulatedShift = VectorD.ZERO_VECTOR;
    }

    public enum State {
        IDLE, SHIFTING;
    }

}

package cz.mzk.androidzoomifyviewer.viewer;

import android.util.Log;

/**
 * @author Martin Řehánek
 * 
 */
public class SwipeShiftManager {

	private static final String TAG = SwipeShiftManager.class.getSimpleName();
	public static final double MIN_DRAG_DISTANCE_TO_RECOGNIZE_SHIFT_DP = 1.0;
	public static final long MAX_DRAG_TIME_TO_BE_CONSIDERED_SINGLE_TAP_NS = 200000000;
	public static final double MAX_DRAG_DISTANCE_TO_BE_CONSIDERED_SINGLE_TAP_DP = 10.0;

	private VectorD accumulatedSwipeShift = VectorD.ZERO_VECTOR;
	private State state = State.IDLE;

	private float mLastX;
	private float mLastY;

	private float mStartX;
	private float mStartY;
	private long mStartTime;

	public VectorD getSwipeShift() {
		return accumulatedSwipeShift;
	}

	public void notifyReadyToDrag(float x, float y) {
		mStartX = x;
		mStartY = y;
		mStartTime = System.nanoTime();
		mLastX = x;
		mLastY = y;
		state = State.READY_TO_DRAG;
		// Log.d(TAG, "state: " + state.name());
		Log.d(TestTags.STATE, "shift: " + state.name());
	}

	public void notifyCanceled() {
		state = State.IDLE;
		// Log.d(TAG, "state: " + state.name());
		Log.d(TestTags.STATE, "shift: " + state.name());
	}

	/**
	 * 
	 * @param keepMomentum
	 * @param g
	 * @param f
	 * @return If total dragging was so small and fast that it should be recognized as single tap
	 */
	public boolean notifyDraggingFinished(boolean keepMomentum, float x, float y) {
		boolean isConsideredSingleTap = dragConsideredSingleTap(System.nanoTime(), x, y);
		// TODO: spustit animaci posunu ze setrvacnosti
		state = State.IDLE;
		// Log.d(TAG, "state: " + state.name());
		Log.d(TestTags.STATE, "shift: " + state.name());
		return isConsideredSingleTap;
	}

	private boolean dragConsideredSingleTap(long currentTime, float x, float y) {
		long time = currentTime - mStartTime;
		float diffX = x - mStartX;
		float diffY = y - mStartY;
		double distance = Utils.pxToDp(Math.sqrt(diffX * diffX + diffY * diffY));
		Log.d(TAG, "distance: " + distance + " dp");
		// double timeSec = time / 1000000000.0;
		// Log.d(TAG, String.format("time: %.3f s", timeSec));
		return time <= MAX_DRAG_TIME_TO_BE_CONSIDERED_SINGLE_TAP_NS
				&& distance <= MAX_DRAG_DISTANCE_TO_BE_CONSIDERED_SINGLE_TAP_DP;
	}

	/**
	 * 
	 * @param newX
	 * @param newY
	 * @param maxShiftUp
	 * @param maxShiftDown
	 * @param maxShiftLeft
	 * @param maxShiftRight
	 * @return true if swipe distance was higher than treshold (gesture was recognized, shift performed)
	 */
	public boolean notifyDragging(float newX, float newY, int maxShiftUp, int maxShiftDown, int maxShiftLeft,
			int maxShiftRight) {
		float diffX = newX - mLastX;
		float diffY = newY - mLastY;
		// drag but not over the image borders

		// horizontal
		float currentDragX = 0;
		if (diffX != 0) {
			if (diffX > 0) {// move left
				currentDragX = Math.min(diffX, maxShiftLeft);
			} else {// move right
				currentDragX = -Math.min(-diffX, maxShiftRight);
			}
		}

		// vertical
		float currentDragY = 0;
		if (diffY != 0) {
			if (diffY > 0) { // move up
				currentDragY = Math.min(diffY, maxShiftUp);
			} else { // move down
				currentDragY = -Math.min(-diffY, maxShiftDown);
			}
		}

		VectorD currentDrag = new VectorD(currentDragX, currentDragY);
		double currentDragDistancePx = currentDrag.getSize();
		double currentDragDistanceDp = Utils.pxToDp(currentDragDistancePx);
		if (currentDragDistanceDp > MIN_DRAG_DISTANCE_TO_RECOGNIZE_SHIFT_DP) {
			accumulatedSwipeShift = VectorD.sum(accumulatedSwipeShift, currentDrag);
			// double accumulatedSwipeShiftDistancePx = accumulatedSwipeShift.getSize();
			// double accumulatedSwipeShiftDistanceDp = Utils.pxToDp(accumulatedSwipeShiftDistancePx);
			// Log.d(TestTags.STATE, String.format("shift: %s, accumated distance: %.2f px / %.2f dp", state.name(),
			// accumulatedSwipeShiftDistancePx, accumulatedSwipeShiftDistanceDp));
			mLastX = newX;
			mLastY = newY;
			state = State.DRAGGING;
			Log.d(TestTags.STATE, String.format("shift: %s, distance: %.2f px / %.2f dp", state.name(),
					currentDragDistancePx, currentDragDistanceDp));
			return true;
		} else {
			Log.d(TestTags.STATE, String.format("shift: %s, distance: %.2f px / %.2f dp (ingored)", state.name(),
					currentDragDistancePx, currentDragDistanceDp));
			return false;
		}
	}

	public State getState() {
		return state;
	}

	public enum State {
		IDLE, READY_TO_DRAG, DRAGGING
		// TODO: animating swipe (SWIPING NOW)
	}

}

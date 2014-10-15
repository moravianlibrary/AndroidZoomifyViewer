package cz.mzk.androidzoomifyviewer.viewer;

import android.util.Log;

/**
 * @author Martin Řehánek
 * 
 */
public class SwipeShiftManager {

	// private static final String TAG =
	// SwipeShiftManager.class.getSimpleName();
	private static final String TAG_STATES = "state";

	private VectorD accumulatedSwipeShift = VectorD.ZERO_VECTOR;
	private State state = State.IDLE;

	private float mLastX;
	private float mLastY;

	public VectorD getSwipeShift() {
		return accumulatedSwipeShift;
	}

	public void notifyReadyToDrag(float x, float y) {
		mLastX = x;
		mLastY = y;
		state = State.READY_TO_DRAG;
		// Log.d(TAG, "state: " + state.name());
		Log.d(TAG_STATES, "shift: " + state.name());
	}

	public void notifyCanceled() {
		state = State.IDLE;
		// Log.d(TAG, "state: " + state.name());
		Log.d(TAG_STATES, "shift: " + state.name());
	}

	public void notifyDraggingFinished(boolean keepMomentum) {
		// TODO: spustit animaci posunu ze setrvacnosti
		state = State.IDLE;
		// Log.d(TAG, "state: " + state.name());
		Log.d(TAG_STATES, "shift: " + state.name());
	}

	/**
	 * 
	 * @param newX
	 * @param newY
	 * @param maxShiftUp
	 * @param maxShiftDown
	 * @param maxShiftLeft
	 * @param maxShiftRight
	 * @return true if swipe distance was higher than treshold (gesture was
	 *         recognized)
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
		double currentDragDistance = currentDrag.getSize();
		if (currentDragDistance > 0.01) {
			accumulatedSwipeShift = VectorD.sum(accumulatedSwipeShift, currentDrag);
			mLastX = newX;
			mLastY = newY;
			state = State.DRAGGING;
			Log.d(TAG_STATES, "shift: " + state.name() + String.format(", distance: %.2f", currentDragDistance));
			return true;
		} else {
			Log.d(TAG_STATES, "shift: " + state.name() + String.format(", distance: %.2f", currentDragDistance)
					+ " (ignored)");
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

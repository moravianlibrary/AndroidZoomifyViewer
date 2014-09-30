package cz.mzk.androidzoomifyviewer.viewer;

import android.util.Log;
import android.view.MotionEvent;

/**
 * @author Martin Řehánek
 * 
 */
public class PinchZoomManager {

	private static final String TAG = PinchZoomManager.class.getSimpleName();

	private static final double MIN_STARTING_FINGER_DISTANCE = 10.0;

	private final TiledImageView imageView;
	private State state = State.IDLE;

	// general zooming state variables
	private double startingFingerDistance;
	private PointD startingZoomCenterInImageCoords;
	private PointD currentZoomCenterInCanvasCoords;

	// zoom shift
	private VectorD accumalatedZoomShift = VectorD.ZERO_VECTOR;
	private VectorD activeZoomShift = VectorD.ZERO_VECTOR;

	// zoom scale
	private double accumulatedZoomScale = 1.0;
	private double activeZoomScale = 1.0;

	public PinchZoomManager(TiledImageView imageView, double zoomLevel) {
		this.imageView = imageView;
		this.accumulatedZoomScale = zoomLevel;
	}

	public State getState() {
		return state;
	}

	public double getCurrentZoomScale() {
		return accumulatedZoomScale * activeZoomScale;
	}

	public PointD getCurrentZoomCenterInCanvas() {
		return currentZoomCenterInCanvasCoords;
	}

	public VectorD getCurrentZoomShift() {
		return VectorD.sum(accumalatedZoomShift, activeZoomShift);
	}

	public void startPinching(MotionEvent event, double resizeFactor, VectorD shift) {
		startingFingerDistance = computeTwoFingersDistance(event);
		// if (startingFingerDistance > MIN_STARTING_FINGER_DISTANCE) {
		// oldDist = zoom(oldDist, event);
		currentZoomCenterInCanvasCoords = computeTwoFingersCenter(event);
		startingZoomCenterInImageCoords = Utils.toImageCoords(currentZoomCenterInCanvasCoords, resizeFactor, shift);
		Log.d(TestTags.MOTION, "POINTER_DOWN, center: x=" + currentZoomCenterInCanvasCoords.x + ", y="
				+ currentZoomCenterInCanvasCoords.y);
		state = State.READY_TO_PINCH;
		Log.d(TestTags.STATE, "pinch zoom: " + state.name());
	}

	/**
	 * 
	 * @param event
	 * @param maxShiftRight
	 * @param maxShiftLeft
	 * @param maxShiftDown
	 * @param maxShiftUp
	 * @return whether there's need to refresh view
	 */
	public boolean continuePinching(MotionEvent event, PointD visibleImageCenter, int maxShiftUp, int maxShiftDown,
			int maxShiftLeft, int maxShiftRight) {
		state = State.PINCHING;
		Log.d(TestTags.STATE, "pinch zoom: " + state.name());
		currentZoomCenterInCanvasCoords = computeTwoFingersCenter(event);
		double currentFingerDistance = computeTwoFingersDistance(event);
		activeZoomScale = currentFingerDistance / startingFingerDistance;
		double currentZoomScale = getCurrentZoomScale();
		double minZoomScale = (imageView.getMinScaleFactor() * currentZoomScale) / imageView.getCurrentScaleFactor();
		double maxZoomScale = (imageView.getMaxScaleFactor() * currentZoomScale) / imageView.getCurrentScaleFactor();

		if (currentZoomScale <= maxZoomScale && currentZoomScale >= minZoomScale) {
			Log.d(TAG, "current: " + currentZoomScale);
			updateActiveZoomShift();
			return true;
		} else if (currentZoomScale < minZoomScale) {
			activeZoomScale = 1.0f;
			accumulatedZoomScale = minZoomScale;
			// Log.d(TAG, "current < min; current: " + currentZoomScale +
			// ", min: " + minZoomScale);
			updateActiveZoomShift();
			return false;
		} else if (currentZoomScale > maxZoomScale) {
			activeZoomScale = 1.0f;
			accumulatedZoomScale = maxZoomScale;
			// Log.d(TAG, "current > max; current: " + currentZoomScale +
			// ", max: " + maxZoomScale);
			updateActiveZoomShift();
			return false;
		} else {
			throw new IllegalStateException();
		}
	}

	private void updateActiveZoomShift() {
		if (activeZoomScale > 1) {// zooming in
			Log.d(TAG, "zooming in");
		} else {// zooming out
			// TODO: resit, jestli nebude viditelna oblast mimo stranku,
			Log.d(TAG, "zooming out");
		}
		VectorD newShift = computeNewShift();
		activeZoomShift = newShift.plus(activeZoomShift);
		// Log.d(TestTags.CENTERS, "shift: " + newShift);
	}

	public void finish() {
		Log.d(TAG, "finished");
		storeDataOfCurrentZoom();
	}

	public void cancel() {
		Log.d(TAG, "canceled");
		storeDataOfCurrentZoom();
	}

	private void storeDataOfCurrentZoom() {
		accumulatedZoomScale *= activeZoomScale;
		activeZoomScale = 1.0;

		accumalatedZoomShift = VectorD.sum(accumalatedZoomShift, activeZoomShift);
		activeZoomShift = VectorD.ZERO_VECTOR;

		startingFingerDistance = 0.0;
		startingZoomCenterInImageCoords = null;
		state = State.IDLE;
		Log.d(TestTags.STATE, "pinch zoom: " + state.name());
	}

	private double computeTwoFingersDistance(MotionEvent event) {
		// float to double
		double diffX = event.getX(0) - event.getX(1);
		double diffY = event.getY(0) - event.getY(1);
		return Math.sqrt(diffX * diffX + diffY * diffY);
	}

	private PointD computeTwoFingersCenter(MotionEvent event) {
		// float to double
		double x = event.getX(0) + event.getX(1);
		double y = event.getY(0) + event.getY(1);
		return new PointD(0.5 * x, 0.5 * y);
	}

	private VectorD computeNewShift() {
		PointD startingZoomCenterInCanvasCoords = Utils.toCanvasCoords(startingZoomCenterInImageCoords,
				imageView.getCurrentScaleFactor(), imageView.getTotalShift());
		VectorD diff = new VectorD(currentZoomCenterInCanvasCoords.x - startingZoomCenterInCanvasCoords.x,
				currentZoomCenterInCanvasCoords.y - startingZoomCenterInCanvasCoords.y);
		// Log.d(TestTags.ZOOM,
		// "zoomMngr: init: " + startingZoomCenterInCanvasCoords.toString() +
		// " now: "
		// + currentZoomCenterInCanvasCoords.toString() + " diff: " +
		// diff.toString()
		// + String.format(" r:%.4f", imageView.getCurrentScaleFactor()) +
		// " s: "
		// + imageView.getTotalShift().toString());
		// Log.d(TestTags.ZOOM, "diff: " + diff.toString() + ", distance: " +
		// String.format("%.3f", diff.getSize()));
		return diff;
	}

	public PointD getStartingZoomCenterInImageCoords() {
		return startingZoomCenterInImageCoords;
	}

	public enum State {
		IDLE, READY_TO_PINCH, PINCHING
	}

}

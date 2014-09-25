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

	// general zooming state varibales
	private double startingFingerDistance;
	private PointD startingZoomCenterInImageCoords;
	private PointD currentZoomCenterInCanvasCoords;// TODO: just temporary

	// zoom shift
	private VectorD accumalatedZoomShift = VectorD.ZERO_VECTOR;
	private VectorD activeZoomShift = VectorD.ZERO_VECTOR;

	// zoom scale
	private double accumulatedZoomScale = 1.0;
	private double activeZoomScale = 1.0;
	private double minZoomScale = 1.0;

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

	public void setMinZoomScale(double maxZoomScale) {
		this.minZoomScale = maxZoomScale;
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
		Log.d(TestTags.STATE, "zoom (pinch): " + state.name());
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
	public boolean continuePinching(MotionEvent event, PointD visibleImageCenter, double resizeFactor,
			VectorD currentTotalShift, int maxShiftUp, int maxShiftDown, int maxShiftLeft, int maxShiftRight) {
		state = State.PINCHING;
		Log.d(TestTags.STATE, "zoom: " + state.name());
		currentZoomCenterInCanvasCoords = computeTwoFingersCenter(event);
		double currentFingerDistance = computeTwoFingersDistance(event);
		activeZoomScale = currentFingerDistance / startingFingerDistance;
		// Log.d(TAG, "scale (pinch): " + activeZoomLevel);

		// TODO: jeste maxZoomLevel - tam, kde uz neni vetsi detail, nema smysl
		// zoomovat (priblizne, treba jeste vynasobit faktorem 2)
		if (getCurrentZoomScale() < minZoomScale) {// zavisi na activeZoomLevel
			activeZoomScale = 1.0f;
			accumulatedZoomScale = minZoomScale;
			// TODO: a jak shift?
			Log.d(TestTags.MOTION, "MOVE finished");
			return false;
		} else {
			if (activeZoomScale > 1) {// zooming in
				Log.d(TAG, "zooming in");
			} else {// zooming out
				// TODO: resit, jestli nebude viditelna oblast mimo stranku,
				// kdyz by nemela a pr
				Log.d(TAG, "zooming out");
			}
			activeZoomShift = VectorD.ZERO_VECTOR;// to je kvuli toho, aby se v
													// computActiveZoomShift
													// nepouzil ten z
													// predchoziho
													// notifyPinchingContinues
													// pres
													// imageView.getTotalShift()
			// activeZoomShift = newShift;
			VectorD newShift = computeActiveZoomShift(currentZoomCenterInCanvasCoords, resizeFactor, currentTotalShift);

			// accumalatedZoomShift = VectorD.sum(accumalatedZoomShift,
			// activeZoomShift);
			// accumalatedZoomShift +=activeZoomShift;
			activeZoomShift = newShift;
			// activeZoomShift = VectorD.sum(activeZoomShift, newShift);
			// Log.d(TestTags.MOTION, "MOVE finished");
			return true;
			// return distance > 50.0;
		}
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
		Log.d(TestTags.STATE, "zoom (pinch): " + state.name());
	}

	private double computeTwoFingersDistance(MotionEvent event) {
		// float to double
		double x = event.getX(0) - event.getX(1);
		double y = event.getY(0) - event.getY(1);
		return Math.sqrt(x * x + y * y);
	}

	private PointD computeTwoFingersCenter(MotionEvent event) {
		// float to double
		double x = event.getX(0) + event.getX(1);
		double y = event.getY(0) + event.getY(1);
		return new PointD(0.5 * x, 0.5 * y);
	}

	private VectorD computeActiveZoomShift(PointD currentZoomCenterInCanvasCoords, double resizeFactor, VectorD shift) {
		Log.d(TestTags.ZOOM,
				String.format("activeZoomLevel: %.4f", activeZoomScale)
						+ String.format(" accumulatedZoomLevel: %.2f", accumulatedZoomScale));
		PointD startingZoomCenterInCanvasCoords = Utils.toCanvasCoords(startingZoomCenterInImageCoords,
				imageView.getTotalResizeFactor(), imageView.getTotalShift());
		VectorD diff = new VectorD(currentZoomCenterInCanvasCoords.x - startingZoomCenterInCanvasCoords.x,
				currentZoomCenterInCanvasCoords.y - startingZoomCenterInCanvasCoords.y);
		Log.d(TestTags.ZOOM,
				"zoomMngr: init: " + startingZoomCenterInCanvasCoords.toString() + " now: "
						+ currentZoomCenterInCanvasCoords.toString() + " diff: " + diff.toString()
						+ String.format(" r:%.4f", imageView.getTotalResizeFactor()) + " s: "
						+ imageView.getTotalShift().toString());
		Log.d(TestTags.ZOOM, "diff: " + diff.toString() + ", distance: " + String.format("%.3f", diff.getSize()));
		return diff;
	}

	public PointD getStartingZoomCenterInImageCoords() {
		return startingZoomCenterInImageCoords;
	}

	public enum State {
		IDLE, READY_TO_PINCH, PINCHING
	}

}

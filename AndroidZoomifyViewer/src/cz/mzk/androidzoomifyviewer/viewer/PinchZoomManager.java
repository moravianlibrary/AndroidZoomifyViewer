package cz.mzk.androidzoomifyviewer.viewer;

import android.util.Log;
import android.view.MotionEvent;

/**
 * @author Martin Řehánek
 * 
 */
public class PinchZoomManager {

	private static final String TAG = PinchZoomManager.class.getSimpleName();
	private static final String TAG_STATES = "state";

	private static final double MIN_STARTING_FINGER_DISTANCE = 10.0;

	// TODO: still needed?
	private TiledImageView imageView;

	private State state = State.IDLE;

	// general zooming state varibales
	private double startingFingerDistance;
	private PointD startingZoomCenterInImageCoords;
	private PointD currentZoomCenterInCanvasCoords;// TODO: just temporary

	// zoom shift
	private VectorD accumalatedZoomShift = VectorD.ZERO_VECTOR;
	private VectorD activeZoomShift = VectorD.ZERO_VECTOR;

	// zoom level
	private double accumulatedZoomLevel = 1.0;
	private double activeZoomLevel = 1.0;
	private double minZoomLevel = 1.0;

	// test stuff
	int testCounter = 0;

	public PinchZoomManager(TiledImageView imageView, double zoomLevel) {
		this.imageView = imageView;
		this.accumulatedZoomLevel = zoomLevel;
	}

	public State getState() {
		return state;
	}

	public double getCurrentZoomLevel() {
		return accumulatedZoomLevel * activeZoomLevel;
	}

	public PointD getCurrentZoomCenter() {
		return currentZoomCenterInCanvasCoords;
	}

	public VectorD getCurrentZoomShift() {
		return VectorD.sum(accumalatedZoomShift, activeZoomShift);
	}

	public void setMinZoomLevel(double maxZoomLevel) {
		this.minZoomLevel = maxZoomLevel;
	}

	public void notifyReadyToPinch(MotionEvent event, double resizeFactor, VectorD shift) {
		startingFingerDistance = computeTwoFingersDistance(event);
		// Log.d("Motion", "oldDist=" + oldDist);
		// if (startingFingerDistance > MIN_STARTING_FINGER_DISTANCE) {
		// oldDist = zoom(oldDist, event);
		currentZoomCenterInCanvasCoords = computeTwoFingersCenter(event);
		// initialZoomCenter = zoomCenter;
		// PointD currentCenter = computeTwoFingersTouchEventCenter(event);
		startingZoomCenterInImageCoords = Utils.toImageCoords(currentZoomCenterInCanvasCoords.x,
				currentZoomCenterInCanvasCoords.y, resizeFactor, shift);
		Log.d("Motion", "POINTER_DOWN, center: x=" + currentZoomCenterInCanvasCoords.x + ", y="
				+ currentZoomCenterInCanvasCoords.y);
		// activeZoomShiftX
		// }
		state = State.READY_TO_PINCH;
		// Log.d(TAG, "state: " + state.name());
		Log.d(TAG_STATES, "zoom: " + state.name());
	}

	public void notifyCanceled() {
		// TODO: clean variables
		startingZoomCenterInImageCoords = null;
		state = State.IDLE;
		// Log.d(TAG, "state: " + state.name());
		Log.d(TAG_STATES, "zoom: " + state.name());
	}

	/**
	 * 
	 * @param event
	 * @return whether there's need to refresh view
	 */
	public boolean notifyPinchingContinues(MotionEvent event, PointD visibleImageCenter, double resizeFactor,
			VectorD shift) {
		currentZoomCenterInCanvasCoords = computeTwoFingersCenter(event);
		double currentFingerDistance = computeTwoFingersDistance(event);
		state = State.PINCHING;
		// Log.d(TAG, "state: " + state.name());
		Log.d(TAG_STATES, "zoom: " + state.name());
		activeZoomLevel = currentFingerDistance / startingFingerDistance;
		Log.d(TAG, "scale: " + activeZoomLevel);

		// TODO: jeste maxZoomLevel - tam, kde uz neni vetsi detail, nema smysl
		// zoomovat (priblizne, treba jeste vynasobit faktorem 2)
		if (getCurrentZoomLevel() < minZoomLevel) {// zavisi na activeZoomLevel
			activeZoomLevel = 1.0f;
			accumulatedZoomLevel = minZoomLevel;// ???
			// TODO: a jak shift?
			Log.d("Motion", "MOVE finished");
			return false;
		} else {
			if (activeZoomLevel > 1) {// zooming in
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
			VectorD newShift = computeActiveZoomShift(currentZoomCenterInCanvasCoords, resizeFactor, shift);
			// accumalatedZoomShift = VectorD.sum(accumalatedZoomShift,
			// activeZoomShift);
			// accumalatedZoomShift +=activeZoomShift;
			activeZoomShift = newShift;
			// activeZoomShift = newShift;
			// activeZoomShift = VectorD.sum(activeZoomShift, newShift);
			double distance = newShift.getSize();
			Log.d("Motion", "MOVE finished");
			// activeZoomLevel = scale;
			return true;
			// return distance > 50.0;
		}
	}

	public void notifyPinchingFinished() {
		// Log.d(TAG, "oldDist=" + oldDist);
		// appendNewShift();
		accumulatedZoomLevel *= activeZoomLevel;
		activeZoomLevel = 1.0;

		accumalatedZoomShift = VectorD.sum(accumalatedZoomShift, activeZoomShift);
		activeZoomShift = VectorD.ZERO_VECTOR;

		startingFingerDistance = 0.0;
		startingZoomCenterInImageCoords = null;
		// zoomingNow = false;
		// currentZoomCenterInCanvasCoords = null;
		// finishedZoomingOneFingerStillDown = true;

		state = State.IDLE;
		// Log.d(TAG, "state: " + state.name());
		Log.d(TAG_STATES, "zoom: " + state.name());
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

		VectorD shift2 = new VectorD(shift.x * activeZoomLevel, shift.y * activeZoomLevel);
		Log.d("z00m",
				String.format("activeZoomLevel: %.4f", activeZoomLevel)
						+ String.format(" accumulatedZoomLevel: %.2f", accumulatedZoomLevel));
		PointD initialZoomCenter = Utils.toCanvasCoords(startingZoomCenterInImageCoords,
				imageView.getTotalResizeFactor(), imageView.getTotalShift());
		// PointD initialZoomCenter =
		// Utils.toCanvasCoords(startingZoomCenterInImageCoords,
		// resizeFactor, shift);

		VectorD diff = new VectorD(currentZoomCenterInCanvasCoords.x - initialZoomCenter.x,
				currentZoomCenterInCanvasCoords.y - initialZoomCenter.y);
		// VectorD diff = new VectorD(initialZoomCenter.x -
		// currentZoomCenterInCanvasCoords.x, initialZoomCenter.y
		// - currentZoomCenterInCanvasCoords.y);

		testCounter++;
		Log.d("z00m",
				"zoomMngr: init: " + initialZoomCenter.toString() + " now: "
						+ currentZoomCenterInCanvasCoords.toString() + " diff: " + diff.toString()
						+ String.format(" r:%.4f", imageView.getTotalResizeFactor()) + " s: "
						+ imageView.getTotalShift().toString() + " c: " + testCounter);

		PointD testInitialZoomCenter = Utils.toCanvasCoords(startingZoomCenterInImageCoords,
				imageView.getTotalResizeFactor(), imageView.getTotalShift());
		VectorD testDiff = new VectorD(currentZoomCenterInCanvasCoords.x - initialZoomCenter.x,
				currentZoomCenterInCanvasCoords.y - initialZoomCenter.y);
		// Log.d("z00m", "zoomMngr: test: " + testInitialZoomCenter.toString() +
		// ", now: "
		// + currentZoomCenterInCanvasCoords.toString() + ", diff: " +
		// testDiff.toString() + ", c: " + testCounter);

		double distance = Math.sqrt(Math.pow(diff.x, 2) + Math.pow(diff.y, 2));

		Log.d("zoom", "vector: " + diff.toString() + ", distance: " + String.format("%.3f", distance));
		return diff;

		// Log.d("zoom", "canvas: zoomCenter init: " +
		// initialZoomCenter.toString());
		// Log.d("zoom", "canvas: zoomCenter now : " +
		// currentZoomCenterInCanvasCoords.toString());

		// Log.d(TAG, "initialZoomCenter(image): " +
		// Utils.toString(initialZoomCenterInImageCoords));
		// float[] vector = new float[] { initialZoomCenter.x -
		// zoomCenterInCanvas.x,
		// initialZoomCenter.y - zoomCenterInCanvas.y };

		// imageview.getDevTools().drawZoomCenters(canv,
		// zoomCenterNowInCanvas, initialZoomCenterInCanvas);
		// float[] vector = new float[] { zoomCenterNow.x * scale -
		// initialZoomCenter.x,
		// zoomCenterNow.y * scale - initialZoomCenter.y };
		// float[] vector = new float[] { (zoomCenterNow.x -
		// initialZoomCenter.x)/2, (zoomCenterNow.y - initialZoomCenter.y)/2
		// };
		// int[] vector = new int[] { (int) (zoomCenterNow.x -
		// initialZoomCenter.x),

		// Vector result = new Vector(zoomCenterNow.x - initialZoomCenter.x,
		// zoomCenterNow.y - initialZoomCenter.y);

		// float[] vector = new float[] { zoomCenter.x -
		// visibleImageCenter.x, zoomCenter.y - visibleImageCenter.y };

		// double distance = Math.sqrt(Math.pow(vector[0], 2) +
		// Math.pow(vector[1], 2));

		// double resizeFactor = imageView.getActualResizeFactor();
		// float factor = (float) (0.01f * distance * resizeFactor);// TODO
		// float factor = (float) (0.01f * resizeFactor * scale);// TODO
		// float[] result = new float[] { visibleImageCenter.x + factor *
		// vector[0],
		// visibleImageCenter.y + factor * vector[1] };
		// float[] result = new float[] { factor * vector[0], factor *
		// vector[1] };
		// float[] result = new float[] { vector[0] * activeZoomLevel,
		// vector[1] * activeZoomLevel };
		// Log.d(TAG, "zoomShift: " + Utils.toString(result) +
		// ", resizeFactor: " + resizeFactor + ", factor: " + factor);
		// Vector result = new Vector(vector[0], vector[1]);
	}

	public PointD getInitialZoomCenterInImageCoords() {
		return startingZoomCenterInImageCoords;
	}

	// TODO: disable
	public void setAccumulatedZoomLevel(double zoomLevel) {
		this.accumulatedZoomLevel = zoomLevel;
	}

	// TODO: disable
	public double getAccumulatedZoomLevel() {
		return accumulatedZoomLevel;
	}

	public enum State {
		IDLE, READY_TO_PINCH, PINCHING
	}

	// public void notifyZoomingStarted(MotionEvent event, double resizeFactor,
	// VectorD shift) {
	// startingFingerDistance = spacing(event);
	// // Log.d("Motion", "oldDist=" + oldDist);
	// if (startingFingerDistance > MIN_STARTING_FINGER_DISTANCE) {
	// zoomingNow = true;
	// // TODO: zoom
	// // oldDist = zoom(oldDist, event);
	// currentZoomCenterInCanvasCoords =
	// computeTwoFingersTouchEventCenter(event);
	// // initialZoomCenter = zoomCenter;
	// startingZoomCenterInImageCoords =
	// Utils.toImageCoords(currentZoomCenterInCanvasCoords.x,
	// currentZoomCenterInCanvasCoords.y, resizeFactor, shift);
	// Log.d("Motion", "POINTER_DOWN, center: x=" +
	// currentZoomCenterInCanvasCoords.x + ", y="
	// + currentZoomCenterInCanvasCoords.y);
	// // activeZoomShiftX
	// }
	// }

	// public void notifyZoomingFinished() {
	// // Log.d(TAG, "oldDist=" + oldDist);
	// // appendNewShift();
	// accumulatedZoomLevel *= activeZoomLevel;
	// activeZoomLevel = 1.0;
	//
	// accumalatedZoomShift = VectorD.sum(accumalatedZoomShift,
	// activeZoomShift);
	// activeZoomShift = VectorD.ZERO_VECTOR;
	//
	// startingFingerDistance = 0.0;
	// zoomingNow = false;
	// // currentZoomCenterInCanvasCoords = null;
	// // startingZoomCenterInImageCoords = null;
	// // finishedZoomingOneFingerStillDown = true;
	// }
}

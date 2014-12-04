package cz.mzk.androidzoomifyviewer.viewer;

import android.graphics.Rect;
import android.view.MotionEvent;
import cz.mzk.androidzoomifyviewer.Logger;

/**
 * @author Martin Řehánek
 * 
 */
public class PinchZoomManager {

	public static final double MIN_STARTING_FINGER_DISTANCE = 10.0;

	private static final Logger logger = new Logger(PinchZoomManager.class);

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
		TestLoggers.MOTION.d("POINTER_DOWN, center: x=" + currentZoomCenterInCanvasCoords.x + ", y="
				+ currentZoomCenterInCanvasCoords.y);
		state = State.READY_TO_PINCH;
		TestLoggers.STATE.d("pinch zoom: " + state.name());
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
		TestLoggers.STATE.d("pinch zoom: " + state.name());
		// previousZoomCenterInCanvasCoords =
		// Utils.toCanvasCoords(startingZoomCenterInImageCoords,
		// imageView.getCurrentScaleFactor(), imageView.getTotalShift());
		currentZoomCenterInCanvasCoords = computeTwoFingersCenter(event);
		double currentFingerDistance = computeTwoFingersDistance(event);
		activeZoomScale = currentFingerDistance / startingFingerDistance;
		double currentZoomScale = getCurrentZoomScale();
		double minZoomScale = (imageView.getMinScaleFactor() * currentZoomScale) / imageView.getCurrentScaleFactor();
		double maxZoomScale = (imageView.getMaxScaleFactor() * currentZoomScale) / imageView.getCurrentScaleFactor();

		if (currentZoomScale <= maxZoomScale && currentZoomScale >= minZoomScale) {
			// logger.d( "current: " + currentZoomScale);
			updateActiveZoomShift(maxShiftUp, maxShiftDown, maxShiftLeft, maxShiftRight);
			return true;
		} else if (currentZoomScale < minZoomScale) {
			activeZoomScale = 1.0f;
			accumulatedZoomScale = minZoomScale;
			// logger.d( "current < min; current: " + currentZoomScale +
			// ", min: " + minZoomScale);
			updateActiveZoomShift(maxShiftUp, maxShiftDown, maxShiftLeft, maxShiftRight);
			return true;
		} else if (currentZoomScale > maxZoomScale) {
			activeZoomScale = 1.0f;
			accumulatedZoomScale = maxZoomScale;
			// logger.d( "current > max; current: " + currentZoomScale +
			// ", max: " + maxZoomScale);
			updateActiveZoomShift(maxShiftUp, maxShiftDown, maxShiftLeft, maxShiftRight);
			return true;
		} else {
			throw new IllegalStateException();
		}
	}

	private void updateActiveZoomShift(int maxShiftUp, int maxShiftDown, int maxShiftLeft, int maxShiftRight) {

		PointD startingZoomCenterInCanvasCoords = Utils.toCanvasCoords(startingZoomCenterInImageCoords,
				imageView.getCurrentScaleFactor(), imageView.getTotalShift());
		VectorD newShift = new VectorD(currentZoomCenterInCanvasCoords.x - startingZoomCenterInCanvasCoords.x,
				currentZoomCenterInCanvasCoords.y - startingZoomCenterInCanvasCoords.y);
		if (activeZoomScale > 1) {// zooming in
			logger.d("zooming in");
			activeZoomShift = newShift.plus(activeZoomShift);
		} else {// zooming out
			logger.d("zooming out");
			logger.d("new shift: " + newShift);
			// logger.d( "max: up: " + maxShiftUp + ", down: " + maxShiftDown +
			// ", left: " + maxShiftLeft + ", right: "
			// + maxShiftRight);
			// logger.d( "max: up: " + maxShiftUp + ", down: " + maxShiftDown);
			VectorD newShiftLimited = limitNewShiftForZoomOut(newShift);
			activeZoomShift = newShiftLimited.plus(activeZoomShift);
		}
	}

	private VectorD limitNewShiftForZoomOut(VectorD newShift) {
		double limitedLocalX = newShift.x;
		double limitedLocalY = newShift.y;
		double scaleFactor = imageView.getCurrentScaleFactor();

		// Log.d(TestTags.CORNERS, "---------------------------");
		// Log.d(TestTags.CORNERS, "scale: " + scaleFactor);

		RectD paddingRectImg = new RectD(0.0, 0.0, imageView.getCanvasImagePaddingHorizontal(),
				imageView.getCanvasImagePaddingVertical());

		Rect imageAreaInCanvasWithoutAnyShift = imageView.computeImageAreaInCanvas(scaleFactor, VectorD.ZERO_VECTOR);
		RectD paddingRectCanv = convertToPaddingInCanvas(paddingRectImg, imageAreaInCanvasWithoutAnyShift);

		// Log.d(TestTags.CORNERS, "padding rect img: " + paddingRectImg);
		// Log.d(TestTags.CORNERS, "padding rect canv: " + paddingRectCanv);

		VectorD totalShift = imageView.getTotalShift();
		VectorD totalPlusNewShift = totalShift.plus(newShift);
		// Log.d(TestTags.CORNERS, "shift:\n\t total: " + totalShift +
		// "\n\t new: " + newShift + "\n\t total+new: "
		// + totalPlusNewShift);
		Rect imageAreaInCanvasWithNewShift = imageView.computeImageAreaInCanvas(scaleFactor, totalPlusNewShift);
		DevTools devTools = imageView.getDevTools();

		// horizontal
		double extraSpaceHorizontalCanv = paddingRectCanv.height();
		double maxTop = extraSpaceHorizontalCanv;
		double minBottom = imageView.getHeight() - extraSpaceHorizontalCanv;

		if (imageAreaInCanvasWithNewShift.top > maxTop) {
			// Log.d(TestTags.CORNERS, "TOP");
			double limitedGlobalY = maxTop;
			// Log.d(TestTags.CORNERS, "newY: " + limitedLocalY);
			limitedLocalY = limitedGlobalY - totalShift.y;
			// Log.d(TestTags.CORNERS, "limitedGlobalY: " + limitedGlobalY);
			// Log.d(TestTags.CORNERS, "limitedLocalY: " + limitedLocalY);
		} else if (imageAreaInCanvasWithNewShift.bottom < minBottom) {
			// Log.d(TestTags.CORNERS, "BOTTOM");
			double limitedGlobalY = minBottom - imageAreaInCanvasWithoutAnyShift.bottom;
			// Log.d(TestTags.CORNERS, "newY: " + limitedLocalY);
			limitedLocalY = limitedGlobalY - totalShift.y;
			// Log.d(TestTags.CORNERS, "limitedGlobalY: " + limitedGlobalY);
			// Log.d(TestTags.CORNERS, "limitedLocalY: " + limitedLocalY);
		}

		// vertical
		double extraSpaceVerticalCanv = paddingRectCanv.width();
		double maxLeft = extraSpaceVerticalCanv;
		double minRight = imageView.getWidth() - extraSpaceVerticalCanv;
		if (imageAreaInCanvasWithNewShift.left > maxLeft) {
			// Log.d(TestTags.CORNERS, "LEFT");
			double limitedGlobalX = maxLeft;
			// Log.d(TestTags.CORNERS, "newX: " + limitedLocalX);
			limitedLocalX = limitedGlobalX - totalShift.x;
			// Log.d(TestTags.CORNERS, "limitedGlobalX: " + limitedGlobalX);
			// Log.d(TestTags.CORNERS, "limitedLocalX: " + limitedLocalX);
		} else if (imageAreaInCanvasWithNewShift.right < minRight) {
			// Log.d(TestTags.CORNERS, "RIGHT");
			double limitedGlobalX = minRight - imageAreaInCanvasWithoutAnyShift.right;
			// Log.d(TestTags.CORNERS, "newX: " + limitedLocalX);
			limitedLocalX = limitedGlobalX - totalShift.x;
			// Log.d(TestTags.CORNERS, "limitedGlobalY: " + limitedGlobalX);
			// Log.d(TestTags.CORNERS, "limitedLocalY: " + limitedLocalX);
		}

		VectorD limitedNewShift = new VectorD(limitedLocalX, limitedLocalY);
		if (devTools != null) {
			// devTools.clearRectStack();
			// VectorD totalPlusLimitedNewShift =
			// totalShift.plus(limitedNewShift);
			// Rect imageAreaInCanvasWithLimitedShift =
			// imageView.computeImageAreaInCanvas(scaleFactor,
			// totalPlusLimitedNewShift);
			// devTools.addToRectStack(new
			// RectWithPaint(imageAreaInCanvasWithoutAnyShift,
			// devTools.getPaintBlackTrans()));
			// devTools.addToRectStack(new
			// RectWithPaint(imageAreaInCanvasWithLimitedShift,
			// devTools.getPaintRedTrans()));
			// devTools.addToRectStack(new
			// RectWithPaint(imageAreaInCanvasWithNewShift,
			// devTools.getPaintYellowTrans()));
			// Rect paddingRectangleVisualisation =
			// paddingVisualisation(paddingRectCanv);
			// devTools.addToRectStack(new
			// RectWithPaint(paddingRectangleVisualisation,
			// devTools.getPaintGreen()));
		}

		return limitedNewShift;
	}

	private RectD convertToPaddingInCanvas(RectD paddingRectImg, Rect imageAreaInCanvasWithoutAnyShift) {
		PointD rightBottomImg = new PointD(paddingRectImg.right, paddingRectImg.bottom);
		PointD rightBottomCanv = Utils.toCanvasCoords(rightBottomImg, imageView.getMinScaleFactor(),
				VectorD.ZERO_VECTOR);
		double width = rightBottomCanv.x;
		double height = rightBottomCanv.y;

		if (width != 0) {
			if (imageAreaInCanvasWithoutAnyShift.width() >= imageView.getWidth()) {
				width = 0;
			} else {
				double min = (imageView.getWidth() - imageAreaInCanvasWithoutAnyShift.width()) * 0.5;
				width = Math.min(width, min);
			}
		}

		if (height != 0) {
			if (imageAreaInCanvasWithoutAnyShift.height() >= imageView.getHeight()) {
				height = 0;
			} else {
				double min = (imageView.getHeight() - imageAreaInCanvasWithoutAnyShift.height()) * 0.5;
				height = Math.min(height, min);
			}
		}

		return new RectD(0, 0, width, height);
	}

	private Rect paddingVisualisation(RectD paddingRectCanv) {
		int x = (int) paddingRectCanv.width();
		int y = (int) paddingRectCanv.height();
		if (x == 0) {
			x = 10;
		}
		if (y == 0) {
			y = 10;
		}
		return new Rect(0, 0, x, y);
	}

	public void finish() {
		logger.d("finished");
		storeDataOfCurrentZoom();
	}

	public void cancel() {
		logger.d("canceled");
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
		TestLoggers.STATE.d("pinch zoom: " + state.name());
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

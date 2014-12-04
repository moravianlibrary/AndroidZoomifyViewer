package cz.mzk.androidzoomifyviewer.viewer;

import android.os.CountDownTimer;
import cz.mzk.androidzoomifyviewer.Logger;

/**
 * @author Martin Řehánek
 * 
 */
public class DoubleTapZoomManager {

	public static final long ANIM_LENGTH_MS = 250;
	public static final long ANIM_STEP_MS = 30;
	public static final double MIN_RESIZE_RATIO = 1.0;
	public static final double MAX_RESIZE_RATIO = 3.0;

	private static final Logger logger = new Logger(DoubleTapZoomManager.class);

	private final TiledImageView imageView;

	private State state = State.IDLE;
	private AnimationTimer animationTimer = null;

	// centers
	private PointD zoomCenterInImage;
	private PointD doubleTapCenterInCanvas;

	// zoom shift
	private VectorD accumalatedZoomShift = VectorD.ZERO_VECTOR;
	private VectorD activeZoomShift = VectorD.ZERO_VECTOR;

	// zoom scale
	private double accumulatedZoomScale = 1.0;
	private double activeZoomScale = 1.0;

	public DoubleTapZoomManager(TiledImageView imageView) {
		this.imageView = imageView;
	}

	public void startZooming(PointD doubleTapCenterInCanvasCoords) {
		this.doubleTapCenterInCanvas = doubleTapCenterInCanvasCoords;
		state = State.ZOOMING;
		// Log.d(TestTags.STATE, "zoom (double tap): " + state.name());
		// Log.d(TestTags.MOTION, "POINTER_DOWN, center: x=" +
		// doubleTapCenterInCanvasCoords.x + ", y="
		// + doubleTapCenterInCanvasCoords.y);
		calculateAndRunAnimation();
	}

	private void calculateAndRunAnimation() {
		zoomCenterInImage = Utils.toImageCoords(doubleTapCenterInCanvas, imageView.getCurrentScaleFactor(),
				imageView.getTotalShift());
		animationTimer = new AnimationTimer();
		animationTimer.start();
	}

	void notifyZoomingIn(double activeZoomScale) {
		double previousActiveZoomScale = this.activeZoomScale;
		this.activeZoomScale = activeZoomScale;
		double currentZoomScale = getCurrentZoomScale();
		double maxZoomScale = (imageView.getMaxScaleFactor() * currentZoomScale) / imageView.getCurrentScaleFactor();
		if (currentZoomScale > maxZoomScale) {
			// Log.d(TAG, "current > max; current: " + currentZoomScale +
			// ", max: " + maxZoomScale);
			this.activeZoomScale = previousActiveZoomScale;
		}
		VectorD newShift = computeNewShift();
		activeZoomShift = newShift.plus(activeZoomShift);
		imageView.invalidate();
	}

	private VectorD computeNewShift() {
		PointD zoomCenterInCanvasCoordsAfterZoomScaleBeforeZoomShift = Utils.toCanvasCoords(zoomCenterInImage,
				imageView.getCurrentScaleFactor(), imageView.getTotalShift());
		return new VectorD((doubleTapCenterInCanvas.x - zoomCenterInCanvasCoordsAfterZoomScaleBeforeZoomShift.x),
				(doubleTapCenterInCanvas.y - zoomCenterInCanvasCoordsAfterZoomScaleBeforeZoomShift.y));
	}

	public void cancelAnimation() {
		if (animationTimer != null) {
			animationTimer.cancel();
		}
	}

	private void storeDataOfCurrentZoom() {
		animationTimer = null;
		accumulatedZoomScale *= activeZoomScale;
		activeZoomScale = 1.0;
		accumalatedZoomShift = VectorD.sum(accumalatedZoomShift, activeZoomShift);
		activeZoomShift = VectorD.ZERO_VECTOR;
		zoomCenterInImage = null;
		state = State.IDLE;
		// Log.d(TestTags.STATE, "zoom (double tap): " + state.name());
		imageView.invalidate();
	}

	public State getState() {
		return state;
	}

	public PointD getDoubleTapCenterInCanvas() {
		return doubleTapCenterInCanvas;
	}

	public PointD getZoomCenterInImage() {
		return zoomCenterInImage;
	}

	public double getCurrentZoomScale() {
		return accumulatedZoomScale * activeZoomScale;
	}

	public VectorD getCurrentZoomShift() {
		return VectorD.sum(accumalatedZoomShift, activeZoomShift);
	}

	public enum State {
		IDLE, ZOOMING
	}

	private class AnimationTimer extends CountDownTimer {

		protected AnimationTimer() {
			super(ANIM_LENGTH_MS, ANIM_STEP_MS);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			float remainingTimeRatio = millisUntilFinished / ((float) ANIM_LENGTH_MS);
			double resizeDiff = MAX_RESIZE_RATIO - MIN_RESIZE_RATIO;
			double scale = MIN_RESIZE_RATIO + (1 - remainingTimeRatio) * resizeDiff;
			// Log.d(TAG, String.format("scale: %.2f", scale));
			notifyZoomingIn(scale);
		}

		@Override
		public void onFinish() {
			double scale = MAX_RESIZE_RATIO;
			// Log.d(TAG, String.format("scale: %.2f", scale));
			notifyZoomingIn(scale);
			storeDataOfCurrentZoom();
		}
	}

}

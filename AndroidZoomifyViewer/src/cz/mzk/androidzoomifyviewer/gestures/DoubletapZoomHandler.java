package cz.mzk.androidzoomifyviewer.gestures;

import android.os.Handler;
import android.os.Message;
import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.viewer.DevTools;
import cz.mzk.androidzoomifyviewer.viewer.PointD;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;
import cz.mzk.androidzoomifyviewer.viewer.Utils;
import cz.mzk.androidzoomifyviewer.viewer.VectorD;

/**
 * @author Martin Řehánek
 * 
 */
public class DoubletapZoomHandler extends Handler {

	public enum State {
		IDLE, ZOOMING;
	}

	public static final long ANIM_LENGTH_MS = 300;
	public static final double MIN_ANIMATION_SCALE_FACTOR = 1.0;
	public static final double MAX_ANIMATION_SCALE_FACTOR = 3.0;
	public static final int ANIM_STEPS = 10;
	public static final long ANIM_STEP_MS = ANIM_LENGTH_MS / ANIM_STEPS;

	private static final Logger logger = new Logger(DoubletapZoomHandler.class);
	// private static final Logger logger = new Logger("GST: double tap zoom");

	private final TiledImageView imageView;
	private final GestureHandler abstractGestureHandler; // since no multiple inheritance in java

	private State state = State.IDLE;
	private Thread workerThread;
	private int correctWorkerId = 0;

	// centers
	private PointD initialFocusInImageCoords;
	private PointD currentFocusInCanvas;

	// shift
	private VectorD accumulatedShift = VectorD.ZERO_VECTOR;
	private VectorD activeShift = VectorD.ZERO_VECTOR;

	// scale
	private double accumulatedScaleFactor = 1.0;
	private double activeScaleFactor = 1.0;

	// for animation
	private final double scaleDiff = MAX_ANIMATION_SCALE_FACTOR - MIN_ANIMATION_SCALE_FACTOR;
	private final double scaleStep = scaleDiff / ANIM_STEPS;

	public DoubletapZoomHandler(TiledImageView imageView) {
		this.imageView = imageView;
		this.abstractGestureHandler = new GestureHandler(imageView);
	}

	public void startZooming(PointD doubleTapCenterInCanvasCoords) {
		state = State.ZOOMING;
		logger.i(state.name());
		currentFocusInCanvas = doubleTapCenterInCanvasCoords;
		initialFocusInImageCoords = Utils.toImageCoords(currentFocusInCanvas, imageView.getTotalScaleFactor(),
				imageView.getTotalShift());
		workerThread = new Thread(new AnimationRunnable(this, correctWorkerId));
		workerThread.start();
		devUpdateZoomCenters();
	}

	private void devUpdateZoomCenters() {
		if (TiledImageView.DEV_MODE) {
			DevTools devTools = imageView.getDevTools();
			if (devTools != null) {
				devTools.setDoubletapZoomCenters(currentFocusInCanvas, initialFocusInImageCoords);
			}
		}
	}

	private class AnimationRunnable implements Runnable {
		private final Handler handler;
		private final int workerId;

		public AnimationRunnable(Handler handler, int workerId) {
			this.handler = handler;
			this.workerId = workerId;
		}

		@Override
		public void run() {
			for (int i = 0; true; i++) {
				Message msg = Message.obtain();
				msg.arg1 = workerId;
				msg.arg2 = i;
				handler.sendMessage(msg);
				// logger.v(String.format("worker thread %d:  sending message: %d", workerId, i));
				try {
					Thread.sleep(ANIM_STEP_MS);
				} catch (InterruptedException e) {
					// logger.v(String.format("worker thread %d:  killed in sleep", workerId));
					return;
				}
			}
		}
	}

	@Override
	public void handleMessage(Message msg) {
		switch (state) {
		case ZOOMING:
			int workerId = msg.arg1;
			if (workerId == correctWorkerId) {
				int i = msg.arg2;
				if (i <= ANIM_STEPS) {
					double ratio = MIN_ANIMATION_SCALE_FACTOR + i * scaleStep;
					// logger.v(String.format("ui thread: message from thread %d: %d - processing", workerId, i));
					boolean maxZoomLevelReached = zoomIn(ratio);
					if (maxZoomLevelReached) {
						stopAnimation();
					}
				} else {
					logger.v(String.format("ui thead: message from thread %d: %d - ignoring (last step reached)",
							workerId, i));
					stopAnimation();
				}
			} else {
				logger.v(String.format("ui thead: message from thread %d: %d - ignoring (old thread)", workerId,
						msg.arg2));
			}
			break;
		case IDLE:
			logger.v(String.format("ui thead: message from thread %d: %d - ignoring (state IDLE)", msg.arg1, msg.arg2));
			break;
		}
	}

	private boolean zoomIn(double currentScaleFactor) {
		// scale factor
		logger.d("scale factor: " + currentScaleFactor);
		activeScaleFactor = 1.0;
		double maxTotalScaleFactor = imageView.getMaxScaleFactor();
		double totalScaleFactorWithoutActive = imageView.getTotalScaleFactor();
		double maxActiveScaleFactor = maxTotalScaleFactor / totalScaleFactorWithoutActive;
		boolean maxScaleFactorReached = currentScaleFactor >= maxActiveScaleFactor;

		if (maxScaleFactorReached) {
			logger.d("max scale reached");
			activeScaleFactor = maxActiveScaleFactor;
		} else {
			activeScaleFactor = currentScaleFactor;
		}
		if (activeScaleFactor > 1) {
			logger.d("zooming in");
		} else {// zooming out
			logger.d("zooming out");
		}
		// shift
		activeShift = VectorD.ZERO_VECTOR;
		PointD initialFocusToBeShiftedInCanvasCoords = Utils.toCanvasCoords(initialFocusInImageCoords,
				imageView.getTotalScaleFactor(), imageView.getTotalShift());
		VectorD newShift = currentFocusInCanvas.minus(initialFocusToBeShiftedInCanvasCoords);
		activeShift = abstractGestureHandler.limitNewShift(newShift);
		imageView.invalidate();
		return maxScaleFactorReached;
	}

	public State getState() {
		return state;
	}

	public double getCurrentScaleFactor() {
		return accumulatedScaleFactor * activeScaleFactor;
	}

	public VectorD getCurrentZoomShift() {
		return VectorD.sum(accumulatedShift, activeShift);
	}

	public void reset() {
		logger.d("resetting");
		if (state != State.IDLE) {
			logger.w("animation still running");
			stopAnimation();
		}
		accumulatedShift = VectorD.ZERO_VECTOR;
		accumulatedScaleFactor = 1.0;
	}

	public void stopAnimation() {
		logger.d("stopping animation");
		if (state == State.IDLE) {
			logger.w("already stopped");
		} else {
			if (workerThread != null && workerThread.isAlive()) {
				workerThread.interrupt();
			}
			workerThread = null;
			correctWorkerId++;
			logger.v("correct worker id: " + correctWorkerId);
			accumulatedScaleFactor *= activeScaleFactor;
			activeScaleFactor = 1.0;
			accumulatedShift = VectorD.sum(accumulatedShift, activeShift);
			activeShift = VectorD.ZERO_VECTOR;
			state = State.ZOOMING;
			logger.i(state.name());
		}
	}

}

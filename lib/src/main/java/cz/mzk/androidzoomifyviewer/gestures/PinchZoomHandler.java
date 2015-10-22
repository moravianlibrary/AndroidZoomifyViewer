package cz.mzk.androidzoomifyviewer.gestures;

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
public class PinchZoomHandler extends GestureHandler {

	public enum State {
		IDLE, READY_TO_PINCH, PINCHING
	}

	// private static final Logger logger = new Logger(PinchZoomManager.class);
	private static final Logger logger = new Logger("GST: pinch zoom");

	private State state = State.IDLE;

	private double initialSpan;
	private PointD initialFocusInImageCoords;

	// shift
	private VectorD accumalatedShift = VectorD.ZERO_VECTOR;
	private VectorD activeShift = VectorD.ZERO_VECTOR;

	// scale
	private double accumulatedScaleFactor = 1.0;
	private double activeScaleFactor = 1.0;

	public PinchZoomHandler(TiledImageView imageView) {
		super(imageView);
	}

	public State getState() {
		return state;
	}

	public double getCurrentScaleFactor() {
		return accumulatedScaleFactor * activeScaleFactor;
	}

	public VectorD getCurrentShift() {
		return VectorD.sum(accumalatedShift, activeShift);
	}

	public void startZooming(double span, PointD focus) {
		initialSpan = span;
		initialFocusInImageCoords = Utils.toImageCoords(focus, imageView.getTotalScaleFactor(),
				imageView.getTotalShift());
		devUpdateZoomCenters(focus);
		state = State.READY_TO_PINCH;
		logger.i(state.name());
	}

	public void zoom(PointD currentFocusInCanvas, double currentSpan) {
		state = State.PINCHING;
		logger.i(state.name());
		// scale
		activeScaleFactor = 1.0;
		double currentScaleFactor = currentSpan / initialSpan;
		logger.d("scale factor: " + currentScaleFactor);
		double totalScaleFactorWithoutActive = imageView.getTotalScaleFactor();
		double maxTotalScaleFactor = imageView.getMaxScaleFactor();
		double maxActiveScaleFactor = maxTotalScaleFactor / totalScaleFactorWithoutActive;
		double minTotalScaleFactor = imageView.getMinScaleFactor();
		double minActiveScaleFactor = minTotalScaleFactor / totalScaleFactorWithoutActive;
		if (currentScaleFactor >= maxActiveScaleFactor) {
			logger.d("max scale reached");
			activeScaleFactor = maxActiveScaleFactor;
		} else if (currentScaleFactor <= minActiveScaleFactor) {
			activeScaleFactor = minActiveScaleFactor;
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
		activeShift = limitNewShift(newShift);
		devUpdateZoomCenters(currentFocusInCanvas);
		imageView.invalidate();
	}

	private void devUpdateZoomCenters(PointD currentFocusInCanvas) {
		if (TiledImageView.DEV_MODE) {
			DevTools devTools = imageView.getDevTools();
			if (devTools != null) {
				devTools.setPinchZoomCenters(currentFocusInCanvas, initialFocusInImageCoords);
			}
		}
	}

	public void finishZooming() {
		accumulatedScaleFactor *= activeScaleFactor;
		activeScaleFactor = 1.0;
		accumalatedShift = VectorD.sum(accumalatedShift, activeShift);
		activeShift = VectorD.ZERO_VECTOR;
		initialSpan = 0.0;
		initialFocusInImageCoords = null;
		state = State.IDLE;
		logger.i(state.name());
	}

	public void reset() {
		accumalatedShift = VectorD.ZERO_VECTOR;
		activeShift = VectorD.ZERO_VECTOR;
		accumulatedScaleFactor = 1.0;
		activeScaleFactor = 1.0;
	}

}
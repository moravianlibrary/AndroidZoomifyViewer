package cz.mzk.androidzoomifyviewer.viewer;

import android.os.AsyncTask.Status;
import android.util.Log;
import cz.mzk.androidzoomifyviewer.ConcurrentAsyncTask;

/**
 * @author Martin Řehánek
 * 
 */
public class DoubleTapZoomManager {

	private static final String TAG = DoubleTapZoomManager.class.getSimpleName();

	private final TiledImageView imageView;
	private State state = State.IDLE;
	private CountDownTask animationTask = null;

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
		// DevTools devTools = imageView.getDevTools();
		// if (devTools != null) {
		// devTools.clearCenters();
		// }
		this.doubleTapCenterInCanvas = doubleTapCenterInCanvasCoords;
		state = State.ZOOMING;
		// Log.d(TestTags.STATE, "zoom (double tap): " + state.name());
		// Log.d(TestTags.MOTION, "POINTER_DOWN, center: x=" +
		// doubleTapCenterInCanvasCoords.x + ", y="
		// + doubleTapCenterInCanvasCoords.y);
		calculateAndRunAnimation();
	}

	private void calculateAndRunAnimation() {
		zoomCenterInImage = Utils.toImageCoords(doubleTapCenterInCanvas, imageView.getTotalResizeFactor(),
				imageView.getTotalShift());
		animationTask = new CountDownTask();
		animationTask.executeConcurrentIfPossible();
	}

	void notifyZoomingIn(double activeZoomLevel) {
		this.activeZoomScale = activeZoomLevel;
		VectorD newZoomShift = computeActiveZoomShift(doubleTapCenterInCanvas, imageView.getTotalResizeFactor(),
				imageView.getTotalShift());
		accumalatedZoomShift = VectorD.sum(accumalatedZoomShift, newZoomShift);
		imageView.invalidate();
	}

	private VectorD computeActiveZoomShift(PointD doubleTapCenterInCanvasCoords, double resizeFactor, VectorD shift) {
		// Log.d(TestTags.ZOOM,
		// String.format("activeZoomLevel: %.4f", activeZoomScale)
		// + String.format(" accumulatedZoomLevel: %.2f",
		// accumulatedZoomScale));
		PointD zoomCenterInCanvasCoordsAfterZoomScaleBeforeZoomShift = Utils.toCanvasCoords(zoomCenterInImage,
				imageView.getTotalResizeFactor(), imageView.getTotalShift());
		// Log.d(TestTags.CENTERS, "gesture: " + doubleTapCenterInCanvasCoords +
		// ", image: "
		// + zoomCenterInCanvasCoordsAfterZoomScaleBeforeZoomShift);
		VectorD diff = new VectorD(
				(doubleTapCenterInCanvasCoords.x - zoomCenterInCanvasCoordsAfterZoomScaleBeforeZoomShift.x),
				(doubleTapCenterInCanvasCoords.y - zoomCenterInCanvasCoordsAfterZoomScaleBeforeZoomShift.y));
		// Log.d(TestTags.ZOOM,
		// "zoomMngr: init: " +
		// zoomCenterInCanvasCoordsAfterZoomScaleBeforeZoomShift.toString() +
		// " now: "
		// + doubleTapCenterInCanvasCoords.toString() + " diff: " +
		// diff.toString()
		// + String.format(" r:%.4f", imageView.getTotalResizeFactor()) + " s: "
		// + imageView.getTotalShift().toString());
		// Log.d(TAG, "diff: " + diff.toString() + ", distance: " +
		// String.format("%.3f", diff.getSize()));
		return diff;
	}

	public void cancelZooming() {
		if (animationTask != null
				&& (animationTask.getStatus() == Status.PENDING || animationTask.getStatus() == Status.RUNNING)) {
			animationTask.cancel(false);
		}
	}

	private void storeDataOfCurrentZoom() {
		animationTask = null;
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

	private class CountDownTask extends ConcurrentAsyncTask<Void, Double, Void> {

		// FIXME: if not enough free threads
		// (e.g. small thread pool and tiles are being downloaded)
		// the animation doesn't take place right away which is obvious to user
		// Perhaps limit number of tiles-downloading-tasks being run in
		// parallel? (according to the pool size)

		@Override
		protected Void doInBackground(Void... params) {
			// long start = System.currentTimeMillis();
			long waitTime = 10;
			double resizeRatio = 1.0;
			while (resizeRatio < 2.0) {
				if (isCancelled()) {
					break;
				}
				resizeRatio += 0.05;
				publishProgress(resizeRatio);
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
					Log.d(TAG, "thread killed", e);
				}
			}
			// long now = System.currentTimeMillis();
			// long time = now - start;
			// Log.d(TAG, "animation length: " + time + " ms");
			return null;
		}

		@Override
		protected void onProgressUpdate(Double... values) {
			notifyZoomingIn(values[0]);
		}

		@Override
		protected void onCancelled(Void result) {
			Log.d(TAG, "canceled");
			storeDataOfCurrentZoom();
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.d(TAG, "finished");
			storeDataOfCurrentZoom();
		}

	}

}

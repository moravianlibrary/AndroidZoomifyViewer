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
	private static final String TAG_STATES = "state";

	private final TiledImageView imageView;
	private State state = State.IDLE;

	// zoom shift
	private VectorD accumalatedZoomShift = VectorD.ZERO_VECTOR;
	private VectorD activeZoomShift = VectorD.ZERO_VECTOR;

	// zoom level
	private double accumulatedZoomLevel = 1.0;
	private double activeZoomLevel = 1.0;

	private PointD startingZoomCenterInImageCoords;
	private PointD currentZoomCenterInCanvasCoords;// TODO: just temporary

	private CountDownTask task = null;

	public DoubleTapZoomManager(TiledImageView imageView) {
		this.imageView = imageView;
	}

	public void startZoomingAnimation(PointD center) {
		state = State.ZOOMING;
		Log.d(TAG_STATES, "zoom (double tap): " + state.name());
		currentZoomCenterInCanvasCoords = center;
		startingZoomCenterInImageCoords = Utils.toImageCoords(currentZoomCenterInCanvasCoords.x,
				currentZoomCenterInCanvasCoords.y, imageView.getTotalResizeFactor(), imageView.getTotalShift());
		Log.d("Motion", "POINTER_DOWN, center: x=" + currentZoomCenterInCanvasCoords.x + ", y="
				+ currentZoomCenterInCanvasCoords.y);
		task = new CountDownTask();
		task.executeConcurrentIfPossible();
	}

	void notifyZoomingIn(double activeZoomLevel) {
		this.activeZoomLevel = activeZoomLevel;
		activeZoomShift = VectorD.ZERO_VECTOR;
		accumalatedZoomShift = computeActiveZoomShift(currentZoomCenterInCanvasCoords,
				imageView.getTotalResizeFactor(), imageView.getTotalShift());
		imageView.invalidate();
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

		// testCounter++;
		Log.d("z00m",
				"zoomMngr: init: " + initialZoomCenter.toString() + " now: "
						+ currentZoomCenterInCanvasCoords.toString() + " diff: " + diff.toString()
						+ String.format(" r:%.4f", imageView.getTotalResizeFactor()) + " s: "
						+ imageView.getTotalShift().toString());// + " c: " +
																// testCounter);

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
	}

	public void cancelZoomingAnimation() {
		if (task != null && (task.getStatus() == Status.PENDING || task.getStatus() == Status.RUNNING)) {
			task.cancel(false);
		}
	}

	void notifyZoomingCanceled() {
		finishAndResetData();
		state = State.IDLE;
		Log.d(TAG_STATES, "zoom (double tap): " + state.name());
		imageView.invalidate();
	}

	void notifyZoomingFinished() {
		finishAndResetData();
		state = State.IDLE;
		Log.d(TAG_STATES, "zoom (double tap): " + state.name());
		imageView.invalidate();
	}

	private void finishAndResetData() {
		accumulatedZoomLevel *= activeZoomLevel;
		activeZoomLevel = 1.0;
		accumalatedZoomShift = VectorD.sum(accumalatedZoomShift, activeZoomShift);
		activeZoomShift = VectorD.ZERO_VECTOR;
		startingZoomCenterInImageCoords = null;
	}

	public State getState() {
		return state;
	}

	public PointD getCurrentZoomCenter() {
		return currentZoomCenterInCanvasCoords;
	}

	public PointD getInitialZoomCenterInImageCoords() {
		return startingZoomCenterInImageCoords;
	}

	public double getCurrentZoomLevel() {
		return accumulatedZoomLevel * activeZoomLevel;
	}

	public VectorD getCurrentZoomShift() {
		return VectorD.sum(accumalatedZoomShift, activeZoomShift);
	}

	public enum State {
		IDLE, ZOOMING
	}

	private class CountDownTask extends ConcurrentAsyncTask<Void, Double, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			long start = System.currentTimeMillis();
			double resizeRatio = 1.0;
			while (resizeRatio < 2.0) {
				if (isCancelled()) {
					break;
				}
				resizeRatio += 0.05;
				publishProgress(resizeRatio);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					Log.d(TAG, "thread killed", e);
				}
			}
			long now = System.currentTimeMillis();
			long time = now - start;
			Log.d(TAG, "animation length: " + time + " ms");
			return null;
		}

		@Override
		protected void onProgressUpdate(Double... values) {
			notifyZoomingIn(values[0]);
		}

		@Override
		protected void onCancelled(Void result) {
			notifyZoomingCanceled();
		}

		@Override
		protected void onPostExecute(Void result) {
			notifyZoomingFinished();
		}

	}

}

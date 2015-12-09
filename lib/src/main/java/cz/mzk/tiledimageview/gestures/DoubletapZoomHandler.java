package cz.mzk.tiledimageview.gestures;

import android.os.Handler;
import android.os.Message;

import cz.mzk.tiledimageview.Logger;
import cz.mzk.tiledimageview.viewer.DevTools;
import cz.mzk.tiledimageview.viewer.PointD;
import cz.mzk.tiledimageview.viewer.TiledImageViewApi;
import cz.mzk.tiledimageview.viewer.Utils;
import cz.mzk.tiledimageview.viewer.VectorD;

/**
 * @author Martin Řehánek
 */
public class DoubletapZoomHandler extends Handler {

    public static final long ANIM_LENGTH_MS = 300;
    public static final double MIN_ANIMATION_SCALE_FACTOR = 1.0;
    public static final double MAX_ANIMATION_SCALE_FACTOR = 3.0;
    public static final int ANIM_STEPS = 10;
    public static final long ANIM_STEP_MS = ANIM_LENGTH_MS / ANIM_STEPS;
    // private static final Logger LOGGER = new Logger("GST: double tap zoom");
    private static final Logger LOGGER = new Logger(DoubletapZoomHandler.class);
    private static final double SCALE_DIFF = MAX_ANIMATION_SCALE_FACTOR - MIN_ANIMATION_SCALE_FACTOR;
    private static final double SCALE_STEP = SCALE_DIFF / ANIM_STEPS;
    private final TiledImageViewApi mImageViewApi;
    private final DevTools mDevTools;

    private final GestureHandler mAbstractGestureHandler; // since no multiple inheritance in java
    private State mState = State.IDLE;
    private Thread mWorkerThread;
    private int mCorrectWorkerId = 0;
    // centers
    private PointD mInitialFocusInImageCoords;
    private PointD mCurrentFocusInCanvas;
    // shift
    private VectorD mAccumulatedShift = VectorD.ZERO_VECTOR;
    private VectorD mActiveShift = VectorD.ZERO_VECTOR;
    // scale
    private double mAccumulatedScaleFactor = 1.0;
    private double mActiveScaleFactor = 1.0;

    public DoubletapZoomHandler(TiledImageViewApi imageViewApi, DevTools devTools) {
        mImageViewApi = imageViewApi;
        mAbstractGestureHandler = new GestureHandler(imageViewApi, devTools);
        mDevTools = devTools;
    }

    public void startZooming(PointD doubleTapCenterInCanvasCoords) {
        mState = State.ZOOMING;
        LOGGER.i(mState.name());
        mCurrentFocusInCanvas = doubleTapCenterInCanvasCoords;
        mInitialFocusInImageCoords = Utils.toImageCoords(mCurrentFocusInCanvas, mImageViewApi.getTotalScaleFactor(),
                mImageViewApi.getTotalShift());
        mWorkerThread = new Thread(new AnimationRunnable(this, mCorrectWorkerId));
        mWorkerThread.start();
        devUpdateZoomCenters();
    }

    private void devUpdateZoomCenters() {
        if (mDevTools != null) {
            mDevTools.setDoubletapZoomCenters(mCurrentFocusInCanvas, mInitialFocusInImageCoords);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (mState) {
            case ZOOMING:
                int workerId = msg.arg1;
                if (workerId == mCorrectWorkerId) {
                    int i = msg.arg2;
                    if (i <= ANIM_STEPS) {
                        double ratio = MIN_ANIMATION_SCALE_FACTOR + i * SCALE_STEP;
                        // LOGGER.v(String.format("ui thread: message from thread %d: %d - processing", workerId, i));
                        boolean maxZoomLevelReached = zoomIn(ratio);
                        if (maxZoomLevelReached) {
                            stopAnimation();
                        }
                    } else {
                        LOGGER.v(String.format("ui thead: message from thread %d: %d - ignoring (last step reached)",
                                workerId, i));
                        stopAnimation();
                    }
                } else {
                    LOGGER.v(String.format("ui thead: message from thread %d: %d - ignoring (old thread)", workerId,
                            msg.arg2));
                }
                break;
            case IDLE:
                LOGGER.v(String.format("ui thead: message from thread %d: %d - ignoring (mState IDLE)", msg.arg1, msg.arg2));
                break;
        }
    }

    private boolean zoomIn(double currentScaleFactor) {
        // scale factor
        LOGGER.d("scale factor: " + currentScaleFactor);
        mActiveScaleFactor = 1.0;
        double maxTotalScaleFactor = mImageViewApi.getMaxScaleFactor();
        double totalScaleFactorWithoutActive = mImageViewApi.getTotalScaleFactor();
        double maxActiveScaleFactor = maxTotalScaleFactor / totalScaleFactorWithoutActive;
        boolean maxScaleFactorReached = currentScaleFactor >= maxActiveScaleFactor;

        if (maxScaleFactorReached) {
            LOGGER.d("max scale reached");
            mActiveScaleFactor = maxActiveScaleFactor;
        } else {
            mActiveScaleFactor = currentScaleFactor;
        }
        if (mActiveScaleFactor > 1) {
            LOGGER.d("zooming in");
        } else {// zooming out
            LOGGER.d("zooming out");
        }
        // shift
        mActiveShift = VectorD.ZERO_VECTOR;
        PointD initialFocusToBeShiftedInCanvasCoords = Utils.toCanvasCoords(mInitialFocusInImageCoords,
                mImageViewApi.getTotalScaleFactor(), mImageViewApi.getTotalShift());
        VectorD newShift = mCurrentFocusInCanvas.minus(initialFocusToBeShiftedInCanvasCoords);
        mActiveShift = mAbstractGestureHandler.limitNewShift(newShift);
        mImageViewApi.invalidate();
        return maxScaleFactorReached;
    }

    public State getState() {
        return mState;
    }

    public double getCurrentScaleFactor() {
        return mAccumulatedScaleFactor * mActiveScaleFactor;
    }

    public VectorD getCurrentZoomShift() {
        return VectorD.sum(mAccumulatedShift, mActiveShift);
    }

    public void reset() {
        LOGGER.d("resetting");
        if (mState != State.IDLE) {
            LOGGER.w("animation still running");
            stopAnimation();
        }
        mAccumulatedShift = VectorD.ZERO_VECTOR;
        mAccumulatedScaleFactor = 1.0;
    }

    public void stopAnimation() {
        LOGGER.d("stopping animation");
        if (mState == State.IDLE) {
            LOGGER.w("already stopped");
        } else {
            if (mWorkerThread != null && mWorkerThread.isAlive()) {
                mWorkerThread.interrupt();
            }
            mWorkerThread = null;
            mCorrectWorkerId++;
            LOGGER.v("correct worker id: " + mCorrectWorkerId);
            mAccumulatedScaleFactor *= mActiveScaleFactor;
            mActiveScaleFactor = 1.0;
            mAccumulatedShift = VectorD.sum(mAccumulatedShift, mActiveShift);
            mActiveShift = VectorD.ZERO_VECTOR;
            mState = State.ZOOMING;
            LOGGER.i(mState.name());
        }
    }

    public enum State {
        IDLE, ZOOMING;
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
            // ThreadGroup group = Thread.currentThread().getThreadGroup();
            // int threadPriority = Thread.currentThread().getPriority();
            // TestLoggers.THREADS.d(String.format(
            // "double-tap worker: priority: %d, TG: name: %s, active: %d, max priority: %d, ", threadPriority,
            // group.getName(), group.activeCount(), group.getMaxPriority()));
            for (int i = 0; true; i++) {
                Message msg = Message.obtain();
                msg.arg1 = workerId;
                msg.arg2 = i;
                handler.sendMessage(msg);
                // LOGGER.v(String.format("worker thread %d:  sending message: %d", workerId, i));
                try {
                    Thread.sleep(ANIM_STEP_MS);
                } catch (InterruptedException e) {
                    // LOGGER.v(String.format("worker thread %d:  killed in sleep", workerId));
                    return;
                }
            }
        }
    }

}

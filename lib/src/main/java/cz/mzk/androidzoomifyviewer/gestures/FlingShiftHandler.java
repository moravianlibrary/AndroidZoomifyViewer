package cz.mzk.androidzoomifyviewer.gestures;

import android.os.Handler;
import android.os.Message;

import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.viewer.DevTools;
import cz.mzk.androidzoomifyviewer.viewer.PointD;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageViewApi;
import cz.mzk.androidzoomifyviewer.viewer.Utils;
import cz.mzk.androidzoomifyviewer.viewer.VectorD;

public class FlingShiftHandler extends Handler {

    public static final long ANIM_STEP_MS = 30;
    public static final float MIN_VELOCITY_PX_P_S = 10f;
    public static final float VELOCITY_PRESERVATION_FACTOR = 0.9f;
    // private static final Logger LOGGER = new Logger("GST: fling shift");
    private static final Logger LOGGER = new Logger(FlingShiftHandler.class);
    // persistent data
    private final TiledImageViewApi mImageViewApi;
    private final GestureHandler mAbstractGestureHandler; // since no multiple inheritance in java
    private State mState = State.IDLE;
    private VectorD mAccumulatedShift = VectorD.ZERO_VECTOR;
    private Thread mWorkerThread;
    private int mCorrectWorkerId = 0;
    // data of running animation
    private PointD mInitialFocusInImg;
    private float mVelocityX;
    private float mVelocityY;

    public FlingShiftHandler(TiledImageViewApi imageViewApi, DevTools devTools) {
        this.mImageViewApi = imageViewApi;
        this.mAbstractGestureHandler = new GestureHandler(imageViewApi, devTools);
    }

    public VectorD getShift() {
        return mAccumulatedShift;
    }

    public void fling(float downX, float downY, float velocityX, float velocityY) {
        mState = State.SHIFTING;
        LOGGER.i(mState.name());
        this.mVelocityX = velocityX;
        this.mVelocityY = velocityY;
        this.mInitialFocusInImg = Utils.toImageCoords(new PointD(downX, downY), mImageViewApi.getTotalScaleFactor(), mImageViewApi.getTotalShift());
        mWorkerThread = new Thread(new AnimationRunnable(this, mCorrectWorkerId));
        mWorkerThread.start();
    }

    @Override
    public void handleMessage(Message msg) {
        switch (mState) {
            case SHIFTING:
                int workerId = msg.arg1;
                if (workerId == mCorrectWorkerId) {
                    boolean keepMoving = shift();
                    if (keepMoving) {
                        // LOGGER.v(String.format("ui thread: message from thread %d - processing", workerId));
                        updateVelocities();
                    } else {
                        LOGGER.v(String
                                .format("ui thread: message from thread %d - ignoring (velocities to low)", workerId));
                        stopAnimation();
                    }
                } else {
                    LOGGER.v(String.format("ui thread: message from thread %d - ignoring (old threadead)", workerId));
                }
                break;
            case IDLE:
                LOGGER.v(String.format("ui thread: message from thread %d - ignoring (mState IDLE)", msg.arg1));
                break;
        }
    }

    private boolean shift() {
        double totalScaleFactor = mImageViewApi.getTotalScaleFactor();
        VectorD totalShift = mImageViewApi.getTotalShift();
        // pixels for animation step
        float stepSecondFraction = ANIM_STEP_MS / 1000.0f;
        float pixelsPerStepX = mVelocityX * stepSecondFraction;
        float pixelsPerStepY = mVelocityY * stepSecondFraction;
        double pixelsPerStepCanvasX = pixelsPerStepX * totalScaleFactor;
        double pixelsPerStepCanvasY = pixelsPerStepY * totalScaleFactor;
        // shift
        PointD currentInCanvas = Utils.toCanvasCoords(mInitialFocusInImg, totalScaleFactor, totalShift);
        PointD nextInCanvas = currentInCanvas.plus(new VectorD(pixelsPerStepCanvasX, pixelsPerStepCanvasY));
        PointD nextInImg = Utils.toImageCoords(nextInCanvas, totalScaleFactor, totalShift);
        VectorD newShift = mAbstractGestureHandler.limitNewShift(mInitialFocusInImg.minus(nextInImg));
        LOGGER.v("shift: " + newShift.toString());
        // optimization for zero shift
        if (newShift.x == 0.0 && newShift.y == 0.0) {
            LOGGER.d("zero shift");
            stopAnimation();
        }
        newShift = mAbstractGestureHandler.limitNewShift(newShift);
        mAccumulatedShift = mAccumulatedShift.plus(newShift);
        mImageViewApi.invalidate();
        return (Math.abs(mVelocityX) > MIN_VELOCITY_PX_P_S || Math.abs(mVelocityY) > MIN_VELOCITY_PX_P_S);
    }

    private void updateVelocities() {
        mVelocityX = mVelocityX * VELOCITY_PRESERVATION_FACTOR;
        mVelocityY = mVelocityY * VELOCITY_PRESERVATION_FACTOR;
        LOGGER.v(String.format("velocities: x: %.2f, y: %.2f px/s", mVelocityX, mVelocityY));
    }

    public void reset() {
        LOGGER.d("resetting");
        if (mState == State.SHIFTING) {
            LOGGER.w("animation still running");
            stopAnimation();
        }
        mAccumulatedShift = VectorD.ZERO_VECTOR;
    }

    public State getmState() {
        return mState;
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
            this.mState = State.IDLE;
            LOGGER.i(mState.name());
        }
    }

    public enum State {
        IDLE, SHIFTING;
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
            // TestLoggers.THREADS.d("fling worker: " + Thread.currentThread().getPriority());
            while (true) {
                Message msg = Message.obtain();
                msg.arg1 = workerId;
                handler.sendMessage(msg);
                // LOGGER.v(String.format("worker thread %d:  sending message", workerId));
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

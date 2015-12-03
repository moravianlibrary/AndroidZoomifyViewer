package cz.mzk.androidzoomifyviewer.gestures;

import android.os.Handler;
import android.os.Message;

import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.viewer.PointD;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;
import cz.mzk.androidzoomifyviewer.viewer.Utils;
import cz.mzk.androidzoomifyviewer.viewer.VectorD;

public class FlingShiftHandler extends Handler {

    public static final long ANIM_STEP_MS = 30;
    public static final float MIN_VELOCITY_PX_P_S = 10f;
    public static final float VELOCITY_PRESERVATION_FACTOR = 0.9f;
    private static final Logger logger = new Logger(FlingShiftHandler.class);
    // persistent data
    private final TiledImageView imageView;
    // private static final Logger logger = new Logger("GST: fling shift");
    private final GestureHandler abstractGestureHandler; // since no multiple inheritance in java
    private State state = State.IDLE;
    private VectorD accumulatedShift = VectorD.ZERO_VECTOR;
    private Thread workerThread;
    private int correctWorkerId = 0;
    // data of running animation
    private PointD initialFocusInImg;
    private float velocityX;
    private float velocityY;
    public FlingShiftHandler(TiledImageView imageView) {
        this.imageView = imageView;
        this.abstractGestureHandler = new GestureHandler(imageView);
    }

    public VectorD getShift() {
        return accumulatedShift;
    }

    public void fling(float downX, float downY, float velocityX, float velocityY) {
        state = State.SHIFTING;
        logger.i(state.name());
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.initialFocusInImg = Utils.toImageCoords(new PointD(downX, downY), imageView.getTotalScaleFactor(),
                imageView.getTotalShift());
        workerThread = new Thread(new AnimationRunnable(this, correctWorkerId));
        workerThread.start();
    }

    @Override
    public void handleMessage(Message msg) {
        switch (state) {
            case SHIFTING:
                int workerId = msg.arg1;
                if (workerId == correctWorkerId) {
                    boolean keepMoving = shift();
                    if (keepMoving) {
                        // logger.v(String.format("ui thread: message from thread %d - processing", workerId));
                        updateVelocities();
                    } else {
                        logger.v(String
                                .format("ui thread: message from thread %d - ignoring (velocities to low)", workerId));
                        stopAnimation();
                    }
                } else {
                    logger.v(String.format("ui thread: message from thread %d - ignoring (old threadead)", workerId));
                }
                break;
            case IDLE:
                logger.v(String.format("ui thread: message from thread %d - ignoring (state IDLE)", msg.arg1));
                break;
        }
    }

    private boolean shift() {
        double totalScaleFactor = imageView.getTotalScaleFactor();
        VectorD totalShift = imageView.getTotalShift();
        // pixels for animation step
        float stepSecondFraction = ANIM_STEP_MS / 1000.0f;
        float pixelsPerStepX = velocityX * stepSecondFraction;
        float pixelsPerStepY = velocityY * stepSecondFraction;
        double pixelsPerStepCanvasX = pixelsPerStepX * totalScaleFactor;
        double pixelsPerStepCanvasY = pixelsPerStepY * totalScaleFactor;
        // shift
        PointD currentInCanvas = Utils.toCanvasCoords(initialFocusInImg, totalScaleFactor, totalShift);
        PointD nextInCanvas = currentInCanvas.plus(new VectorD(pixelsPerStepCanvasX, pixelsPerStepCanvasY));
        PointD nextInImg = Utils.toImageCoords(nextInCanvas, totalScaleFactor, totalShift);
        VectorD newShift = abstractGestureHandler.limitNewShift(initialFocusInImg.minus(nextInImg));
        logger.v("shift: " + newShift.toString());
        // optimization for zero shift
        if (newShift.x == 0.0 && newShift.y == 0.0) {
            logger.d("zero shift");
            stopAnimation();
        }
        newShift = abstractGestureHandler.limitNewShift(newShift);
        accumulatedShift = accumulatedShift.plus(newShift);
        imageView.invalidate();
        return (Math.abs(velocityX) > MIN_VELOCITY_PX_P_S || Math.abs(velocityY) > MIN_VELOCITY_PX_P_S);
    }

    private void updateVelocities() {
        velocityX = velocityX * VELOCITY_PRESERVATION_FACTOR;
        velocityY = velocityY * VELOCITY_PRESERVATION_FACTOR;
        logger.v(String.format("velocities: x: %.2f, y: %.2f px/s", velocityX, velocityY));
    }

    public void reset() {
        logger.d("resetting");
        if (state == State.SHIFTING) {
            logger.w("animation still running");
            stopAnimation();
        }
        accumulatedShift = VectorD.ZERO_VECTOR;
    }

    public State getState() {
        return state;
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
            this.state = State.IDLE;
            logger.i(state.name());
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
                // logger.v(String.format("worker thread %d:  sending message", workerId));
                try {
                    Thread.sleep(ANIM_STEP_MS);
                } catch (InterruptedException e) {
                    // logger.v(String.format("worker thread %d:  killed in sleep", workerId));
                    return;
                }
            }
        }
    }
}

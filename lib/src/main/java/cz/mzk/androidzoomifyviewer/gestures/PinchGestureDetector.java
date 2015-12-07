package cz.mzk.androidzoomifyviewer.gestures;

import android.annotation.SuppressLint;
import android.view.MotionEvent;

import cz.mzk.androidzoomifyviewer.Logger;

public class PinchGestureDetector {

    // private static final Logger LOGGER = new Logger("GST: pinch detector");
    private static final Logger LOGGER = new Logger(PinchGestureDetector.class);

    private final OnScaleGestureListener listener;
    private double mFocusX;
    private double mFocusY;
    private double mSpan;

    public PinchGestureDetector(OnScaleGestureListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NewApi")
    public void onTouchEvent(MotionEvent event) {
        int actionMasked = event.getActionMasked();
        switch (actionMasked) {
            case MotionEvent.ACTION_CANCEL:
                // TODO
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    mSpan = computeSpan(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    mFocusX = (event.getX(0) + event.getX(1)) * 0.5;
                    mFocusY = (event.getY(0) + event.getY(1)) * 0.5;
                    listener.onScaleBegin(this);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2) {
                    listener.onScaleEnd(this);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 2) {
                    mSpan = computeSpan(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    mFocusX = (event.getX(0) + event.getX(1)) * 0.5;
                    mFocusY = (event.getY(0) + event.getY(1)) * 0.5;
                    listener.onScale(this);
                }
                break;
        }
    }

    public double getmFocusX() {
        return mFocusX;
    }

    public double getmFocusY() {
        return mFocusY;
    }

    public double getCurrentSpan() {
        return mSpan;
    }

    private double computeSpan(float firstX, float firstY, float secondX, float secondY) {
        double diffX = firstX - secondX;
        double diffY = firstY - secondY;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    public interface OnScaleGestureListener {

        boolean onScale(PinchGestureDetector detector);

        boolean onScaleBegin(PinchGestureDetector detector);

        void onScaleEnd(PinchGestureDetector detector);

    }

}

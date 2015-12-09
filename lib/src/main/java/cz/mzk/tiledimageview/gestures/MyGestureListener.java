package cz.mzk.tiledimageview.gestures;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import cz.mzk.tiledimageview.Logger;
import cz.mzk.tiledimageview.PointD;
import cz.mzk.tiledimageview.TiledImageViewApi;
import cz.mzk.tiledimageview.VectorD;
import cz.mzk.tiledimageview.dev.DevTools;
import cz.mzk.tiledimageview.gestures.PinchGestureDetector.OnScaleGestureListener;
import cz.mzk.tiledimageview.gestures.PinchZoomHandler.State;

public class MyGestureListener implements OnGestureListener, OnDoubleTapListener, OnScaleGestureListener {

    // private static final Logger LOGGER = new Logger("GST: gestures");
    private static final Logger LOGGER = new Logger(MyGestureListener.class);

    // detectors
    private final PinchGestureDetector mScaleGestureDetector;
    private final GestureDetector mGestureDetector;
    // handlers
    private final TiledImageViewApi mImageViewApi;
    private final PinchZoomHandler mPinchZoomHandler;
    private final DoubletapZoomHandler mDoubletapZoomHandler;
    private final DragShiftHandler mDragShiftHandler;
    private final FlingShiftHandler mFlingShiftHandler;

    public MyGestureListener(Context context, TiledImageViewApi imageViewApi, DevTools devTools) {
        mImageViewApi = imageViewApi;
        mGestureDetector = new GestureDetector(context, this);
        mGestureDetector.setOnDoubleTapListener(this);
        mScaleGestureDetector = new PinchGestureDetector(this);

        mPinchZoomHandler = new PinchZoomHandler(imageViewApi, devTools);
        mDoubletapZoomHandler = new DoubletapZoomHandler(imageViewApi, devTools);
        mDragShiftHandler = new DragShiftHandler(imageViewApi, devTools);
        mFlingShiftHandler = new FlingShiftHandler(imageViewApi, devTools);
    }

    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        // LOGGER.d("onSingleTapConfirmed");
        // stop possibly running DOUBLE-TAP ZOOM animation
        if (mDoubletapZoomHandler.getState() == DoubletapZoomHandler.State.ZOOMING) {
            mDoubletapZoomHandler.stopAnimation();
        }
        // stop possibly running FLING SHIFT animation
        if (mFlingShiftHandler.getmState() == FlingShiftHandler.State.SHIFTING) {
            mFlingShiftHandler.stopAnimation();
        }
        mImageViewApi.getSingleTapListener().onSingleTap(e.getX(), e.getY(), mImageViewApi.getVisibleImageAreaInCanvas());
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // LOGGER.d("onDoubleTap");
        if (mPinchZoomHandler.getmState() != State.PINCHING) {
            // stop possibly running DOUBLE-TAP ZOOM animation
            if (mDoubletapZoomHandler.getState() == DoubletapZoomHandler.State.ZOOMING) {
                mDoubletapZoomHandler.stopAnimation();
            }
            // stop possibly running FLING SHIFT animation
            if (mFlingShiftHandler.getmState() == FlingShiftHandler.State.SHIFTING) {
                mFlingShiftHandler.stopAnimation();
            }
            mDoubletapZoomHandler.startZooming(new PointD(e.getX(), e.getY()));
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // LOGGER.d("onScroll");
        if (mPinchZoomHandler.getmState() != State.PINCHING) {
            // stop possibly running DOUBLE-TAP ZOOM animation
            if (mDoubletapZoomHandler.getState() == DoubletapZoomHandler.State.ZOOMING) {
                mDoubletapZoomHandler.stopAnimation();
            }
            // stop possibly running FLING SHIFT animation
            if (mFlingShiftHandler.getmState() == FlingShiftHandler.State.SHIFTING) {
                mFlingShiftHandler.stopAnimation();
            }
            mDragShiftHandler.drag(-distanceX, -distanceY);
        }
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // LOGGER.d("onFling");
        if (mPinchZoomHandler.getmState() != State.PINCHING) {
            // stop possibly running DOUBLE-TAP ZOOM animation
            if (mDoubletapZoomHandler.getState() == DoubletapZoomHandler.State.ZOOMING) {
                mDoubletapZoomHandler.stopAnimation();
            }
            // stop possibly running FLING SHIFT animation
            if (mFlingShiftHandler.getmState() == FlingShiftHandler.State.SHIFTING) {
                mFlingShiftHandler.stopAnimation();
            }
            mFlingShiftHandler.fling(e2.getX(), e2.getY(), -velocityX, -velocityY);
        }
        return false;
    }

    @Override
    public boolean onScale(PinchGestureDetector detector) {
        // LOGGER.d("onScale");
        PointD focus = new PointD(detector.getmFocusX(), detector.getmFocusY());
        double span = detector.getCurrentSpan();
        mPinchZoomHandler.zoom(focus, span);
        return false;
    }

    @Override
    public boolean onScaleBegin(PinchGestureDetector detector) {
        // LOGGER.d("onScaleBegin");
        // stop possibly running DOUBLE-TAP ZOOM animation
        if (mDoubletapZoomHandler.getState() == DoubletapZoomHandler.State.ZOOMING) {
            mDoubletapZoomHandler.stopAnimation();
        }
        // stop possibly running FLING SHIFT animation
        if (mFlingShiftHandler.getmState() == FlingShiftHandler.State.SHIFTING) {
            mFlingShiftHandler.stopAnimation();
        }
        mPinchZoomHandler
                .startZooming(detector.getCurrentSpan(), new PointD(detector.getmFocusX(), detector.getmFocusY()));
        return false;
    }

    @Override
    public void onScaleEnd(PinchGestureDetector detector) {
        LOGGER.d("onScaleEnd");
        mPinchZoomHandler.finishZooming();
    }

    public void stopAllAnimations() {
        if (mDoubletapZoomHandler.getState() == DoubletapZoomHandler.State.ZOOMING) {
            mDoubletapZoomHandler.stopAnimation();
        }
        if (mFlingShiftHandler.getmState() == FlingShiftHandler.State.SHIFTING) {
            mFlingShiftHandler.stopAnimation();
        }
    }

    public void reset() {
        mDragShiftHandler.reset();
        mFlingShiftHandler.reset();
        mPinchZoomHandler.reset();
        mDoubletapZoomHandler.reset();
    }

    /**
     * @return Shift caused by all gestures. Accumulated shift and also active one from gesture currently in progress.
     */
    public VectorD getTotalShift() {
        VectorD swipeShift = mDragShiftHandler.getShift();
        VectorD pinchZoomShift = mPinchZoomHandler.getCurrentShift();
        VectorD doubleTapZoomShift = mDoubletapZoomHandler.getCurrentZoomShift();
        VectorD flingShift = mFlingShiftHandler.getShift();
        return VectorD.sum(swipeShift, pinchZoomShift, doubleTapZoomShift, flingShift);
    }

    /**
     * @return Scale factorecaused by all gestures. Accumulated shift and also active one from gesture currently in progress.
     */
    public double getTotalScaleFactor() {
        return mPinchZoomHandler.getCurrentScaleFactor() * mDoubletapZoomHandler.getCurrentScaleFactor();
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // LOGGER.d("onDoubleTapEvent");
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // LOGGER.d("onDown");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // LOGGER.d("onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // LOGGER.d("onSingleTapUp");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // LOGGER.d("onLongPress");
    }

}

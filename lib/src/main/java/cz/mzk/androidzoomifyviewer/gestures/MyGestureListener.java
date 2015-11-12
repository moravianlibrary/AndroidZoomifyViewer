package cz.mzk.androidzoomifyviewer.gestures;

import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.gestures.PinchGestureDetector.OnScaleGestureListener;
import cz.mzk.androidzoomifyviewer.gestures.PinchZoomHandler.State;
import cz.mzk.androidzoomifyviewer.viewer.PointD;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;
import cz.mzk.androidzoomifyviewer.viewer.VectorD;

public class MyGestureListener implements OnGestureListener, OnDoubleTapListener, OnScaleGestureListener {

    // private static final Logger logger = new Logger("GST: gestures");
    private static final Logger logger = new Logger(MyGestureListener.class);

    // detectors
    private final PinchGestureDetector scaleGestureDetector;
    private final GestureDetectorCompat gestureDetector;
    // handlers
    private final TiledImageView imageView;
    private final PinchZoomHandler pinchZoomHandler;
    private final DoubletapZoomHandler doubletapZoomHandler;
    private final DragShiftHandler dragShiftHandler;
    private final FlingShiftHandler flingShiftHandler;

    public MyGestureListener(TiledImageView imageView) {
        this.imageView = imageView;
        gestureDetector = new GestureDetectorCompat(imageView.getContext(), this);
        gestureDetector.setOnDoubleTapListener(this);
        scaleGestureDetector = new PinchGestureDetector(this);

        pinchZoomHandler = new PinchZoomHandler(imageView);
        doubletapZoomHandler = new DoubletapZoomHandler(imageView);
        dragShiftHandler = new DragShiftHandler(imageView);
        flingShiftHandler = new FlingShiftHandler(imageView);
    }

    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        // logger.d("onSingleTapConfirmed");
        // stop possibly running DOUBLE-TAP ZOOM animation
        if (doubletapZoomHandler.getState() == DoubletapZoomHandler.State.ZOOMING) {
            doubletapZoomHandler.stopAnimation();
        }
        // stop possibly running FLING SHIFT animation
        if (flingShiftHandler.getState() == FlingShiftHandler.State.SHIFTING) {
            flingShiftHandler.stopAnimation();
        }
        imageView.getSingleTapListener().onSingleTap(e.getX(), e.getY(), imageView.getVisibleImageInCanvas());
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // logger.d("onDoubleTap");
        if (pinchZoomHandler.getState() != State.PINCHING) {
            // stop possibly running DOUBLE-TAP ZOOM animation
            if (doubletapZoomHandler.getState() == DoubletapZoomHandler.State.ZOOMING) {
                doubletapZoomHandler.stopAnimation();
            }
            // stop possibly running FLING SHIFT animation
            if (flingShiftHandler.getState() == FlingShiftHandler.State.SHIFTING) {
                flingShiftHandler.stopAnimation();
            }
            doubletapZoomHandler.startZooming(new PointD(e.getX(), e.getY()));
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // logger.d("onScroll");
        if (pinchZoomHandler.getState() != State.PINCHING) {
            // stop possibly running DOUBLE-TAP ZOOM animation
            if (doubletapZoomHandler.getState() == DoubletapZoomHandler.State.ZOOMING) {
                doubletapZoomHandler.stopAnimation();
            }
            // stop possibly running FLING SHIFT animation
            if (flingShiftHandler.getState() == FlingShiftHandler.State.SHIFTING) {
                flingShiftHandler.stopAnimation();
            }
            dragShiftHandler.drag(-distanceX, -distanceY);
        }
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // logger.d("onFling");
        if (pinchZoomHandler.getState() != State.PINCHING) {
            // stop possibly running DOUBLE-TAP ZOOM animation
            if (doubletapZoomHandler.getState() == DoubletapZoomHandler.State.ZOOMING) {
                doubletapZoomHandler.stopAnimation();
            }
            // stop possibly running FLING SHIFT animation
            if (flingShiftHandler.getState() == FlingShiftHandler.State.SHIFTING) {
                flingShiftHandler.stopAnimation();
            }
            flingShiftHandler.fling(e2.getX(), e2.getY(), -velocityX, -velocityY);
        }
        return false;
    }

    @Override
    public boolean onScale(PinchGestureDetector detector) {
        // logger.d("onScale");
        PointD focus = new PointD(detector.getFocusX(), detector.getFocusY());
        double span = detector.getCurrentSpan();
        pinchZoomHandler.zoom(focus, span);
        return false;
    }

    @Override
    public boolean onScaleBegin(PinchGestureDetector detector) {
        // logger.d("onScaleBegin");
        // stop possibly running DOUBLE-TAP ZOOM animation
        if (doubletapZoomHandler.getState() == DoubletapZoomHandler.State.ZOOMING) {
            doubletapZoomHandler.stopAnimation();
        }
        // stop possibly running FLING SHIFT animation
        if (flingShiftHandler.getState() == FlingShiftHandler.State.SHIFTING) {
            flingShiftHandler.stopAnimation();
        }
        pinchZoomHandler
                .startZooming(detector.getCurrentSpan(), new PointD(detector.getFocusX(), detector.getFocusY()));
        return false;
    }

    @Override
    public void onScaleEnd(PinchGestureDetector detector) {
        logger.d("onScaleEnd");
        pinchZoomHandler.finishZooming();
    }

    public void stopAllAnimations() {
        if (doubletapZoomHandler.getState() == DoubletapZoomHandler.State.ZOOMING) {
            doubletapZoomHandler.stopAnimation();
        }
        if (flingShiftHandler.getState() == FlingShiftHandler.State.SHIFTING) {
            flingShiftHandler.stopAnimation();
        }
    }

    public void reset() {
        dragShiftHandler.reset();
        flingShiftHandler.reset();
        pinchZoomHandler.reset();
        doubletapZoomHandler.reset();
    }

    /**
     * @return Shift caused by all gestures. Accumulated shift and also active one from gesture currently in progress.
     */
    public VectorD getTotalShift() {
        VectorD swipeShift = dragShiftHandler.getShift();
        VectorD pinchZoomShift = pinchZoomHandler.getCurrentShift();
        VectorD doubleTapZoomShift = doubletapZoomHandler.getCurrentZoomShift();
        VectorD flingShift = flingShiftHandler.getShift();
        return VectorD.sum(swipeShift, pinchZoomShift, doubleTapZoomShift, flingShift);
    }

    /**
     * @return Scale factorecaused by all gestures. Accumulated shift and also active one from gesture currently in progress.
     */
    public double getTotalScaleFactor() {
        return pinchZoomHandler.getCurrentScaleFactor() * doubletapZoomHandler.getCurrentScaleFactor();
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // logger.d("onDoubleTapEvent");
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // logger.d("onDown");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // logger.d("onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // logger.d("onSingleTapUp");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // logger.d("onLongPress");
    }

}

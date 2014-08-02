package cz.mzk.androidzoomifyviewer.viewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import cz.mzk.androidzoomifyviewer.R;

/**
 * @author Martin Řehánek
 * 
 */
public class DevTools {

	private static final String TAG = DevTools.class.getSimpleName();

	private Paint mPaintBlue;
	private Paint mPaintRed;
	private Paint mPaintYellow;
	private Paint mPaintGreen;
	private Paint mPaintBlack;
	private Paint mPaintWhite;

	public DevTools(Context context) {
		initPaints(context);
	}

	private void initPaints(Context context) {
		mPaintBlue = new Paint();
		mPaintBlue.setColor(context.getResources().getColor(R.color.androidzoomifyviewer_blue));
		mPaintRed = new Paint();
		mPaintRed.setColor(context.getResources().getColor(R.color.androidzoomifyviewer_red));
		mPaintYellow = new Paint();
		mPaintYellow.setColor(context.getResources().getColor(R.color.androidzoomifyviewer_yellow));
		mPaintGreen = new Paint();
		mPaintGreen.setColor(context.getResources().getColor(R.color.androidzoomifyviewer_green));
		mPaintBlack = new Paint();
		mPaintBlack.setColor(context.getResources().getColor(R.color.androidzoomifyviewer_black));
		mPaintWhite = new Paint();
		mPaintWhite.setColor(context.getResources().getColor(R.color.androidzoomifyviewer_white));
	}

	public void drawCanvasYellow(Canvas canv) {
		Rect wholeCanvas = new Rect(0, 0, canv.getWidth(), canv.getHeight());
		canv.drawRect(wholeCanvas, mPaintYellow);
	}

	public void drawCanvasBlue(Canvas canv) {
		Rect wholeCanvas = new Rect(0, 0, canv.getWidth(), canv.getHeight());
		canv.drawRect(wholeCanvas, mPaintBlue);
	}

	public void drawWholePageRed(Canvas canv, Rect pageWhole) {
		canv.drawRect(pageWhole, mPaintRed);
	}

	public void drawPageVisiblePartGreen(Canvas canv, Rect pageVisiblePart) {
		canv.drawRect(pageVisiblePart, mPaintGreen);
	}

	public void drawPageVisiblePartCenterRed(Canvas canv, int visiblePageCenterX, int visiblePageCenterY) {
		canv.drawCircle(visiblePageCenterX, visiblePageCenterY, 10.0f, mPaintRed);
	}

	// public void drawPageVisiblePartCenterAndZoomCenter(Canvas canv, PointD
	// visiblePageCenter, PointD zoomCenter,
	// Point initialZoomCenterInCanvas) {
	// // canv.drawCircle(visiblePageCenter.x, visiblePageCenter.y, 10.0f,
	// // mPaintRed);
	// if (zoomCenter != null) {
	// canv.drawCircle((float) zoomCenter.x, (float) zoomCenter.y, 10.0f,
	// mPaintRed);
	// canv.drawLine((float) initialZoomCenterInCanvas.x, (float)
	// initialZoomCenterInCanvas.y,
	// (float) zoomCenter.x, (float) zoomCenter.y, mPaintRed);
	// }
	// if (initialZoomCenterInCanvas != null) {
	// canv.drawCircle(initialZoomCenterInCanvas.x, initialZoomCenterInCanvas.y,
	// 10.0f, mPaintGreen);
	// }
	// }

	public void drawPageCoordPoints(Canvas canv, PageCoordsPoints testPoints, double resizeFactor,
			VectorD pageShiftInCanvas) {
		drawPageCoordPoint(canv, testPoints.getCenter(), resizeFactor, pageShiftInCanvas, mPaintYellow);
		for (Point corner : testPoints.getCorners()) {
			drawPageCoordPoint(canv, corner, resizeFactor, pageShiftInCanvas, mPaintYellow);
		}
		// for (Point clickedPoint : testPoints.getClickedPoints()) {
		// drawPageCoordPoint(canv, clickedPoint, resizeFactor, totalShift,
		// mPaintWhite);
		// }
	}

	private void drawPageCoordPoint(Canvas canv, Point point, double resizeFactor, VectorD pageShiftInCanvas,
			Paint paint) {
		int resizedAndShiftedX = (int) (point.x * resizeFactor + pageShiftInCanvas.x);
		int resizedAndShiftedY = (int) (point.y * resizeFactor + pageShiftInCanvas.y);
		canv.drawCircle(resizedAndShiftedX, resizedAndShiftedY, 15f, paint);
	}

	int testCounter = 0;

	public void drawZoomCenters(Canvas canv, PointD currentZoomCenter, PointD initialZoomCenterInCanvas,
			double resizeFactor, VectorD totalShift) {
		if (currentZoomCenter != null) {
			canv.drawCircle((float) currentZoomCenter.x, (float) currentZoomCenter.y, 10.0f, mPaintRed);
			canv.drawLine((float) initialZoomCenterInCanvas.x, (float) initialZoomCenterInCanvas.y,
					(float) currentZoomCenter.x, (float) currentZoomCenter.y, mPaintRed);
		}
		if (initialZoomCenterInCanvas != null) {
			canv.drawCircle((float) initialZoomCenterInCanvas.x, (float) initialZoomCenterInCanvas.y, 10.0f,
					mPaintGreen);
		}
		testCounter++;
		VectorD diff = new VectorD(currentZoomCenter.x - initialZoomCenterInCanvas.x, currentZoomCenter.y
				- initialZoomCenterInCanvas.y);

		Log.d("z00m",
				"devTools: init: " + initialZoomCenterInCanvas.toString() + " now: " + currentZoomCenter.toString()
						+ " diff: " + diff.toString() + String.format(" r:%.3f", resizeFactor) + " s: " + totalShift
						+ " c: " + testCounter);
	}

}

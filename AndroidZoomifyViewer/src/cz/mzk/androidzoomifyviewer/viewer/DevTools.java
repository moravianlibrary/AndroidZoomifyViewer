package cz.mzk.androidzoomifyviewer.viewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
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

	public void drawWholeImageRed(Canvas canv, Rect wholeImage) {
		canv.drawRect(wholeImage, mPaintRed);
	}

	public void drawImageVisiblePartGreen(Canvas canv, Rect imageVisiblePart) {
		canv.drawRect(imageVisiblePart, mPaintGreen);
	}

	public void drawVisibleImageCenterRed(Canvas canv, int visibleImageCenterX, int visibleImageCenterY) {
		canv.drawCircle(visibleImageCenterX, visibleImageCenterY, 10.0f, mPaintRed);
	}

	public void drawImageCoordPoints(Canvas canv, ImageCoordsPoints testPoints, double resizeFactor,
			VectorD imageShiftInCanvas) {
		drawImageCoordPoint(canv, testPoints.getCenter(), resizeFactor, imageShiftInCanvas, mPaintYellow);
		for (Point corner : testPoints.getCorners()) {
			drawImageCoordPoint(canv, corner, resizeFactor, imageShiftInCanvas, mPaintYellow);
		}
	}

	private void drawImageCoordPoint(Canvas canv, Point point, double resizeFactor, VectorD imageShiftInCanvas,
			Paint paint) {
		int resizedAndShiftedX = (int) (point.x * resizeFactor + imageShiftInCanvas.x);
		int resizedAndShiftedY = (int) (point.y * resizeFactor + imageShiftInCanvas.y);
		canv.drawCircle(resizedAndShiftedX, resizedAndShiftedY, 15f, paint);
	}

	public void drawZoomCenters(Canvas canv, PointD gestureCenterInCanvas, PointD zoomCenterInImage,
			double resizeFactor, VectorD totalShift) {
		if (gestureCenterInCanvas != null && zoomCenterInImage != null) {
			PointD zoomCenterInImageInCanvasCoords = Utils.toCanvasCoords(zoomCenterInImage, resizeFactor, totalShift);
			canv.drawCircle((float) zoomCenterInImageInCanvasCoords.x, (float) zoomCenterInImageInCanvasCoords.y,
					15.0f, mPaintGreen);
			canv.drawCircle((float) gestureCenterInCanvas.x, (float) gestureCenterInCanvas.y, 12.0f, mPaintRed);
			canv.drawLine((float) zoomCenterInImageInCanvasCoords.x, (float) zoomCenterInImageInCanvasCoords.y,
					(float) gestureCenterInCanvas.x, (float) gestureCenterInCanvas.y, mPaintRed);
		}
	}

	public void highlightTile(Canvas canv, Rect rect) {
		Paint paint = mPaintBlack;
		// vertical borders
		canv.drawLine(rect.left, rect.top, rect.left, rect.bottom, paint);
		canv.drawLine(rect.right, rect.top, rect.right, rect.bottom, paint);
		// horizontal borders
		canv.drawLine(rect.left, rect.top, rect.right, rect.top, paint);
		canv.drawLine(rect.left, rect.bottom, rect.left, rect.bottom, paint);
		// diagonals
		canv.drawLine(rect.left, rect.top, rect.right, rect.bottom, paint);
		canv.drawLine(rect.right, rect.top, rect.left, rect.bottom, paint);
		// center
		int centerX = (int) (rect.left + (rect.right - rect.left) / 2.0);
		int centerY = (int) (rect.top + (double) (rect.bottom - rect.top) / 2.0);
		canv.drawCircle((float) centerX, (float) centerY, 7.0f, paint);
	}

}

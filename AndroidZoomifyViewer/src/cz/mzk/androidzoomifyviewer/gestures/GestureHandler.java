package cz.mzk.androidzoomifyviewer.gestures;

import android.graphics.Rect;
import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.viewer.DevTools;
import cz.mzk.androidzoomifyviewer.viewer.DevTools.RectWithPaint;
import cz.mzk.androidzoomifyviewer.viewer.PointD;
import cz.mzk.androidzoomifyviewer.viewer.RectD;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;
import cz.mzk.androidzoomifyviewer.viewer.Utils;
import cz.mzk.androidzoomifyviewer.viewer.VectorD;

public class GestureHandler {

	private static final Logger logger = new Logger(GestureHandler.class);

	final TiledImageView imageView;

	public GestureHandler(TiledImageView imageView) {
		this.imageView = imageView;
	}

	protected VectorD limitNewShift(VectorD newShift) {
		double limitedLocalX = newShift.x;
		double limitedLocalY = newShift.y;
		double scaleFactor = imageView.getTotalScaleFactor();

		RectD paddingRectImg = new RectD(0.0, 0.0, imageView.getCanvasImagePaddingHorizontal(),
				imageView.getCanvasImagePaddingVertical());
		Rect imageAreaInCanvasWithoutAnyShift = imageView.computeImageAreaInCanvas(scaleFactor, VectorD.ZERO_VECTOR);
		RectD paddingRectCanv = convertToPaddingInCanvas(paddingRectImg, imageAreaInCanvasWithoutAnyShift);

		VectorD totalShift = imageView.getTotalShift();
		VectorD totalPlusNewShift = totalShift.plus(newShift);
		Rect imageAreaInCanvasWithNewShift = imageView.computeImageAreaInCanvas(scaleFactor, totalPlusNewShift);

		// horizontal
		double extraSpaceHorizontalCanv = paddingRectCanv.height();
		double maxTop = extraSpaceHorizontalCanv;
		double minBottom = imageView.getHeight() - extraSpaceHorizontalCanv;
		if (imageAreaInCanvasWithNewShift.top > maxTop) {
			double limitedGlobalY = maxTop;
			limitedLocalY = limitedGlobalY - totalShift.y;
		} else if (imageAreaInCanvasWithNewShift.bottom < minBottom) {
			double limitedGlobalY = minBottom - imageAreaInCanvasWithoutAnyShift.bottom;
			limitedLocalY = limitedGlobalY - totalShift.y;
		}

		// vertical
		double extraSpaceVerticalCanv = paddingRectCanv.width();
		double maxLeft = extraSpaceVerticalCanv;
		double minRight = imageView.getWidth() - extraSpaceVerticalCanv;
		if (imageAreaInCanvasWithNewShift.left > maxLeft) {
			double limitedGlobalX = maxLeft;
			limitedLocalX = limitedGlobalX - totalShift.x;
		} else if (imageAreaInCanvasWithNewShift.right < minRight) {
			double limitedGlobalX = minRight - imageAreaInCanvasWithoutAnyShift.right;
			limitedLocalX = limitedGlobalX - totalShift.x;
		}

		if (TiledImageView.DEV_MODE) {
			devVisualisePaddingArea(paddingRectCanv);
		}
		return new VectorD(limitedLocalX, limitedLocalY);
	}

	private RectD convertToPaddingInCanvas(RectD paddingRectImg, Rect imageAreaInCanvasWithoutAnyShift) {
		PointD rightBottomImg = new PointD(paddingRectImg.right, paddingRectImg.bottom);
		PointD rightBottomCanv = Utils.toCanvasCoords(rightBottomImg, imageView.getMinScaleFactor(),
				VectorD.ZERO_VECTOR);
		double width = rightBottomCanv.x;
		double height = rightBottomCanv.y;

		if (width != 0) {
			if (imageAreaInCanvasWithoutAnyShift.width() >= imageView.getWidth()) {
				width = 0;
			} else {
				double min = (imageView.getWidth() - imageAreaInCanvasWithoutAnyShift.width()) * 0.5;
				width = Math.min(width, min);
			}
		}

		if (height != 0) {
			if (imageAreaInCanvasWithoutAnyShift.height() >= imageView.getHeight()) {
				height = 0;
			} else {
				double min = (imageView.getHeight() - imageAreaInCanvasWithoutAnyShift.height()) * 0.5;
				height = Math.min(height, min);
			}
		}

		return new RectD(0, 0, width, height);
	}

	private void devVisualisePaddingArea(RectD paddingRectCanv) {
		DevTools devTools = imageView.getDevTools();
		if (devTools != null) {
			Rect paddingRectangleVisualisation = paddingVisualisation(paddingRectCanv);
			devTools.addToRectStack(new RectWithPaint(paddingRectangleVisualisation, devTools.getPaintGreen()));
		}
	}

	private Rect paddingVisualisation(RectD paddingRectCanv) {
		int x = (int) paddingRectCanv.width();
		int y = (int) paddingRectCanv.height();
		if (x == 0) {
			x = 10;
		}
		if (y == 0) {
			y = 10;
		}
		return new Rect(0, 0, x, y);
	}

}

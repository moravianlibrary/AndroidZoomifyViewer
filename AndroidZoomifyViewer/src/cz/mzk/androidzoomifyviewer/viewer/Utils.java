package cz.mzk.androidzoomifyviewer.viewer;

import android.content.res.Resources;
import android.graphics.Rect;

/**
 * @author Martin Řehánek
 * 
 */
public class Utils {

	private static final String TAG = Utils.class.getSimpleName();

	public static String toString(Rect rect, String unit) {
		StringBuilder builder = new StringBuilder();
		builder.append("horizontal: ").append(rect.left).append("-").append(rect.right).append(" (")
				.append(rect.width()).append(' ').append(unit).append(')');
		builder.append(", ");
		builder.append("vertical: ").append(rect.top).append("-").append(rect.bottom).append(" (")
				.append(rect.height()).append(' ').append(unit).append(')');
		return builder.toString();
	}

	public static PointD toImageCoords(PointD canvasCoords, double imageToCanvasScaleFactor, VectorD imageShiftInCanvas) {
		double imageX = (canvasCoords.x - imageShiftInCanvas.x) / imageToCanvasScaleFactor;
		double imageY = (canvasCoords.y - imageShiftInCanvas.y) / imageToCanvasScaleFactor;
		return new PointD(imageX, imageY);
	}

	public static PointD toCanvasCoords(PointD inImageCoords, double imageScaleFactor, VectorD imageShiftInCanvas) {
		double canvasX = (inImageCoords.x * imageScaleFactor + imageShiftInCanvas.x);
		double canvasY = (inImageCoords.y * imageScaleFactor + imageShiftInCanvas.y);
		PointD result = new PointD(canvasX, canvasY);
		// Log.d(TAG,
		// inImageCoords.toString() + "->" + result.toString() +
		// String.format(" SCALE: %.4f", imageScaleFactor)
		// + " shift: " + imageShiftInCanvas);
		return result;
	}

	public static PointD computeShift(PointD inImageCoords, PointD inCanvasCoords, double imageToCanvasScaleFactor) {
		double shiftX = inCanvasCoords.x - inImageCoords.x * imageToCanvasScaleFactor;
		double shiftY = inCanvasCoords.y - inImageCoords.y * imageToCanvasScaleFactor;
		return new PointD(shiftX, shiftY);
	}

	public static double computeShiftX(double xInImageCoords, double xInCanvasCoords, double imageToCanvasScaleFactor) {
		return xInCanvasCoords - xInImageCoords * imageToCanvasScaleFactor;
	}
	
	public static double computeShiftY(double yInImageCoords, double yInCanvasCoords, double imageToCanvasScaleFactor) {
		return yInCanvasCoords - yInImageCoords * imageToCanvasScaleFactor;
	}

	public static double toImageX(double canvasX, double imageToCanvasResizeFactor, double imageShiftInCanvasX) {
		return (canvasX - imageShiftInCanvasX) / imageToCanvasResizeFactor;
	}

	public static double toImageY(double canvasY, double imageToCanvasResizeFactor, double imageShiftInCanvasY) {
		return (canvasY - imageShiftInCanvasY) / imageToCanvasResizeFactor;
	}

	public static String toString(Rect rect) {
		return toString(rect, "px");
	}

	public static int dpToPx(int dp) {
		return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
	}

	public static int pxToDp(int px) {
		return (int) (px / Resources.getSystem().getDisplayMetrics().density);
	}

}

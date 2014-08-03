package cz.mzk.androidzoomifyviewer.viewer;

import android.graphics.Rect;
import android.util.Log;

/**
 * @author Martin Řehánek
 * 
 */
public class Utils {

	public static String toString(Rect rect, String unit) {
		StringBuilder builder = new StringBuilder();
		builder.append("horizontal: ").append(rect.left).append("-").append(rect.right).append(" (")
				.append(rect.width()).append(' ').append(unit).append(')');
		builder.append(", ");
		builder.append("vertical: ").append(rect.top).append("-").append(rect.bottom).append(" (")
				.append(rect.height()).append(' ').append(unit).append(')');
		return builder.toString();
	}

	public static String toString(Rect rect) {
		return toString(rect, "px");
	}

	public static String toString(float[] vector) {
		return "[" + vector[0] + ";" + vector[1] + "]";
	}

	public static String toString(PointD point) {
		return "[" + point.x + ";" + point.y + "]";
	}

	public static String toString(Vector vector) {
		return "[" + vector.x + ";" + vector.y + "]";
	}

	public static String toString(Point point) {
		return "[" + point.x + ";" + point.y + "]";
	}

	public static PointD toImageCoords(double canvasX, double canvasY, double imageToCanvasResizeFactor,
			VectorD imageShiftInCanvas) {
		double imageX = (canvasX - imageShiftInCanvas.x) / imageToCanvasResizeFactor;
		double imageY = (canvasY - imageShiftInCanvas.y) / imageToCanvasResizeFactor;
		return new PointD(imageX, imageY);
	}

	public static double toImageX(double canvasX, double imageToCanvasResizeFactor, double imageShiftInCanvasX) {
		return (canvasX - imageShiftInCanvasX) / imageToCanvasResizeFactor;
	}

	public static double toImageY(double canvasY, double imageToCanvasResizeFactor, double imageShiftInCanvasY) {
		return (canvasY - imageShiftInCanvasY) / imageToCanvasResizeFactor;
	}

	public static PointD toCanvasCoords(PointD inImageCoords, double imageToCanvasResizeFactor, VectorD imageShiftInCanvas) {
		double canvasX = ((inImageCoords.x * imageToCanvasResizeFactor) + imageShiftInCanvas.x);
		double canvasY = ((inImageCoords.y * imageToCanvasResizeFactor) + imageShiftInCanvas.y);
		PointD result = new PointD(canvasX, canvasY);
		Log.d("z00m",
				"utils: " + inImageCoords.toString() + "->" + result.toString()
						+ String.format(" resize: %.4f", imageToCanvasResizeFactor) + " shift: "
						+ imageShiftInCanvas.toString());
		return result;

	}

	public static String toString(int[] vector) {
		return "[" + vector[0] + ";" + vector[1] + "]";
	}

}

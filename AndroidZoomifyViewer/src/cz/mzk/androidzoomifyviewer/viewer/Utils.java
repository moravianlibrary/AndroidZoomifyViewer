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

	public static PointD toPageCoords(double canvasX, double canvasY, double pageToCanvasResizeFactor,
			VectorD pageShiftInCanvas) {
		double pageX = (canvasX - pageShiftInCanvas.x) / pageToCanvasResizeFactor;
		double pageY = (canvasY - pageShiftInCanvas.y) / pageToCanvasResizeFactor;
		return new PointD(pageX, pageY);
	}

	public static double toPageX(double canvasX, double pageToCanvasResizeFactor, double pageShiftInCanvasX) {
		return (canvasX - pageShiftInCanvasX) / pageToCanvasResizeFactor;
	}

	public static double toPageY(double canvasY, double pageToCanvasResizeFactor, double pageShiftInCanvasY) {
		return (canvasY - pageShiftInCanvasY) / pageToCanvasResizeFactor;
	}

	public static PointD toCanvasCoords(PointD inPageCoords, double pageToCanvasResizeFactor, VectorD pageShiftInCanvas) {
		double canvasX = ((inPageCoords.x * pageToCanvasResizeFactor) + pageShiftInCanvas.x);
		double canvasY = ((inPageCoords.y * pageToCanvasResizeFactor) + pageShiftInCanvas.y);
		PointD result = new PointD(canvasX, canvasY);
		Log.d("z00m",
				"utils: " + inPageCoords.toString() + "->" + result.toString()
						+ String.format(" resize: %.4f", pageToCanvasResizeFactor) + " shift: "
						+ pageShiftInCanvas.toString());
		return result;

	}

	public static String toString(int[] vector) {
		return "[" + vector[0] + ";" + vector[1] + "]";
	}

}

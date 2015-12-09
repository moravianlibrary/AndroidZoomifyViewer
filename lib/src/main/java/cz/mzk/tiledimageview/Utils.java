package cz.mzk.tiledimageview;

import android.content.res.Resources;
import android.graphics.Rect;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * @author Martin Řehánek
 */
public class Utils {

    private static final Logger LOGGER = new Logger(Utils.class);

    //SHITFT & SCALE COMPUTATIONS

    //shift

    // TODO: 3.12.15 actually use
    public static PointD computeShift(PointD pointInImageCoords, PointD pointInCanvasCoords, double imageToCanvasScaleFactor) {
        double shiftX = pointInCanvasCoords.x - pointInImageCoords.x * imageToCanvasScaleFactor;
        double shiftY = pointInCanvasCoords.y - pointInImageCoords.y * imageToCanvasScaleFactor;
        return new PointD(shiftX, shiftY);
    }

    // TODO: 3.12.15 actually use
    public static double computeShiftX(double xInImageCoords, double xInCanvasCoords, double imageToCanvasScaleFactor) {
        return xInCanvasCoords - xInImageCoords * imageToCanvasScaleFactor;
    }

    // TODO: 3.12.15 actually use
    public static double computeShiftY(double yInImageCoords, double yInCanvasCoords, double imageToCanvasScaleFactor) {
        return yInCanvasCoords - yInImageCoords * imageToCanvasScaleFactor;
    }

    //scale
    // TODO: 3.12.15


    //COORDINATES TRANSFORMATIONS (IMAGE <-> CANVAS)

    //single dimension

    public static double toXInImageCoords(double xInCanvasCoords, double imageToCanvasScaleFactor, double imageShiftInCanvasX) {
        return (xInCanvasCoords - imageShiftInCanvasX) / imageToCanvasScaleFactor;
    }

    public static double toYInImageCoords(double yInCanvasCoords, double imageToCanvasScaleFactor, double imageShiftInCanvasY) {
        return (yInCanvasCoords - imageShiftInCanvasY) / imageToCanvasScaleFactor;
    }

    public static double toXInCanvasCoords(double xInImageCoords, double imageToCanvasScaleFactor, double imageShiftInCanvasX) {
        return xInImageCoords * imageToCanvasScaleFactor + imageShiftInCanvasX;
    }

    public static double toYInCanvasCoords(double yInImageCoords, double imageToCanvasScaleFactor, double imageShiftInCanvasY) {
        return yInImageCoords * imageToCanvasScaleFactor + imageShiftInCanvasY;
    }

    //point

    public static PointD toImageCoords(PointD pointInCanvasCoords, double imageToCanvasScaleFactor, VectorD imageShiftInCanvas) {
        double imageX = (pointInCanvasCoords.x - imageShiftInCanvas.x) / imageToCanvasScaleFactor;
        double imageY = (pointInCanvasCoords.y - imageShiftInCanvas.y) / imageToCanvasScaleFactor;
        return new PointD(imageX, imageY);
    }

    public static PointD toCanvasCoords(PointD pointInImageCoords, double imageToCanvasScaleFactor, VectorD imageShiftInCanvas) {
        double canvasX = pointInImageCoords.x * imageToCanvasScaleFactor + imageShiftInCanvas.x;
        double canvasY = pointInImageCoords.y * imageToCanvasScaleFactor + imageShiftInCanvas.y;
        PointD result = new PointD(canvasX, canvasY);
        return result;
    }

    //rectangle

    public static Rect toImageCoords(Rect rectInCanvasCoords, double imageToCanvasScaleFactor, VectorD imageShiftInCanvas) {
        double left = (rectInCanvasCoords.left - imageShiftInCanvas.x) / imageToCanvasScaleFactor;
        double top = (rectInCanvasCoords.top - imageShiftInCanvas.y) / imageToCanvasScaleFactor;
        double right = (rectInCanvasCoords.right - imageShiftInCanvas.x) / imageToCanvasScaleFactor;
        double bottom = (rectInCanvasCoords.bottom - imageShiftInCanvas.y) / imageToCanvasScaleFactor;
        return new Rect((int) left, (int) top, (int) right, (int) bottom);
    }

    public static Rect toCanvasCoords(Rect rectInImageCoords, double imageToCanvasScaleFactor, VectorD imageShiftInCanvas) {
        double left = rectInImageCoords.left * imageToCanvasScaleFactor + imageShiftInCanvas.x;
        double top = rectInImageCoords.top * imageToCanvasScaleFactor + imageShiftInCanvas.y;
        double right = rectInImageCoords.right * imageToCanvasScaleFactor + imageShiftInCanvas.x;
        double bottom = rectInImageCoords.bottom * imageToCanvasScaleFactor + imageShiftInCanvas.y;
        return new Rect((int) left, (int) top, (int) right, (int) bottom);
    }

    //DP <-> PX CONVERSIONS

    public static String toString(Rect rect) {
        return toString(rect, "px");
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static double dpToPx(double dp) {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static double pxToDp(double px) {
        return px / Resources.getSystem().getDisplayMetrics().density;
    }

    //OTHER CALCULATIONS

    /**
     * Should not be used for big number, use Math.pow(double, double) instead.
     *
     * @param x
     * @param power
     * @return
     */
    public static int pow(int x, int power) {
        int result = 1;
        for (int i = 1; i <= power; i++) {
            result *= x;
        }
        return result;
    }

    /**
     * General base logarithm.
     *
     * @param x
     * @param base
     * @return
     */
    public static double logarithm(double x, double base) {
        return Math.log(x) / Math.log(base);
    }

    /**
     * Rounds float to given decimals.
     *
     * @param d
     * @param decimals
     * @return
     */
    public static float round(float d, int decimals) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimals, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    //STRING builders

    public static String formatBytes(long cacheSizeBytes) {
        if (cacheSizeBytes < 1024) {
            return String.format(Locale.US, "%d B", cacheSizeBytes);
        } else if (cacheSizeBytes < 1024 * 1024) {
            long kB = cacheSizeBytes / 1024;
            return String.format(Locale.US, "%d kB", kB);
        } else if (cacheSizeBytes < 1024 * 1024 * 1024) {
            long MB = cacheSizeBytes / (1024 * 1024);
            return String.format(Locale.US, "%d MB", MB);
        } else {
            long GB = cacheSizeBytes / (1024 * 1024 * 1024);
            return String.format(Locale.US, "%d GB", GB);
        }
    }

    public static String toString(Rect rect, String unit) {
        StringBuilder builder = new StringBuilder();
        builder.append("horizontal: ").append(rect.left).append("-").append(rect.right).append(" (")
                .append(rect.width()).append(' ').append(unit).append(')');
        builder.append(", ");
        builder.append("vertical: ").append(rect.top).append("-").append(rect.bottom).append(" (")
                .append(rect.height()).append(' ').append(unit).append(')');
        return builder.toString();
    }

    public static String coordsToString(int[] coords) {
        return "[" + coords[0] + ',' + coords[1] + ']';
    }

    /**
     * @param x
     * @param min start of interval
     * @param max end of interval
     * @return x if x is in <min;max>, min if x < min, or max if x > max
     */
    public static int collapseToInterval(int x, int min, int max) {
        if (x < min) {
            return min;
        } else if (x > max) {
            return max;
        } else {
            return x;
        }
    }


}

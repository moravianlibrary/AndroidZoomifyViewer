package cz.mzk.androidzoomifyviewer.viewer;

import java.util.Locale;

/**
 * @author Martin Řehánek
 */
public class RectD {

    public final double left;
    public final double top;
    public final double right;
    public final double bottom;

    public RectD(double left, double top, double right, double bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public double height() {
        return bottom - top;
    }

    public double width() {
        return right - left;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "[%.3f;%.3f - %.3f;%.3f]", left, top, right, bottom);
    }

}

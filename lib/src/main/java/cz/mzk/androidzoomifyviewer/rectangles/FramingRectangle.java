package cz.mzk.androidzoomifyviewer.rectangles;

import android.content.Context;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

import cz.mzk.androidzoomifyviewer.R;

/**
 * Created by Martin Řehánek on 12.11.15.
 */
public class FramingRectangle {

    private final Rect rect;
    private final Border border;
    private final Integer fillColorRes;

    /**
     * @param rect            Rectangle.
     * @param border          Border or null if border should not be drawn.
     * @param fillingColorRes Filling color resource id (e.g. R.colors.black) or null if filling shouldn't be drawn.
     */
    public FramingRectangle(Rect rect, Border border, Integer fillingColorRes) {
        this.rect = rect;
        this.border = border;
        this.fillColorRes = fillingColorRes;
    }


    /**
     * @param left            Rectangle left coordinate.
     * @param top             Rectangle top coordinate.
     * @param right           Rectangle right coordinate.
     * @param bottom          Rectangle bottom coordinate.
     * @param border          Border or null if border should not be drawn.
     * @param fillingColorRes Filling color resource id (e.g. R.colors.black) or null if filling shouldn't be drawn.
     */
    public FramingRectangle(int left, int top, int right, int bottom, Border border, Integer fillingColorRes) {
        this(new Rect(left, top, right, bottom), border, fillingColorRes);
    }

    public static List<FramingRectangle> getTestRectangles(Context context) {
        List<FramingRectangle> result = new ArrayList<>();
        result.add(new FramingRectangle(500, 1000, 3000, 5000, new Border(R.color.androidzoomifyviewer_black_trans, 3), R.color.androidzoomifyviewer_blue_trans));
        result.add(new FramingRectangle(3300, 5500, 4000, 6000, new Border(R.color.androidzoomifyviewer_blue_trans, 5), R.color.androidzoomifyviewer_green_trans));
        result.add(new FramingRectangle(2500, 300, 3500, 500, new Border(R.color.androidzoomifyviewer_blue, 2), R.color.androidzoomifyviewer_blue_trans));
        //search
        result.add(new FramingRectangle(1111, 775, 1204, 821, new Border(R.color.androidzoomifyviewer_blue, 1), R.color.androidzoomifyviewer_blue_trans));
        result.add(new FramingRectangle(1120, 1293, 1216, 1340, new Border(R.color.androidzoomifyviewer_blue, 1), R.color.androidzoomifyviewer_blue_trans));
        return result;
    }

    public Rect getRect() {
        return rect;
    }

    public Border getBorder() {
        return border;
    }

    public Integer getFillColorRes() {
        return fillColorRes;
    }

    public static class Border {
        private final int colorRes;
        private final int thicknessDp;

        /**
         * @param colorRes    Color resource id. E.g. R.colors.black.
         * @param thicknessDp Thickness in density-independent pixels.
         */
        public Border(int colorRes, int thicknessDp) {
            this.colorRes = colorRes;
            this.thicknessDp = thicknessDp;
        }

        /**
         * @return Color resource id. E.g. R.colors.black.
         */
        public int getColorRes() {
            return colorRes;
        }

        /**
         * @return Thickness in density-independent pixels.
         */
        public int getThicknessDp() {
            return thicknessDp;
        }
    }


}

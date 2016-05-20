package cz.mzk.tiledimageview.gestures;

import android.graphics.Rect;

import cz.mzk.tiledimageview.Logger;
import cz.mzk.tiledimageview.PointD;
import cz.mzk.tiledimageview.TiledImageView;
import cz.mzk.tiledimageview.TiledImageViewApi;
import cz.mzk.tiledimageview.Utils;
import cz.mzk.tiledimageview.VectorD;
import cz.mzk.tiledimageview.dev.DevTools;
import cz.mzk.tiledimageview.dev.DevTools.RectWithPaint;

public class GestureHandler {

    private static final Logger LOGGER = new Logger(GestureHandler.class);

    protected final TiledImageViewApi mImageViewApi;
    protected final DevTools mDevTools;

    public GestureHandler(TiledImageViewApi imageView, DevTools devTools) {
        mImageViewApi = imageView;
        mDevTools = devTools;
    }

    protected VectorD limitNewShift(VectorD newShift) {
        double limitedLocalX = newShift.x;
        double limitedLocalY = newShift.y;
        double scaleFactor = mImageViewApi.getTotalScaleFactor();

        RectD paddingRectImg = new RectD(0.0, 0.0, mImageViewApi.getCanvasImagePaddingHorizontal(),
                mImageViewApi.getCanvasImagePaddingVertical());

        Rect imageAreaInCanvasWithoutAnyShift = computeWholeImageAreaInCanvasCoords(scaleFactor, VectorD.ZERO_VECTOR);
        RectD paddingRectCanv = convertToPaddingInCanvas(paddingRectImg, imageAreaInCanvasWithoutAnyShift);

        VectorD totalShift = mImageViewApi.getTotalShift();
        VectorD totalPlusNewShift = totalShift.plus(newShift);
        Rect imageAreaInCanvasWithNewShift = computeWholeImageAreaInCanvasCoords(scaleFactor, totalPlusNewShift);

        // horizontal
        double extraSpaceHorizontalCanv = paddingRectCanv.height();
        double maxTop = extraSpaceHorizontalCanv;
        double minBottom = mImageViewApi.getHeight() - extraSpaceHorizontalCanv;
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
        double minRight = mImageViewApi.getWidth() - extraSpaceVerticalCanv;
        if (imageAreaInCanvasWithNewShift.left > maxLeft) {
            double limitedGlobalX = maxLeft;
            limitedLocalX = limitedGlobalX - totalShift.x;
        } else if (imageAreaInCanvasWithNewShift.right < minRight) {
            double limitedGlobalX = minRight - imageAreaInCanvasWithoutAnyShift.right;
            limitedLocalX = limitedGlobalX - totalShift.x;
        }

        if (mDevTools != null) {
            devVisualisePaddingArea(paddingRectCanv);
        }
        return new VectorD(limitedLocalX, limitedLocalY);
    }

    private RectD convertToPaddingInCanvas(RectD paddingRectImg, Rect imageAreaInCanvasWithoutAnyShift) {
        PointD rightBottomImg = new PointD(paddingRectImg.right, paddingRectImg.bottom);
        PointD rightBottomCanv = Utils.toCanvasCoords(rightBottomImg, mImageViewApi.getMinScaleFactor(),
                VectorD.ZERO_VECTOR);
        double width = rightBottomCanv.x;
        double height = rightBottomCanv.y;

        if (width != 0) {
            if (imageAreaInCanvasWithoutAnyShift.width() >= mImageViewApi.getWidth()) {
                width = 0;
            } else {
                double min = (mImageViewApi.getWidth() - imageAreaInCanvasWithoutAnyShift.width()) * 0.5;
                width = Math.min(width, min);
            }
        }

        if (height != 0) {
            if (imageAreaInCanvasWithoutAnyShift.height() >= mImageViewApi.getHeight()) {
                height = 0;
            } else {
                double min = (mImageViewApi.getHeight() - imageAreaInCanvasWithoutAnyShift.height()) * 0.5;
                height = Math.min(height, min);
            }
        }

        return new RectD(0, 0, width, height);
    }

    private void devVisualisePaddingArea(RectD paddingRectCanv) {
        if (mDevTools != null) {
            Rect paddingRectangleVisualisation = paddingVisualisation(paddingRectCanv);
            mDevTools.addToRectStack(new RectWithPaint(paddingRectangleVisualisation, mDevTools.getPaintGreen()));
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

    Rect computeWholeImageAreaInCanvasCoords(double scaleFactor, VectorD shift) {
        Rect imgArea = new Rect(0, 0, mImageViewApi.getImageWidth(), mImageViewApi.getImageHeight());
        return Utils.toCanvasCoords(imgArea, scaleFactor, shift);
    }

}

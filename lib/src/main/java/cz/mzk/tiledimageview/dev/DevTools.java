package cz.mzk.tiledimageview.dev;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

import cz.mzk.tiledimageview.Logger;
import cz.mzk.tiledimageview.Point;
import cz.mzk.tiledimageview.PointD;
import cz.mzk.tiledimageview.R;
import cz.mzk.tiledimageview.Utils;
import cz.mzk.tiledimageview.VectorD;

/**
 * @author Martin Řehánek
 */
public class DevTools {

    private static final Logger LOGGER = new Logger(DevTools.class);

    // colors
    private final Paint paintBlue = new Paint();
    private final Paint paintRed = new Paint();
    private final Paint paintYellow = new Paint();
    private final Paint paintGreen = new Paint();
    private final Paint paintBlack = new Paint();
    private final Paint paintWhite = new Paint();
    // transparent colors
    private final Paint paintRedTrans = new Paint();
    private final Paint paintWhiteTrans = new Paint();
    private final Paint paintYellowTrans = new Paint();
    private final Paint paintBlackTrans = new Paint();
    private final Paint paintGreenTrans = new Paint();
    private final Paint paintBlueTrans = new Paint();
    private final List<RectWithPaint> rectStackAfterPrimaryDraws = new ArrayList<RectWithPaint>();
    private Canvas mCanv;
    private PointD pinchZoomCenterInCanvas;
    private PointD pinchZoomCenterInImage;
    private PointD doubletapZoomCenterInCanvas;
    private PointD doubletapZoomCenterInImage;

    public DevTools(Context context) {
        // initImageMetadata paints
        paintBlue.setColor(context.getResources().getColor(R.color.tiledimageview_blue));
        paintRed.setColor(context.getResources().getColor(R.color.tiledimageview_red));
        paintYellow.setColor(context.getResources().getColor(R.color.tiledimageview_yellow));
        paintGreen.setColor(context.getResources().getColor(R.color.tiledimageview_green));
        paintBlack.setColor(context.getResources().getColor(R.color.tiledimageview_black));
        paintWhite.setColor(context.getResources().getColor(R.color.tiledimageview_white));
        // transparent
        paintWhiteTrans.setColor(context.getResources().getColor(R.color.tiledimageview_white_trans));
        paintRedTrans.setColor(context.getResources().getColor(R.color.tiledimageview_red_trans));
        paintYellowTrans.setColor(context.getResources().getColor(R.color.tiledimageview_yellow_trans));
        paintBlackTrans.setColor(context.getResources().getColor(R.color.tiledimageview_black_trans));
        paintGreenTrans.setColor(context.getResources().getColor(R.color.tiledimageview_green_trans));
        paintBlueTrans.setColor(context.getResources().getColor(R.color.tiledimageview_blue_trans));
    }

    public void setCanvas(Canvas canv) {
        this.mCanv = canv;
    }

    public void fillWholeCanvasWithColor(Paint paint) {
        Rect wholeCanvas = new Rect(0, 0, mCanv.getWidth(), mCanv.getHeight());
        mCanv.drawRect(wholeCanvas, paint);
    }

    public void fillRectAreaWithColor(Rect rect, Paint paint) {
        mCanv.drawRect(rect, paint);
    }

    public void drawPoint(PointD pointInCanvas, Paint paint, float size) {
        mCanv.drawCircle((float) pointInCanvas.x, (float) pointInCanvas.y, size, paint);
    }

    public void drawImageCoordPoints(DevPoints testPoints, double resizeFactor, VectorD imageShiftInCanvas) {
        drawImageCoordPoint(testPoints.getCenter(), resizeFactor, imageShiftInCanvas, paintRedTrans);
        for (Point corner : testPoints.getCorners()) {
            drawImageCoordPoint(corner, resizeFactor, imageShiftInCanvas, paintRedTrans);
        }
    }

    private void drawImageCoordPoint(Point point, double resizeFactor, VectorD imageShiftInCanvas, Paint paint) {
        int resizedAndShiftedX = (int) (point.x * resizeFactor + imageShiftInCanvas.x);
        int resizedAndShiftedY = (int) (point.y * resizeFactor + imageShiftInCanvas.y);
        mCanv.drawCircle(resizedAndShiftedX, resizedAndShiftedY, 30f, paint);
    }

    public void setDoubletapZoomCenters(PointD zoomCenterInCanvas, PointD zoomCenterInImage) {
        this.doubletapZoomCenterInCanvas = zoomCenterInCanvas;
        this.doubletapZoomCenterInImage = zoomCenterInImage;
    }

    public void setPinchZoomCenters(PointD zoomCenterInCanvas, PointD zoomCenterInImage) {
        this.pinchZoomCenterInCanvas = zoomCenterInCanvas;
        this.pinchZoomCenterInImage = zoomCenterInImage;
    }

    public void drawDoubletapZoomCenters(double resizeFactor, VectorD totalShift) {
        if (doubletapZoomCenterInCanvas != null && doubletapZoomCenterInImage != null) {
            PointD zoomCenterInImageInCanvasCoords = Utils.toCanvasCoords(doubletapZoomCenterInImage, resizeFactor,
                    totalShift);
            mCanv.drawLine((float) zoomCenterInImageInCanvasCoords.x, (float) zoomCenterInImageInCanvasCoords.y,
                    (float) doubletapZoomCenterInCanvas.x, (float) doubletapZoomCenterInCanvas.y, paintGreenTrans);
            mCanv.drawCircle((float) zoomCenterInImageInCanvasCoords.x, (float) zoomCenterInImageInCanvasCoords.y,
                    15.0f, paintYellow);
            mCanv.drawCircle((float) doubletapZoomCenterInCanvas.x, (float) doubletapZoomCenterInCanvas.y, 12.0f,
                    paintGreen);
        }
    }

    public void drawPinchZoomCenters(double resizeFactor, VectorD totalShift) {
        if (pinchZoomCenterInCanvas != null && pinchZoomCenterInImage != null) {
            PointD zoomCenterInImageInCanvasCoords = Utils.toCanvasCoords(pinchZoomCenterInImage, resizeFactor,
                    totalShift);
            mCanv.drawLine((float) zoomCenterInImageInCanvasCoords.x, (float) zoomCenterInImageInCanvasCoords.y,
                    (float) pinchZoomCenterInCanvas.x, (float) pinchZoomCenterInCanvas.y, paintBlueTrans);
            mCanv.drawCircle((float) zoomCenterInImageInCanvasCoords.x, (float) zoomCenterInImageInCanvasCoords.y,
                    15.0f, paintYellow);
            mCanv.drawCircle((float) pinchZoomCenterInCanvas.x, (float) pinchZoomCenterInCanvas.y, 12.0f, paintBlue);
        }
    }

    public void highlightTile(Rect rect, Paint paint) {
        // vertical borders
        mCanv.drawLine(rect.left, rect.top, rect.left, rect.bottom, paint);
        mCanv.drawLine(rect.right, rect.top, rect.right, rect.bottom, paint);
        // horizontal borders
        mCanv.drawLine(rect.left, rect.top, rect.right, rect.top, paint);
        mCanv.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, paint);
        // diagonals
        mCanv.drawLine(rect.left, rect.top, rect.right, rect.bottom, paint);
        mCanv.drawLine(rect.right, rect.top, rect.left, rect.bottom, paint);
        // center
        int centerX = (int) (rect.left + (rect.right - rect.left) / 2.0);
        int centerY = (int) (rect.top + (double) (rect.bottom - rect.top) / 2.0);
        mCanv.drawCircle((float) centerX, (float) centerY, 7.0f, paint);
    }

    public Paint getPaintBlue() {
        return paintBlue;
    }

    public Paint getPaintRed() {
        return paintRed;
    }

    public Paint getPaintYellow() {
        return paintYellow;
    }

    public Paint getPaintGreen() {
        return paintGreen;
    }

    public Paint getPaintBlack() {
        return paintBlack;
    }

    public Paint getPaintWhite() {
        return paintWhite;
    }

    public Paint getPaintWhiteTrans() {
        return paintWhiteTrans;
    }

    public Paint getPaintRedTrans() {
        return paintRedTrans;
    }

    public Paint getPaintYellowTrans() {
        return paintYellowTrans;
    }

    public Paint getPaintBlackTrans() {
        return paintBlackTrans;
    }

    public Paint getPaintGreenTrans() {
        return paintGreenTrans;
    }

    public Paint getPaintBlueTrans() {
        return paintBlueTrans;
    }

    public void clearRectStack() {
        rectStackAfterPrimaryDraws.clear();
    }

    public void addToRectStack(RectWithPaint rect) {
        rectStackAfterPrimaryDraws.add(rect);
    }

    public void drawTileRectStack() {
        for (RectWithPaint rect : rectStackAfterPrimaryDraws) {
            fillRectAreaWithColor(rect.getRect(), rect.getPaint());
        }
    }

    public static class RectWithPaint {
        private final Rect rect;
        private final Paint paint;

        public RectWithPaint(Rect rect, Paint paint) {
            super();
            this.rect = rect;
            this.paint = paint;
        }

        public Rect getRect() {
            return rect;
        }

        public Paint getPaint() {
            return paint;
        }

    }

}

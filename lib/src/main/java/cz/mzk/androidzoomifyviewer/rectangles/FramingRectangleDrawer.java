package cz.mzk.androidzoomifyviewer.rectangles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.viewer.Utils;
import cz.mzk.androidzoomifyviewer.viewer.VectorD;

/**
 * Created by Martin Řehánek on 12.11.15.
 */
public class FramingRectangleDrawer {
    private static final Logger logger = new Logger(FramingRectangleDrawer.class);


    private final Context mContext;

    private Canvas mCanv;
    private List<FramingRectangle> mFramingRectangles;
    private Map<Integer, Paint> mBorderPaintMap;
    private Map<Integer, Paint> mFillingPaintMap;


    public FramingRectangleDrawer(Context context) {
        mContext = context;
    }

    private void initPaints() {
        if (mFramingRectangles != null) {
            for (FramingRectangle rect : mFramingRectangles) {
                Integer fillColor = rect.getFillColorRes();
                if (fillColor != null) {
                    initFillingPaintIfNeeded(fillColor);
                }
                FramingRectangle.Border border = rect.getBorder();
                if (border != null) {
                    initBorderPaintIfNeeded(border.getColorRes(), border.getThicknessDp());
                }
            }
        }
    }

    private void initFillingPaintIfNeeded(Integer color) {
        Paint found = mFillingPaintMap.get(color);
        if (found == null) {
            Paint paint = new Paint();
            paint.setColor(mContext.getResources().getColor(color));
            mFillingPaintMap.put(color, paint);
        }
    }

    private void initBorderPaintIfNeeded(Integer color, int thicknessDp) {
        Paint found = mBorderPaintMap.get(color);
        if (found == null) {
            Paint paint = new Paint();
            paint.setColor(mContext.getResources().getColor(color));
            paint.setStrokeWidth(Utils.dpToPx(thicknessDp));
            mBorderPaintMap.put(color, paint);
        }
    }

    public void setCanvas(Canvas canv) {
        this.mCanv = canv;
    }

    public void setFrameRectangles(List<FramingRectangle> framingRectangles) {
        mFramingRectangles = framingRectangles;
        mBorderPaintMap = new HashMap<>();
        mFillingPaintMap = new HashMap<>();
        initPaints();
    }

    public void draw(double totalScaleFactor, VectorD totalShift) {
        if (mCanv == null) {
            logger.w("draw() called, but canvas not initialized yet");
        } else {
            if (mFramingRectangles != null) {
                for (FramingRectangle rect : mFramingRectangles) {
                    drawRect(rect, totalScaleFactor, totalShift);
                }
            }
        }
    }

    private void drawRect(FramingRectangle framingRect, double totalScaleFactor, VectorD totalShift) {
        Rect rectInCanvasCoords = Utils.toCanvasCoords(framingRect.getRect(), totalScaleFactor, totalShift).toRect();
        if (framingRect.getFillColorRes() != null) {
            Paint fillPaint = mFillingPaintMap.get(framingRect.getFillColorRes());
            mCanv.drawRect(rectInCanvasCoords, fillPaint);
        }
        if (framingRect.getBorder() != null) {
            Paint borderPaint = mBorderPaintMap.get(framingRect.getBorder().getColorRes());
            // vertical
            mCanv.drawLine(rectInCanvasCoords.left, rectInCanvasCoords.top, rectInCanvasCoords.left, rectInCanvasCoords.bottom, borderPaint);
            mCanv.drawLine(rectInCanvasCoords.right, rectInCanvasCoords.top, rectInCanvasCoords.right, rectInCanvasCoords.bottom, borderPaint);
            // horizontal
            mCanv.drawLine(rectInCanvasCoords.left, rectInCanvasCoords.top, rectInCanvasCoords.right, rectInCanvasCoords.top, borderPaint);
            mCanv.drawLine(rectInCanvasCoords.left, rectInCanvasCoords.bottom, rectInCanvasCoords.right, rectInCanvasCoords.bottom, borderPaint);
        }
    }
}

package cz.mzk.androidzoomifyviewer.viewer;

import android.graphics.Rect;

import java.util.List;

import cz.mzk.androidzoomifyviewer.rectangles.FramingRectangle;

/**
 * Created by Martin Řehánek on 6.12.15.
 */
public interface TiledImageViewApi {


    //SCALE & SHIFT

    public double getInitialScaleFactor();

    public double getTotalScaleFactor();

    public double getMinScaleFactor();

    public double getMaxScaleFactor();

    public VectorD getTotalShift();

    //LOADING IMAGE
    // TODO: 6.12.15 Define protocol, for now zoomify only
    public void loadImage(String zoomifyBaseUrl);

    //HANDLERS

    public void setImageInitializationHandler(TiledImageView.ImageInitializationHandler imageInitializationHandler);

    public void setTileDownloadErrorListener(TiledImageView.TileDownloadErrorListener tileDownloadErrorListener);


    //GESTURES

    public TiledImageView.SingleTapListener getSingleTapListener();

    public void setSingleTapListener(TiledImageView.SingleTapListener singleTapListener);


    //FRAMING RECTANGLES

    public void setFramingRectangles(List<FramingRectangle> framingRectangles);


    //VIEW MODE

    public TiledImageView.ViewMode getViewMode();

    public void setViewMode(TiledImageView.ViewMode viewMode);


    //CANVAS

    // TODO: 7.12.15 Tohle spis vracet v img souradnicich
    public Rect getVisibleImageAreaInCanvas();


    public double getCanvasImagePaddingHorizontal();

    public double getCanvasImagePaddingVertical();


    //ORIGINAL IMAGE

    /**
     * @return Width of original image in px. Does not concern View and it's canvas.
     */
    public int getImageWidth();

    /**
     * @return Height of original image in px. Does not concern View and it's canvas.
     */
    public int getImageHeight();


    //VIEW

    public void invalidate();

    public int getWidth();

    public int getHeight();

}

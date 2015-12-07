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

    // TODO: 6.12.15 Dva interfacy TileDownloadHandler, asi prejmenovat, at neni zmatek
    public void setTileDownloadHandler(TiledImageView.TileDownloadHandler tileDownloadHandler);


    //GESTURES

    public void setSingleTapListener(TiledImageView.SingleTapListener singleTapListener);

    public TiledImageView.SingleTapListener getSingleTapListener();


    //FRAMING RECTANGLES

    public void setFramingRectangles(List<FramingRectangle> framingRectangles);


    //VIEW MODE

    public void setViewMode(TiledImageView.ViewMode viewMode);

    public TiledImageView.ViewMode getViewMode();


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


    //TODO

    /*public void destroy();

    public void pause();*/

}

package cz.mzk.tiledimageview;

import android.graphics.Rect;

import java.util.List;

import cz.mzk.tiledimageview.images.TiledImageProtocol;
import cz.mzk.tiledimageview.rectangles.FramingRectangle;

/**
 * This interface doesn't have to be used in your code. It just summarizes new public operations avaible in TiledImageView. Also some inherited from View (technical reasons).
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
    public void loadImage(TiledImageProtocol tiledImageProtocol, String baseUrl);

    //HANDLERS

    public void setMetadataInitializationHandler(TiledImageView.MetadataInitializationHandler metadataInitializationHandler);

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

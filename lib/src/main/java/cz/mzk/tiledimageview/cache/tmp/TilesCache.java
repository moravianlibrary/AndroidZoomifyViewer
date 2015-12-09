package cz.mzk.tiledimageview.cache.tmp;

import android.graphics.Bitmap;

import cz.mzk.tiledimageview.tiles.TilePositionInPyramid;

/**
 * @author Martin Řehánek
 */
public interface TilesCache {

    public Bitmap getTile(String zoomifyBaseUrl, TilePositionInPyramid tilePositionInPyramid);

    public void storeTile(Bitmap tile, String zoomifyBaseUrl, TilePositionInPyramid tilePositionInPyramid);

    public State getState();

    public static enum State {
        INITIALIZING, READY, DISABLED;
    }

}

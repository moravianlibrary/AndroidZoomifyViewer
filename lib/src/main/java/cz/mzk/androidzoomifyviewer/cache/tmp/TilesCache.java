package cz.mzk.androidzoomifyviewer.cache.tmp;

import android.graphics.Bitmap;

import cz.mzk.androidzoomifyviewer.tiles.zoomify.ZoomifyTileId;

/**
 * @author Martin Řehánek
 */
public interface TilesCache {

    public Bitmap getTile(String zoomifyBaseUrl, ZoomifyTileId zoomifyTileId);

    public void storeTile(Bitmap tile, String zoomifyBaseUrl, ZoomifyTileId zoomifyTileId);

    public State getState();

    public static enum State {
        INITIALIZING, READY, DISABLED;
    }

}

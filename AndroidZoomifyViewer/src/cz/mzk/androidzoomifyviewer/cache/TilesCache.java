package cz.mzk.androidzoomifyviewer.cache;

import android.graphics.Bitmap;
import cz.mzk.androidzoomifyviewer.tiles.TileId;

/**
 * @author Martin Řehánek
 * 
 */
public interface TilesCache {

	public Bitmap getTile(String zoomifyBaseUrl, TileId tileId);

	public void storeTile(Bitmap tile, String zoomifyBaseUrl, TileId tileId);

}

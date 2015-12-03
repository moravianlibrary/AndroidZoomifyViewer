package cz.mzk.androidzoomifyviewer.tiles;

import cz.mzk.androidzoomifyviewer.tiles.zoomify.ZoomifyTileId;

/**
 * Created by Martin Řehánek on 3.12.15.
 */
public class TileIdFactory {


    public static TileId valueOf(String string) {
        if (string.startsWith(ZoomifyTileId.PREFIX)) {
            return ZoomifyTileId.valueOf(string);
        } else {
            throw new IllegalArgumentException(String.format("unknown format of tile id: '%d'", string));
        }
    }
}

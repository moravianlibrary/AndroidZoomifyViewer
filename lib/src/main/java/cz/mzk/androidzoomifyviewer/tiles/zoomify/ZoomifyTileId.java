package cz.mzk.androidzoomifyviewer.tiles.zoomify;

import cz.mzk.androidzoomifyviewer.tiles.TileId;

/**
 * @author Martin Řehánek
 */
public class ZoomifyTileId implements TileId {

    public static final String PREFIX = "zoomify";

    private final int layer;
    private final TileCoords coords;

    public ZoomifyTileId(int layer, int x, int y) {
        this(layer, new TileCoords(x, y));
    }

    public ZoomifyTileId(int layer, TileCoords coords) {
        this.layer = layer;
        this.coords = coords;
    }

    public static ZoomifyTileId valueOf(String string) {
        String[] tokens = string.split(":");
        String zoomifyPrefix = tokens[0];
        if (!zoomifyPrefix.equals(PREFIX)) {
            throw new IllegalArgumentException(String.format("'%s' is not zoomify tile id", string));
        }
        int layer = Integer.valueOf(tokens[1]);
        int x = Integer.valueOf(tokens[2]);
        int y = Integer.valueOf(tokens[3]);
        return new ZoomifyTileId(layer, x, y);
    }

    @Override
    public String toSerializableString() {
        return PREFIX + ':' + layer + ':' + getX() + ':' + getY();
    }

    @Override
    public String toString() {
        return "" + layer + ':' + getX() + ':' + getY();
    }

    public int getLayer() {
        return layer;
    }

    @Deprecated
    public int getX() {
        return coords.x;
    }

    @Deprecated
    public int getY() {

        return coords.y;
    }

    public TileCoords getCoords() {
        return coords;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + layer;
        result = prime * result + getX();
        result = prime * result + getY();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ZoomifyTileId other = (ZoomifyTileId) obj;
        if (layer != other.layer)
            return false;
        if (getX() != other.getX())
            return false;
        if (getY() != other.getY())
            return false;
        return true;
    }

    /**
     * Tile coordinates as specified by Zoomify. For example the only image in layer 0 has coordinates 0,0.
     * If there are 6 pictures in level 1 (portrait picture), they have coordinates 0,0 0,1 1,0 1,1 2,0 and 2,1.
     */
    public static class TileCoords {
        public final int x;
        public final int y;

        public TileCoords(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

}

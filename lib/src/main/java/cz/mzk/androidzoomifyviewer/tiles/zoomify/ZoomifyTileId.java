package cz.mzk.androidzoomifyviewer.tiles.zoomify;

import cz.mzk.androidzoomifyviewer.tiles.TileId;

/**
 * @author Martin Řehánek
 */
public class ZoomifyTileId implements TileId {

    public static final String PREFIX = "zoomify";

    private final int layer;
    private final int x;
    private final int y;

    public ZoomifyTileId(int layer, int x, int y) {
        super();
        this.layer = layer;
        this.x = x;
        this.y = y;
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
        return PREFIX + ':' + layer + ':' + x + ':' + y;
    }

    @Override
    public String toString() {
        return "" + layer + ':' + x + ':' + y;
    }

    public int getLayer() {
        return layer;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + layer;
        result = prime * result + x;
        result = prime * result + y;
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
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }

}

package cz.mzk.androidzoomifyviewer.tiles;

/**
 * @author Martin Řehánek
 */
public class TileId {

    private final int layer;
    private final int x;
    private final int y;

    public TileId(int layer, int x, int y) {
        super();
        this.layer = layer;
        this.x = x;
        this.y = y;
    }

    public static TileId valueOf(String string) {
        String[] tokens = string.split(":");
        int layer = Integer.valueOf(tokens[0]);
        int x = Integer.valueOf(tokens[1]);
        int y = Integer.valueOf(tokens[2]);
        return new TileId(layer, x, y);
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

    public String toString() {
        return "" + layer + ':' + x + ':' + y;
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
        TileId other = (TileId) obj;
        if (layer != other.layer)
            return false;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }

}

package cz.mzk.androidzoomifyviewer.tiles;

/**
 * @author Martin Řehánek
 */
public class TilePositionInPyramid {

    private final int mLayer;
    private final TilePositionInLayer mPositionInLayer;

    public TilePositionInPyramid(int layer, int row, int column) {
        this(layer, new TilePositionInLayer(row, column));
    }

    public TilePositionInPyramid(int layer, TilePositionInLayer position) {
        this.mLayer = layer;
        this.mPositionInLayer = position;
    }

    public static TilePositionInPyramid valueOf(String string) {
        String[] tokens = string.split(":");
        int position = 0;
        int layer = Integer.valueOf(tokens[position++]);
        int x = Integer.valueOf(tokens[position++]);
        int y = Integer.valueOf(tokens[position++]);
        return new TilePositionInPyramid(layer, x, y);
    }

    @Override
    public String toString() {
        return "" + mLayer + ':' + mPositionInLayer.row + ':' + mPositionInLayer.column;
    }

    public int getLayer() {
        return mLayer;
    }

    public TilePositionInLayer getPositionInLayer() {
        return mPositionInLayer;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mLayer;
        result = prime * result + mPositionInLayer.column;
        result = prime * result + mPositionInLayer.row;
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
        TilePositionInPyramid other = (TilePositionInPyramid) obj;
        if (mLayer != other.mLayer)
            return false;
        if (mPositionInLayer.column != other.getPositionInLayer().column)
            return false;
        if (mPositionInLayer.row != other.getPositionInLayer().row)
            return false;
        return true;
    }

    /**
     * Tile position within mLayer. For example the only image in mLayer 0 has coordinates 0,0 (column=0, row=0)
     * If there are 6 pictures in level 1 (portrait picture), they have coordinates 0,0 0,1 1,0 1,1 2,0 and 2,1.
     */
    public static class TilePositionInLayer {
        public final int column;
        public final int row;

        public TilePositionInLayer(int column, int row) {
            this.column = column;
            this.row = row;
        }
    }

}

package cz.mzk.androidzoomifyviewer.tiles.zoomify;

import cz.mzk.androidzoomifyviewer.tiles.metadata.ImageMetadata;

/**
 * @author Martin Řehánek
 */
public class ZoomifyImageMetadata implements ImageMetadata {

    private final int width;
    private final int height;
    private final int numtiles; // pro kontrolu
    private final int tileSize;

    public ZoomifyImageMetadata(int width, int height, int numtiles, int tilesize) {
        this.width = width;
        this.height = height;
        this.numtiles = numtiles;
        this.tileSize = tilesize;
        // int xTiles = (int) Math.ceil(width / tileSize);
        // int yTiles = (int) Math.ceil(height / tileSize);
        // this.level = xTiles * yTiles;
    }

    public int getNumtiles() {
        return numtiles;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }


    @Override
    public int getTileSize() {
        return tileSize;
    }

    @Override
    public Orientation getOrientation() {
        if (width > height) {
            return Orientation.LANDSCAPE;
        } else {
            return Orientation.PORTRAIT;
        }
    }

    @Override
    public String toString() {
        return "ZoomifyImageMetadata [width=" + width + ", height=" + height + ", numtiles=" + numtiles + ", tileSize="
                + tileSize + "]";
    }

}

package cz.mzk.androidzoomifyviewer.tiles;

/**
 * Tile's dimension for given layer. For example for layer 0 the only available tile has width and height same as whole image.
 * If the image was in layer 1 composed of 6 tiles (portrait), each of them square, than each tile would have same dimensions:
 * basicSize = img.width/2, actualWidth = img.width/2, actualSize = img.width/2.
 * Created by Martin Řehánek on 4.12.15.
 */
public class TileDimensionsInImage {

    /**
     * width/hight of typical tile (i.e. every tile except the border ones - unless there are only border ones)
     */
    public final int basicSize;
    /**
     * Tile's actual width.
     */
    public final int actualWidth;
    /**
     * Tile's actual height.
     */
    public final int actualHeight;

    public TileDimensionsInImage(int basicSize, int actualWidth, int actualHeight) {
        this.basicSize = basicSize;
        this.actualWidth = actualWidth;
        this.actualHeight = actualHeight;
    }
}

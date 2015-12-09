package cz.mzk.tiledimageview.images.metadata;

/**
 * Created by Martin Řehánek on 8.12.15.
 */
public interface ImageMetadata {


    public int getWidth();

    public int getHeight();

    public int getTileSize();

    public Orientation getOrientation();

    public enum Orientation {

        LANDSCAPE, PORTRAIT;

    }


}

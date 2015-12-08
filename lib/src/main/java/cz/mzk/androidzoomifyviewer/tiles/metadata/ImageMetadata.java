package cz.mzk.androidzoomifyviewer.tiles.metadata;

import cz.mzk.androidzoomifyviewer.tiles.Orientation;

/**
 * Created by Martin Řehánek on 8.12.15.
 */
public interface ImageMetadata {


    public int getWidth();

    public int getHeight();

    public int getTileSize();

    public Orientation getOrientation();

}

package cz.mzk.tiledimageview.tiles.metadata;

import cz.mzk.tiledimageview.tiles.exceptions.InvalidDataException;
import cz.mzk.tiledimageview.tiles.exceptions.OtherIOException;

/**
 * Created by Martin Řehánek on 8.12.15.
 */
public interface ImageMetadataParser {

    public ImageMetadata parse(String metadataStr, String metadataUrl) throws InvalidDataException, OtherIOException;
}

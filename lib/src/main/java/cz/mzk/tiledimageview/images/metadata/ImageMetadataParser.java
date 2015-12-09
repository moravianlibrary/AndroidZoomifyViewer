package cz.mzk.tiledimageview.images.metadata;

import cz.mzk.tiledimageview.images.exceptions.InvalidDataException;
import cz.mzk.tiledimageview.images.exceptions.OtherIOException;

/**
 * Created by Martin Řehánek on 8.12.15.
 */
public interface ImageMetadataParser {

    public ImageMetadata parse(String metadataStr, String metadataUrl) throws InvalidDataException, OtherIOException;
}

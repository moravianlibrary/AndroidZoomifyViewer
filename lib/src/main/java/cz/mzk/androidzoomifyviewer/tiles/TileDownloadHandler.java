package cz.mzk.androidzoomifyviewer.tiles;

import android.graphics.Bitmap;

/**
 * Created by Martin Řehánek on 6.12.15.
 */
public interface TileDownloadHandler {
    public void onSuccess(TilePositionInPyramid tilePositionInPyramid, Bitmap bitmap);

    public void onUnhandableResponseCode(TilePositionInPyramid tilePositionInPyramid, String tileUrl, int responseCode);

    public void onRedirectionLoop(TilePositionInPyramid tilePositionInPyramid, String tileUrl, int redirections);

    public void onDataTransferError(TilePositionInPyramid tilePositionInPyramid, String tileUrl, String errorMessage);

    public void onInvalidData(TilePositionInPyramid tilePositionInPyramid, String tileUrl, String errorMessage);
}

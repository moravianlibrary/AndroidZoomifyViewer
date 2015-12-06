package cz.mzk.androidzoomifyviewer.tiles;

/**
 * Created by Martin Řehánek on 6.12.15.
 */
public interface MetadataInitializationHandler {
    public void onSuccess();

    public void onUnhandableResponseCode(String imagePropertiesUrl, int responseCode);

    public void onRedirectionLoop(String imagePropertiesUrl, int redirections);

    public void onDataTransferError(String imagePropertiesUrl, String errorMessage);

    public void onInvalidData(String imagePropertiesUrl, String errorMessage);
}

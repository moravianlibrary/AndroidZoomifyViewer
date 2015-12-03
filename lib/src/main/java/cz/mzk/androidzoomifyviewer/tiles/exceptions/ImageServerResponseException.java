package cz.mzk.androidzoomifyviewer.tiles.exceptions;

public class ImageServerResponseException extends Exception {
    private static final long serialVersionUID = -9136216127329035507L;
    private final int errorCode;
    private final String url;

    public ImageServerResponseException(String url, int errorCode) {
        super();
        this.url = url;
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getUrl() {
        return url;
    }
}
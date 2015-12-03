package cz.mzk.androidzoomifyviewer.tiles.exceptions;

public class OtherIOException extends Exception {
    private static final long serialVersionUID = 8890317963393224790L;
    private final String url;

    public OtherIOException(String message, String url) {
        super(message);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}

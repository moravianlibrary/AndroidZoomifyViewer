package cz.mzk.tiledimageview.tiles.exceptions;

public class InvalidDataException extends Exception {
    private static final long serialVersionUID = -6344968475737321154L;
    private final String url;

    public InvalidDataException(String url, String message) {
        super(message);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}

package cz.mzk.androidzoomifyviewer.tiles.exceptions;

public class TooManyRedirectionsException extends Exception {
    private static final long serialVersionUID = -6653657291115225081L;
    private final String url;
    private final int redirections;

    public TooManyRedirectionsException(String url, int redirections) {
        super();
        this.url = url;
        this.redirections = redirections;
    }

    public String getUrl() {
        return url;
    }

    public int getRedirections() {
        return redirections;
    }
}

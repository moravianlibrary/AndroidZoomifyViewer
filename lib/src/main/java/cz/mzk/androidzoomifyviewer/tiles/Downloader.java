package cz.mzk.androidzoomifyviewer.tiles;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cz.mzk.androidzoomifyviewer.Logger;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.ImageServerResponseException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.OtherIOException;
import cz.mzk.androidzoomifyviewer.tiles.exceptions.TooManyRedirectionsException;

/**
 * Created by Martin Řehánek on 7.12.15.
 */
public class Downloader {

    public static final int MAX_REDIRECTIONS = 5;
    public static final int METADATA_TIMEOUT = 3000;
    public static final int TILES_TIMEOUT = 5000;

    private static final Logger LOGGER = new Logger(Downloader.class);

    public static Bitmap downloadTile(String tileUrl) throws TooManyRedirectionsException, ImageServerResponseException, OtherIOException {
        return downloadTile(tileUrl, MAX_REDIRECTIONS);
    }

    private static Bitmap downloadTile(String tileUrl, int remainingRedirections) throws TooManyRedirectionsException, ImageServerResponseException, OtherIOException {
        LOGGER.d("downloading tile from " + tileUrl);
        if (remainingRedirections == 0) {
            throw new TooManyRedirectionsException(tileUrl, MAX_REDIRECTIONS);
        }
        // LOGGER.d( tileUrl + " remaining redirections: " +
        // remainingRedirections);
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(tileUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(TILES_TIMEOUT);
            urlConnection.setInstanceFollowRedirects(false); //because I handle following redirects manually to avoid redirection loop
            int responseCode = urlConnection.getResponseCode();
            switch (responseCode) {
                case 200:
                    return bitmapFromUrlConnection(urlConnection);
                case 300:
                case 301:
                case 302:
                case 303:
                case 305:
                case 307:
                    String location = urlConnection.getHeaderField("Location");
                    if (location == null || location.isEmpty()) {
                        throw new ImageServerResponseException(tileUrl, responseCode);
                    } else {
                        urlConnection.disconnect();
                        return downloadTile(location, remainingRedirections - 1);
                    }
                default:
                    throw new ImageServerResponseException(tileUrl, responseCode);
            }
        } catch (IOException e) {
            throw new OtherIOException(e.getMessage(), tileUrl);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private static Bitmap bitmapFromUrlConnection(HttpURLConnection urlConnection) throws IOException {
        InputStream in = null;
        try {
            in = new BufferedInputStream(urlConnection.getInputStream());
            return BitmapFactory.decodeStream(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public static String downloadMetadata(String metadataUrl) throws TooManyRedirectionsException, ImageServerResponseException, OtherIOException {
        return downloadMetadata(metadataUrl, MAX_REDIRECTIONS);
    }

    private static String downloadMetadata(String metadataUrl, int remainingRedirections) throws TooManyRedirectionsException, ImageServerResponseException, OtherIOException {
        LOGGER.d("downloading metadata from " + metadataUrl);
        if (remainingRedirections == 0) {
            throw new TooManyRedirectionsException(metadataUrl, MAX_REDIRECTIONS);
        }
        HttpURLConnection urlConnection = null;
        // LOGGER.d( metadataUrl + " remaining redirections: " +
        // remainingRedirections);
        try {
            URL url = new URL(metadataUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(METADATA_TIMEOUT);
            urlConnection.setInstanceFollowRedirects(false); //because I handle following redirects manually to avoid redirection loop
            int responseCode = urlConnection.getResponseCode();
            // LOGGER.d( "http code: " + responseCode);
            String location = urlConnection.getHeaderField("Location");
            switch (responseCode) {
                case 200:
                    return stringFromUrlConnection(urlConnection);
                case 300:
                    if (location == null || location.isEmpty()) {
                        throw new ImageServerResponseException(metadataUrl, responseCode);
                    }
                    urlConnection.disconnect();
                    return downloadMetadata(location, remainingRedirections - 1);
                case 301:
                    if (location == null || location.isEmpty()) {
                        throw new ImageServerResponseException(metadataUrl, responseCode);
                    }
                    urlConnection.disconnect();
                    return downloadMetadata(location, remainingRedirections - 1);
                case 302:
                case 303:
                case 305:
                case 307:
                    if (location == null || location.isEmpty()) {
                        throw new ImageServerResponseException(metadataUrl, responseCode);
                    }
                    urlConnection.disconnect();
                    return downloadMetadata(location, remainingRedirections - 1);
                default:
                    throw new ImageServerResponseException(metadataUrl, responseCode);
            }
        } catch (IOException e) {
            throw new OtherIOException(e.getMessage(), metadataUrl);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private static String stringFromUrlConnection(HttpURLConnection urlConnection) throws IOException {
        InputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            in = new BufferedInputStream(urlConnection.getInputStream());
            byte[] buffer = new byte[8 * 1024];
            out = new ByteArrayOutputStream();
            int readBytes = 0;
            while ((readBytes = in.read(buffer)) != -1) {
                out.write(buffer, 0, readBytes);
            }
            return out.toString();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
}

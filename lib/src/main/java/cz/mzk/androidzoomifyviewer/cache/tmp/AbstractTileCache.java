package cz.mzk.androidzoomifyviewer.cache.tmp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cz.mzk.androidzoomifyviewer.tiles.TileId;

/**
 * @author Martin Řehánek
 */

public abstract class AbstractTileCache {

    protected static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final char SEPARATOR = '-';
    private static final char ESCAPE_CHAR = '-';
    private final Map<Character, Character> POSSIBLY_RESERVED_CHARS = initPossiblyReservedChars();

    /**
     * Creates a unique subdirectory of the designated app cache directory. Tries to use external but if not mounted, falls back
     * on internal storage.
     *
     * @param context
     * @param diskCacheDir
     * @return
     */
    protected static File getDiskCacheDir(Context context, String subdir) {
        // Check if media is mounted or storage is built-in, if so, try and use
        // external cache dir (and exteranlscache dir is not null}
        // otherwise use internal cache dir
        // TODO: rozmyslet velikost cache podle zvoleneho uloziste
        // FIXME: na S3 haze nullpointerexception
        // String cacheDirPath =
        // Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
        // || !Environment.isExternalStorageRemovable() ?
        // context.getExternalCacheDir().getPath() : context
        // .getCacheDir().getPath();
        String cacheDirPath = context.getCacheDir().getPath();
        return new File(cacheDirPath + File.separator + subdir);
    }

    protected static int getDefaultMemoryCacheSizeKB() {
        // Get max available VM memory, exceeding this amount will throw an OutOfMemory exception. Stored in kilobytes as LruCache
        // takes an int in its constructor.
        int maxMemoryKB = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        return maxMemoryKB / 8;
    }

    // TODO: exception if file name to long (probably over 127 chars)
    protected String buildKey(String zoomifyBaseUrl, TileId tileId) {
        StringBuilder builder = new StringBuilder();
        builder.append(escapeSpecialChars(zoomifyBaseUrl)).append(SEPARATOR);
        builder.append(tileId.getLayer()).append(SEPARATOR);
        builder.append(tileId.getX()).append(SEPARATOR);
        builder.append(tileId.getY());
        return builder.toString();
    }

    private String escapeSpecialChars(String zoomifyBaseUrl) {
        StringBuilder builder = new StringBuilder();
        Set<Character> keys = POSSIBLY_RESERVED_CHARS.keySet();
        for (int i = 0; i < zoomifyBaseUrl.length(); i++) {
            char original = zoomifyBaseUrl.charAt(i);
            Character key = Character.valueOf(original);
            if (keys.contains(key)) {
                builder.append(ESCAPE_CHAR).append(POSSIBLY_RESERVED_CHARS.get(key));
            } else {
                builder.append(original);
            }
        }
        return builder.toString();
    }

    private Map<Character, Character> initPossiblyReservedChars() {
        Map<Character, Character> map = new HashMap<Character, Character>();
        // RFC 3986 reserved characters
        map.put(Character.valueOf('%'), Character.valueOf('a'));
        map.put(Character.valueOf('*'), Character.valueOf('b'));
        map.put(Character.valueOf('\''), Character.valueOf('c'));
        map.put(Character.valueOf('('), Character.valueOf('d'));
        map.put(Character.valueOf(')'), Character.valueOf('e'));
        map.put(Character.valueOf(';'), Character.valueOf('f'));
        map.put(Character.valueOf(':'), Character.valueOf('g'));
        map.put(Character.valueOf('@'), Character.valueOf('h'));
        map.put(Character.valueOf('&'), Character.valueOf('i'));
        map.put(Character.valueOf('='), Character.valueOf('j'));
        map.put(Character.valueOf('+'), Character.valueOf('k'));
        map.put(Character.valueOf('$'), Character.valueOf('l'));
        map.put(Character.valueOf(','), Character.valueOf('m'));
        map.put(Character.valueOf('/'), Character.valueOf('n'));
        map.put(Character.valueOf('?'), Character.valueOf('o'));
        map.put(Character.valueOf('#'), Character.valueOf('p'));
        map.put(Character.valueOf(']'), Character.valueOf('q'));
        map.put(Character.valueOf('['), Character.valueOf('r'));

        // RFC 3986 unreserved non-alphanumeric characters
        map.put(Character.valueOf('-'), Character.valueOf('s'));
        map.put(Character.valueOf('_'), Character.valueOf('t'));
        map.put(Character.valueOf('.'), Character.valueOf('u'));
        map.put(Character.valueOf('~'), Character.valueOf('v'));
        return map;
    }

    @SuppressLint("NewApi")
    protected int getBitmapSizeInKB(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount() / 1024;
        } else {
            return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
        }
    }

    protected byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100, bos);
        return bos.toByteArray();
    }

}

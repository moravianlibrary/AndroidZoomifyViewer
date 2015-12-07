package cz.mzk.androidzoomifyviewer.cache;

import android.graphics.Bitmap;

/**
 * @author Martin Řehánek
 */
public interface TilesCache {

    /**
     * Returns tile's bitmap from memory cache. If it's not found there, returns bitmap loaded from disc cache (blocking). Returns
     * null if bitmap was not found in either cache.
     *
     * @param tileUrl
     * @return
     */
    public Bitmap getTile(String tileUrl);

    /**
     * Returns tile's bitmap if it has been found in memory cache. Or if the bitmap is not in memory but in disk cache, tries to
     * execute async task that fetches bitmap from disk and stores into memory cache. Or informs that bitmap was not found in
     * either cache.
     *
     * @param tileUrl
     * @param handler
     * @return TileBitmap object that contains bitmap it self (or null) and tile's bitmap state.
     */
    public TileBitmap getTileAsync(String tileUrl, FetchingBitmapFromDiskHandler handler);


    public boolean containsTile(String tileUrl);


    public boolean containsTileInMemory(String tileUrl);


    public void storeTile(Bitmap tile, String tileUrl);


    public void cancelAllTasks();


    public void updateMemoryCacheSizeInItems(int size);

    // TODO: use this
    public void close();

    public static interface FetchingBitmapFromDiskHandler {
        public void onFetched();
    }

}
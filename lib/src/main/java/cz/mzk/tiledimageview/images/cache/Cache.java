package cz.mzk.tiledimageview.images.cache;

import android.support.annotation.WorkerThread;

/**
 * Created by Martin Řehánek on 11.12.15.
 */
public interface Cache<Item> {


    //MEMORY

    /**
     * Only method of this interface that can may be call from both worker and ui threads.
     */
    public Item getItemFromMemoryCache(String key);

    @WorkerThread
    public void storeItemToMemoryCache(String key, Item item);

    @WorkerThread
    public void increasMemoryCacheSize(int maxItems);


    //DISK

    @WorkerThread
    public boolean isDiskCacheEnabled();

    @WorkerThread
    public boolean isItemInDiskCache(String key);

    @WorkerThread
    public Item getItemFromDiskCache(String key);

    @WorkerThread
    public void storeItemToDiskCache(String key, Item item);

    // TODO: 11.12.15 use when something like Application.onDestroyed() is implemented
    // or at least create method flush() annd call it when destroying the view
    @WorkerThread
    public void close();

}

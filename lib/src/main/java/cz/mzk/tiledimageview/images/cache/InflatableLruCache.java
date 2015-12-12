package cz.mzk.tiledimageview.images.cache;

import android.os.Build;
import android.util.LruCache;

import java.util.Map;

import cz.mzk.tiledimageview.Logger;

/**
 * Created by Martin Řehánek on 11.12.15.
 */
public class InflatableLruCache<Key, Item> {

    private final Logger mLogger;
    private LruCache<Key, Item> mCache;

    public InflatableLruCache(int size, Logger logger) {
        mLogger = logger;
        mCache = new LruCache<>(size);
    }

    public void inflate(int newMaxSize) {
        int currentSize = mCache.maxSize();
        if (newMaxSize > currentSize) {
            mLogger.d("Increasing cache max size " + currentSize + " -> " + newMaxSize + " items");
            if (Build.VERSION.SDK_INT >= 21) {
                mCache.resize(newMaxSize);
            } else {//resize manually by creating new cache instance
                Map<Key, Item> snapshot = mCache.snapshot();
                mCache = new LruCache<>(newMaxSize);
                for (Key key : snapshot.keySet()) {
                    mCache.put(key, snapshot.get(key));
                }
            }
        }
    }

    public Item get(Key key) {
        return mCache.get(key);
    }

    public void put(Key key, Item item) {
        mCache.put(key, item);
    }
}

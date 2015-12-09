package cz.mzk.tiledimageview.demonstration.kramerius;

import android.content.Context;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;

import java.io.File;

import javax.net.ssl.SSLSocketFactory;

import cz.mzk.tiledimageview.demonstration.ssl.SSLSocketFactoryProvider;

/**
 * @author Martin Řehánek
 */
public class VolleyRequestManager {

    public static final int DISK_CACHE_SIZE_B = 1024 * 1024 * 2;// 2MB
    public static final String DEFAULT_CACHE_DIR = "volley";
    private static final String TAG = VolleyRequestManager.class.getSimpleName();
    private static RequestQueue requestQueue = null;

    public static void initialize(Context context) {
        if (requestQueue != null) {
            throw new IllegalStateException(VolleyRequestManager.class.getSimpleName()
                    + " has been already initialized");
        }
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);
        Cache cache = new DiskBasedCache(cacheDir, DISK_CACHE_SIZE_B);
        Network network = new BasicNetwork(getHttpStack(context));
        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();
    }

    private static HttpStack getHttpStack(Context context) {
        // HttpURLConnection
        SSLSocketFactory sslSocketFactory = getSslSocketFactory(context);
        if (sslSocketFactory != null) {
            return new HurlStack(null, sslSocketFactory);
        } else {
            return new HurlStack();
        }
    }

    private static SSLSocketFactory getSslSocketFactory(Context context) {
        try {
            return SSLSocketFactoryProvider.instanceOf(context).getSslSocketFactory();
        } catch (Exception e) {
            Log.d(TAG, "error getting SSL Socket factory", e);
            return null;
        }
    }

    public static <T> void addToRequestQueue(Request<T> request) {
        if (requestQueue == null) {
            throw new IllegalStateException(VolleyRequestManager.class.getSimpleName() + " has not been initialized");
        }
        requestQueue.add(request);
    }

    public static void cancelRequestByTag(String tag) {
        if (requestQueue == null) {
            throw new IllegalStateException(VolleyRequestManager.class.getSimpleName() + " has not been initialized");
        }
        requestQueue.cancelAll(tag);
    }

}

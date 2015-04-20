package cz.mzk.androidzoomifyviewer.examples.kramerius;

import java.io.File;

import javax.net.ssl.SSLSocketFactory;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;

import cz.mzk.androidzoomifyviewer.examples.ssl.SSLSocketFactoryProvider;

/**
 * @author Martin Řehánek
 * 
 */
public class VolleyRequestManager {

	private static final String TAG = VolleyRequestManager.class.getSimpleName();
	public static final int DISK_CACHE_SIZE_B = 1024 * 1024 * 2;// 2MB
	public static final String DEFAULT_CACHE_DIR = "volley";

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
		// Prior to Gingerbread, HttpUrlConnection was unreliable.
		// See:
		// http://android-developers.blogspot.com/2011/09/androids-http-clients.html
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			// HttpURLConnection
			SSLSocketFactory sslSocketFactory = getSslSocketFactory(context);
			if (sslSocketFactory != null) {
				return new HurlStack(null, sslSocketFactory);
			} else {
				return new HurlStack();
			}
		} else {
			// SSL probably not working, but minSdkVersion is GINGERBREAD anyway
			return buildAndroidHttpClientStack(context);
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

	private static HttpStack buildAndroidHttpClientStack(Context context) {
		String userAgent = "volley/0";
		try {
			String packageName = context.getPackageName();
			PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
			userAgent = packageName + "/" + info.versionCode;
			return new HttpClientStack(AndroidHttpClient.newInstance(userAgent));
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException();
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

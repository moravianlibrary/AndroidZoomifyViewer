package cz.mzk.androidzoomifyviewer.examples;

import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import cz.mzk.androidzoomifyviewer.examples.kramerius.VolleyRequestManager;
import cz.mzk.androidzoomifyviewer.examples.ssl.SSLSocketFactoryProvider;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;

/**
 * @author Martin Řehánek
 * 
 */
public class AndroidZoomifyViewerExamplesApp extends Application {

	private static final String TAG = AndroidZoomifyViewerExamplesApp.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();
		// TODO: tohle taky hodit do resource
		// boolean clearCache = AppConfig.DEV_MODE && AppConfig.DEV_MODE_CLEAR_CACHE_ON_STARTUP;
		setupHttpUrlConnection(this);
		TiledImageView.initialize(this);
		VolleyRequestManager.initialize(this);
		// TiledImageView.DEV_MODE = AppConfig.DEV_MODE;
	}

	private void setupHttpUrlConnection(Context context) {
		// must handle redirect myself
		// because some things don't allways work properly. For instance http://something -> https://something.
		HttpURLConnection.setFollowRedirects(false);
		HttpsURLConnection.setFollowRedirects(false);
		try {
			HttpsURLConnection.setDefaultSSLSocketFactory(SSLSocketFactoryProvider.instanceOf(context).getSslSocketFactory());
		} catch (Exception e) {
			Log.e(TAG, "error initializing SSL Socket factory", e);
		}
	}
}

package cz.mzk.androidzoomifyviewer.examples;

import android.app.Application;
import cz.mzk.androidzoomifyviewer.CacheManager;
import cz.mzk.androidzoomifyviewer.examples.kramerius.VolleyRequestManager;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;

/**
 * @author Martin Řehánek
 * 
 */
public class AndroidZoomifyViewerExamplesApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		// TODO: tohle taky hodit do resource
		// boolean clearCache = AppConfig.DEV_MODE && AppConfig.DEV_MODE_CLEAR_CACHE_ON_STARTUP;
		TiledImageView.initialize(this);
		VolleyRequestManager.initialize(this);
		// TiledImageView.DEV_MODE = AppConfig.DEV_MODE;
	}
}

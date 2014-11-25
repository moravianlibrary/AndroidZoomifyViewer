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
		boolean clearCache = AppConfig.DEV_MODE && AppConfig.DEV_MODE_CLEAR_CACHE_ON_STARTUP;
		CacheManager.initialize(this, clearCache);
		VolleyRequestManager.initialize(this);
		TiledImageView.DEV_MODE = AppConfig.DEV_MODE;
	}
}

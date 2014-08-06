package cz.mzk.androidzoomifyviewer.examples;

import android.app.Application;
import cz.mzk.androidzoomifyviewer.CacheManager;


/**
 * @author Martin Řehánek
 * 
 */
public class AndroidZoomifyViewerExamplesApp extends Application {
	
	public static final boolean DEV_MODE = false;

	@Override
	public void onCreate() {
		super.onCreate();
		CacheManager.initialize(this);
	}
}

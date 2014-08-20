Android Zoomify Viewer
=====================

This project contains Android view for Zoomify tiles with some usage examples. 
Main features are:
 * gradual image quality refinement
 * two level tiles (and ImageProperties.xml) caching - memory and disk
 * tiles are downloaded and cached to disk in non-ui threads
 * fluent zooming and swiping
 * Listeners for server errors 
 * working examples of real data
 * examples of errors on mock server
 * library project with view that can be used from xml or code just like ImageView

Integration into Application
============================
Required permissions:
```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
Cache manager must be initialized with context. Best place to do this is in onCreate() method of class extending Application like this:
```
public class AndroidZoomifyViewerExamplesApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		boolean clearCache = AppConfig.DEV_MODE && AppConfig.DEV_MODE_CLEAR_CACHE_ON_STARTUP;
		CacheManager.initialize(this, clearCache);
	}
}

```




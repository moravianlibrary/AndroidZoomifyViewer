# Android Zoomify Viewer

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
 
## Requirements
Minimal SDK version is 9 (Android 2.3) for both library itself and example project.

## Using the library in your app
The library project is located in AndroidZoomifyViewer directory. Project depends only on android-support-v4.

Required permissions:
```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
Cache manager must be initialized with context. Best place to do this is in onCreate() method of class extending Application like this:
```
public class MyApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		boolean clearCache = AppConfig.DEV_MODE && AppConfig.DEV_MODE_CLEAR_CACHE_ON_STARTUP;
		CacheManager.initialize(this, clearCache);
	}
}

```

## Example Application
Directory AndroidZoomifyViewerExamples contains Android App project that shows how to use the library. There are some examples of publicly available images in zoomify format as well as possible error situations. 
Backend project to simulate errors is deployed at AppEngine but no specific AppEngine APIs are used here so it can be deploy into any Servlet container. Backend project for example App project is in directory AndroidZoomifyViewerExamplesBackend.


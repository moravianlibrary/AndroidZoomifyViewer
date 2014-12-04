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
Library project needs android-support-v4.jar so that LruCache works in API < 12.
Examples App project depends on appcompat-v7 (from SDK 21) in order to enable Fragments, ActionBar/ToolBar in older versions.

## Using the library in your app
The library project is located in AndroidZoomifyViewer directory. Project depends only on android-support-v4.

Required permissions:
```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
TiledImageView must be initialized with application context. Best place to do this is in onCreate() method of class extending Application like this:
```
public class MyApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		TiledImageView.initialize(this);
	}
}

```
This initializes memory and disk cache. Disk cache sice can be configured through resource as well as whether disk cache will be cleared on startup.
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
	<bool name="androidzoomifyviewer_disk_cache_clear_on_startup">false</bool>
	<integer name="androidzoomifyviewer_disk_cache_size_kb">51200</integer>
</resources>
```
## Logging and dev mode
For production most of the logs are not created. Only level Log.WARN and higher. If you want more logs, set Logger.PRODUCTION_LOG_LEVEL to lower level.
```
PRODUCTION_LOG_LEVEL = Log.INFO;
```
Or you can enable all logs in TiledImageView by setting TiledImageView.DEV_MODE to true. This will also enable other developer features like visualization of tiles being drawn.

## Example Application
Directory AndroidZoomifyViewerExamples contains Android App project that shows how to use the library. There are some examples of publicly available images in zoomify format as well as possible error situations. 
Backend project to simulate errors is deployed at AppEngine but no specific AppEngine APIs are used here so it can be deploy into any Servlet container. Backend project for example App project is in directory AndroidZoomifyViewerExamplesBackend.


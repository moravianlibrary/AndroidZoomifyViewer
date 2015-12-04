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

## Adding the library to your app
### Android studio / Gradle
Just add this dependency to your app module's build.gradle:
```
compile 'cz.mzk.androidzoomifyviewer:android-zoomify-viewer:1.0'
```
### Eclipse / ADT / Other
You can either clone this whole repository, which consisted of multiple Eclipse projects. You need to checkout tag 'eclipse'. Eclipse version will no longer be supported though.
Next option is to download necessary files from here:
https://bintray.com/rzeh4n/maven/android-zoomify-viewer/view#files

## Using the library
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
This configures disk cache through resouces. You can disable disk cache or set its size or enable that disk cache is cleared on application startup .
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <bool name="androidzoomifyviewer_disk_cache_enabled">true</bool>
    <bool name="androidzoomifyviewer_disk_cache_clear_on_startup">false</bool>
    <integer name="androidzoomifyviewer_tile_disk_cache_size_kb">51200</integer>
</resources>
```
## Logging and dev mode
In production, most of logs are not being created at all. Only those in level Log.WARN and higher. 
If you can edit library's source coude, you can enable more logs even in production by setting cz.mzk.androidzoomifyviewer.Logger.PRODUCTION_LOG_LEVEL to lower level.
```
PRODUCTION_LOG_LEVEL = Log.INFO;
```
Or you can enable all logs in TiledImageView by setting TiledImageView.DEV_MODE to true. This will also enable other developer features like visualization of tiles being drawn.

## Example Application
Module app contains example android application project, that shows how to use the library. There are some examples of publicly available images in zoomify format as well as possible error situations. 
Module backand contains web project to simulate errors and is deployed in Google AppEngine. But no specific AppEngine APIs are used here so it can be easily deploy into any Servlet container. Example android app uses among other data from backend web app.

## HTTPS
Both Library and Example project can handle https requests. But if you need to access web resources with X.509 certificate, that can't be validated with android pre-installed issuers (for example self-signed or without whole certificate chain packed), you need to add required certificate as a resource and load it in SSL context provider.

1. put DER encoded certifiate(s) in /res/raw
2. add certificate(s) resource id(s) into CERT_RES_IDS array in class cz.mzk.androidzoomifyviewer.examples.ssl.SSLProvider

If you use Library in your own project, use same technique as in AndroidZoomifyViewerExamples. I.e.:

1. Create own X509TrustManager that uses both default KeyStore and custom KeyStore. (see cz.mzk.androidzoomifyviewer.examples.ssl.TrustManagerWithSystemAndLocalKeystores)
2. Create your SSL SocketFactory provider with custom KeyStore that loads certificates from resource. SSL SocketProvider must use custom X509TrustManager. (see cz.mzk.androidzoomifyviewer.examples.ssl.SSLSocketFactoryProvider)
3. Set SSL SocketFactory either for single connection:
```
java.net.ssl.HttpsURLConnection.setSSLSocketFactory(factory)
```
Or as default factory for all connections with static method:
```
java.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(factory)
```
I'd advice to set default factory because this way even third party network libraries should work with your custom certificates. Best place for this is in onCreate() of class extending Application (see cz.mzk.androidzoomifyviewer.examples.AndroidZoomifyViewerExamplesApp). 










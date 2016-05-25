# Tiled Image View

This project contains Android library for *Tiled Image View* - view that loads image gradually by tiles. And also demo app to demonstrate the library.

Main features are:
 * gradual image quality refinement (according to zoom level)
 * two level caching (tiles and metadata) - memory and disk
 * tiles are downloaded and cached to disk in non-ui threads
 * fluent zooming and swiping
 * Listeners for server errors and single-tap gesture 
 * highligting rectangle areas (for example for fulltext search results, notes, etc. )
 * view modes (how should image be mapped into container)
 * server side errors accessible through event handlers
 * https support
 * working demo with real data and examples of errors on mock server
 
## Requirements
Minimal SDK version is 12 (Android 3.1.x) for both library itself and demo project.
Examples App project depends on appcompat-v7 (from SDK 21) in order to enable Fragments, ActionBar/ToolBar in older versions.

## Adding the library to your app
### Android studio / Gradle
Just add this dependency to your app module's build.gradle:
```
compile 'cz.mzk.tiledimageview:tiled-image-view:2.2.4'
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
You can create the view through layout or constructor. Do not use wrap_content since the images's width and hight is unknown in advance, image can be zoomed and image can be replaced with another one.
In layout:
```
<cz.mzk.tiledimageview.TiledImageView
       xmlns:app="http://schemas.android.com/apk/res-auto"
       android:id="@+id/tiledImageView"
       android:layout_width="match_parent"
       android:layout_height="match_parent"
       android:background="@color/black"
       app:show_dev_visualisations="true"
       app:view_mode="fit_in_view"/>
```
With constructor:
```
new TiledImageView(context);
new TiledImageView(context, attributeSet);
new TiledImageView(context, showDevVisualisations);
new TiledImageView(context, viewMode);
new TiledImageView(context, showDevVisualisations,viewMode);
```
Now you can define image source, change viewMode, add listeners, etc:
```
        //Most important method - setting protocol and base url for image. Only zoomify is supported now.
        //To replace image in view, call this with other url.
        mImageView.loadImage(TiledImageProtocol.ZOOMIFY, "http://imageserver.mzk.cz/mzk03/001/048/663/2619269773/");

        //You can always change view mode, but It will throw away current zoom level and shift.
        mImageView.setViewMode(TiledImageView.ViewMode.FILL_VIEW_ALIGN_CENTER_CENTER);

        //You can define rectangles with background color and/or border with width and color to highlight or cover parts of image.
        mImageView.setFramingRectangles(framingRectangles);

        //Getting width and hight of current image, no matter how it is mapped to view.
        mImageView.getImageWidth();
        mImageView.getImageHeight();

        //Listener for single-tap event.
        mImageView.setSingleTapListener(new TiledImageView.SingleTapListener() {
            @Override
            public void onSingleTap(float x, float y, Rect boundingBox) {
                //if you need to handle single tap. X and Y are coordinates withing view, boundingBox is area of image view that contains acutal image - i.e. at most [0,0] - [view_width,view_height]
            }
        });

        //Listener for metadata-initialization events.
        mImageView.setMetadataInitializationListener(new TiledImageView.MetadataInitializationListener() {
            @Override
            public void onMetadataInitialized() {
                // Ok, metadata loaded, loading tiles from now on.
            }

            @Override
            public void onMetadataUnhandableResponseCode(String imageMetadataUrl, int responseCode) {
                // Use this to handle errors concerning unexpected http response code when downloading the image metadata.
            }

            @Override
            public void onMetadataRedirectionLoop(String imageMetadataUrl, int redirections) {
                // Use this to handle errors concerning redirection loop code when downloading the image metadata.
            }

            @Override
            public void onMetadataDataTransferError(String imageMetadataUrl, String errorMessage) {
                // Use this to handle network/data transfer errors when downloading the image metadata.
            }

            @Override
            public void onMetadataInvalidData(String imageMetadataUrl, String errorMessage) {
                // Use this to handle errors concerning invalid image metadata.
            }

            @Override
            public void onCannotExecuteMetadataInitialization(String imageMetadataUrl) {
                // Use this to handle errors when there is no free space in thread queue to schedule metadata-fetch task.
                // This might happen if you have to many TiledImageViews at once. Up to 3 TiledImageView instances at once should be fine.
            }
        });
```
## Cache configuration
Cache size, whether it is enabled, and if it should be cleared on application startup is defined in resources:
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <bool name="tiledimageview_disk_cache_enabled">true</bool>
    <bool name="tiledimageview_disk_cache_clear_in_initialization">true</bool>
    <!--50 MB-->
    <integer name="tiledimageview_tile_disk_cache_size_kb">51200</integer>
</resources>
```
Cache is initialized in onAttachedToWindow() method of first TiledImageView used.
## Logging and dev mode
In production, most of logs are not being created at all. Only those in level Log.WARN and higher. 
If you can edit library's source coude, you can enable more logs even in production by setting cz.mzk.tiledimageview.Logger.PRODUCTION_LOG_LEVEL to lower level.
```
PRODUCTION_LOG_LEVEL = Log.INFO;
```
Or you can enable all logs in TiledImageView by setting TiledImageView.DEV_MODE to true. This will also enable other developer features like visualization of tiles being drawn.

## Demo App
Module app contains demo android application project, that shows how to use the library. There are some examples of publicly available images in zoomify format as well as possible error situations. 

Module backend contains web project to simulate errors and is deployed in Google AppEngine. But no specific AppEngine APIs are used here so it can be easily deploye into any Servlet container. Demo app uses among other data from backend web app.

<a href="https://play.google.com/store/apps/details?id=cz.mzk.tiledimageview.demonstration"><img height="80" src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"/></a>


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
I'd advice to set default factory because this way even third party network libraries should work with your custom certificates. Best place for this is in onCreate() of class extending Application. See <a href="https://github.com/moravianlibrary/AndroidZoomifyViewer/blob/master/app/src/main/java/cz/mzk/tiledimageview/demonstration/TiledImageViewDemostrationApplication.java">cz.mzk.tiledimageview.demonstration.TiledImageViewDemostrationApplication</a>

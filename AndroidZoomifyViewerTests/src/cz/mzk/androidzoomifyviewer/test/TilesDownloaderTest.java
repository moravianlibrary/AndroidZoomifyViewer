package cz.mzk.androidzoomifyviewer.test;

import java.util.List;

import junit.framework.TestCase;
import android.util.Log;
import cz.mzk.androidzoomifyviewer.tiles.InitTilesDownloaderTask;
import cz.mzk.androidzoomifyviewer.tiles.InitTilesDownloaderTask.ImagePropertiesDownloadResultHandler;
import cz.mzk.androidzoomifyviewer.tiles.Layer;
import cz.mzk.androidzoomifyviewer.tiles.TilesDownloader;

/**
 * @author Martin Řehánek
 * 
 */
public class TilesDownloaderTest extends TestCase {

	private static final String TAG = TilesDownloaderTest.class.getSimpleName();

	private TilesDownloader testDownloader = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		String baseUrl = "http://krameriustest.mzk.cz/search/zoomify/uuid:821b6258-43d0-42c8-ad27-5b83bb6667bc/";
		new InitTilesDownloaderTask(baseUrl, new ImagePropertiesDownloadResultHandler() {

			@Override
			public void onUnhandableResponseCode(String imagePropertiesUrl, int responseCode) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(TilesDownloader downloader) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onRedirectionLoop(String imagePropertiesUrl, int redirections) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onInvalidData(String imagePropertiesUrl, String errorMessage) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDataTransferError(String imagePropertiesUrl, String errorMessage) {
				// TODO Auto-generated method stub

			}
		}).execute();
		// busy wait until initialized
		while (testDownloader == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Log.d(TAG, TilesDownloader.class.getSimpleName() + " initialized");
	}

	public void testSomething() {
		int width = testDownloader.getImageProperties().getWidth();
		int height = testDownloader.getImageProperties().getHeight();
		// Random rand = new Random();
		// for (int layerId = 0; layerId <
		// downloader.getLayers().size();
		// layerId++) {
		// int pixelX = rand.nextInt(width);
		// int pixelY = rand.nextInt(height);
		// int[] pic = downloader.getPicture(layerId, pixelX, pixelY);
		// Log.d(TAG, "layer=" + layerId + ", pixel: " + pixelX + "," +
		// pixelY);
		// Log.d(TAG, "pic: " + pic[0] + "," + pic[1]);
		// }
		List<Layer> layers = testDownloader.getLayers();
		int[] topLeftCorner = { 0, 0 };
		int[] topRightCorner = { 2166, 0 };
		int[] bottomLeftCorner = { 0, 2885 };
		int[] bottomRightCorner = { 2166, 2885 };
		// testDetectPic(0, topLeftCorner, new int[] { 0, 0 });
		// testDetectPic(0, topRightCorner, new int[] { 0, 0 });
		// testDetectPic(0, bottomLeftCorner, new int[] { 0, 0 });
		// testDetectPic(0, bottomRightCorner, new int[] { 0, 0 });
		//
		// testDetectPic(1, topLeftCorner, new int[] { 0, -1 });
		// testDetectPic(1, topRightCorner, new int[] { 1, -1 });
		// testDetectPic(1, bottomLeftCorner, new int[] { 0, -1 });
		// testDetectPic(1, bottomRightCorner, new int[] { 1, -1 });
		//
		// testDetectPic(1, topLeftCorner, new int[] { 0, -1 });
		// testDetectPic(1, topRightCorner, new int[] {
		// layers.get(1).getTilesHorizontal() - 1, -1 });
		// testDetectPic(1, bottomLeftCorner, new int[] { 0, -1 });
		// testDetectPic(1, bottomRightCorner, new int[] { 1, -1 });

		for (int layer = 0; layer < layers.size(); layer++) {
			Log.d(TAG, "testing layer " + layer);
			int horizontal = layers.get(layer).getTilesHorizontal();
			int vertical = layers.get(layer).getTilesVertical();
			testDetectPic(layer, topLeftCorner, new int[] { 0, 0 });
			testDetectPic(layer, topRightCorner, new int[] { horizontal - 1, 0 });
			testDetectPic(layer, bottomLeftCorner, new int[] { 0, vertical - 1 });
			testDetectPic(layer, bottomRightCorner, new int[] { horizontal - 1, vertical - 1 });
		}
	}

	private void testDetectPic(int layerId, int[] pixel, int[] expectedPic) {
		// if (mTestDownloader != null) {
		int[] pic = testDownloader.getTileCoords(layerId, pixel[0], pixel[1]);
		assertEquals(expectedPic[0], pic[0]);
		assertEquals(expectedPic[1], pic[1]);
		// if (pic[0] != expectedPic[0] || pic[1] != expectedPic[1]) {
		// Log.e(TAG, "Failed: layer=" + layerId + ", pixel=" + pixel[0] + "," +
		// pixel[1] + ": expected="
		// + expectedPic[0] + "," + expectedPic[1] + "; found=" + pic[0] + "," +
		// pic[1]);
		// }
		// } else {
		// initTestDownloader();
		// }
	}

	private void testDetectPic(int layerId, int[] pixel) {
		testDetectPic(layerId, pixel[0], pixel[1]);
	}

	private void testDetectPic(int layerId, int pixelX, int pixelY) {
		int[] pic = testDownloader.getTileCoords(layerId, pixelX, pixelY);
		Log.d(TAG, "layer=" + layerId + ", pixel: " + pixelX + "," + pixelY);
		Log.d(TAG, "pic: " + pic[0] + "," + pic[1]);
	}

}

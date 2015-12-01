package cz.mzk.androidzoomifyviewer;

import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import cz.mzk.androidzoomifyviewer.tiles.InitTilesDownloaderTask;
import cz.mzk.androidzoomifyviewer.tiles.InitTilesDownloaderTask.ImagePropertiesDownloadResultHandler;
import cz.mzk.androidzoomifyviewer.tiles.Layer;
import cz.mzk.androidzoomifyviewer.tiles.TilesDownloader;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Martin Řehánek
 */

@RunWith(AndroidJUnit4.class)
public class TilesCoordinatesTest extends AndroidTestCase {


    private static final String TAG = TilesCoordinatesTest.class.getSimpleName();

    @Before
    public void init() throws Exception {
        Log.d(TAG, "init");
        if (getContext() == null) {
            Log.d(TAG, "initializing mock context");
            setContext(new MockContext());
        }
        assertNotNull(getContext());

        if (!CacheManager.isInitialized()) {
            Log.d(TAG, "initializing " + CacheManager.class.getSimpleName());
            CacheManager.initialize(getContext(), false, false, 0);
        }
    }

    class TilesDownloaderInitializationResult {
        public boolean finished = false;
        public TilesDownloader downloader;
    }

    private TilesDownloader initTilesDownloader(String baseUrl) {
        double pxRatio = 0.5;
        final TilesDownloaderInitializationResult result = new TilesDownloaderInitializationResult();
        new InitTilesDownloaderTask(baseUrl, pxRatio, new ImagePropertiesDownloadResultHandler() {

            @Override
            public void onUnhandableResponseCode(String imagePropertiesUrl, int responseCode) {
                Log.e(TAG, "unexpected response code: " + responseCode);
                result.finished = true;
            }

            @Override
            public void onSuccess(TilesDownloader downloader) {
                result.finished = true;
                result.downloader = downloader;
                Log.d(TAG, TilesDownloader.class.getSimpleName() + " initialized");
            }

            @Override
            public void onRedirectionLoop(String imagePropertiesUrl, int redirections) {
                Log.e(TAG, "redirection loop for " + imagePropertiesUrl);
                result.finished = true;
            }

            @Override
            public void onInvalidData(String imagePropertiesUrl, String errorMessage) {
                Log.e(TAG, String.format("invalid data for %s: %s", imagePropertiesUrl, errorMessage));
                result.finished = true;
            }

            @Override
            public void onDataTransferError(String imagePropertiesUrl, String errorMessage) {
                Log.e(TAG, String.format("data transfer error for %s: %s", imagePropertiesUrl, errorMessage));
                result.finished = true;
            }
        }).execute();

        while (!result.finished) {
            try {
                Thread.sleep(100);
                //Log.d(TAG, "waiting");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result.downloader;
    }


    @Test
    public void testZero() {
        assertTrue("problem with Junit configuration", true);
        assertThat(1, is(1));
    }


    @Test
    public void testCornerTilesCoordsMapyMzk1() {
        testCornerTilesCoords("http://mapy.mzk.cz/AA22/0103/");
    }

    @Test
    public void testCornerTilesCoordsMapyMzk2() {
        testCornerTilesCoords("http://mapy.mzk.cz/AA22/0104/");
    }

    @Test
    public void testCornerTilesCoordsMapyMzk3() {
        testCornerTilesCoords("http://mapy.mzk.cz/AA22/0105/");
    }

    @Test
    public void testCornerTilesCoordsTricedesigns1() {
        testCornerTilesCoords("http://www.tricedesigns.com/panoramas/Pemberton-Park-4/Pemberton-Park-4/");
    }

    @Test
    public void testCornerTilesCoordsTricedesigns2() {
        testCornerTilesCoords("http://www.tricedesigns.com/panoramas/office-outside/office-outside/");
    }

    //url no longer available
    /*@Test
    public void testCornerTilesCoordsTricedesigns3() {
        testCornerTilesCoords("http://www.tricedesigns.com/panoramas/Pemberton-Park-4/Pemberton-Park-3/");
    }*/


    //url no longer available
    /*@Test
    public void testCornerTilesCoordsFookes1() {
        testCornerTilesCoords("http://www.fookes.com/ezimager/zoomify/105_0532/");
    }*/

    public void testCornerTilesCoords(String baseUrl) {
        Log.d(TAG, "testing corner tiles coords for: " + baseUrl);
        TilesDownloader mTilesDownloader = initTilesDownloader(baseUrl);
        assertNotNull("Tiles downloader not initialized. Probably image no longer available on base url: " + baseUrl, mTilesDownloader);
        int width = mTilesDownloader.getImageProperties().getWidth();
        int height = mTilesDownloader.getImageProperties().getHeight();
        List<Layer> layers = mTilesDownloader.getLayers();
        int[] topLeftCorner = {0, 0};
        int[] topRightCorner = {width - 1, 0};
        int[] bottomLeftCorner = {0, height - 1};
        int[] bottomRightCorner = {width - 1, height - 1};

        for (int layer = 0; layer < layers.size(); layer++) {
            Log.d(TAG, "layer: " + layer);
            int horizontal = layers.get(layer).getTilesHorizontal();
            int vertical = layers.get(layer).getTilesVertical();
            assertTileCoords(mTilesDownloader, layer, topLeftCorner, new int[]{0, 0});
            assertTileCoords(mTilesDownloader, layer, topRightCorner, new int[]{horizontal - 1, 0});
            assertTileCoords(mTilesDownloader, layer, bottomLeftCorner, new int[]{0, vertical - 1});
            assertTileCoords(mTilesDownloader, layer, bottomRightCorner, new int[]{horizontal - 1, vertical - 1});
        }
    }

    private void assertTileCoords(TilesDownloader mTilesDownloader, int layerId, int[] pixel, int[] expectedCoords) {
        int[] actualCoords = mTilesDownloader.getTileCoords(layerId, pixel[0], pixel[1]);
        assertEquals(expectedCoords[0], actualCoords[0]);
        assertEquals(expectedCoords[1], actualCoords[1]);
    }


}

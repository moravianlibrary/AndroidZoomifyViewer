package cz.mzk.tiledimageview.demonstration.intro;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import cz.mzk.tiledimageview.TiledImageView;
import cz.mzk.tiledimageview.demonstration.R;
import cz.mzk.tiledimageview.images.TiledImageProtocol;

/**
 * Created by Martin Řehánek on 21.5.16.
 */
public class IntroTilesActivity extends AppCompatActivity implements TiledImageView.MetadataInitializationListener {

    private static final String TAG = IntroTilesActivity.class.getSimpleName();
    //private static final String BASE_URL = "http://imageserver.mzk.cz/mzk03/001/036/862/2619267293/";
    //private static final String BASE_URL = "http://imageserver.mzk.cz/mzk03/001/048/667/2619269768/";
    //private static final String BASE_URL = "http://imageserver.mzk.cz/mzk03/001/051/015/2619268856_02/";
    private static final String BASE_URL = "http://imageserver.mzk.cz/mzk03/001/051/449/2619269096/";


    private TiledImageView mImageView;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_tiles);
        mProgressView = findViewById(R.id.progressView);
        mImageView = (TiledImageView) findViewById(R.id.tiledImageView);
        mImageView.setMetadataInitializationListener(this);
        showImage();
    }

    private void showImage() {
        Log.v(TAG, "showing image");
        mImageView.setVisibility(View.INVISIBLE);
        mProgressView.setVisibility(View.VISIBLE);
        mImageView.loadImage(TiledImageProtocol.ZOOMIFY, BASE_URL);
    }

    @Override
    public void onMetadataInitialized() {
        mProgressView.setVisibility(View.INVISIBLE);
        mImageView.setVisibility(View.VISIBLE);
    }


    private void toastAndFinish(String message) {
        Toast.makeText(this, "error fetching metadata: " + message, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onMetadataUnhandableResponseCode(String imageMetadataUrl, int responseCode) {
        toastAndFinish("unhandable response code");
    }


    @Override
    public void onMetadataRedirectionLoop(String imageMetadataUrl, int redirections) {
        toastAndFinish("redirection loop");
    }

    @Override
    public void onMetadataDataTransferError(String imageMetadataUrl, String errorMessage) {
        toastAndFinish("transfer error");
    }

    @Override
    public void onMetadataInvalidData(String imageMetadataUrl, String errorMessage) {
        toastAndFinish("invalid data");
    }

    @Override
    public void onCannotExecuteMetadataInitialization(String imageMetadataUrl) {
        toastAndFinish("cannot execute metadata initialization");
    }
}

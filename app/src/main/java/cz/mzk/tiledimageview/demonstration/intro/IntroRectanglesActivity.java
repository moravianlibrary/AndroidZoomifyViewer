package cz.mzk.tiledimageview.demonstration.intro;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.mzk.tiledimageview.TiledImageView;
import cz.mzk.tiledimageview.demonstration.R;
import cz.mzk.tiledimageview.images.TiledImageProtocol;

public class IntroRectanglesActivity extends Activity implements View.OnClickListener, TiledImageView.MetadataInitializationListener {

    private static final String TAG = IntroRectanglesActivity.class.getSimpleName();
    //TODO: replace with something with text
    private static final String BASE_URL = "http://imageserver.mzk.cz/mzk03/001/051/449/2619269096/";

    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.imageView) TiledImageView mImageView;
    @BindView(R.id.progressView) View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_rectangles);
        ButterKnife.bind(this);
        mFab.setOnClickListener(this);
        mProgressView.setOnClickListener(this);
        mImageView.setMetadataInitializationListener(this);
        showImage();
    }

    @Override
    public void onClick(View v) {
        if (v == mFab) {
            Snackbar.make(mFab.getRootView(), "TODO:dialog for adding rectangle", Snackbar.LENGTH_SHORT).show();
        }
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
        mImageView.requestLayout();
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

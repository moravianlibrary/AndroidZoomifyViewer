package cz.mzk.tiledimageview.demonstration.intro;

import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.mzk.tiledimageview.TiledImageView;
import cz.mzk.tiledimageview.demonstration.R;
import cz.mzk.tiledimageview.images.TiledImageProtocol;

/**
 * Created by Martin Řehánek on 21.5.16.
 */
public class IntroGestureActivity extends AppCompatActivity implements TiledImageView.MetadataInitializationListener, TiledImageView.SingleTapListener {

    private static final String TAG = IntroGestureActivity.class.getSimpleName();
    //private static final String BASE_URL = "http://imageserver.mzk.cz/mzk03/001/036/862/2619267293/";
    //private static final String BASE_URL = "http://imageserver.mzk.cz/mzk03/001/048/667/2619269768/";
    //private static final String BASE_URL = "http://imageserver.mzk.cz/mzk03/001/051/015/2619268856_02/";
    //private static final String BASE_URL = "http://imageserver.mzk.cz/mzk03/001/051/449/2619269096/";
    private static final String BASE_URL = "http://imageserver.mzk.cz/mzk03/001/050/049/2619270290/";

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.container) View mContainer;
    @BindView(R.id.imageView) TiledImageView mImageView;
    @BindView(R.id.progressView) View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_gestures);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mImageView.setMetadataInitializationListener(this);
        mImageView.setSingleTapListener(this);
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
        mImageView.requestLayout();
    }

    @Override
    public void onSingleTap(float x, float y, Rect boundingBox) {
        String message = String.format("single tap at: [%d,%d], bounding box: %s", (int) x, (int) y, boundingBox);
        Snackbar.make(mContainer, message, Snackbar.LENGTH_LONG)
                .show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.intro_example, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info:
                showInfoDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.intro_info_gestures)
                .setCancelable(true)
                .setPositiveButton("close",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //nothing
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}

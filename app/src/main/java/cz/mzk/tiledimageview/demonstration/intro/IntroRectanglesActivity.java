package cz.mzk.tiledimageview.demonstration.intro;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.mzk.tiledimageview.TiledImageView;
import cz.mzk.tiledimageview.demonstration.R;
import cz.mzk.tiledimageview.images.TiledImageProtocol;
import cz.mzk.tiledimageview.rectangles.FramingRectangle;

public class IntroRectanglesActivity extends AppCompatActivity implements View.OnClickListener, TiledImageView.MetadataInitializationListener {

    private static final String TAG = IntroRectanglesActivity.class.getSimpleName();
    private static final String BASE_URL = "http://kramerius.mzk.cz/search/zoomify/uuid:5673da95-435f-11dd-b505-00145e5790ea/";

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.imageView) TiledImageView mImageView;
    @BindView(R.id.progressView) View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_rectangles);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProgressView.setOnClickListener(this);
        mImageView.setMetadataInitializationListener(this);
        showImage();
    }

    @Override
    public void onClick(View v) {
        if (v == mProgressView) {
            //nothing, just block
        }
    }

    private void showImage() {
        Log.v(TAG, "showing image");
        mImageView.setVisibility(View.INVISIBLE);
        mProgressView.setVisibility(View.VISIBLE);
        mImageView.loadImage(TiledImageProtocol.ZOOMIFY, BASE_URL);
        mImageView.setFramingRectangles(buildFramingRectangles());
    }

    private List<FramingRectangle> buildFramingRectangles() {
        List<FramingRectangle> list = new ArrayList<>();
        //search result
        list.add(new FramingRectangle(new Rect(2316, 3010, 3836, 3310), new FramingRectangle.Border(R.color.blue, 1), R.color.blue_50));
        //censor bars
        list.add(new FramingRectangle(new Rect(235, 2200, 845, 2380), null, R.color.red));
        list.add(new FramingRectangle(new Rect(848, 4940, 1500, 5265), null, R.color.red));
        //highlighting
        list.add(new FramingRectangle(new Rect(2110, 1445, 2730, 2280), null, R.color.green_50));
        list.add(new FramingRectangle(new Rect(230, 3163, 848, 3645), null, R.color.yellow_50));
        //just border
        list.add(new FramingRectangle(new Rect(3020, 1248, 3804, 2208), new FramingRectangle.Border(R.color.red, 3), null));
        return list;
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

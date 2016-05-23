package cz.mzk.tiledimageview.demonstration.intro.viewMode;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import cz.mzk.tiledimageview.TiledImageView;
import cz.mzk.tiledimageview.demonstration.AppConfig;
import cz.mzk.tiledimageview.demonstration.R;
import cz.mzk.tiledimageview.images.TiledImageProtocol;

/**
 * Created by Martin Řehánek on 21.5.16.
 */
public class IntroViewmodeActivity extends AppCompatActivity implements View.OnClickListener /*implements TiledImageView.MetadataInitializationListener */ {

    private static final String TAG = IntroViewmodeActivity.class.getSimpleName();
    //private static final String BASE_URL = "http://imageserver.mzk.cz/mzk03/001/036/862/2619267293/";
    //private static final String BASE_URL = "http://imageserver.mzk.cz/mzk03/001/048/667/2619269768/";
    private static final String BASE_URL1 = "http://kramerius.mzk.cz/search/zoomify/uuid:dd60c135-27e5-48d4-a81d-cc10f4aa791a/";
    private static final String BASE_URL2 = "http://imageserver.mzk.cz/mzk03/001/051/449/2619269096/";

    private Spinner mSpinner1;
    private Spinner mSpinner2;
    private View mProgressView1;
    private View mProgressView2;
    private TiledImageView mImageView1;
    private TiledImageView mImageView2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_viewmode);
        mSpinner1 = (Spinner) findViewById(R.id.viewModeSpinner1);
        mSpinner2 = (Spinner) findViewById(R.id.viewModeSpinner2);
        mProgressView1 = findViewById(R.id.progressView1);
        mProgressView2 = findViewById(R.id.progressView2);
        mProgressView1.setOnClickListener(this);
        mProgressView2.setOnClickListener(this);
        mImageView1 = (TiledImageView) findViewById(R.id.imageView1);
        mImageView2 = (TiledImageView) findViewById(R.id.imageView2);

        initViewModeSpinner(mSpinner1, mImageView1, BASE_URL1);
        initViewModeSpinner(mSpinner2, mImageView2, BASE_URL2);
        initImageView(mImageView1, mProgressView1, BASE_URL1);
        initImageView(mImageView2, mProgressView2, BASE_URL2);
    }


    private void initImageView(final TiledImageView imageView, final View progressView, final String baseUrl) {
        imageView.setVisibility(View.INVISIBLE);
        progressView.setVisibility(View.VISIBLE);
        imageView.loadImage(TiledImageProtocol.ZOOMIFY, baseUrl);
        imageView.setMetadataInitializationListener(new TiledImageView.MetadataInitializationListener() {
            @Override
            public void onMetadataInitialized() {
                Log.d(TAG, "initialized: " + baseUrl);
                progressView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                imageView.requestLayout();
            }

            private void toast(String message) {
                Toast.makeText(IntroViewmodeActivity.this, "error fetching metadata: " + message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMetadataUnhandableResponseCode(String imageMetadataUrl, int responseCode) {
                toast("unhandable response code");
            }


            @Override
            public void onMetadataRedirectionLoop(String imageMetadataUrl, int redirections) {
                toast("redirection loop");
            }

            @Override
            public void onMetadataDataTransferError(String imageMetadataUrl, String errorMessage) {
                toast("transfer error");
            }

            @Override
            public void onMetadataInvalidData(String imageMetadataUrl, String errorMessage) {
                toast("invalid data");
            }

            @Override
            public void onCannotExecuteMetadataInitialization(String imageMetadataUrl) {
                toast("cannot execute metadata initialization");
            }
        });
    }

    private void initViewModeSpinner(Spinner spinner, final TiledImageView imageView, final String baseUrl) {
        spinner.setAdapter(buildViewmodeSpinnerAdapter());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TiledImageView.ViewMode mode = TiledImageView.ViewMode.values()[position];
                AppConfig.VIEW_MODE = mode;
                if (imageView != null) {
                    imageView.setViewMode(AppConfig.VIEW_MODE);
                    imageView.loadImage(TiledImageProtocol.ZOOMIFY, baseUrl);
                    imageView.requestLayout();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nothing
            }
        });
    }

    private SpinnerAdapter buildViewmodeSpinnerAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_view_mode);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.item_view_mode_dropdown);
        for (TiledImageView.ViewMode mode : TiledImageView.ViewMode.values()) {
            adapter.add(mode.name());
        }
        return adapter;
    }

    @Override
    public void onClick(View v) {
        if (v == mProgressView1 || v == mProgressView2) {
            //nothing, only disable gestures on views bellow progress views
        }
    }

}

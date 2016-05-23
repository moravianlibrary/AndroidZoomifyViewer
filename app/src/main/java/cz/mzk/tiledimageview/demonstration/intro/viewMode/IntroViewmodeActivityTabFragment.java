package cz.mzk.tiledimageview.demonstration.intro.viewMode;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Created by Martin Řehánek on 23.5.16.
 */
public class IntroViewmodeActivityTabFragment extends Fragment implements View.OnClickListener {

    public static final String EXTRA_BASE_URL = "baseUrl";
    private String mBaseurl;

    private Spinner mSpinner;
    private View mProgressView;
    private TiledImageView mImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro_viewmode_tab, container, false);
        //  ((TextView) view.findViewById(R.id.text)).setText(mBaseurl);

        mSpinner = (Spinner) view.findViewById(R.id.viewModeSpinner);
        mProgressView = view.findViewById(R.id.progressView);
        mProgressView.setOnClickListener(this);
        mImageView = (TiledImageView) view.findViewById(R.id.imageView);

        /*initViewModeSpinner(mSpinner1, mImageView1, BASE_URL1);
        initViewModeSpinner(mSpinner2, mImageView2, BASE_URL2);
        initImageView(mImageView1, mProgressView1, BASE_URL1);
        initImageView(mImageView2, mProgressView2, BASE_URL2);*/

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //adapter must be associated with current activity, memory leak otherwise
        initViewModeSpinner();
        initImageView();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBaseurl = getArguments().getString(EXTRA_BASE_URL);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mProgressView) {
            //nothing, only disable gestures on views bellow progress views
        }
    }

    private void initViewModeSpinner() {
        mSpinner.setAdapter(buildViewmodeSpinnerAdapter());
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TiledImageView.ViewMode mode = TiledImageView.ViewMode.values()[position];
                AppConfig.VIEW_MODE = mode;
                if (mImageView != null) {
                    mImageView.setViewMode(AppConfig.VIEW_MODE);
                    mImageView.loadImage(TiledImageProtocol.ZOOMIFY, mBaseurl);
                    mImageView.requestLayout();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nothing
            }
        });
    }

    private SpinnerAdapter buildViewmodeSpinnerAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.item_view_mode);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.item_view_mode_dropdown);
        for (TiledImageView.ViewMode mode : TiledImageView.ViewMode.values()) {
            adapter.add(mode.name());
        }
        return adapter;
    }

    private void initImageView() {
        mImageView.setVisibility(View.INVISIBLE);
        mProgressView.setVisibility(View.VISIBLE);
        mImageView.setMetadataInitializationListener(new TiledImageView.MetadataInitializationListener() {
            @Override
            public void onMetadataInitialized() {
                //Log.d(TAG, "initialized: " + baseUrl);
                mProgressView.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);
                mImageView.requestLayout();
            }

            private void toast(String message) {
                Toast.makeText(getActivity(), "error fetching metadata: " + message, Toast.LENGTH_SHORT).show();
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
        mImageView.loadImage(TiledImageProtocol.ZOOMIFY, mBaseurl);
    }
}

package cz.mzk.androidzoomifyviewer.examples.kramerius;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cz.mzk.androidzoomifyviewer.examples.R;
import cz.mzk.androidzoomifyviewer.examples.kramerius.DownloadPageListTask.PagePidListUtilizer;
import cz.mzk.androidzoomifyviewer.tiles.TileId;
import cz.mzk.androidzoomifyviewer.viewer.PinchZoomManager;
import cz.mzk.androidzoomifyviewer.viewer.PointD;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.ImageInitializationHandler;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.SingleTapListener;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.TileDownloadHandler;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.ViewMode;

/**
 * @author Martin Řehánek
 * 
 */
public class FullscreenPagesActivity extends Activity implements OnClickListener, ImageInitializationHandler,
		TileDownloadHandler, SingleTapListener {
	// public class FullscreenPagesActivity extends Activity implements
	// OnClickListener {

	private static final String TAG = FullscreenPagesActivity.class.getSimpleName();

	public static final String EXTRA_PROTOCOL = "protocol";
	public static final String EXTRA_DOMAIN = "domain";
	public static final String EXTRA_TOP_LEVEL_PID = "topLevelPid";
	public static final String EXTRA_PAGE_ID = "pageId";

	private TiledImageView mImageView;

	// Views to reflect ImageViewer state
	private View mProgressView;
	private View mErrorView;
	private TextView mErrorTitle;
	private TextView mErrorDescription;
	private TextView mErrorResourceUrl;

	// pages data
	private String mProtocol;
	private String mDomain;
	private String mTopLevelPid;
	private int mPageId = 0;
	private List<String> mPagePids;// = TestData.getTestPages();

	// for testing
	private Button mZoomInBtn;
	private Button mZoomOutBtn;
	private Button mPreviousPageBtn;
	private Button mNextPageBtn;
	private Button mTestBtn;
	private CheckBox mShowTilesUnder;
	private CheckBox mfitToScreenMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen_pages);
		if (savedInstanceState != null) {
			restoreData(savedInstanceState);
		} else {
			restoreData(getIntent().getExtras());
		}
		mProgressView = findViewById(R.id.progressView);
		mErrorView = findViewById(R.id.errorView);
		mErrorTitle = (TextView) findViewById(R.id.errorTitle);
		mErrorResourceUrl = (TextView) findViewById(R.id.errorResourceUrl);
		mErrorDescription = (TextView) findViewById(R.id.errorDescription);

		mZoomInBtn = (Button) findViewById(R.id.zoomIn);
		mZoomInBtn.setOnClickListener(this);
		mZoomOutBtn = (Button) findViewById(R.id.zoomOut);
		mZoomOutBtn.setOnClickListener(this);
		mPreviousPageBtn = (Button) findViewById(R.id.previousPage);
		mPreviousPageBtn.setOnClickListener(this);
		mNextPageBtn = (Button) findViewById(R.id.nextPage);
		mNextPageBtn.setOnClickListener(this);
		mTestBtn = (Button) findViewById(R.id.testBtn);
		mTestBtn.setOnClickListener(this);
		mShowTilesUnder = (CheckBox) findViewById(R.id.showLayersUnder);
		mShowTilesUnder.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mImageView.setDrawLayerWithWorseResolution(isChecked);
			}
		});
		mfitToScreenMode = (CheckBox) findViewById(R.id.fitToScreenMode);
		mfitToScreenMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mImageView.setViewMode(isChecked ? ViewMode.FIT_TO_SCREEN : ViewMode.NO_FREE_SPACE_ALIGN_TOP_LEFT);
			}
		});

		mImageView = (TiledImageView) findViewById(R.id.tiledImageView);
		mImageView.setTileDownloadHandler(this);
		mImageView.setSingleTapListener(this);
		// showActivePage();
		// loadPagePids();
	}

	private void showActivePage() {
		mImageView.setVisibility(View.INVISIBLE);
		mProgressView.setVisibility(View.VISIBLE);
		PageDataSource pageDataSource = new PageDataSource(mProtocol, mDomain, mPagePids.get(mPageId));
		mImageView.loadImage(pageDataSource.toZoomifyBaseUrl());
	}

	@Override
	public void onClick(View v) {
		PinchZoomManager zoomManager = mImageView.getZoomManager();
		if (v == mZoomInBtn) {
			zoomManager.setAccumulatedZoomLevel(zoomManager.getAccumulatedZoomLevel() * 1.1f);
			mImageView.invalidate();
		} else if (v == mZoomOutBtn) {
			double zoomLevel = zoomManager.getAccumulatedZoomLevel();
			// if (zoomLevel >= 1.0) {
			zoomManager.setAccumulatedZoomLevel(zoomLevel * 0.9f);
			mImageView.invalidate();
			// }
		} else if (v == mPreviousPageBtn) {
			goToPreviousPageIfPossible();
		} else if (v == mNextPageBtn) {
			goToNextPageIfPossible();
		}
	}

	public void goToNextPageIfPossible() {
		if (mPagePids != null && mPageId != mPagePids.size() - 1) {
			mPageId++;
			showActivePage();
		}
	}

	public void goToPreviousPageIfPossible() {
		if (mPagePids != null && mPageId != 0) {
			mPageId--;
			showActivePage();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
			goToPreviousPageIfPossible();
			return true;
		} else if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
			goToNextPageIfPossible();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(EXTRA_PROTOCOL, mProtocol);
		outState.putString(EXTRA_DOMAIN, mDomain);
		outState.putInt(EXTRA_PAGE_ID, mPageId);
		outState.putString(EXTRA_TOP_LEVEL_PID, mTopLevelPid);
		super.onSaveInstanceState(outState);
	}

	private void restoreData(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			Log.d(TAG, "restoring");
			if (savedInstanceState.containsKey(EXTRA_TOP_LEVEL_PID)) {
				mTopLevelPid = savedInstanceState.getString(EXTRA_TOP_LEVEL_PID);
				// mPagePids = TestData.getTestPages(mTopLevelPid);
			}
			if (savedInstanceState.containsKey(EXTRA_PAGE_ID)) {
				mPageId = savedInstanceState.getInt(EXTRA_PAGE_ID);
			}
			if (savedInstanceState.containsKey(EXTRA_DOMAIN)) {
				mDomain = savedInstanceState.getString(EXTRA_DOMAIN);
			}
			if (savedInstanceState.containsKey(EXTRA_PROTOCOL)) {
				mProtocol = savedInstanceState.getString(EXTRA_PROTOCOL);
			}
			if (mPagePids == null) {
				new DownloadPageListTask(mProtocol, mDomain, mTopLevelPid, new PagePidListUtilizer() {

					@Override
					public void utilize(List<String> pidList) {
						mPagePids = pidList;
						showActivePage();
					}
					// }).execute();
				}).executeConcurrentIfPossible();
			}
		} else {
			Log.d(TAG, "bundle is null");
		}
	}

	@Override
	protected void onStop() {
		mImageView.cancelUnnecessaryTasks();
		super.onStop();
	}

	@Override
	public void onImagePropertiesProcessed() {
		mProgressView.setVisibility(View.INVISIBLE);
		mImageView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onImagePropertiesUnhandableResponseCodeError(String imagePropertiesUrl, int responseCode) {
		mProgressView.setVisibility(View.INVISIBLE);
		mErrorView.setVisibility(View.VISIBLE);
		mErrorTitle.setText("Cannot process server resource");
		mErrorResourceUrl.setText(imagePropertiesUrl);
		mErrorDescription.setText("HTTP code: " + responseCode);
	}

	@Override
	public void onImagePropertiesRedirectionLoopError(String imagePropertiesUrl, int redirections) {
		mProgressView.setVisibility(View.INVISIBLE);
		mErrorView.setVisibility(View.VISIBLE);
		mErrorTitle.setText("Redirection loop");
		mErrorResourceUrl.setText(imagePropertiesUrl);
		mErrorDescription.setText("Too many redirections: " + redirections);
	}

	@Override
	public void onImagePropertiesDataTransferError(String imagePropertiesUrl, String errorMessage) {
		mProgressView.setVisibility(View.INVISIBLE);
		mErrorView.setVisibility(View.VISIBLE);
		mErrorTitle.setText("Data transfer error");
		mErrorResourceUrl.setText(imagePropertiesUrl);
		mErrorDescription.setText(errorMessage);
	}

	@Override
	public void onImagePropertiesInvalidDataError(String imagePropertiesUrl, String errorMessage) {
		mProgressView.setVisibility(View.INVISIBLE);
		mErrorView.setVisibility(View.VISIBLE);
		mErrorTitle.setText("Invalid content in ImageProperties.xml");
		mErrorResourceUrl.setText(imagePropertiesUrl);
		mErrorDescription.setText(errorMessage);
	}

	@Override
	public void onTileProcessed(TileId tileId) {
		// nothing
	}

	@Override
	public void onTileUnhandableResponseError(TileId tileId, String tileUrl, int responseCode) {
		Toast.makeText(this,
				"Failed to download tile " + tileId.toString() + " from '" + tileUrl + "': HTTP error " + responseCode,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onTileRedirectionLoopError(TileId tileId, String tileUrl, int redirections) {
		Toast.makeText(this,
				"Failed to download tile " + tileId.toString() + " from '" + tileUrl + "': redirection loop",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onTileDataTransferError(TileId tileId, String tileUrl, String errorMessage) {
		Toast.makeText(this,
				"Failed to download tile " + tileId.toString() + " from '" + tileUrl + "': " + errorMessage,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onTileInvalidDataError(TileId tileId, String tileUrl, String errorMessage) {
		Toast.makeText(
				this,
				"Failed to download tile " + tileId.toString() + " from '" + tileUrl + "': invalid data: "
						+ errorMessage, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onSingleTap(float x, float y) {
		PointD point = new PointD(x, y);
		Toast.makeText(this, "Single tap at " + point.toString(), Toast.LENGTH_SHORT).show();
	}

}

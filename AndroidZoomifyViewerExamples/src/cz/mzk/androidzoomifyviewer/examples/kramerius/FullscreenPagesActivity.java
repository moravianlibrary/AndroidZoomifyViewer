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
import cz.mzk.androidzoomifyviewer.examples.R;
import cz.mzk.androidzoomifyviewer.examples.kramerius.DownloadPageListTask.PagePidListUtilizer;
import cz.mzk.androidzoomifyviewer.viewer.PinchZoomManager;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.LoadingHandler;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.ViewMode;

/**
 * @author Martin Řehánek
 * 
 */
public class FullscreenPagesActivity extends Activity implements OnClickListener, LoadingHandler {
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
	private View mDownloadErrorView;
	private View mNoAccessRightsView;
	private View mDoesntExistView;
	private View mTmpGeneralErrorView;

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
		mDownloadErrorView = findViewById(R.id.downloadErrorView);
		mNoAccessRightsView = findViewById(R.id.noAccessRightsView);
		mDoesntExistView = findViewById(R.id.doesntExistView);
		mTmpGeneralErrorView = findViewById(R.id.errorView);

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
		mImageView.setLoadingHandler(this);
		// showActivePage();
		// loadPagePids();
	}

	private void showActivePage() {
		mDoesntExistView.setVisibility(View.INVISIBLE);
		mNoAccessRightsView.setVisibility(View.INVISIBLE);
		mDownloadErrorView.setVisibility(View.INVISIBLE);
		mDoesntExistView.setVisibility(View.INVISIBLE);
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
	public void onImagePropertiesProcessed(String imagePropertiesUrl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onImagePropertiesInvalidStateError(String imagePropertiesUrl, int responseCode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onImagePropertiesRedirectionLoopError(String imagePropertiesUrl, int redirections) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onImagePropertiesDataTransferError(String imagePropertiesUrl, String errorMessage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onImagePropertiesInvalidDataError(String imagePropertiesUrl, String errorMessage) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onStop() {
		mImageView.cancelUnnecessaryTasks();
		super.onStop();
	}

}

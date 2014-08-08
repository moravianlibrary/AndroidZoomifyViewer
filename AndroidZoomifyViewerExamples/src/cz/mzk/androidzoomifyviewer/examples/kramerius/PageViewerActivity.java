package cz.mzk.androidzoomifyviewer.examples.kramerius;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import cz.mzk.androidzoomifyviewer.examples.R;
import cz.mzk.androidzoomifyviewer.examples.kramerius.DownloadPageListTask.DownloadPidListResultHandler;
import cz.mzk.androidzoomifyviewer.examples.kramerius.PageViewerFragment.EventListener;

/**
 * @author Martin Řehánek
 * 
 */
public class PageViewerActivity extends Activity implements EventListener {

	private static final String TAG = PageViewerActivity.class.getSimpleName();

	public static final String EXTRA_DOMAIN = "domain";
	public static final String EXTRA_TOP_LEVEL_PID = "topLevelPid";
	public static final String EXTRA_PAGE_ID = "pageId";

	private String mDomain;
	private String mTopLevelPid;
	private int mPageId = 0;
	private List<String> mPagePids;

	private View mViewProgressBar;
	private PageViewerFragment mPageViewerFragment;
	private PageControlsFragment mControlFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_page_viewer);
		mViewProgressBar = findViewById(R.id.viewProgressBar);
		mPageViewerFragment = (PageViewerFragment) getFragmentManager().findFragmentById(R.id.fragmentViewer);
		mPageViewerFragment.setEventListener(this);
		mControlFragment = (PageControlsFragment) getFragmentManager().findFragmentById(R.id.fragmentControls);
		if (savedInstanceState != null) {
			restoreOrLoadData(savedInstanceState);
		} else {
			restoreOrLoadData(getIntent().getExtras());
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(EXTRA_DOMAIN, mDomain);
		outState.putInt(EXTRA_PAGE_ID, mPageId);
		outState.putString(EXTRA_TOP_LEVEL_PID, mTopLevelPid);
		super.onSaveInstanceState(outState);
	}

	private void restoreOrLoadData(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			Log.d(TAG, "restoring data");
			if (savedInstanceState.containsKey(EXTRA_TOP_LEVEL_PID)) {
				mTopLevelPid = savedInstanceState.getString(EXTRA_TOP_LEVEL_PID);
			}
			if (savedInstanceState.containsKey(EXTRA_PAGE_ID)) {
				mPageId = savedInstanceState.getInt(EXTRA_PAGE_ID);
			}
			if (savedInstanceState.containsKey(EXTRA_DOMAIN)) {
				mDomain = savedInstanceState.getString(EXTRA_DOMAIN);
			}
			if (mPagePids == null) {
				new DownloadPageListTask("http", mDomain, mTopLevelPid, new DownloadPidListResultHandler() {

					@Override
					public void onSuccess(List<String> pidList) {
						mPagePids = pidList;
						mViewProgressBar.setVisibility(View.INVISIBLE);
						Log.d(TAG, "initializing PageViewerFragment");
						mPageViewerFragment.init(mDomain, mPagePids);
					}

					@Override
					public void onError(String errorMessage) {
						mViewProgressBar.setVisibility(View.INVISIBLE);
						Toast.makeText(PageViewerActivity.this, "error getting pages: " + errorMessage,
								Toast.LENGTH_LONG).show();
					}

				}).executeConcurrentIfPossible();
			}
		} else {
			Log.d(TAG, "bundle is null");
		}
	}

	@Override
	public void onReady() {
		Log.d(TAG, "PageViewerFragment ready");
		int currentPageIndex = mPageViewerFragment.getCurrentPageIndex();
		Log.d(TAG, "current page: " + currentPageIndex);
		showPage(mPageViewerFragment, currentPageIndex);
	}

	@Override
	public void onSingleTap(float x, float y) {
		Log.d(TAG, "Showing metadata after single tap");
		int pageIndex = mPageViewerFragment.getCurrentPageIndex();
		String pagePid = mPagePids.get(pageIndex);
		Intent intent = new Intent(this, PageMetadataActivity.class);
		intent.putExtra(PageMetadataActivity.EXTRA_TOP_LEVEL_PID, mTopLevelPid);
		intent.putExtra(PageMetadataActivity.EXTRA_PAGE_PID, pagePid);
		intent.putExtra(PageMetadataActivity.EXTRA_PAGE_INDEX, pageIndex);
		startActivity(intent);
	}

	public void showNextPage() {
		int index = mPageViewerFragment.getCurrentPageIndex() + 1;
		Log.d(TAG, "showing next page (" + index + ")");
		showPage(mPageViewerFragment, index);
	}

	public void showPreviousPage() {
		int index = mPageViewerFragment.getCurrentPageIndex() - 1;
		Log.d(TAG, "showing previous page (" + index + ")");
		showPage(mPageViewerFragment, index);
	}

	private void showPage(PageViewerFragment viewerFragment, int index) {
		mControlFragment.setBtnPreviousPageEnabled(index > 0);
		mControlFragment.setBtnNextPageEnabled(index < (mPagePids.size() - 1));
		viewerFragment.showPage(index);
	}

}

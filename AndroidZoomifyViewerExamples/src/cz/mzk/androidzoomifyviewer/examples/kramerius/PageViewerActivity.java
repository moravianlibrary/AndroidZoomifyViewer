package cz.mzk.androidzoomifyviewer.examples.kramerius;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import cz.mzk.androidzoomifyviewer.examples.R;
import cz.mzk.androidzoomifyviewer.examples.kramerius.DownloadPageListTask.DownloadPidListResultHandler;
import cz.mzk.androidzoomifyviewer.examples.kramerius.IPageViewerFragment.EventListener;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.ViewMode;

/**
 * @author Martin Řehánek
 * 
 */
public class PageViewerActivity extends Activity implements EventListener {

	private static final String TAG = PageViewerActivity.class.getSimpleName();

	public static final String EXTRA_DOMAIN = "domain";
	public static final String EXTRA_TOP_LEVEL_PID = "topLevelPid";
	public static final String EXTRA_PAGE_PIDS = "pagePids";

	private String mDomain;
	private String mTopLevelPid;
	private List<String> mPagePids;

	private View mViewProgressBar;
	private IPageViewerFragment mPageViewerFragment;
	private PageControlsFragment mControlFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_page_viewer);
		mViewProgressBar = findViewById(R.id.viewProgressBar);
		mPageViewerFragment = (IPageViewerFragment) getFragmentManager().findFragmentById(R.id.fragmentViewer);
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
		outState.putString(EXTRA_TOP_LEVEL_PID, mTopLevelPid);
		if (mPagePids != null) {
			String[] pidsArray = new String[mPagePids.size()];
			outState.putStringArray(EXTRA_PAGE_PIDS, mPagePids.toArray(pidsArray));
		}
		super.onSaveInstanceState(outState);
	}

	private void restoreOrLoadData(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			Log.d(TAG, "restoring data");
			mTopLevelPid = savedInstanceState.getString(EXTRA_TOP_LEVEL_PID);
			mDomain = savedInstanceState.getString(EXTRA_DOMAIN);
			if (savedInstanceState.containsKey(EXTRA_PAGE_PIDS)) {
				mPagePids = Arrays.asList(savedInstanceState.getStringArray(EXTRA_PAGE_PIDS));
				initPageViewerFragment();
			} else {
				new DownloadPageListTask("http", mDomain, mTopLevelPid, new DownloadPidListResultHandler() {

					@Override
					public void onSuccess(List<String> pidList) {
						mPagePids = pidList;
						initPageViewerFragment();
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

	private void initPageViewerFragment() {
		Log.d(TAG, "initializing PageViewerFragment");
		mViewProgressBar.setVisibility(View.INVISIBLE);
		if (!mPageViewerFragment.isPopulated()) {
			mPageViewerFragment.populate(mDomain, mPagePids);
		} else {
			int currentPageIndex = mPageViewerFragment.getCurrentPageIndex();
			Log.d(TAG, "current page: " + currentPageIndex);
			showPage(currentPageIndex);
		}
	}

	@Override
	public void onReady() {
		Log.d(TAG, "PageViewerFragment ready");
		int currentPageIndex = mPageViewerFragment.getCurrentPageIndex();
		Log.d(TAG, "current page: " + currentPageIndex);
		showPage(currentPageIndex);
	}

	@Override
	public void onSingleTap(float x, float y, Rect boundingBox) {
		Log.d(TAG, "Showing metadata after single tap");
		int pageIndex = mPageViewerFragment.getCurrentPageIndex();
		String pagePid = mPageViewerFragment.getPagePid(pageIndex);
		Intent intent = new Intent(this, PageMetadataActivity.class);
		intent.putExtra(PageMetadataActivity.EXTRA_TOP_LEVEL_PID, mTopLevelPid);
		intent.putExtra(PageMetadataActivity.EXTRA_PAGE_PID, pagePid);
		intent.putExtra(PageMetadataActivity.EXTRA_PAGE_INDEX, pageIndex);
		startActivity(intent);
	}

	public void showNextPage() {
		int index = mPageViewerFragment.getCurrentPageIndex() + 1;
		Log.d(TAG, "showing next page (" + index + ")");
		showPage(index);
	}

	public void showPreviousPage() {
		int index = mPageViewerFragment.getCurrentPageIndex() - 1;
		Log.d(TAG, "showing previous page (" + index + ")");
		showPage(index);
	}

	private void showPage(int index) {
		mControlFragment.setBtnPreviousPageEnabled(index > 0);
		mControlFragment.setBtnNextPageEnabled(index < (mPageViewerFragment.getPageNumber() - 1));
		mControlFragment.setPageCounterContent(mPageViewerFragment.getPageNumber(), index + 1);
		mPageViewerFragment.showPage(index);
	}

	public void setViewMode(ViewMode mode) {
		mPageViewerFragment.setViewMode(mode);
		if (mPageViewerFragment.isPopulated()) {
			mPageViewerFragment.showPage(mPageViewerFragment.getCurrentPageIndex());
		}
	}

}

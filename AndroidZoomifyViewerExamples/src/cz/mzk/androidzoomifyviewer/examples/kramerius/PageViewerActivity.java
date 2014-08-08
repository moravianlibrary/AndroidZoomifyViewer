package cz.mzk.androidzoomifyviewer.examples.kramerius;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_page_viewer);
		PageViewerFragment viewerFragment = (PageViewerFragment) getFragmentManager().findFragmentById(
				R.id.fragmentViewer);
		viewerFragment.setEventListener(this);
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
						initPageViewerFragment();
					}
				}).executeConcurrentIfPossible();
			}
		} else {
			Log.d(TAG, "bundle is null");
		}
	}

	private void initPageViewerFragment() {
		Log.d(TAG, "initializing PageViewerFragment");
		PageViewerFragment viewerFragment = (PageViewerFragment) getFragmentManager().findFragmentById(
				R.id.fragmentViewer);
		viewerFragment.init(mDomain, mPagePids);
	}

	@Override
	public void onReady() {
		Log.d(TAG, "PageViewerFragment ready");
		PageViewerFragment viewerFragment = (PageViewerFragment) getFragmentManager().findFragmentById(
				R.id.fragmentViewer);
		int currentPageIndex = viewerFragment.getCurrentPageIndex();
		Log.d(TAG, "current page: " + currentPageIndex);
		showPage(viewerFragment, viewerFragment.getCurrentPageIndex());
	}

	@Override
	public void onSingleTap(float x, float y) {
		Log.d(TAG, "PageViewerFragment single tap");
		PageViewerFragment viewerFragment = (PageViewerFragment) getFragmentManager().findFragmentById(
				R.id.fragmentViewer);
		int pageIndex = viewerFragment.getCurrentPageIndex();
		String pagePid = mPagePids.get(pageIndex);
		Intent intent = new Intent(this, PageMetadataActivity.class);
		intent.putExtra(PageMetadataActivity.EXTRA_TOP_LEVEL_PID, mTopLevelPid);
		intent.putExtra(PageMetadataActivity.EXTRA_PAGE_PID, pagePid);
		intent.putExtra(PageMetadataActivity.EXTRA_PAGE_INDEX, pageIndex);
		startActivity(intent);
	}

	public void showNextPage() {
		PageViewerFragment viewerFragment = (PageViewerFragment) getFragmentManager().findFragmentById(
				R.id.fragmentViewer);
		int index = viewerFragment.getCurrentPageIndex() + 1;
		Log.d(TAG, "showing next page (" + index + ")");
		showPage(viewerFragment, index);
	}

	public void showPreviousPage() {
		PageViewerFragment viewerFragment = (PageViewerFragment) getFragmentManager().findFragmentById(
				R.id.fragmentViewer);
		int index = viewerFragment.getCurrentPageIndex() - 1;
		Log.d(TAG, "showing previous page (" + index + ")");
		showPage(viewerFragment, index);
	}

	private void showPage(PageViewerFragment viewerFragment, int index) {
		PageControlsFragment controlFragment = (PageControlsFragment) getFragmentManager().findFragmentById(
				R.id.fragmentControls);
		controlFragment.setBtnPreviousPageEnabled(index > 0);
		controlFragment.setBtnNextPageEnabled(index < (mPagePids.size() - 1));
		viewerFragment.goToPage(index);
	}

}

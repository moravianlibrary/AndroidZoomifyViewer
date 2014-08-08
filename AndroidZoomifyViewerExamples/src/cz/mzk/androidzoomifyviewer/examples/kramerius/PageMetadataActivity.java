package cz.mzk.androidzoomifyviewer.examples.kramerius;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import cz.mzk.androidzoomifyviewer.examples.R;

/**
 * @author Martin Řehánek
 * 
 */
public class PageMetadataActivity extends Activity {

	private static final String TAG = PageMetadataActivity.class.getSimpleName();

	public static final String EXTRA_TOP_LEVEL_PID = "topLevelPid";
	public static final String EXTRA_PAGE_PID = "pagePid";
	public static final String EXTRA_PAGE_INDEX = "pageIndex";

	private String mTopLevelPid;
	private String mPagePid;
	private int mPageIndex = -1;

	private TextView mViewTopLevelPid;
	private TextView mViewPagePid;
	private TextView mViewPageIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_page_metadata);
		mViewTopLevelPid = (TextView) findViewById(R.id.topLevelPid);
		mViewPagePid = (TextView) findViewById(R.id.pagePid);
		mViewPageIndex = (TextView) findViewById(R.id.pageIndex);
		if (savedInstanceState != null) {
			restoreData(savedInstanceState);
		} else {
			restoreData(getIntent().getExtras());
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(EXTRA_TOP_LEVEL_PID, mTopLevelPid);
		outState.putString(EXTRA_PAGE_PID, mPagePid);
		outState.putInt(EXTRA_PAGE_INDEX, mPageIndex);
		super.onSaveInstanceState(outState);
	}

	private void restoreData(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			Log.d(TAG, "restoring data");
			if (savedInstanceState.containsKey(EXTRA_TOP_LEVEL_PID)) {
				mTopLevelPid = savedInstanceState.getString(EXTRA_TOP_LEVEL_PID);
			}
			if (savedInstanceState.containsKey(EXTRA_TOP_LEVEL_PID)) {
				mTopLevelPid = savedInstanceState.getString(EXTRA_TOP_LEVEL_PID);
			}
			if (savedInstanceState.containsKey(EXTRA_PAGE_PID)) {
				mPagePid = savedInstanceState.getString(EXTRA_PAGE_PID);
			}
			if (savedInstanceState.containsKey(EXTRA_PAGE_INDEX)) {
				mPageIndex = savedInstanceState.getInt(EXTRA_PAGE_INDEX);
			}
			mViewTopLevelPid.setText("top level pid: " + mTopLevelPid);
			mViewPagePid.setText("page pid: " + mPagePid);
			mViewPageIndex.setText("page index: " + mPageIndex);
		} else {
			Log.d(TAG, "bundle is null");
		}
	}

}

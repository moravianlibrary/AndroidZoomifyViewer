package cz.mzk.androidzoomifyviewer.examples.kramerius;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import cz.mzk.androidzoomifyviewer.examples.R;

/**
 * @author Martin Řehánek
 */
public class PageMetadataActivity extends Activity implements OnClickListener {

    private static final String TAG = PageMetadataActivity.class.getSimpleName();

    public static final String EXTRA_TOP_LEVEL_PID = "topLevelPid";
    public static final String EXTRA_PAGE_PID = "pagePid";
    public static final String EXTRA_PAGE_INDEX = "pageIndex";
    public static final String EXTRA_BOUNDING_BOX_TOP = "boundingBoxTop";
    public static final String EXTRA_BOUNDING_BOX_BOTTOM = "boundingBoxBottom";
    public static final String EXTRA_BOUNDING_BOX_RIGHT = "boundingBoxRight";
    public static final String EXTRA_BOUNDING_BOX_LEFT = "boundingBoxLeft";

    private String mTopLevelPid;
    private String mPagePid;
    private int mPageIndex = -1;
    private Rect mBoundingBox;

    private View mRoot;
    private TextView mViewTopLevelPid;
    private TextView mViewPagePid;
    private TextView mViewPageIndex;
    private TextView mViewBoundingBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_metadata);
        mRoot = findViewById(R.id.root);
        mRoot.setOnClickListener(this);
        mViewTopLevelPid = (TextView) findViewById(R.id.topLevelPid);
        mViewPagePid = (TextView) findViewById(R.id.pagePid);
        mViewPageIndex = (TextView) findViewById(R.id.pageIndex);
        mViewBoundingBox = (TextView) findViewById(R.id.boundingBox);
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
        outState.putInt(EXTRA_BOUNDING_BOX_TOP, mBoundingBox.top);
        outState.putInt(EXTRA_BOUNDING_BOX_BOTTOM, mBoundingBox.bottom);
        outState.putInt(EXTRA_BOUNDING_BOX_RIGHT, mBoundingBox.right);
        outState.putInt(EXTRA_BOUNDING_BOX_LEFT, mBoundingBox.left);
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
            mBoundingBox = new Rect(savedInstanceState.getInt(EXTRA_BOUNDING_BOX_LEFT),
                    savedInstanceState.getInt(EXTRA_BOUNDING_BOX_TOP),
                    savedInstanceState.getInt(EXTRA_BOUNDING_BOX_RIGHT),
                    savedInstanceState.getInt(EXTRA_BOUNDING_BOX_BOTTOM));

            mViewTopLevelPid.setText("top level pid: " + mTopLevelPid);
            mViewPagePid.setText("page pid: " + mPagePid);
            mViewPageIndex.setText("page index: " + mPageIndex);
            mViewBoundingBox.setText("bounding box: " + mBoundingBox.toString());
        } else {
            Log.d(TAG, "bundle is null");
        }
    }

    @Override
    public void onClick(View v) {
        finish();
    }

}

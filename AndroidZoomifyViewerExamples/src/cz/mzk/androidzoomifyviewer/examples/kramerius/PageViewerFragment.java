package cz.mzk.androidzoomifyviewer.examples.kramerius;

import java.util.Arrays;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;
import cz.mzk.androidzoomifyviewer.examples.R;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.ImageInitializationHandler;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.SingleTapListener;

/**
 * @author Martin Řehánek
 * 
 */
public class PageViewerFragment extends Fragment implements IPageViewerFragment, OnTouchListener,
		ImageInitializationHandler, SingleTapListener {

	private static final String TAG = PageViewerFragment.class.getSimpleName();

	public static final String KEY_DOMAIN = PageViewerFragment.class.getSimpleName() + "_domain";
	public static final String KEY_PAGE_PIDS = PageViewerFragment.class.getSimpleName() + "_pagePids";
	public static final String KEY_CURRENT_PAGE_INDEX = PageViewerFragment.class.getSimpleName() + "_pageIndex";
	public static final String KEY_POPULATED = PageViewerFragment.class.getSimpleName() + ":_populated";

	private String mDomain;
	private List<String> mPagePids;
	private int mCurrentPageIndex;

	private TiledImageView mImageView;

	// Views to reflect TiledImageView state
	private View mProgressView;
	private View mErrorView;
	private TextView mErrorTitle;
	private TextView mErrorDescription;
	private TextView mErrorResourceUrl;
	private View mViewNoAccessRights;

	private GestureDetector mGestureDetector;
	private EventListener mEventListener;

	private boolean mPopulated = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mDomain = savedInstanceState.getString(KEY_DOMAIN);
			mPopulated = savedInstanceState.getBoolean(KEY_POPULATED);
			mCurrentPageIndex = savedInstanceState.getInt(KEY_CURRENT_PAGE_INDEX, 0);
			if (savedInstanceState.containsKey(KEY_PAGE_PIDS)) {
				mPagePids = Arrays.asList(savedInstanceState.getStringArray(KEY_PAGE_PIDS));
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_page_viewer, container, false);
		mProgressView = view.findViewById(R.id.progressView);
		mProgressView.setOnTouchListener(this);
		mErrorView = view.findViewById(R.id.errorView);
		mErrorView.setOnTouchListener(this);
		mErrorTitle = (TextView) view.findViewById(R.id.errorTitle);
		mErrorResourceUrl = (TextView) view.findViewById(R.id.errorResourceUrl);
		mErrorDescription = (TextView) view.findViewById(R.id.errorDescription);
		mImageView = (TiledImageView) view.findViewById(R.id.tiledImageView);
		mImageView.setImageInitializationHandler(this);
		// mImageView.setTileDownloadHandler(this);
		mImageView.setSingleTapListener(this);
		mViewNoAccessRights = view.findViewById(R.id.viewNoAccessRights);
		mViewNoAccessRights.setOnTouchListener(this);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mGestureDetector = initGestureDetector(getActivity());
	}

	private GestureDetector initGestureDetector(Context context) {
		return new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				mEventListener.onSingleTap(e.getX(), e.getY());
				return true;
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(KEY_DOMAIN, mDomain);
		outState.putInt(KEY_CURRENT_PAGE_INDEX, mCurrentPageIndex);
		outState.putBoolean(KEY_POPULATED, mPopulated);
		if (mPagePids != null) {
			String[] pidsArray = new String[mPagePids.size()];
			outState.putStringArray(KEY_PAGE_PIDS, mPagePids.toArray(pidsArray));
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void setEventListener(EventListener eventListener) {
		this.mEventListener = eventListener;
	}

	@Override
	public void populate(String domain, List<String> pagePids) {
		Log.d(TAG, "populating");
		this.mDomain = domain;
		this.mPagePids = pagePids;
		this.mCurrentPageIndex = 0;
		this.mPopulated = true;
		hideViews();
		if (mEventListener != null) {
			mEventListener.onReady();
		}
	}

	@Override
	public boolean isPopulated() {
		return mPopulated;
	}

	@Override
	public int getCurrentPageIndex() {
		return mCurrentPageIndex;
	}

	@Override
	public Integer getNextPageIndex() {
		int next = mCurrentPageIndex + 1;
		return next == mPagePids.size() ? null : Integer.valueOf(next);
	}

	@Override
	public Integer getPreviousPageIndex() {
		int next = mCurrentPageIndex - 1;
		return next == -1 ? null : Integer.valueOf(next);
	}

	@Override
	public void showPage(int pageIndex) {
		Log.d(TAG, "Showing page " + pageIndex);
		if (pageIndex >= 0 && pageIndex < mPagePids.size()) {
			hideViews();
			mProgressView.setVisibility(View.VISIBLE);
			mCurrentPageIndex = pageIndex;
			String pid = mPagePids.get(pageIndex);
			String url = buildZoomifyBaseUrl(pid);
			mImageView.loadImage(url.toString());
		} else {
			Log.w(TAG, "Page index out of range: " + pageIndex);
		}
	}

	private String buildZoomifyBaseUrl(String pid) {
		StringBuilder builder = new StringBuilder();
		builder.append("http://");
		builder.append(mDomain).append('/');
		builder.append("search/zoomify/");
		builder.append(pid).append('/');
		return builder.toString();
	}

	private void hideViews() {
		mProgressView.setVisibility(View.INVISIBLE);
		mViewNoAccessRights.setVisibility(View.INVISIBLE);
		mErrorView.setVisibility(View.INVISIBLE);
		mImageView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onImagePropertiesProcessed() {
		Log.d(TAG, "onImagePropertiesProcessed");
		mProgressView.setVisibility(View.INVISIBLE);
		mImageView.setVisibility(View.VISIBLE);
		// TODO: handle situations when tiles not available and whole image
		// should be loaded from datastream instead
	}

	@Override
	public void onImagePropertiesUnhandableResponseCodeError(String imagePropertiesUrl, int responseCode) {
		Log.d(TAG, "onImagePropertiesUnhandableResponseCodeError");
		mProgressView.setVisibility(View.INVISIBLE);
		mImageView.setVisibility(View.INVISIBLE);
		if (responseCode == 403) {
			mErrorView.setVisibility(View.INVISIBLE);
			mViewNoAccessRights.setVisibility(View.VISIBLE);
		} else {
			mViewNoAccessRights.setVisibility(View.INVISIBLE);
			mErrorView.setVisibility(View.VISIBLE);
			mErrorTitle.setText("Cannot process server resource");
			mErrorResourceUrl.setText(imagePropertiesUrl);
			mErrorDescription.setText("HTTP code: " + responseCode);
		}
	}

	@Override
	public void onImagePropertiesRedirectionLoopError(String imagePropertiesUrl, int redirections) {
		Log.d(TAG, "onImagePropertiesRedirectionLoopError");
		mProgressView.setVisibility(View.INVISIBLE);
		mImageView.setVisibility(View.INVISIBLE);
		mViewNoAccessRights.setVisibility(View.INVISIBLE);
		mErrorView.setVisibility(View.VISIBLE);
		mErrorTitle.setText("Redirection loop");
		mErrorResourceUrl.setText(imagePropertiesUrl);
		mErrorDescription.setText("Too many redirections: " + redirections);
	}

	@Override
	public void onImagePropertiesDataTransferError(String imagePropertiesUrl, String errorMessage) {
		Log.d(TAG, "onImagePropertiesDataTransferError");
		mProgressView.setVisibility(View.INVISIBLE);
		mImageView.setVisibility(View.INVISIBLE);
		mViewNoAccessRights.setVisibility(View.INVISIBLE);
		mErrorView.setVisibility(View.VISIBLE);
		mErrorTitle.setText("Data transfer error");
		mErrorResourceUrl.setText(imagePropertiesUrl);
		mErrorDescription.setText(errorMessage);
	}

	@Override
	public void onImagePropertiesInvalidDataError(String imagePropertiesUrl, String errorMessage) {
		Log.d(TAG, "onImagePropertiesInvalidDataError");
		mProgressView.setVisibility(View.INVISIBLE);
		mImageView.setVisibility(View.INVISIBLE);
		mViewNoAccessRights.setVisibility(View.INVISIBLE);
		mErrorView.setVisibility(View.VISIBLE);
		mErrorTitle.setText("Invalid content in ImageProperties.xml");
		mErrorResourceUrl.setText(imagePropertiesUrl);
		mErrorDescription.setText(errorMessage);
	}

	@Override
	public void onSingleTap(float x, float y) {
		if (mEventListener != null) {
			mEventListener.onSingleTap(x, y);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mGestureDetector.onTouchEvent(event);
		return true;
	}

	@Override
	public String getPagePid(int pageIndex) {
		return mPagePids.get(pageIndex);
	}

	@Override
	public int getPageNumber() {
		return mPagePids.size();
	}

}

package cz.mzk.androidzoomifyviewer.examples.kramerius;

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
public class PageViewerFragment extends Fragment implements OnTouchListener, ImageInitializationHandler,
		SingleTapListener {

	private static final String TAG = PageViewerFragment.class.getSimpleName();

	private String mDomain;
	private List<String> mPids;
	private int mCurrentPageIndex;

	private EventListener mEventListener;
	private TiledImageView mImageView;

	// Views to reflect TiledImageView state
	private View mProgressView;
	private View mErrorView;
	private TextView mErrorTitle;
	private TextView mErrorDescription;
	private TextView mErrorResourceUrl;
	private View mViewNoAccessRights;

	private GestureDetector mGestureDetector;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_page_viewer, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mGestureDetector = initGestureDetector(getActivity());
		mProgressView = getView().findViewById(R.id.progressView);
		mProgressView.setOnTouchListener(this);
		mErrorView = getView().findViewById(R.id.errorView);
		mErrorView.setOnTouchListener(this);
		mErrorTitle = (TextView) getView().findViewById(R.id.errorTitle);
		mErrorResourceUrl = (TextView) getView().findViewById(R.id.errorResourceUrl);
		mErrorDescription = (TextView) getView().findViewById(R.id.errorDescription);
		mImageView = (TiledImageView) getView().findViewById(R.id.tiledImageView);
		mImageView.setImageInitializationHandler(this);
		// mImageView.setTileDownloadHandler(this);
		mImageView.setSingleTapListener(this);
		mViewNoAccessRights = getView().findViewById(R.id.viewNoAccessRights);
		mViewNoAccessRights.setOnTouchListener(this);
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

	public void setEventListener(EventListener eventListener) {
		this.mEventListener = eventListener;
	}

	public void init(String domain, List<String> pids) {
		this.mDomain = domain;
		this.mPids = pids;
		this.mCurrentPageIndex = 0;
		if (mEventListener != null) {
			mEventListener.onReady();
		}
	}

	public int getCurrentPageIndex() {
		return mCurrentPageIndex;
	}

	public Integer getNextPageIndex() {
		int next = mCurrentPageIndex + 1;
		return next == mPids.size() ? null : Integer.valueOf(next);
	}

	public Integer getPreviousPageIndex() {
		int next = mCurrentPageIndex - 1;
		return next == -1 ? null : Integer.valueOf(next);
	}

	public void showPage(int pageIndex) {
		Log.d(TAG, "Showing page " + pageIndex);
		if (pageIndex >= 0 && pageIndex < mPids.size()) {
			hideViews();
			mProgressView.setVisibility(View.VISIBLE);
			mCurrentPageIndex = pageIndex;
			String pid = mPids.get(pageIndex);
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

	public interface EventListener {
		public void onReady();

		public void onSingleTap(float x, float y);

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

}

package cz.mzk.androidzoomifyviewer.examples.kramerius;

import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
public class PageViewerFragment extends Fragment implements ImageInitializationHandler, SingleTapListener {

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_page_viewer, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mProgressView = getView().findViewById(R.id.progressView);
		mErrorView = getView().findViewById(R.id.errorView);
		mErrorTitle = (TextView) getView().findViewById(R.id.errorTitle);
		mErrorResourceUrl = (TextView) getView().findViewById(R.id.errorResourceUrl);
		mErrorDescription = (TextView) getView().findViewById(R.id.errorDescription);
		mImageView = (TiledImageView) getView().findViewById(R.id.tiledImageView);
		mImageView.setImageInitializationHandler(this);
		// mImageView.setTileDownloadHandler(this);
		mImageView.setSingleTapListener(this);
		mViewNoAccessRights = getView().findViewById(R.id.viewNoAccessRights);
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

	public void goToPage(int pageIndex) {
		if (pageIndex >= 0 && pageIndex < mPids.size()) {
			resetViews();
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

	private void resetViews() {
		mProgressView.setVisibility(View.VISIBLE);
		mViewNoAccessRights.setVisibility(View.INVISIBLE);
		mErrorView.setVisibility(View.INVISIBLE);
		mImageView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onImagePropertiesProcessed() {
		mProgressView.setVisibility(View.INVISIBLE);
		mImageView.setVisibility(View.VISIBLE);
		// TODO: handle situations when tiles not available and whole image
		// should be loaded from datastream instead
	}

	@Override
	public void onImagePropertiesUnhandableResponseCodeError(String imagePropertiesUrl, int responseCode) {
		if (responseCode == 403) {
			mProgressView.setVisibility(View.INVISIBLE);
			mImageView.setVisibility(View.INVISIBLE);
			mErrorView.setVisibility(View.INVISIBLE);
			mViewNoAccessRights.setVisibility(View.VISIBLE);
		} else {
			mProgressView.setVisibility(View.INVISIBLE);
			mImageView.setVisibility(View.INVISIBLE);
			mViewNoAccessRights.setVisibility(View.INVISIBLE);
			mErrorView.setVisibility(View.VISIBLE);
			mErrorTitle.setText("Cannot process server resource");
			mErrorResourceUrl.setText(imagePropertiesUrl);
			mErrorDescription.setText("HTTP code: " + responseCode);
		}
	}

	@Override
	public void onImagePropertiesRedirectionLoopError(String imagePropertiesUrl, int redirections) {
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
}

package cz.mzk.androidzoomifyviewer.examples.kramerius;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import cz.mzk.androidzoomifyviewer.examples.R;

/**
 * @author Martin Řehánek
 * 
 */
public class PageControlsFragment extends Fragment implements OnClickListener {

	private static final String TAG = PageControlsFragment.class.getSimpleName();

	private Button mBtnPreviousPage;
	private Button mBtnNextPage;
	private TextView mPageCounter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_page_controls, container, false);
		mBtnPreviousPage = (Button) view.findViewById(R.id.btnPreviousPage);
		mBtnPreviousPage.setOnClickListener(this);
		mBtnNextPage = (Button) view.findViewById(R.id.btnNextPage);
		mBtnNextPage.setOnClickListener(this);
		mPageCounter = (TextView) view.findViewById(R.id.pageCounter);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnPreviousPage) {
			PageViewerActivity activity = (PageViewerActivity) getActivity();
			activity.showPreviousPage();
		} else if (v == mBtnNextPage) {
			PageViewerActivity activity = (PageViewerActivity) getActivity();
			activity.showNextPage();
		}
	}

	public void setBtnPreviousPageEnabled(boolean enabled) {
		mBtnPreviousPage.setEnabled(enabled);
	}

	public void setBtnNextPageEnabled(boolean enabled) {
		mBtnNextPage.setEnabled(enabled);
	}

	public void setPageCounterContent(int pages, int activePage) {
		String text = "" + activePage + "/" + pages;
		mPageCounter.setText(text);
	}

}

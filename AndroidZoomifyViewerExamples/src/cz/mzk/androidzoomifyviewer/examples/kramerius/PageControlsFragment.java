package cz.mzk.androidzoomifyviewer.examples.kramerius;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import cz.mzk.androidzoomifyviewer.examples.R;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.ViewMode;

/**
 * @author Martin Řehánek
 * 
 */
public class PageControlsFragment extends Fragment implements OnClickListener, OnItemSelectedListener {

	private static final String TAG = PageControlsFragment.class.getSimpleName();

	private Button mBtnPreviousPage;
	private Button mBtnNextPage;
	private TextView mPageCounter;
	private Spinner mSpinnerViewMode;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_page_controls, container, false);
		mBtnPreviousPage = (Button) view.findViewById(R.id.btnPreviousPage);
		mBtnPreviousPage.setOnClickListener(this);
		mBtnNextPage = (Button) view.findViewById(R.id.btnNextPage);
		mBtnNextPage.setOnClickListener(this);
		mPageCounter = (TextView) view.findViewById(R.id.pageCounter);
		mSpinnerViewMode = (Spinner) view.findViewById(R.id.spinnerViewMode);
		mSpinnerViewMode.setOnItemSelectedListener(this);
		mSpinnerViewMode.setAdapter(getSpinnerAdapter());
		return view;
	}

	private SpinnerAdapter getSpinnerAdapter() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (ViewMode mode : ViewMode.values()) {
			adapter.add("mode " + mode.name());
		}
		return adapter;
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

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		ViewMode mode = ViewMode.values()[position];
		PageViewerActivity activity = (PageViewerActivity) getActivity();
		activity.setViewMode(mode);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

}

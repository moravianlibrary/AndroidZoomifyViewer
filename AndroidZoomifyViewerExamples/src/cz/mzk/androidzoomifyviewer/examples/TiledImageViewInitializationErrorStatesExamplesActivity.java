package cz.mzk.androidzoomifyviewer.examples;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cz.mzk.androidzoomifyviewer.examples.tmp.ErrorUrlsExamples;
import cz.mzk.androidzoomifyviewer.examples.tmp.ErrorUrlsExamples.Example;

/**
 * @author Martin Řehánek
 * 
 */
public class TiledImageViewInitializationErrorStatesExamplesActivity extends Activity {
	private static final String TAG = TiledImageViewInitializationErrorStatesExamplesActivity.class.getSimpleName();

	private ListView mListViewWithExamples;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_initialization_error_states_examples);
		mListViewWithExamples = (ListView) findViewById(R.id.listErrorExamples);
		mListViewWithExamples.setAdapter(new MyAdapter(this, ErrorUrlsExamples.getExamples()));
	}

	class MyAdapter extends ArrayAdapter<Example> {

		private final Context context;
		private final List<Example> itemsArrayList;

		public MyAdapter(Context context, List<Example> list) {
			// TODO: layout zaznamu
			super(context, R.layout.error_list_item, list);
			this.context = context;
			this.itemsArrayList = list;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.error_list_item, parent, false);

			((TextView) rowView.findViewById(R.id.errorCode)).setText(String.valueOf(itemsArrayList.get(position)
					.getErrorCode()));
			((TextView) rowView.findViewById(R.id.errorName)).setText(itemsArrayList.get(position).getErrorName());
			((TextView) rowView.findViewById(R.id.url)).setText(itemsArrayList.get(position).getUrl());

			rowView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String url = itemsArrayList.get(position).getUrl();
					startFullscreenSinglePageViewerActivity(url);
				}
			});
			return rowView;
		}
	}

	private void startFullscreenSinglePageViewerActivity(String zoomifyBaseUrl) {
		Log.d(TAG, "opening '" + zoomifyBaseUrl + "'");
		Intent intent = new Intent(this, FullscreenSingleImageActivity.class);
		intent.putExtra(FullscreenSingleImageActivity.EXTRA_BASE_URL, zoomifyBaseUrl);
		startActivity(intent);
	}

}

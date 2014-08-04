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
public class ImagePropertiesInvalidContentActivity extends Activity {
	private static final String TAG = ImagePropertiesInvalidContentActivity.class.getSimpleName();

	private ListView mListExamples;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_properties_invalid_content);
		mListExamples = (ListView) findViewById(R.id.listExamples);
		mListExamples.setAdapter(new MyAdapter(this, ErrorUrlsExamples.getInvalidContentExamples()));
	}

	class MyAdapter extends ArrayAdapter<Example> {

		private final Context context;
		private final List<Example> itemsArrayList;

		public MyAdapter(Context context, List<Example> list) {
			super(context, R.layout.error_list_item, list);
			this.context = context;
			this.itemsArrayList = list;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.item_img_prop_invalid_content, parent, false);

			((TextView) rowView.findViewById(R.id.description)).setText(String.valueOf(itemsArrayList.get(position)
					.getErrorName()));
			((TextView) rowView.findViewById(R.id.url)).setText(itemsArrayList.get(position).getUrl());

			rowView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String url = itemsArrayList.get(position).getUrl();
					startFullscreenSingleImageActivity(url);
				}
			});
			return rowView;
		}
	}

	private void startFullscreenSingleImageActivity(String zoomifyBaseUrl) {
		Log.d(TAG, "opening '" + zoomifyBaseUrl + "'");
		Intent intent = new Intent(this, FullscreenSingleImageActivity.class);
		intent.putExtra(FullscreenSingleImageActivity.EXTRA_BASE_URL, zoomifyBaseUrl);
		startActivity(intent);
	}

}

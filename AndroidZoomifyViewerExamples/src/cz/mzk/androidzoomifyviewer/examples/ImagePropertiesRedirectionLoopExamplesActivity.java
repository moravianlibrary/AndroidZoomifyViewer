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
import cz.mzk.androidzoomifyviewer.examples.SinglePageExamplesFactory.ImageExampleWithHttpResponseCode;

/**
 * @author Martin Řehánek
 * 
 */
public class ImagePropertiesRedirectionLoopExamplesActivity extends Activity {
	private static final String TAG = ImagePropertiesRedirectionLoopExamplesActivity.class.getSimpleName();

	private ListView mListViewExamples;
	private TextView mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_examples);
		mTitle = (TextView) findViewById(R.id.title);
		mTitle.setText("ImageProperties.xml redirection loop examples");
		mListViewExamples = (ListView) findViewById(R.id.listExamples);
		mListViewExamples.setAdapter(new MyAdapter(this, SinglePageExamplesFactory.getErrorsExamples()));
	}

	class MyAdapter extends ArrayAdapter<ImageExampleWithHttpResponseCode> {

		private final Context context;
		private final List<ImageExampleWithHttpResponseCode> items;

		public MyAdapter(Context context, List<ImageExampleWithHttpResponseCode> items) {
			super(context, R.layout.item_image_with_error_code, items);
			this.context = context;
			this.items = items;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.item_image_with_error_code, parent, false);

			((TextView) rowView.findViewById(R.id.errorCode)).setText(String.valueOf(items.get(position)
					.getErrorCode()));
			((TextView) rowView.findViewById(R.id.errorName)).setText(items.get(position).getErrorName());
			((TextView) rowView.findViewById(R.id.url)).setText(items.get(position).getUrl());

			rowView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String url = items.get(position).getUrl();
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

package cz.mzk.androidzoomifyviewer.examples;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import cz.mzk.androidzoomifyviewer.examples.SingleImageExamplesFactory.ImageExample;

/**
 * @author Martin Řehánek
 */
public class ImagePropertiesInvalidContentExamplesActivity extends ExamplesListActivity {
    private static final String TAG = ImagePropertiesInvalidContentExamplesActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, "ZoomifyImageMetadata.xml", "invalid content examples", new MyAdapter(this,
                SingleImageExamplesFactory.getImagePropertiesInvalidContentExamples()));
    }

    private void startFullscreenSingleImageActivity(String zoomifyBaseUrl) {
        Log.d(TAG, "opening '" + zoomifyBaseUrl + "'");
        Intent intent = new Intent(this, FullscreenSingleImageActivity.class);
        intent.putExtra(FullscreenSingleImageActivity.EXTRA_BASE_URL, zoomifyBaseUrl);
        startActivity(intent);
    }

    class MyAdapter extends ArrayAdapter<ImageExample> {

        private final Context context;
        private final List<ImageExample> itemsArrayList;

        public MyAdapter(Context context, List<ImageExample> list) {
            super(context, R.layout.item_image_with_error_code, list);
            this.context = context;
            this.itemsArrayList = list;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.item_image, parent, false);

            ((TextView) rowView.findViewById(R.id.description)).setText(String.valueOf(itemsArrayList.get(position)
                    .getErrorName()));
            ((TextView) rowView.findViewById(R.id.url)).setText(itemsArrayList.get(position).getUrl());

            rowView.findViewById(R.id.container).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String url = itemsArrayList.get(position).getUrl();
                    startFullscreenSingleImageActivity(url);
                }
            });
            return rowView;
        }
    }

}

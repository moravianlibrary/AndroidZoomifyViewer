package cz.mzk.androidzoomifyviewer.examples;

import java.text.ParseException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import cz.mzk.androidzoomifyviewer.examples.kramerius.KrameriusObjectPersistentUrl;
import cz.mzk.androidzoomifyviewer.examples.tmp.TestData;
import cz.mzk.androidzoomifyviewer.examples.tmp.TestData.MonographExample;

/**
 * @author Martin Řehánek
 * 
 */
public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = MainActivity.class.getSimpleName();

	private Button mQrScanBtn;
	private ListView mListViewWithExamples;
	private Button mBtnSingleImageWorkingExamples;
	private Button mBtnImagePropertiesHttpResponseCodes;
	private Button mBtnImagePropertiesRedirectionLoops;
	private Button mBtnImagePropertiesInvalidContent;
	private Button mBtnImagePropertiesOtherErrors;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mQrScanBtn = (Button) findViewById(R.id.qrScannerBtn);
		mQrScanBtn.setOnClickListener(this);
		mListViewWithExamples = (ListView) findViewById(R.id.buttonList);
		mListViewWithExamples.setAdapter(new MyAdapter(this, TestData.getTestTopLevelUrls()));

		mBtnSingleImageWorkingExamples = (Button) findViewById(R.id.btnSingleImageWorkingExamples);
		mBtnSingleImageWorkingExamples.setOnClickListener(this);
		mBtnImagePropertiesHttpResponseCodes = (Button) findViewById(R.id.btnImagePropertiesHttpResponseCodes);
		mBtnImagePropertiesHttpResponseCodes.setOnClickListener(this);
		mBtnImagePropertiesRedirectionLoops = (Button) findViewById(R.id.btnImagePropertiesRedirectionLoops);
		mBtnImagePropertiesRedirectionLoops.setOnClickListener(this);
		mBtnImagePropertiesInvalidContent = (Button) findViewById(R.id.btnImagePropertiesInvalidContent);
		mBtnImagePropertiesInvalidContent.setOnClickListener(this);
		mBtnImagePropertiesOtherErrors = (Button) findViewById(R.id.btnImagePropertiesOtherErrors);
		mBtnImagePropertiesOtherErrors.setOnClickListener(this);
	}

	class MyAdapter extends ArrayAdapter<MonographExample> {

		private final Context context;
		private final ArrayList<MonographExample> itemsArrayList;

		public MyAdapter(Context context, ArrayList<MonographExample> itemsArrayList) {
			super(context, R.layout.activity_bak_test_list_row, itemsArrayList);
			this.context = context;
			this.itemsArrayList = itemsArrayList;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.activity_bak_test_list_row, parent, false);
			((TextView) rowView.findViewById(R.id.label)).setText(itemsArrayList.get(position).getTitle());
			((TextView) rowView.findViewById(R.id.src)).setText(itemsArrayList.get(position).getSource());
			rowView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String url = itemsArrayList.get(position).getUrl();
					startFullscreenPagesActivity(url);
				}
			});
			return rowView;
		}
	}

	void startFullscreenPagesActivity(String urlStr) {
		try {
			Intent intent = new Intent(this, FullscreenPagesActivity.class);
			KrameriusObjectPersistentUrl url = KrameriusObjectPersistentUrl.valueOf(urlStr);
			intent.putExtra(FullscreenPagesActivity.EXTRA_PROTOCOL, url.getProtocol());
			intent.putExtra(FullscreenPagesActivity.EXTRA_DOMAIN, url.getDomain());
			intent.putExtra(FullscreenPagesActivity.EXTRA_TOP_LEVEL_PID, url.getPid());
			intent.putExtra(FullscreenPagesActivity.EXTRA_PAGE_ID, 0);
			startActivity(intent);
		} catch (ParseException e) {
			Log.e(TAG, "error parsing url '" + urlStr + "'");
			Toast.makeText(this, "error parsing url '" + urlStr + "'", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mQrScanBtn) {
			// String url =
			// "http://docker.mzk.cz/search/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22";
			try {
				IntentIntegrator integrator = new IntentIntegrator(this);
				integrator.initiateScan();
			} catch (Exception e) {
				Log.d(TAG, "error starting qr intent", e);
			}
		} else if (v == mBtnSingleImageWorkingExamples) {
			startActivity(new Intent(this, SinglePageExamplesActivity.class));
		} else if (v == mBtnImagePropertiesHttpResponseCodes) {
			startActivity(new Intent(this, ImagePropertiesHttpResponseCodesActivity.class));
		} else if (v == mBtnImagePropertiesRedirectionLoops) {
			startActivity(new Intent(this, ImagePropertiesRedirectonLoopActivity.class));
		} else if (v == mBtnImagePropertiesInvalidContent) {
			startActivity(new Intent(this, ImagePropertiesInvalidContentActivity.class));
		} else if (v == mBtnImagePropertiesOtherErrors) {
			startActivity(new Intent(this, ImagePropertiesOtherErrorsActivity.class));
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == IntentIntegrator.REQUEST_CODE) {
			IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
			if (scanResult != null) {
				Log.d(TAG, "QR content: " + scanResult.getContents());
				try {
					KrameriusObjectPersistentUrl url = KrameriusObjectPersistentUrl.valueOf(scanResult.getContents());
					// String url = "http://www.example.com";
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url.toString()));
					startActivity(i);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				Log.d(TAG, "no qr scanning result");
			}
		}
	}
}

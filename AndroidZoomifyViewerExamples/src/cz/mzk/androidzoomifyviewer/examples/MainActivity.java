package cz.mzk.androidzoomifyviewer.examples;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import cz.mzk.androidzoomifyviewer.examples.kramerius.KrameriusMultiplePageExamplesActivity;

/**
 * @author Martin Řehánek
 * 
 */
public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = MainActivity.class.getSimpleName();

	private Button mBtnSingleImageWorkingExamples;
	private Button mBtnImagePropertiesHttpResponseCodes;
	private Button mBtnImagePropertiesRedirectionLoops;
	private Button mBtnImagePropertiesInvalidContent;
	private Button mBtnImagePropertiesOtherErrors;
	private Button mBtnKrameriusMultiplePageExamples;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
		mBtnKrameriusMultiplePageExamples = (Button) findViewById(R.id.btnKrameriusMultiplePageExamples);
		mBtnKrameriusMultiplePageExamples.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnSingleImageWorkingExamples) {
			startActivity(new Intent(this, SingleImageWorkingExamplesActivity.class));
		} else if (v == mBtnImagePropertiesHttpResponseCodes) {
			startActivity(new Intent(this, ImagePropertiesHttpResponseCodeExamplesActivity.class));
		} else if (v == mBtnImagePropertiesRedirectionLoops) {
			startActivity(new Intent(this, ImagePropertiesRedirectionLoopExamplesActivity.class));
		} else if (v == mBtnImagePropertiesInvalidContent) {
			startActivity(new Intent(this, ImagePropertiesInvalidContentExamplesActivity.class));
		} else if (v == mBtnImagePropertiesOtherErrors) {
			startActivity(new Intent(this, ImagePropertiesOtherErrorsExamplesActivity.class));
		} else if (v == mBtnKrameriusMultiplePageExamples) {
			startActivity(new Intent(this, KrameriusMultiplePageExamplesActivity.class));
		}
	}

}

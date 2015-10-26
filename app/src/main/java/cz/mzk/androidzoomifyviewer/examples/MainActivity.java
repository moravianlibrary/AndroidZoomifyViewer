package cz.mzk.androidzoomifyviewer.examples;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import cz.mzk.androidzoomifyviewer.examples.kramerius.KrameriusMultiplePageExamplesActivity;
import cz.mzk.androidzoomifyviewer.examples.ssl.SslTestActivity;

/**
 * @author Martin Řehánek
 * 
 */
public class MainActivity extends AppCompatActivity implements OnClickListener {
	private static final String TAG = MainActivity.class.getSimpleName();

	private Toolbar mActionBar;
	private Button mBtnSingleImageWorkingExamples;
	private Button mBtnImagePropertiesHttpResponseCodes;
	private Button mBtnImagePropertiesRedirectionLoops;
	private Button mBtnImagePropertiesInvalidContent;
	private Button mBtnImagePropertiesOtherErrors;
	private Button mBtnKrameriusMultiplePageExamples;
	private Button mBtnSslTest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mActionBar = (Toolbar) findViewById(R.id.action_bar);
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
		mBtnSslTest = (Button) findViewById(R.id.btnSslTest);
		mBtnSslTest.setOnClickListener(this);

		initActionBar();
	}

	private void initActionBar() {
		if (mActionBar != null) {
			setSupportActionBar(mActionBar);
			getSupportActionBar().setDisplayShowTitleEnabled(true);
			getSupportActionBar().setTitle(R.string.actionbar_title);
			getSupportActionBar().setDisplayUseLogoEnabled(false);
			getSupportActionBar().setDisplayShowHomeEnabled(false);
		}
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
		} else if (v == mBtnSslTest) {
			startActivity(new Intent(this, SslTestActivity.class));
		}
	}

}

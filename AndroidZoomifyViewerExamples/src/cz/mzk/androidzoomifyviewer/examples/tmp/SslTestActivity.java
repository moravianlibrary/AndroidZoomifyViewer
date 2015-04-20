package cz.mzk.androidzoomifyviewer.examples.tmp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import cz.mzk.androidzoomifyviewer.examples.R;

public class SslTestActivity extends Activity {

	private RecyclerView mList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ssl_test);
		mList = (RecyclerView) findViewById(R.id.list);
		mList.setLayoutManager(new LinearLayoutManager(this));
		mList.setAdapter(new UrlItemAdapter(this));
	}

}

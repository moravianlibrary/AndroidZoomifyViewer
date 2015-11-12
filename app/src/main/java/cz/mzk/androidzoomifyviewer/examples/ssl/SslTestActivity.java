package cz.mzk.androidzoomifyviewer.examples.ssl;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;

import cz.mzk.androidzoomifyviewer.examples.R;

public class SslTestActivity extends AppCompatActivity {

    private Toolbar mActionBar;
    private RecyclerView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ssl_test);
        mList = (RecyclerView) findViewById(R.id.list);
        mList.setLayoutManager(new LinearLayoutManager(this));
        mList.setAdapter(new UrlItemAdapter(this));
        mActionBar = (Toolbar) findViewById(R.id.action_bar);

        // action bar
        if (mActionBar != null) {
            setSupportActionBar(mActionBar);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setTitle("SSL Test");
            mActionBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
            mActionBar.setNavigationOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

}

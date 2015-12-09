package cz.mzk.tiledimageview.demonstration;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ExamplesListActivity extends AppCompatActivity {

    private Toolbar mActionBar;
    private ListView mListExamples;

    protected void onCreate(Bundle savedInstanceState, String title, String subtitle, ListAdapter adapter) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examples);
        // mTitle = (TextView) findViewById(R.id.title);
        // mTitle.setText("Kramerius multiple page examples");
        mListExamples = (ListView) findViewById(R.id.listExamples);
        mListExamples.setAdapter(adapter);
        // mListExamples.setAdapter(new MyAdapter(this, KrameriusExamplesFactory.getTestTopLevelUrls()));
        mActionBar = (Toolbar) findViewById(R.id.action_bar);
        initActionBar(title, subtitle);
    }

    private void initActionBar(String title, String subtitle) {
        if (mActionBar != null) {
            setSupportActionBar(mActionBar);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            // getSupportActionBar().setTitle(R.string.actionbar_title);
            getSupportActionBar().setTitle(title);
            if (subtitle != null) {
                getSupportActionBar().setSubtitle(subtitle);
            }
            // getSupportActionBar().setSubtitle("Examples");
            getSupportActionBar().setDisplayUseLogoEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // mActionBar.setTitleTextColor(getResources().getColor(R.color.white));
            // mActionBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
            mActionBar.setNavigationOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

}

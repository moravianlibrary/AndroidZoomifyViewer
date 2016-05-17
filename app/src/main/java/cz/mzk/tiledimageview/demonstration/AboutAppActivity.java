package cz.mzk.tiledimageview.demonstration;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

public class AboutAppActivity extends AppCompatActivity {

    private Toolbar mActionBar;
    private TextView mGithubLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);
        mActionBar = (Toolbar) findViewById(R.id.action_bar);
        mGithubLink = (TextView) findViewById(R.id.github_link);

        initActionBar();
        mGithubLink.setMovementMethod(LinkMovementMethod.getInstance());

    }

    private void initActionBar() {
        if (mActionBar != null) {
            setSupportActionBar(mActionBar);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(R.string.actionbar_title_about_app);
            getSupportActionBar().setDisplayUseLogoEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mActionBar.setNavigationOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }
}

package cz.mzk.tiledimageview.demonstration.intro.viewMode;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.mzk.tiledimageview.demonstration.R;

/**
 * Created by Martin Řehánek on 21.5.16.
 */
public class IntroViewmodeActivity extends AppCompatActivity /*implements View.OnClickListener TiledImageView.MetadataInitializationListener */ {

    private static final String TAG = IntroViewmodeActivity.class.getSimpleName();

    @BindView(R.id.pager) ViewPager mViewPager;
    @BindView(R.id.tab_layout) TabLayout mTabLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_viewmode);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initTabLayout();
        initViewPager();
    }

    private void initViewPager() {
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
    }

    private void initTabLayout() {
        mTabLayout.addTab(mTabLayout.newTab().setText("extreme landscape"));
        mTabLayout.addTab(mTabLayout.newTab().setText("extreme portrait"));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.intro_example, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info:
                showInfoDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.intro_info_viewmode)
                .setCancelable(true)
                .setPositiveButton("close",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //nothing
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}

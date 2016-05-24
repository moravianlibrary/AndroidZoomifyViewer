package cz.mzk.tiledimageview.demonstration.intro.viewMode;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

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
}

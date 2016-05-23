package cz.mzk.tiledimageview.demonstration.intro.viewMode;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Martin Řehánek on 23.5.16.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

    //private static final String BASE_URL1 = "http://kramerius.mzk.cz/search/zoomify/uuid:dd60c135-27e5-48d4-a81d-cc10f4aa791a/";
    //private static final String BASE_URL2 = "http://imageserver.mzk.cz/mzk03/001/051/449/2619269096/";
    //http://imageserver.mzk.cz/mzk03/001/049/118/2619270028/
    private static final String BASE_URL1 = "http://imageserver.mzk.cz/mzk03/001/049/037/2619269983/";

    //private static final String BASE_URL2 = "http://imageserver.mzk.cz/mzk03/001/049/012/2619270011/";
    //private static final String BASE_URL2 = "http://imageserver.mzk.cz/mzk03/001/049/088/2619270047_00_01/";
    private static final String BASE_URL2 = "http://imageserver.mzk.cz/mzk03/001/049/138/2619270004_01/";

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        IntroViewmodeActivityTabFragment fragment = new IntroViewmodeActivityTabFragment();
        Bundle args = new Bundle();

        switch (position) {
            case 0:
                args.putString(IntroViewmodeActivityTabFragment.EXTRA_BASE_URL, BASE_URL1);
                break;
            case 1:
                args.putString(IntroViewmodeActivityTabFragment.EXTRA_BASE_URL, BASE_URL2);
                break;
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }
}

package cz.mzk.tiledimageview.demonstration.intro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cz.mzk.tiledimageview.demonstration.R;


public class IntroViewmodeFragment extends Fragment implements View.OnClickListener {

    private Button mBtnTry;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro_viewmode, container, false);
        mBtnTry = (Button) view.findViewById(R.id.btnTry);
        mBtnTry.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if (mBtnTry == v) {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                startActivity(new Intent(activity, IntroViewmodeActivity.class));
            }
        }

    }
}

package cz.mzk.tiledimageview.demonstration.intro;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import cz.mzk.tiledimageview.demonstration.R;


public class IntroTiles extends Fragment implements View.OnClickListener {

    private Button mBtnTry;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro_tiles, container, false);
        mBtnTry = (Button) view.findViewById(R.id.btnTry);
        mBtnTry.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if (mBtnTry == v) {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                Toast.makeText(activity, "TODO: aktivita s fullscreen obrazkem a mrizkou", Toast.LENGTH_SHORT).show();
            }
        }

    }
}

package cz.mzk.tiledimageview.demonstration.intro;

import android.content.Intent;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;

import cz.mzk.tiledimageview.demonstration.MainActivity;
import cz.mzk.tiledimageview.demonstration.R;

/**
 * Created by Martin Řehánek on 20.5.16.
 */
public class IntroActivity extends AppIntro {

    // Please DO NOT override onCreate. Use init.
    @Override
    public void init(Bundle savedInstanceState) {

        // Add your slide's fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.

        addSlide(new IntroTilesFragment());//1
        addSlide(new IntroGesturesFragment());//2
        addSlide(new IntroViewmodeFragment());//3
        addSlide(new IntroRectanglesFragment());//4
        addSlide(new IntroCachingFragment());//5

        // OPTIONAL METHODS
        // Override bar/separator color.
        setBarColor(getResources().getColor(R.color.theme_primary));
        setSeparatorColor(getResources().getColor(R.color.theme_primary_dark));
        //setSeparatorColor(getResources().getColor(R.color.theme_primary_light));

        // Hide Skip/Done button.
        showSkipButton(true);
        setProgressButtonEnabled(true);

        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permisssion in Manifest.
        setVibrate(false);
        //setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onDonePressed() {
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onSlideChanged() {
        // Do something when the slide changes.
    }

    @Override
    public void onNextPressed() {
        // Do something when users tap on Next button.
    }


}
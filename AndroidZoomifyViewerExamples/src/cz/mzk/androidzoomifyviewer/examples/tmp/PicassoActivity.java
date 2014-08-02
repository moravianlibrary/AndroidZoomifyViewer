package cz.mzk.androidzoomifyviewer.examples.tmp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import cz.mzk.androidzoomifyviewer.examples.R;

/**
 * @author Martin Řehánek
 * 
 */
public class PicassoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bak_picasso);
		ImageView imgView = (ImageView) findViewById(R.id.imageView);
		Picasso picasso = Picasso.with(this);
		picasso.setDebugging(true);
		picasso.load("http://i.imgur.com/DvpvklR.png").placeholder(R.drawable.loading).error(R.drawable.error)
				.resize(50, 50).into(imgView);
	}

}

package edu.dartmouth.cs.dartcard;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class PhotoMapActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_map);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.photo_map, menu);
		return true;
	}

}

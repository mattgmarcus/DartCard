package edu.dartmouth.cs.dartcard;

import java.io.FileInputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class PhotoViewActivity extends Activity {
	
	private ImageView mImageView;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photoview);

		mImageView = (ImageView)findViewById(R.id.imageView);
		loadImage();
	}

	
	// Load the image
    private void loadImage() {
    	// Load profile photo from internal storage
    	try {
    		FileInputStream fis = openFileInput(getString(R.string.selected_photo_name));
    		Bitmap bmap = BitmapFactory.decodeStream(fis);
    		mImageView.setImageBitmap(bmap);
    		fis.close();
    	} catch (IOException e) {
    		// Default profile photo if no photo saved before.
    		//mImageView.setImageResource(R.drawable.default_profile);
    	}
    }
    
    public void onNotOkayClicked(View v) {
    	finish();
    	// Should also wipe photo from memory??
    }
    
    public void onOkayClicked(View v) {
    	finish();
    	/*
    	Intent intent = new Intent(MessageActivity.class, this);
    	startActivity(intent);
    	*/
    }
    
}

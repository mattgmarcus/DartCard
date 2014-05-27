package edu.dartmouth.cs.dartcard;

import java.io.FileInputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import edu.dartmouth.cs.dartcard.DartCardDialogFragment.DialogExitListener;

public class PhotoViewActivity extends Activity implements DialogExitListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private ImageView mImageView;

	private LocationClient mLocationClient;

	private boolean fromDatabase;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photoview);

		fromDatabase = getIntent().getBooleanExtra(Globals.IS_FROM_DB_KEY,
				false);
		mImageView = (ImageView) findViewById(R.id.imageView);
		loadImage();

		mLocationClient = new LocationClient(this, this, this);
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
			// mImageView.setImageResource(R.drawable.default_profile);
		}
	}

	public void onNotOkayClicked(View v) {
		finish();
		// Should also wipe photo from memory??
	}

	public void onOkayClicked(View v) {
		// if photo is not from the photo database, then ask if user wants to
		// save
		if (!fromDatabase) {
			DartCardDialogFragment frag = DartCardDialogFragment
					.newInstance(Globals.DIALOG_KEY_SAVE_PHOTO);
			frag.show(getFragmentManager(), "photo save dialog");
		} else {

			Intent intent = new Intent(this, FromActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public void onSavePhotoExit(boolean savePhoto) {
		if (savePhoto) {
			// save photo

			Location location = mLocationClient.getLastLocation();

			if (location == null) {
				// DartCardDialogFragment frag =
				// DartCardDialogFragment.newInstance(Globals.DIALOG_KEY_TRY_SAVE_AGAIN);
				// frag.show(getFragmentManager(), "try again save dialog");
				Toast.makeText(getApplicationContext(), "no location found",
						Toast.LENGTH_SHORT).show();

			} else {
				Toast.makeText(getApplicationContext(),
						"inserting photo with location", Toast.LENGTH_SHORT)
						.show();

				// create new Photo entry
				PhotoEntry photo = new PhotoEntry();
				double lat = location.getLatitude();
				double longi = location.getLongitude();
				photo.setLatitude(lat);
				photo.setLongitude(longi);
				photo.setSectorId(SectorHelper.getSectorIdFromLatLong(lat, longi));
				mImageView.buildDrawingCache();
				photo.setPhotoFromBitmap(mImageView.getDrawingCache());
				PhotoEntryDbHelper db = new PhotoEntryDbHelper(this);
				db.insertPhoto(photo);
				Intent intent = new Intent(this, FromActivity.class);
				startActivity(intent);
			}
		}

	}

	@Override
	public void onTrySaveAgainExit(boolean tryAgain) {
		if (tryAgain) {
			onSavePhotoExit(true);
		} else {
			onSavePhotoExit(false);
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
	}

	@Override
	protected void onStop() {
		mLocationClient.disconnect();
		super.onStop();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

}

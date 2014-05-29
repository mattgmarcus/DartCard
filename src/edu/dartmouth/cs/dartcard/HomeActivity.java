package edu.dartmouth.cs.dartcard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class HomeActivity extends Activity {

	private Uri mImageCaptureUri;

	public static final int REQUEST_CODE_TAKE_FROM_CAMERA = 0;
	public static final int REQUEST_CODE_GET_FROM_GALLERY = 1;
	public static final int REQUEST_CODE_CROP_PHOTO = 2;

	private static final String IMAGE_UNSPECIFIED = "image/*";

	private byte[] mProfilePictureArray;
	
	private ActionBar mActionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		mActionBar = getActionBar();
		mActionBar.setTitle("DartCard");
	}

	//launch intent to take a photo with the phone's camera
	public void onTakeClicked(View v) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		mImageCaptureUri = Uri.fromFile(new File(Environment
				.getExternalStorageDirectory(), "tmp_"
				+ String.valueOf(System.currentTimeMillis()) + ".jpg"));
		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
				mImageCaptureUri);

		intent.putExtra("return-data", true);

		try {
			startActivityForResult(intent, REQUEST_CODE_TAKE_FROM_CAMERA);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	//launch intent to choose photo from the phone's local gallery
	public void onChooseClicked(View v) {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		startActivityForResult(Intent.createChooser(intent, "Select File"),
				REQUEST_CODE_GET_FROM_GALLERY);
	}

	//launch intent to choose photo from the app's online database
	public void onFindClicked(View v) {
		Intent intent = new Intent(this, PhotoMapActivity.class);
		startActivity(intent);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;
		switch (requestCode) {
		case REQUEST_CODE_TAKE_FROM_CAMERA:
			// Send image taken from camera for cropping
			cropImage();
			break;
			
		case REQUEST_CODE_CROP_PHOTO:
			// Update image view after image crop
			Bundle extras = data.getExtras();
			try {
				Bitmap photo = (Bitmap) extras.getParcelable("data");
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				photo.compress(Bitmap.CompressFormat.PNG, 100, bos);
				mProfilePictureArray = bos.toByteArray();
				// go from byteArray to FOS
				FileOutputStream fos = openFileOutput(
						getString(R.string.selected_photo_name), MODE_PRIVATE);
				fos.write(mProfilePictureArray);
				fos.flush();
				fos.close();
				bos.close();
				Intent intent = new Intent(this, PhotoViewActivity.class);
				intent.putExtra(Globals.IS_FROM_DB_KEY, false);
				startActivity(intent);
			} catch (IOException ioe) {
				//Pass
			}
			break;
			
		case REQUEST_CODE_GET_FROM_GALLERY:
			Uri selectedImageUri = data.getData();
			mImageCaptureUri = selectedImageUri;
			cropImage();
			break;
		}
	}

	// Crop the image the camera takes
	private void cropImage() {
		// Use existing crop activity.
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(mImageCaptureUri, IMAGE_UNSPECIFIED);

		// Specify image size
		intent.putExtra("outputX", 720);
		intent.putExtra("outputY", 480);

		// Specify aspect ratio, 1:1
		intent.putExtra("aspectX", 3);
		intent.putExtra("aspectY", 2);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		// REQUEST_CODE_CROP_PHOTO is an integer tag you defined to
		// identify the activity in onActivityResult() when it returns
		startActivityForResult(intent, REQUEST_CODE_CROP_PHOTO);
	}
}

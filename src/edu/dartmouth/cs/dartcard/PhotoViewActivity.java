package edu.dartmouth.cs.dartcard;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import edu.dartmouth.cs.dartcard.DartCardDialogFragment.DialogExitListener;
import edu.dartmouth.cs.dartcard.StripeUtilities.CardResponse;

public class PhotoViewActivity extends Activity implements DialogExitListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private ImageView mImageView;

	private LocationClient mLocationClient;

	private boolean fromDatabase;
	private Bitmap bmap;

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
			bmap = BitmapFactory.decodeStream(fis);
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
				//mImageView.buildDrawingCache();
				//photo.setPhotoFromBitmap(mImageView.getDrawingCache());
				double lat = location.getLatitude();
				double longi = location.getLongitude();
				photo.setLatitude(lat);
				photo.setLongitude(longi);
				photo.setSectorId(SectorHelper.getSectorIdFromLatLong(lat, longi));
				photo.setPhotoFromBitmap(bmap);
				savePhoto(photo);
				PhotoEntryDbHelper db = new PhotoEntryDbHelper(this);
				db.insertPhoto(photo);
				//Intent intent = new Intent(this, FromActivity.class);
				//startActivity(intent);
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
	
	private boolean savePhoto(PhotoEntry photo) {
		PhotoTask task = new PhotoTask(photo, this);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
		else
			task.execute((Void[])null);
		
		return true;

	}

	public class PhotoTask extends AsyncTask<Void,Void,Void> {
		private PhotoEntry photo;
		private Activity activity;
		private boolean DEBUG = true;
		
		public PhotoTask(PhotoEntry photo, Activity activity) {
			this.photo = photo;
			this.activity = activity;
		}
		
		protected Void doInBackground(Void ... p) {
			HttpClient httpClient = new DefaultHttpClient();

	        HttpGet get = new HttpGet(activity.getString(R.string.server_addr)+"/blob/getuploadurl");
	        HttpResponse response = null;
			try {
				response = httpClient.execute(get);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
	        String uploadURL = "";
	        try {
				uploadURL = EntityUtils.toString(response.getEntity()).trim();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
			httpClient = new DefaultHttpClient();
			if (DEBUG) {
				String[] url_parts = uploadURL.split("_");
				uploadURL = activity.getString(R.string.server_addr)+"/_"+url_parts[1];
			}
			Log.d("upload url is" , uploadURL);
			
			HttpPost httppost = new HttpPost(uploadURL);

			ByteArrayBody attachment = new ByteArrayBody(photo.getPhotoByteArray(), "image");				
			MultipartEntity reqEntity = new MultipartEntity();

			reqEntity.addPart("image", attachment);

			httppost.setEntity(reqEntity);
			response = null;
			String blobKey = null;

			try {
				response = httpClient.execute(httppost);
				//Kinda hacky way to get the blobKey from the entity
				blobKey = EntityUtils.toString(response.getEntity()).split(":")[1].split("\"")[1];
				Log.d("blobkey is ", blobKey);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONArray jsonArray = new JSONArray();
			jsonArray.put(photo.toJSONObject());
			Log.d("photo json", jsonArray.toString());

			HashMap<String, String> data = new HashMap<String, String>();
			data.put("blobKey", blobKey);
			data.put("data", jsonArray.toString());
			HttpUtilities.post(activity.getString(R.string.server_addr)+"/save", data);

			return null;
		}
		/*@Override
		protected Void doInBackground(Void ... p) {
			String url = 
					HttpUtilities.get(activity.getString(R.string.server_addr)+"/blob/getuploadurl");
			
			Log.d("return url is", url);
			if (null != url) {
				HttpClient httpclient = new DefaultHttpClient();
				if (DEBUG) {
					String[] url_parts = url.split(":");
					url = activity.getString(R.string.server_addr)+url_parts[1];
				}
				HttpPost httppost = new HttpPost(url);

				ContentBody attachment = new ByteArrayBody(photo.getPhotoByteArray(), "image");				MultipartEntity reqEntity = new MultipartEntity();

				reqEntity.addPart("file", attachment);

				httppost.setEntity(reqEntity);
				HttpResponse response = null;
				try {
					response = httpclient.execute(httppost);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.d("Got a rsponse!", response.toString());
				
				JSONObject resultJson = null;
				try {
					resultJson = new JSONObject(response.toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//Last step
				String blobKey = null;
				try {
					blobKey = resultJson.getString("blobKey");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				JSONArray jsonArray = new JSONArray();
				jsonArray.put(photo.toJSONObject());

				HashMap<String, String> data = new HashMap<String, String>();
				data.put("blobKey", blobKey);
				data.put("data", jsonArray.toString());
				HttpUtilities.post(activity.getString(R.string.server_addr)+"/save", data);
			}
			return null;
		}*/
		

		@Override
		protected void onPostExecute(Void result) {
			/*mProgressDialog.dismiss();
			if (!result) {
				Log.d("PayActivity.async.onpostexecute", "Failed!");
				
				mPayButton.setEnabled(true);	
			}
			else {
				Intent intent = new Intent(activity, HomeActivity.class);
				
				activity.startActivity(intent);
			}*/
			Intent intent = new Intent(activity, FromActivity.class);
			activity.startActivity(intent);
		}
	};


}

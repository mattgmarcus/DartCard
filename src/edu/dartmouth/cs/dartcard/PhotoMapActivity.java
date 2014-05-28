package edu.dartmouth.cs.dartcard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import edu.dartmouth.cs.dartcard.DartCardDialogFragment.DialogExitListener;
import edu.dartmouth.cs.dartcard.PayActivity.StripeTask;
import edu.dartmouth.cs.dartcard.StripeUtilities.CardResponse;

public class PhotoMapActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, DialogExitListener{
	private static final String TAB_KEY_INDEX = "tab_key";

	PhotoMapFragment mapFragment;
	PhotoGridFragment gridFragment;
	Location location;
	private LocationClient mLocationClient;
	private GoogleMap map;

	PriorityQueue<PhotoEntry> closest100Photos;
	
	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("DartCard", "onCreate in photomapactivity");
		setContentView(R.layout.activity_photo_map);

		// setup action tabs
		ActionBar actionbar = getActionBar();
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// set up the tabs for the action bar
		ActionBar.Tab gridTab = actionbar.newTab().setText(
				getString(R.string.grid_tab_name));
		ActionBar.Tab mapTab = actionbar.newTab().setText(
				getString(R.string.map_tab_name));

		// set up the fragments that correspond to the tabs
		gridFragment = new PhotoGridFragment();
		mapFragment = new PhotoMapFragment();

		// link fragments to tabs by setting up listeners for tabs
		gridTab.setTabListener(new MyTabListener(gridFragment,
				getApplicationContext()));
		mapTab.setTabListener(new MyTabListener(mapFragment,
				getApplicationContext()));

		// add the tabs to the actionbar
		actionbar.addTab(gridTab);
		actionbar.addTab(mapTab);

		// stay on the same tab if the screen is rotated
		if (savedInstanceState != null) {
			actionbar.setSelectedNavigationItem(savedInstanceState.getInt(
					TAB_KEY_INDEX, 0));
		}
		
		
		mLocationClient = new LocationClient(this, this, this);
		map = mapFragment.getMap();
		
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle("Loading images");
		mProgressDialog.setMessage("This should take only a second");
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.show();


	}

	// keep track of which tab we're on when the screen is rotated
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(TAB_KEY_INDEX, getActionBar()
				.getSelectedNavigationIndex());

	}

	class MyTabListener implements ActionBar.TabListener {
		public Fragment fragment;
		public Context context;

		// set the fragment and context correspondding to the tab
		public MyTabListener(Fragment fragment, Context context) {
			this.fragment = fragment;
			this.context = context;
		}

		// when a tab is selected, set its to the current visible fragment
		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			ft.replace(R.id.photo_view_container, fragment);
		}

		// remove the fragment when the tab is unselected
		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			ft.remove(fragment);
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}

	}

	@Override
	protected void onStart(){
		super.onStart();
		mLocationClient.connect();
	}
	
    @Override
    protected void onStop() {
        mLocationClient.disconnect();
        super.onStop();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	gridFragment.setAdapter();
        	gridFragment.setNumColumns(3);
           
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
        	gridFragment.setAdapter();
        	gridFragment.setNumColumns(3);
        }
    }

	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		location = mLocationClient.getLastLocation();
		
		PhotoTask task = new PhotoTask(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
		else
			task.execute((Void[])null);

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	
	private ArrayList<PhotoEntry> fetchPhotos(int sectorId) {
		ArrayList<PhotoEntry> photos = new ArrayList<PhotoEntry>(); 
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("sector", String.valueOf(sectorId));
		Log.d("params", params.toString());
		String result = HttpUtilities.get(getString(R.string.server_addr)+"/serve?sector="+sectorId);
		
		if (null == result) {
			return null;
		}
		
		try {
			JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {
		        JSONObject data = jsonArray.getJSONObject(i);
		        photos.add(new PhotoEntry(data));
			}
		} catch (JSONException e) {
			return null;
		}
		
		return photos;
		
	}
	
	public PriorityQueue<PhotoEntry> fetch100ClosestPhotoEntries(Context context, Location location) {
		Comparator<PhotoEntry> comparator = new LocationComparator(location);
		PriorityQueue<PhotoEntry> photoQueue = new PriorityQueue<PhotoEntry>(100, comparator);
		//calculate the sector id for current location
		int sectorId = SectorHelper.getSectorIdFromLatLong(location.getLatitude(), location.getLongitude());
		Log.d("DartCard", "Current Sector Id is " + sectorId);
		
		ArrayList<PhotoEntry> sectorPhotoList = fetchPhotos(sectorId);
		
		if (null == sectorPhotoList) {
			return null;
		}
		
		//go through all entries, if the list fills up, keep the shortest
		//distanced photos
		for (PhotoEntry currEntry : sectorPhotoList){
			Log.d("DartCard","Adding to pq a photo from " + currEntry.getLatitude() + ", " + currEntry.getLongitude());
			photoQueue.add(currEntry);
			if (photoQueue.size() > 100){
				photoQueue.poll();
			}
		}			
		
		return photoQueue;
	}

	/*public static PriorityQueue<PhotoEntry> fetch100ClosestPhotoEntries(Context context, Location location) {
		Comparator<PhotoEntry> comparator = new LocationComparator(location);
		PriorityQueue<PhotoEntry> photoQueue = new PriorityQueue<PhotoEntry>(100, comparator);
		//calculate the sector id for current location
		int sectorId = SectorHelper.getSectorIdFromLatLong(location.getLatitude(), location.getLongitude());
		Log.d("DartCard", "Current Sector Id is " + sectorId);
		PhotoEntryDbHelper db = new PhotoEntryDbHelper(context);
		ArrayList<PhotoEntry> sectorPhotoList = db.fetchSectorEntries(sectorId);
		ArrayList<PhotoEntry> allPhotos = db.fetchEntries();
		Log.d("DartCard", "size of photolist for sector is " + sectorPhotoList.size());
		Log.d("DartCard", "number of photos in database is " + allPhotos.size());
		//go through all entries, if the list fills up, keep the shortest
		//distanced photos
		for (PhotoEntry currEntry : sectorPhotoList){
			photoQueue.add(currEntry);
			if (photoQueue.size() > 100){
				photoQueue.poll();
			}
		}
		
		//once you've gotten all photos from the current sector, if you
		//aren't at the limit yet, get photos from adjacent sectors, also, mainting
		//the hundred closest photos in the queue. locations in adjacent sectors could be closer,
		//so just keep adding
		if (photoQueue.size() < 100){
			int[] adjacentSectors = SectorHelper.getAdjacentSectors(sectorId);
			//if it's a boundary case for adjacent secotrs, the getADjacentSEctors
			//call will return an empty array and only the current sector will be loaded
			for (int i = 0; i < adjacentSectors.length; i++){
				int currSecId = adjacentSectors[i];
				ArrayList<PhotoEntry> secPhotoList = db.fetchSectorEntries(currSecId);
				for (PhotoEntry currEntry : secPhotoList){
					photoQueue.add(currEntry);
					if (photoQueue.size() > 100){
						photoQueue.poll();
					}
				}
			}
		}
			
		
		return photoQueue;
	}*/
	
	public static class LocationComparator implements Comparator<PhotoEntry>{
		
		private Location location;
		public LocationComparator(Location loc){
			location = loc;
		}
		
		@Override
		public int compare(PhotoEntry photo1, PhotoEntry photo2) {
			
			Location location1 = new Location("");
			location1.setLatitude(photo1.getLatitude());
			location1.setLongitude(photo1.getLongitude());
			
			Location location2 = new Location("");
			location2.setLatitude(photo2.getLatitude());
			location2.setLongitude(photo2.getLongitude());
			
			double distance1 = location.distanceTo(location1);
			double distance2 = location.distanceTo(location2);
			Log.d("DartCard", "Current location is " + location.getLatitude() + ", " + location.getLongitude());
			Log.d("DartCard", "distance to photo 1 is " + distance1);
			Log.d("DartCard", "distance to photo 2 is " + distance2);
			//want the furtherst distance to stay at top of queue, so we can get it when
			//we poll. so return positive if distance 1 is greater than distance 2
			if (distance1 > distance2){
				Log.d("DartCard", "returning photo1 > photo2");
				return 1;
			}
			else if (distance1 < distance2){
				Log.d("DartCard", "returning photo1 < photo2");
				return -1;
			}
			else
				return 0;
		}
		
	}
	
	public class PhotoTask extends AsyncTask<Void,Void,Boolean> {
		private Activity activity;
		
		public PhotoTask(Activity activity) {
			this.activity = activity;
		}
		
		@Override
		protected Boolean doInBackground(Void ... p) {
			closest100Photos = fetch100ClosestPhotoEntries(activity, location);
			return null != closest100Photos;
		}
		

		@Override
		protected void onPostExecute(Boolean success) {
			if (!success) {
				DartCardDialogFragment frag = DartCardDialogFragment
						.newInstance(Globals.DIALOG_PHOTO_MAP_ERROR);
				frag.show(getFragmentManager(), "photo map error dialog");
			}
			else {
				//refresh both fragments once connected
				gridFragment.updateGridView(closest100Photos);
				mapFragment.updateMap(closest100Photos);
				
				mProgressDialog.dismiss();
			}
		}
	}

	@Override
	public void onReturn() {
		finish();
	};

	@Override
	public void onSavePhotoExit(boolean savePhoto) {}
	@Override
	public void onTrySaveAgainExit(boolean tryAgain) {}

}

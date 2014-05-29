package edu.dartmouth.cs.dartcard;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PhotoMapFragment extends MapFragment implements
		OnMarkerClickListener {
	Context context;
	GoogleMap map;
	//this is a map that links map markers to photoentries
	Map<Marker, PhotoEntry> markerPhotos = new HashMap<Marker, PhotoEntry>();
	Marker locationMarker;
	Location location;

	@Override
	public void onResume() {
		super.onResume();
		context = getActivity();
		//get the current location of the phone
		location = ((PhotoMapActivity) getActivity()).location;
		if (location == null) {
			Log.i("DartCard", "location is null");
		}

		//zoom the camera in on the current location, populate the map with markers
		//representing photoentries
		map = getMap();
		if (map != null) {
			map.setOnMarkerClickListener(this);
			if (location != null) {
				LatLng latLng = new LatLng(location.getLatitude(),
						location.getLongitude());
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
			}
			PriorityQueue<PhotoEntry> photoQueue = ((PhotoMapActivity) getActivity()).closestPhotos;
			updateMap(photoQueue);
		}
	}

	//given a priorityqueue of photoentries, puts markers on the map
	//that represent those entries
	public void updateMap(PriorityQueue<PhotoEntry> photos) {
		map = getMap();
		if (photos != null && map != null) {
			for (PhotoEntry photo : photos) {
				Log.i("DartCard", "photo's lat is " + photo.getLatitude());
				LatLng latlng = new LatLng(photo.getLatitude(),
						photo.getLongitude());
				Marker marker = map.addMarker(new MarkerOptions()
						.position(latlng));
				//link this marker to the photoentry it corresponds to
				markerPhotos.put(marker, photo);
			}
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		// launch photoviewactivity with photo corresponding to marker
		// so save the photo to the location where photoviewactivity will load
		// its bitmap from from
		byte[] pic = markerPhotos.get(marker).getPhoto();
		try {
			FileOutputStream fileOut = context.openFileOutput(
					getString(R.string.selected_photo_name),
					context.MODE_PRIVATE);
			fileOut.write(pic);
			fileOut.flush();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Intent i = new Intent(getActivity(), PhotoViewActivity.class);
		i.putExtra(Globals.IS_FROM_DB_KEY, true);
		startActivity(i);

		return true;
	}

}

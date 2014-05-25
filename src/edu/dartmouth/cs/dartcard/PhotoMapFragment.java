package edu.dartmouth.cs.dartcard;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PhotoMapFragment extends MapFragment implements
		OnMarkerClickListener {
	Context context;
	GoogleMap map;
	Map<Marker, PhotoEntry> markerPhotos = new HashMap<Marker, PhotoEntry>();

	@Override
	public void onResume() {
		super.onResume();
		Location location = ((PhotoMapActivity) getActivity()).location;
		if (location == null) {
			Log.i("DartCard", "location is null");
		}
		map = getMap();
		map.setOnMarkerClickListener(this);
		if (map == null) {
			Log.i("DartCard", "map is null");
			Toast.makeText(getActivity(), "map is null", Toast.LENGTH_SHORT)
					.show();
		} else {
			// get photos from database
			PhotoEntryDbHelper db = new PhotoEntryDbHelper(getActivity());
			ArrayList<PhotoEntry> photos = db.fetchEntries();
			for (PhotoEntry photo : photos) {
				Log.i("DartCard", "photo's lat is " + photo.getLatitude());

				Marker marker = map.addMarker(new MarkerOptions()
						.position(new LatLng(photo.getLatitude(), photo
								.getLongitude())));
				markerPhotos.put(marker, photo);

			}
		}

		if (location != null) {
			LatLng latLng = new LatLng(location.getLatitude(),
					location.getLongitude());
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		// launch photoviewactivity with photo corresponding to marker
		// so save the photo to the location where photoviewactivity will load
		// from
		byte[] pic = markerPhotos.get(marker).getPhoto();
		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(
							getString(R.string.selected_photo_name)));
			bos.write(pic);
			bos.flush();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Intent i = new Intent(getActivity(), PhotoViewActivity.class);
		i.putExtra(Globals.IS_FROM_DB_KEY, true);
		startActivity(i);
		return true;
	}

}

package edu.dartmouth.cs.dartcard;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Base64;
import android.util.Log;

public class PhotoEntry {
	private Long id;
	private byte[] photo;
	private double latitude;
	private double longitude;
	private int sectorId;

	public PhotoEntry() {
	}

	public PhotoEntry(JSONObject data) {
		fromJSONObject(data);
	}

	public Long getId(){
		return this.id;
	}
	
	public byte[] getPhoto() {
		return photo;
	}

	public void setPhoto(byte[] pic) {
		photo = pic;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLatitude(double lat) {
		latitude = lat;
	}

	public void setLongitude(double longi) {
		longitude = longi;
	}

	public void setSectorId(int secId) {
		sectorId = secId;

	}

	public void setPhotoFromByteArray(byte[] blob) {
		photo = blob;

	}

	public void setId(long long1) {
		id = long1;

	}

	public int getSectorId() {
		return sectorId;
	}

	public byte[] getPhotoByteArray() {
		return photo;
	}

	public void setPhotoFromBitmap(Bitmap bmap) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bmap.compress(Bitmap.CompressFormat.PNG, 100, out);
		photo = out.toByteArray();
	}

	public Bitmap getBitmapPhoto() {
		return BitmapFactory.decodeByteArray(photo, 0, photo.length);
	}

	public void fromJSONObject(JSONObject obj) {
			try {
				setPhotoFromByteArray(Base64.decode(obj.getString("image").getBytes(), Base64.DEFAULT));
				latitude = obj.getDouble("latitude");
				longitude = obj.getDouble("longitude");
				sectorId = obj.getInt("sector");
			} catch (JSONException e) {
				// Fail silently
			}
	}
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("latitude", latitude);
			obj.put("longitude", longitude);
			obj.put("sector", sectorId);
		} 
		catch (JSONException e) {
			return null;
		}
		
		return obj;

	}
	
	

}

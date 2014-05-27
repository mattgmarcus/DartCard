package edu.dartmouth.cs.dartcard;

import java.io.ByteArrayOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

public class PhotoEntry {
	private Long id;
	private byte[] photo;
	private double latitude;
	private double longitude;
	private int sectorId;
	
	public PhotoEntry(){}
	
	public byte[] getPhoto(){
		return photo;
	}
	public void setPhoto(byte[] pic){
		photo = pic;
	}
	
	public double getLatitude(){
		return latitude;
	}
	
	public double getLongitude(){
		return longitude;
	}
	
	public void setLatitude(double lat){
		latitude=lat;
	}
	
	public void setLongitude(double longi){
		longitude=longi;
	}
	
	public void setSectorId(Location location){
		//calculate sector Id
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

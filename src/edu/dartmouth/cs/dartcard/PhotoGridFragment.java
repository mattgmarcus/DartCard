package edu.dartmouth.cs.dartcard;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class PhotoGridFragment extends Fragment {

	private GridView gridView;
	private Context context;
	private ArrayList<PhotoEntry> photos;
	private boolean photosLoaded = false;
	Map<ImageView, PhotoEntry> thumbnailPhotos = new HashMap<ImageView, PhotoEntry>();
	private Location location;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();

		//initialize gridview with correct adapter
		gridView = new GridView(getActivity());

		if (photos == null) {
			PriorityQueue<PhotoEntry> photoQueue = ((PhotoMapActivity) getActivity()).closestPhotos;
			getPhotoListFromQueue(photoQueue);
		} 
		setAdapter();

		gridView.setNumColumns(3);
		gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		//when gridview item is clicked, get the photoentry linked
		//to that item, and save that photo so that the photoview activity
		//can load it
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				savePhoto(thumbnailPhotos.get((ImageView) v)
						.getPhotoByteArray());
				// savePhoto((ImageView) gridView.getChildAt(position));
				Intent intent = new Intent(context, PhotoViewActivity.class);
				intent.putExtra(Globals.IS_FROM_DB_KEY, true);
				startActivity(intent);
			}
		});
		return gridView;
	}

	//load the photos from the priorityqueue into an arraylist
	public void getPhotoListFromQueue(PriorityQueue<PhotoEntry> photoQueue) {
		// get photo list
		// load photos from database
		photos = new ArrayList<PhotoEntry>();
		if (photoQueue != null) {
			photos.addAll(photoQueue);
		} 
	}

	//called when the locationclient in PhotoMapActivity connects
	public void updateGridView(PriorityQueue<PhotoEntry> pq) {
		getPhotoListFromQueue(pq);
		setAdapter();
	}

	public void setAdapter() {
		gridView.setAdapter(new ImageAdapter(getActivity()));
	}

	public void setNumColumns(int numColumns) {
		gridView.setNumColumns(numColumns);
	}

	//adapter class for this fragment's gridview
	public class ImageAdapter extends BaseAdapter {

		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;
		}

		@Override
		public int getCount() {
			return photos.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		// this is from Google....
		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) { // if it's not recycled, initialize some
										// attributes
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(8, 8, 8, 8);

				// set image view to one third width of phone
				WindowManager wm = (WindowManager) context
						.getSystemService(Context.WINDOW_SERVICE);
				DisplayMetrics metrics = new DisplayMetrics();
				wm.getDefaultDisplay().getMetrics(metrics);
				int width = metrics.widthPixels / 3;
				imageView.getLayoutParams().width = width;
				imageView.getLayoutParams().height = 2 * width / 3;
			} else {
				imageView = (ImageView) convertView;
			}

			//set the imageview to the current photoentry's photo
			//and link the imageview to the photoentry in the thumbnailPhotos map
			imageView.setImageBitmap(photos.get(position).getBitmapPhoto());
			thumbnailPhotos.put(imageView, photos.get(position));
			return imageView;

		}

	}

	//save photo to predetermined filepath that will be loaded by
	//photoviewactivity
	private void savePhoto(byte[] pic) {
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
	}
}

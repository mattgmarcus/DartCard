package edu.dartmouth.cs.dartcard;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
	private ImageView chosenPhoto;
//	private Integer[] pics = new Integer[100];
	private Context context;
	private ArrayList<PhotoEntry> photos;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
		
		//get photo list
		//load photos from database
		PhotoEntryDbHelper db = new PhotoEntryDbHelper(context);
		photos = db.fetchEntries();
		Log.d("DartCard", "size of photo list is " + photos.size());
		

		gridView = new GridView(getActivity());
		
		gridView.setAdapter(new ImageAdapter(getActivity()));
		
		gridView.setNumColumns(3);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				chosenPhoto = (ImageView) gridView.getChildAt(position);
				savePhoto();
				Intent intent = new Intent(context, PhotoViewActivity.class);
				intent.putExtra(Globals.IS_FROM_DB_KEY, true);
				startActivity(intent);
			}
		});
		return gridView;
	}

	public class ImageAdapter extends BaseAdapter {

		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return photos.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		// this is from Google....
		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.i("DartCard", "getView");
			ImageView imageView;
			if (convertView == null) { // if it's not recycled, initialize some
										// attributes
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(8, 8, 8, 8);
				
				//set image view to one third width of phone
				WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
				DisplayMetrics metrics = new DisplayMetrics();
				wm.getDefaultDisplay().getMetrics(metrics);
				imageView.getLayoutParams().width = metrics.widthPixels / 3;
				imageView.getLayoutParams().height = metrics.widthPixels / 3;
			} else {
				imageView = (ImageView) convertView;
			}

			imageView.setImageBitmap(photos.get(position).getBitmapPhoto());

			return imageView;

		}

	}

	private void savePhoto(){
		//the image we want to save is displayed in the image view for the profile
		//photo, so take the bitmap representation of that photo that photo, 
		//and save it to the pre-defined file name on the phone
		chosenPhoto.buildDrawingCache();
		Bitmap bmapProfPhoto = chosenPhoto.getDrawingCache();
		try{
			FileOutputStream fileOut = context.openFileOutput(getString(R.string.selected_photo_name), context.MODE_PRIVATE);
			bmapProfPhoto.compress(Bitmap.CompressFormat.PNG, 100, fileOut);
			fileOut.flush();
			fileOut.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}

package edu.dartmouth.cs.dartcard;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

public class PhotoMapActivity extends Activity {
	private static final String TAB_KEY_INDEX = "tab_key";
	private GoogleMap map;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		Fragment gridFragment = new PhotoGridFragment();
		Fragment mapFragment = new MapFragment();

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
	

}

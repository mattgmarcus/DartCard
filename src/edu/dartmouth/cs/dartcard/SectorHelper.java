package edu.dartmouth.cs.dartcard;

import android.util.Log;

public class SectorHelper {
	private static final double FRACTION = 0.2; //determines how many units we split globe into
	private static final int MAX_LATITUDE = 90;
	private static final int MAX_LONGITUDE = 180;
	private static final int NUM_ROWS = (MAX_LATITUDE * 2) * (int) (1.0 / FRACTION);
	private static final int NUM_COLUMNS = (MAX_LONGITUDE * 2) * (int) (1.0 / FRACTION);
	
	
	
	//use binary search to determine sector id
	//all sector ids correspond to a certain range of lat/lon
	//so we're finding the id corresponding to the range in which
	//this lat falls
	private static int getLatSectorIndex(double lat){
		int maxIndex = (NUM_ROWS - 1);
		if (lat == -MAX_LATITUDE)
			return 0;
		if (lat == MAX_LATITUDE)
			return maxIndex;
		int low = 0;
		int high = maxIndex;
		while (low <= high) {
			int mid = (low + high) / 2;
			double left = -MAX_LATITUDE + (FRACTION * mid);
			double right = left + FRACTION;
			if (lat >= left && lat < right)
				return mid;
			else if (lat < left)
				high = mid - 1;
			else 
				low = mid + 1;	
		}
		return -1; //return -1 on default
	}
	
	private static int getLongSectorIndex(double longi){
		int maxIndex = (int)((MAX_LONGITUDE*2) * (1.0 / FRACTION) - 1);
		if (longi == -MAX_LONGITUDE)
			return 0;
		if (longi == MAX_LONGITUDE)
			return maxIndex;
		int low = 0;
		int high = maxIndex;
		while (low <= high) {
			int mid = (low + high) / 2;
			double left = -MAX_LONGITUDE + (FRACTION * mid);
			double right = left + FRACTION;
			if (longi >= left && longi < right)
				return mid;
			else if (longi < left)
				high = mid - 1;
			else 
				low = mid + 1;	
		}
		return -1; //return -1 on default
	}
	
	public static int getSectorIdFromLatLong(double lat, double longi){
		int latIndex = getLatSectorIndex(lat);
		int longIndex = getLongSectorIndex(longi);
		return (latIndex * NUM_ROWS * (int)(1.0/FRACTION)) + longIndex;
	}
	
	//finding adjacent sectors, need to check for boundary cases
	private static boolean isLeftEdgeOfMapping(int sector){
		if (sector % NUM_COLUMNS == 0){
			return true;
		}
		else
			return false;
	}
	
	private static boolean isRightEdgeOfMapping(int sector){
		if (isLeftEdgeOfMapping(sector + 1) == true){
			return true;
		}
		else
			return false;
	}
	
	private static boolean isTopEdgeOfMapping(int sector){
		if (sector >= 0 && sector <= NUM_COLUMNS - 1){
			return true;
		}
		else
			return false;
	}
	
	private static boolean isBottomEdgeOfMapping(int sector){
		int firstValInLastRow = NUM_COLUMNS * (NUM_ROWS - 1);
		int lastValInLastRow = NUM_COLUMNS * NUM_ROWS - 1;
		if (sector >= firstValInLastRow && sector <= lastValInLastRow){
			return true;
		}
		else 
			return false;
	}
	
	public static int[] getAdjacentSectors(int sector){
		int[] result = new int[8];
//		if (true){
		//check if on boundary, for now just return null if so
		if (!isLeftEdgeOfMapping(sector) && !isRightEdgeOfMapping(sector)
				&& !isTopEdgeOfMapping(sector) && !isBottomEdgeOfMapping(sector)){
			result[0] = sector - NUM_COLUMNS - 1; //top left corner
		    result[1] = sector - NUM_COLUMNS; //directly above
		    result[2] = sector - NUM_COLUMNS + 1; //top right corner
		    result[3] = sector - 1; //directly to left
		    result[4] = sector + 1; //directly to right
		    result[5] = sector + NUM_COLUMNS - 1; //bottom left corner
		    result[6] = sector + NUM_COLUMNS; //directly below
		    result[7] = sector + NUM_COLUMNS + 1; //bottom right corner
		}
		//returns empty array if boundary case
		return result;
	}

}

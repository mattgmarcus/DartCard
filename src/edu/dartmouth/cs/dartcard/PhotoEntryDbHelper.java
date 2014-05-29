package edu.dartmouth.cs.dartcard;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

public class PhotoEntryDbHelper extends SQLiteOpenHelper {

	public static final String TABLE_NAME_ENTRIES = "entries";
	public static final String KEY_ROW_ID = "row_id";
	public static final String KEY_PHOTO = "photo";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_SECTOR_ID = "sector_id";

	private String[] allColumns = { KEY_ROW_ID, KEY_PHOTO, KEY_LATITUDE, KEY_LONGITUDE,
			KEY_SECTOR_ID };

	private static final String DATABASE_NAME = "photos.db";
	private static final int DATABASE_VERSION = 1;

	// SQL query to create the table for the first time
	// Data types are defined below
	public static final String CREATE_TABLE_ENTRIES = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME_ENTRIES
			+ " ("
			+ KEY_ROW_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ KEY_PHOTO
			+ " BLOB, "
			+ KEY_LATITUDE
			+ " DOUBLE, "
			+ KEY_LONGITUDE
			+ " DOUBLE, "
			+ KEY_SECTOR_ID
			+ " INTEGER " + ");";

	SQLiteDatabase dbObject;

	public PhotoEntryDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_ENTRIES);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS ");
		onCreate(db);
	}

	// Insert a item given each column value
	public long insertPhoto(PhotoEntry entry) {
		ContentValues values = new ContentValues();
		values.put(KEY_PHOTO, entry.getPhotoByteArray());
		values.put(KEY_LATITUDE, entry.getLatitude());
		values.put(KEY_LONGITUDE, entry.getLongitude());
		values.put(KEY_SECTOR_ID, entry.getSectorId());

		dbObject = getWritableDatabase();
		long entryId = dbObject.insert(TABLE_NAME_ENTRIES, null, values);
		dbObject.close();
		return entryId;
	}

	public void removePhoto(long rowIndex) {
		dbObject = getWritableDatabase();
		dbObject.delete(TABLE_NAME_ENTRIES, KEY_ROW_ID + " = " + rowIndex, null);
		dbObject.close();
	}

	public PhotoEntry fetchPhotoByIndex(long rowId) {
		dbObject = getReadableDatabase();
		Cursor cursor = dbObject.query(TABLE_NAME_ENTRIES, allColumns,
				KEY_ROW_ID + " = " + rowId, null, null, null, null);
		cursor.moveToFirst();
		PhotoEntry entry = cursorToEntry(cursor);

		// Make sure to close the cursor
		cursor.close();
		dbObject.close();
		return entry;

	}

	// Query the entire table, return all rows
	public ArrayList<PhotoEntry> fetchEntries() {
		ArrayList<PhotoEntry> entries = new ArrayList<PhotoEntry>();

		dbObject = getReadableDatabase();

		Cursor cursor = dbObject.query(TABLE_NAME_ENTRIES, allColumns, null,
				null, null, null, null);

		cursor.moveToFirst();
		// while (cursor.moveToNext()) {
		while (!cursor.isAfterLast()) {
			PhotoEntry entry = cursorToEntry(cursor);
			entries.add(entry);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		dbObject.close();
		return entries;
	}

	public ArrayList<PhotoEntry> fetchSectorEntries(int sectorId) {
		ArrayList<PhotoEntry> entries = new ArrayList<PhotoEntry>();

		dbObject = getReadableDatabase();

		Cursor cursor = dbObject.query(TABLE_NAME_ENTRIES, allColumns, KEY_SECTOR_ID + "=" + Integer.toString(sectorId), null,
				null, null, null);

		cursor.moveToFirst();
//		 while (cursor.moveToNext()) {
		while (!cursor.isAfterLast()) {
			PhotoEntry entry = cursorToEntry(cursor);
			entries.add(entry);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		dbObject.close();
		return entries;
	}

	private PhotoEntry cursorToEntry(Cursor cursor) {
		PhotoEntry entry = new PhotoEntry();
		entry.setId(cursor.getLong(0));
		entry.setPhoto(cursor.getBlob(1));
		entry.setLatitude(cursor.getDouble(2));
		entry.setLongitude(cursor.getDouble(3));
		entry.setSectorId(cursor.getInt(4));

		return entry;
	}

}

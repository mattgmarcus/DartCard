package edu.dartmouth.cs.dartcard;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RecipientAddressDbHelper extends SQLiteOpenHelper {
	
	private static String DATABASE_NAME = "recipient.db";
	public static final String TABLE_COMMENTS = "comments";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_COMMENT = "comment";
	private static final int DATABASE_VERSION = 1;
	private static final String ADDRESSES = "ADDRESSES";

	private static final String ROW_KEY = "_id";
	private static final String NAME_KEY = "name";
	private static final String LINEONE_KEY = "address1";
	private static final String LINETWO_KEY = "address2";
	private static final String CITY_KEY = "city";
	private static final String STATE_KEY = "state";
	private static final String ZIP_KEY = "zip";
	private static final String LABEL_KEY = "label";

	
	private String[] allColumns = { ROW_KEY, NAME_KEY, LINEONE_KEY, LINETWO_KEY, CITY_KEY, STATE_KEY, ZIP_KEY, LABEL_KEY};
	
	private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS ADDRESSES ("
		    + "_id INTEGER PRIMARY KEY AUTOINCREMENT," 
		    + "name TEXT," 
		    + "address1 TEXT," 
		    + "address2 TEXT," 
		    + "city TEXT," 
		    + "state TEXT," 
		    + "zip TEXT,"
		    + "label TEXT);";

	public RecipientAddressDbHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}
	
	public long insertAddress(Recipient add) {
		ContentValues values = new ContentValues();
		values.put(NAME_KEY, add.getName());
		values.put(LINEONE_KEY, add.getStreet1());
		values.put(LINETWO_KEY, add.getStreet2());
		values.put(CITY_KEY, add.getCity());
		values.put(STATE_KEY, add.getState());
		values.put(ZIP_KEY, add.getZip());
		values.put(LABEL_KEY, add.getLabel());
		SQLiteDatabase db = getWritableDatabase();
		long id = db.insert(ADDRESSES, null, values);
		db.close();
		return id;
	}
				
		
		
	public void removeAddress(long rowIndex) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(ADDRESSES, ROW_KEY + " = " + rowIndex, null);
		db.close();
	}
	
	public ArrayList<Recipient> fetchAddresses() {
		SQLiteDatabase db = getReadableDatabase();
		ArrayList<Recipient> entryList = new ArrayList<Recipient>();
		Cursor cursor = db.query(ADDRESSES, allColumns, null, null,
				null, null, null);
		while (cursor.moveToNext()) {
            Recipient add= cursorToEntry(cursor);
            entryList.add(add);
        }
		cursor.close();
		db.close();
		return entryList;
	}

	public Recipient fetchAddressByIndex(long rowId) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(ADDRESSES, allColumns, COLUMN_ID + " = " + rowId, 
				null, null, null, null);
		cursor.moveToFirst();
		Recipient add = cursorToEntry(cursor);
		cursor.close();
		db.close();
		return add;
	}
	
	private Recipient cursorToEntry(Cursor cursor) {
		Recipient add = new Recipient();
		add.setId(cursor.getLong(cursor.getColumnIndex(ROW_KEY)));
		add.setName(cursor.getString(cursor.getColumnIndex(NAME_KEY)));
		add.setStreet1(cursor.getString(cursor.getColumnIndex(LINEONE_KEY)));
		add.setStreet2(cursor.getString(cursor.getColumnIndex(LINETWO_KEY)));
		add.setCity(cursor.getString(cursor.getColumnIndex(CITY_KEY)));
		add.setState(cursor.getString(cursor.getColumnIndex(STATE_KEY)));
		add.setZip(cursor.getString(cursor.getColumnIndex(ZIP_KEY)));
		add.setLabel(cursor.getString(cursor.getColumnIndex(LABEL_KEY)));
		return add;
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}

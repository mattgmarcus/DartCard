package edu.dartmouth.cs.dartcard;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;


// This class handles all interactions with a database that stores credit cards used for
// pay activity. It's pretty straightforward and similar to the one we used for MyRuns
// If you have any security concerns about storing credit cards on phones, fear not!
// We don't actually store information that's sensitive. We just keep the last four
// digits, the credit card type (Visa, Amex, etc.), the expiration month/year, and 
// their email. We also store an id that is used for interacting with the Stripe API
// for later payments/postcards. Their full/actual credit card number isn't saved though.
public class CardDBHelper extends SQLiteOpenHelper {	
	private static String DATABASE_NAME = "card.db";
	public static final String COLUMN_ID = "_id";
	private static final int DATABASE_VERSION = 1;
	private static final String CARDS = "CARDS";

	private static final String ROW_KEY = "_id";
	private static final String CUS_ID_KEY = "cus_id";
	private static final String LAST_FOUR_KEY = "lastfour";
	private static final String TYPE_KEY = "type";
	private static final String EXP_MONTH_KEY = "exp_month";
	private static final String EXP_YEAR_KEY = "exp_year";
	private static final String EMAIL_KEY = "email";

	
	private String[] allColumns = { ROW_KEY, CUS_ID_KEY, LAST_FOUR_KEY, TYPE_KEY, EXP_MONTH_KEY,
			EXP_YEAR_KEY, EMAIL_KEY};
	
	
	private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS CARDS ("
		    + "_id INTEGER PRIMARY KEY AUTOINCREMENT," 
		    + "cus_id TEXT," 
		    + "lastfour TEXT," 
		    + "type TEXT," 
		    + "exp_month INTEGER," 
		    + "exp_year INTEGER,"
		    + "email TEXT);"; 

	public CardDBHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}
	
	public long insertCard(Card card) {
		ContentValues values = new ContentValues();
		values.put(CUS_ID_KEY, card.getCusId());
		values.put(LAST_FOUR_KEY, card.getLastFour());
		values.put(TYPE_KEY, card.getType());
		values.put(EXP_MONTH_KEY, card.getExpMonth());
		values.put(EXP_YEAR_KEY, card.getExpYear());
		values.put(EMAIL_KEY, card.getEmail());
		SQLiteDatabase db = getWritableDatabase();
		long id = db.insert(CARDS, null, values);
		db.close();
		return id;
	}
				
		
		
	public void removeCard(long rowIndex) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(CARDS, ROW_KEY + " = " + rowIndex, null);
		db.close();
	}
	
	public ArrayList<Card> fetchCards() {
		SQLiteDatabase db = getReadableDatabase();
		ArrayList<Card> entryList = new ArrayList<Card>();
		Cursor cursor = db.query(CARDS, allColumns, null, null,
				null, null, null);
		while (cursor.moveToNext()) {
            Card card= cursorToEntry(cursor);
            entryList.add(card);
        }
		cursor.close();
		db.close();
		return entryList;
	}

	public Card fetchCardByIndex(long rowId) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(CARDS, allColumns, COLUMN_ID + " = " + rowId, 
				null, null, null, null);
		cursor.moveToFirst();
		Card card = cursorToEntry(cursor);
		cursor.close();
		db.close();
		return card;
	}
	
	private Card cursorToEntry(Cursor cursor) {
		Card card = new Card();
		card.setId(cursor.getLong(cursor.getColumnIndex(ROW_KEY)));
		card.setCusId(cursor.getString(cursor.getColumnIndex(CUS_ID_KEY)));
		card.setLastFour(cursor.getString(cursor.getColumnIndex(LAST_FOUR_KEY)));
		card.setType(cursor.getString(cursor.getColumnIndex(TYPE_KEY)));
		card.setExpMonth(cursor.getInt(cursor.getColumnIndex(EXP_MONTH_KEY)));
		card.setExpYear(cursor.getInt(cursor.getColumnIndex(EXP_YEAR_KEY)));
		card.setEmail(cursor.getString(cursor.getColumnIndex(EMAIL_KEY)));
		return card;
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS ");
		onCreate(db);
	}
}

package com.astro.scope;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBAdapter 
{
	public static final String KEY_ROWID = "_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_YEAR = "year";
	public static final String DB_MONTH = "month";
	public static final String DB_DAY = "day";
	public static final String DB_HOUR = "hour";
	public static final String DB_MINUTE = "minute";
	public static final String DB_LAT = "lat";
	public static final String DB_LNG = "lng";
	public static final String DB_TZ = "tz";
	
	private static final String DATABASE_NAME = "users";
	private static final String DATABASE_TABLE = "natal";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE = 
		"create table natal " +
		"(_id integer primary key autoincrement, name text not null, " +
		"year integer, month integet, day integer, hour ingeter, minute integer, " +
		"lat real, lng real, tz real);";

	private final Context context;
	
	private DBHelper dbHelper;
	private SQLiteDatabase db;
	
	public DBAdapter(Context ctx) {
		this.context = ctx;
		dbHelper = new DBHelper(context);
	}
	
	private static class DBHelper extends SQLiteOpenHelper
	{
		DBHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i("Astro", "DBHelper onCreate");
			try {
				db.execSQL(DATABASE_CREATE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i("Astro", "DBHelper onUpgrade from version " + oldVersion +
				" to version " + newVersion);
			db.execSQL("DROP TABLE IF EXISTS natal");
			onCreate(db);
		}
	} // class DBHelper
	
	public DBAdapter open() throws SQLException
	{
		Log.i("Astro", "DBAdapter open");
		try {
			db = dbHelper.getWritableDatabase();
		} catch (Exception e) {
			Log.e("Astro", "Exception: " + e.getMessage());
		}
		return this;
	}
	
	public void close()
	{
		dbHelper.close();
	}
	
	public long insertNatal(String name, 
		int year, int month, int day, int hour, int minute, double lat, double lng, double tz) 
	{
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, name);
		values.put(KEY_YEAR, year);
		values.put(DB_MONTH, month);
		values.put(DB_DAY, day);
		values.put(DB_HOUR, hour);
		values.put(DB_MINUTE, minute);
		values.put(DB_LAT, lat);
		values.put(DB_LNG, lng);
		values.put(DB_TZ, tz);
		return db.insert(DATABASE_TABLE, null, values);
	}
	
	public boolean updateNatal(long rowId, String name, 
		int year, int month, int day, int hour, int minute, int lat, int lng, int tz) 
	{
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, name);
		values.put(KEY_YEAR, year);
		values.put(DB_MONTH, month);
		values.put(DB_DAY, day);
		values.put(DB_HOUR, hour);
		values.put(DB_MINUTE, minute);
		values.put(DB_LAT, lat);
		values.put(DB_LNG, lng);
		values.put(DB_TZ, tz);
		return db.update(DATABASE_TABLE, values, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean deleteNatal(long rowId) {
		return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	public Cursor getAllNatals()
	{
		Log.i("Astro", "DBAdapter getAllNatals");
		return db.query(DATABASE_TABLE, 
			new String[] {KEY_ROWID, KEY_NAME, 
				KEY_YEAR, DB_MONTH, DB_DAY, DB_HOUR, DB_MINUTE, DB_LAT, DB_LNG, DB_TZ}, 
				null, null, null, null, KEY_NAME);
	}

	public Cursor getNatalById(long rowId)
	{
		Log.i("Astro", "DBAdapter getAllNatals");
		Cursor cursor = 
			db.query(true, DATABASE_TABLE, 
				new String[] {KEY_ROWID, KEY_NAME,
				KEY_YEAR, DB_MONTH, DB_DAY, DB_HOUR, DB_MINUTE, DB_LAT, DB_LNG, DB_TZ},
				KEY_ROWID + "=" + rowId, 
				null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
}

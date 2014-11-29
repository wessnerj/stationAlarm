package org.wessner.android.stationAlarm.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DataBaseHelper extends SQLiteOpenHelper {
	/**
	 * DataBaseName used by this helper
	 */
	private static final String DATABASE_NAME = "stationAlarm";
	/**
	 * Version of database, must be increased with every update
	 */
	private static final int DATABASE_VERSION = 1;
	
	/**
	 * Name of the default PRIMARY KEY
	 */
	public static final String KEY_ROWID = "_id";
	
	/**
	 * Holds the Context
	 */
	// private final Context context;
	
	/**
	 * Constructor for DataBaseHelper
	 * 
	 * @param context
	 */
	public DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	//	this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE locations ("
				+ "_id int(11) PRIMARY KEY, "
				+ "name varchar(255), "
				+ "lat decimal(8,6), "
				+ "lon decimal(8,6), "
				+ "active tinyint(1), "
				+ "created datetime, "
				+ "modified datetime"
				);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS locations");
		this.onCreate(db);
	}
	
	/**
	 * Function to get the integer value of field columnName
	 * 
	 * @param cursor
	 * @param columnName
	 * @return
	 */
	public Integer getInteger(Cursor cursor, String columnName) {
		int columnID = cursor.getColumnIndex(columnName);
		if (columnID == -1)
			return null;
		return Integer.valueOf(cursor.getInt(columnID));
	}
	
	/**
	 * Function to get the double value of field columnName
	 * 
	 * @param cursor
	 * @param columnName
	 * @return
	 */
	public Double getDouble(Cursor cursor, String columnName) {
		int columnID = cursor.getColumnIndex(columnName);
		if (columnID == -1)
			return null;
		return Double.valueOf(cursor.getDouble(columnID));
	}
	
	/**
	 * Function to get the string value of field columnName
	 * 
	 * @param cursor
	 * @param columnName
	 * @return
	 */
	public String getString(Cursor cursor, String columnName) {
		int columnID = cursor.getColumnIndex(columnName);
		if (columnID == -1)
			return null;
		return cursor.getString(columnID);
	}

}

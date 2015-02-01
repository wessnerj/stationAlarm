/**
 * stationAlarm - Android app which wakes you before you reach your target station.
 * Copyright (C) 2015  Joseph Wessner <joseph@wessner.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.wessner.android.stationAlarm.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHelper for accessing the SQLite database.
 * 
 * @author Joseph Wessner <joseph@wessner.org>
 */
public class DataBaseHelper extends SQLiteOpenHelper {
	/**
	 * DataBaseName used by this helper
	 */
	private static final String DATABASE_NAME = "stationAlarm";
	/**
	 * Version of database, must be increased with every update
	 */
	private static final int DATABASE_VERSION = 4;
	
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
		db.execSQL("CREATE TABLE stations ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "name varchar(255), "
				+ "lat decimal(8,6), "
				+ "lon decimal(8,6), "
				+ "distance REAL, "
				+ "active tinyint(1), "
				+ "created datetime, "
				+ "modified datetime)"
				);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS stations");
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

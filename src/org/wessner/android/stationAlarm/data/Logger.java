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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.wessner.android.stationAlarm.SettingsFragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Helper class to log messages, both to Android's logcat and to the apps internal database.
 * 
 * @author Joseph Wessner <joseph@wessner.org>
 */
public class Logger {
	/**
	 * Output to logcat
	 */
	static private boolean bLogcat = true;
	
	/**
	 * Save to database
	 */
	static private boolean bLogDB = true;
	
	/**
	 * Access to database
	 */
	static private DataBaseHelper dbh = null;

	/**
	 * Link to Context
	 */
	static private Context context = null;
	
	/**
	 * Initialize the Logger class
	 * 
	 * @param context
	 */
	public static void init(Context context)
	{
		Logger.context = context;
		Logger.dbh = new DataBaseHelper(context);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		bLogDB = sharedPref.getBoolean(SettingsFragment.KEY_PREF_LOG, true);
	}
	
	/**
	 * Debug log message.
	 * 
	 * @param tag	Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
	 * @param msg	The message you would like logged. 
	 */
	public static void d(String tag, String msg)
	{
		if (bLogcat)	// Call standard Log.d
			Log.d(tag, msg);
		
		if (null == dbh || !bLogDB)
			return;
		
		// Store log to database
		ContentValues cv = new ContentValues();
		cv.put("type", "d");
		cv.put("tag", tag);
		cv.put("msg", msg);
		cv.put("date", getDateTime());
		SQLiteDatabase db = dbh.getWritableDatabase();
		db.insert("log", null, cv);
	}

	/**
	 * Error log message.
	 *
	 * @param tag	Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
	 * @param msg	The message you would like logged.
	 */
	public static void e(String tag, String msg)
	{
		if (bLogcat)	// Call standard Log.d
			Log.e(tag, msg);

		if (null == dbh || !bLogDB)
			return;

		// Store log to database
		ContentValues cv = new ContentValues();
		cv.put("type", "e");
		cv.put("tag", tag);
		cv.put("msg", msg);
		cv.put("date", getDateTime());
		SQLiteDatabase db = dbh.getWritableDatabase();
		db.insert("log", null, cv);
	}
	
	/**
	 * Get current dateTime for SQLite
	 * 
	 * @return		Current datetime as "yyyy-MM-dd HH:mm:ss"
	 */
	private static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
}
}

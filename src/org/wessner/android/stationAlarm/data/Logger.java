package org.wessner.android.stationAlarm.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Logger {
	static private boolean logcat = false;
	static private DataBaseHelper dbh;
	static private Context context;
	
	public static void init(Context context)
	{
		Logger.context = context;
		Logger.dbh = new DataBaseHelper(context);
	}
	
	public static void d(String tag, String msg)
	{
		if (logcat)
			Log.d(tag, msg);
		
		ContentValues cv = new ContentValues();
		cv.put("type", "d");
		cv.put("tag", tag);
		cv.put("msg", msg);
		cv.put("date", getDateTime());
		SQLiteDatabase db = dbh.getWritableDatabase();
		db.insert("log", null, cv);
	}
	
	private static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
}
}

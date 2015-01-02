package org.wessner.android.stationAlarm.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class StationManager extends AbstractEntityManager<Station> {
	public static String tableName = "stations";

	public StationManager(DataBaseHelper dbh) {
		super(dbh, tableName, new String[]{ "_id", "name", "lat", "lon", "active" });
	}

	@Override
	protected Station cursorToItem(Cursor c) {
		return new Station(this.dbh.getInteger(c, "_id"), this.dbh.getString(c,
				"name"), this.dbh.getDouble(c, "lat"), this.dbh.getDouble(c,
				"lon"), this.dbh.getInteger(c, "active") > 0);
	}

	public boolean save(Station s) {
		boolean ret = false;
		
		SQLiteDatabase db = this.dbh.getWritableDatabase();
		
		ContentValues cv = new ContentValues();
		// cv.put("_id", 		s._id);
		cv.put("name", 		s.name);
		cv.put("lat", 		s.lat);
		cv.put("lon", 		s.lon);
		cv.put("active", 	s.active);
		
		if (s._id > 0) {
			// Update
			String where = "_id = ?";
			String[] whereArgs = { "" + s._id };
			ret = (db.update(tableName, cv, where, whereArgs) > 0);
		}
		else {
			// Add
			final long id = db.insert(tableName, null, cv);
			ret = id > 0;
			if (ret)
				s._id = (int) id;
		}
		
		db.close();
		
		return ret;
	}
	
	public Cursor getAllActiveCursor(String orderBy) {
		SQLiteDatabase db = this.dbh.getWritableDatabase();
		
		String where = "active = ?";
		String[] whereArgs = { "1" };
		
		return db.query(tableName, this.columns, where, whereArgs, null, null, orderBy, null);
	}
	
	/**
	 * Get ArrayList with all active stations in database
	 * 
	 * @return
	 */
	public ArrayList<Station> getAllActive() {
		ArrayList<Station> entities = new ArrayList<Station>();
		
		Cursor result = getAllActiveCursor(null);
		result.moveToFirst();
		
		while (result.isAfterLast() != true) {
			entities.add(this.cursorToItem(result));

			result.moveToNext();
		}
		result.close();
		// db.close();

		return entities;
	}
}

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

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Manager for the station entity.
 * 
 * @author Joseph Wessner <joseph@wessner.org>
 */
public class StationManager extends AbstractEntityManager<Station> {
	public static String tableName = "stations";

	public StationManager(DataBaseHelper dbh) {
		super(dbh, tableName, new String[]{ "_id", "name", "lat", "lon", "distance", "active" });
	}

	@Override
	protected Station cursorToItem(Cursor c) {
		return new Station(this.dbh.getInteger(c, "_id"), this.dbh.getString(c,
				"name"), this.dbh.getDouble(c, "lat"), this.dbh.getDouble(c,
				"lon"), this.dbh.getDouble(c, "distance").floatValue(), this.dbh.getInteger(c, "active") > 0);
	}

	public boolean save(Station s) {
		boolean ret = false;
		
		SQLiteDatabase db = this.dbh.getWritableDatabase();
		
		ContentValues cv = new ContentValues();
		// cv.put("_id", 		s._id);
		cv.put("name", 		s.name);
		cv.put("lat", 		s.lat);
		cv.put("lon", 		s.lon);
		cv.put("distance",  s.distance);
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

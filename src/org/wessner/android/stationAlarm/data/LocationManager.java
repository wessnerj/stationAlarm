package org.wessner.android.stationAlarm.data;

import android.database.Cursor;

public class LocationManager extends AbstractEntityManager<Location> {
	protected static String tableName = "locations";

	public LocationManager(DataBaseHelper dbh) {
		super(dbh, tableName, new String[]{ "_id", "name", "lat", "lon", "active" });
	}

	@Override
	protected Location cursorToItem(Cursor c) {
		return new Location(this.dbh.getInteger(c, "_id"), this.dbh.getString(c,
				"name"), this.dbh.getDouble(c, "lat"), this.dbh.getDouble(c,
				"lon"), this.dbh.getInteger(c, "active") > 0);
	}

}

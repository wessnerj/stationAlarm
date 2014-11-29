package org.wessner.android.stationAlarm.data;

public class Location extends AbstractEntity {
	public String name;
	public double lat;
	public double lon;
	public boolean active;
	
	public Location() {
		this._id = -1;
	}
	
	public Location(int _id, String name, double lat, double lon, boolean active) {
		this._id = _id;
		this.name = name;
		this.lat = lat;
		this.lon = lon;
		this.active = active;
	}
}

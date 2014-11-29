package org.wessner.android.stationAlarm.data;

public class Station extends AbstractEntity {
	public String name;
	public double lat;
	public double lon;
	public boolean active;
	
	public Station() {
		this._id = -1;
		this.name = "";
		this.lat = 0.0;
		this.lon = 0.0;
		this.active = false;
	}
	
	public Station(int _id, String name, double lat, double lon, boolean active) {
		this._id = _id;
		this.name = name;
		this.lat = lat;
		this.lon = lon;
		this.active = active;
	}
}

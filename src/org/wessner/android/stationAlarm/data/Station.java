package org.wessner.android.stationAlarm.data;

public class Station extends AbstractEntity {
	public String name;
	public double lat;
	public double lon;
	public float distance;
	public boolean active;
	
	public Station() {
		this._id = -1;
		this.name = "";
		this.lat = 0.0;
		this.lon = 0.0;
		this.distance = 1000.f; // default: 1km
		this.active = false;
	}
	
	public Station(int _id, String name, double lat, double lon, float distance, boolean active) {
		this._id = _id;
		this.name = name;
		this.lat = lat;
		this.lon = lon;
		this.distance = distance;
		this.active = active;
	}
}

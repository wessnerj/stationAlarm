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

/**
 * Station entity.
 * 
 * @author Joseph Wessner <joseph@wessner.org>
 */
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

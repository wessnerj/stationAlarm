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

import org.wessner.android.stationAlarm.R;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter for station entity.
 * 
 * @author Joseph Wessner <joseph@wessner.org>
 */
public class StationAdapter extends CursorAdapter {
	/**
	 * Saves the DataBaseHelper, which is used for database querys
	 */
	private final DataBaseHelper dataBaseHelper;
	
	/**
	 * Default constructor
	 * 
	 * @param activity
	 * @param c
	 */
	public StationAdapter(Activity activity, Cursor c) {
		super(activity, c, false);

		this.dataBaseHelper = new DataBaseHelper(activity);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.station_item, null);

		this.bindView(view, context, cursor);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Station station = this.createItem(cursor);

		ImageView iv = (ImageView) view.findViewById(R.id.station_item_icon);
		TextView tv = (TextView) view.findViewById(R.id.station_item_headline);
		TextView sv = (TextView) view.findViewById(R.id.station_item_subline);

		if (station.active)
			iv.setImageResource(R.drawable.ic_launcher);
		else
			iv.setImageResource(R.drawable.deactive_alarm);
		
		tv.setText(station.name);
		sv.setText(String.format("%.1f km", station.distance/1000.f));
	}
	
	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		if (getFilterQueryProvider() != null) {
			return getFilterQueryProvider().runQuery(constraint);
		}

		SQLiteDatabase db = this.dataBaseHelper.getWritableDatabase();
		Cursor cursor = db.query(StationManager.tableName, new String[]{ "_id", "name", "lat", "lon", "distance", "active" }, null, null, null, null, "name", null);

		return cursor;
	}
	
	@Override
	public CharSequence convertToString(Cursor cursor) {
		Station s = this.createItem(cursor);

		return s.name;
	}
	
	public Station getItemByPosition(int pos) {
		Cursor c = this.getCursor();
		c.moveToPosition(pos);
		return createItem(c);
	}
	
	/**
	 * Creates new Station object from cursor's data
	 * 
	 * @param cursor
	 * @return
	 */
	private Station createItem(Cursor cursor) {
		Station station = new Station(
				this.dataBaseHelper.getInteger(cursor, "_id"),
				this.dataBaseHelper.getString(cursor, "name"),
				this.dataBaseHelper.getDouble(cursor, "lat"),
				this.dataBaseHelper.getDouble(cursor, "lon"),
				this.dataBaseHelper.getDouble(cursor, "distance").floatValue(),
				this.dataBaseHelper.getInteger(cursor, "active") > 0
			);

		return station;
	}

}

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
package org.wessner.android.stationAlarm.alarmHelper;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Build;

/**
 * Helper class for Android's AlarmManger to hide the API change in API 19.
 * 
 * This version should be used for API >= 19
 * 
 * @author Joseph Wessner <joseph@wessner.org>
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class AlarmHelperCurrent extends AlarmHelper {
	/**
	 * Window length (5s)
	 */
	private static long WINDOW_LENGTH = 5000;

	/**
	 * Default constructor, initializes AlarmManager
	 * 
	 * @param am
	 *            AlarmManager to use
	 */
	public AlarmHelperCurrent(AlarmManager am) {
		super(am);
	}

	@Override
	public void set(int type, long triggerAtMillis, PendingIntent operation) {
		this.alarmManager.setWindow(type, triggerAtMillis - WINDOW_LENGTH,
				WINDOW_LENGTH, operation);
	}

}

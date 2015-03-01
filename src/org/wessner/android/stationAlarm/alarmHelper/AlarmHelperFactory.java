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

import android.app.AlarmManager;

/**
 * Return the right Helper class depending on the running API level.
 * 
 * @author Joseph Wessner <joseph@wessner.org>
 */
public class AlarmHelperFactory {
	/**
	 * Get the right AlarmHelper depending on running API level.
	 * 
	 * @param am
	 *            AlarmManager to use
	 * @return AlarmHelper to use
	 */
	public static AlarmHelper getAlarmHelper(AlarmManager am) {
		if (android.os.Build.VERSION.SDK_INT < 19)
			return new AlarmHelperLegacy(am);
		else
			return new AlarmHelperCurrent(am);
	}
}

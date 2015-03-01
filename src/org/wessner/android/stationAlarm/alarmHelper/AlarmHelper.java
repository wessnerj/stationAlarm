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
import android.app.PendingIntent;

/**
 * Helper class for Android's AlarmManger to hide the API change in API 19.
 * 
 * @author Joseph Wessner <joseph@wessner.org>
 */
public abstract class AlarmHelper {
	/**
	 * AlarmManger to schedule PendingIntents
	 */
	protected AlarmManager alarmManager;

	/**
	 * Default constructor, initializes AlarmManager
	 * 
	 * @param am
	 *            AlarmManager to use
	 */
	public AlarmHelper(AlarmManager am) {
		this.alarmManager = am;
	}

	/**
	 * Schedule an alarm. Note: for timing operations (ticks, timeouts, etc) it
	 * is easier and much more efficient to use Handler. If there is already an
	 * alarm scheduled for the same IntentSender, that previous alarm will first
	 * be canceled.
	 * 
	 * @param type
	 *            One of ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC, or
	 *            RTC_WAKEUP.
	 * @param triggerAtMillis
	 *            time in milliseconds that the alarm should go off, using the
	 *            appropriate clock (depending on the alarm type).
	 * @param operation
	 *            Action to perform when the alarm goes off; typically comes
	 *            from IntentSender.getBroadcast().
	 */
	public abstract void set(int type, long triggerAtMillis,
			PendingIntent operation);
}

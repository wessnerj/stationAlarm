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
package org.wessner.android.stationAlarm;

import org.wessner.android.stationAlarm.data.DataBaseHelper;
import org.wessner.android.stationAlarm.data.Station;
import org.wessner.android.stationAlarm.data.StationManager;
import org.wessner.android.stationAlarm.data.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.WindowManager;

/**
 * ShowAlarmActivity shows the DialogBox, plays the alarm sound and starts the phone's vibrator,
 * if a Alarm is fired from AlarmService.
 * 
 * @author Joseph Wessner <joseph@wessner.org>
 */
public class ShowAlarmActivity extends Activity implements
		DialogInterface.OnClickListener, OnCancelListener {
	/**
	 * MediaPlayer for playing alarm sound
	 */
	private MediaPlayer mp;
	
	/**
	 * Id of current alarm
	 */
	private int alarmId;
	
	/**
	 * Is sound playing already running?
	 */
	private static boolean soundStarted = false;
	/**
	 * Is vibration already running?
	 */
	private static boolean vibrateStarted = false;
	/**
	 * Is there already a alarm running?
	 */
	private static boolean alarmRunning = false;

	/**
	 * AudioManger for getting active RingerMode
	 */
	private AudioManager audioManager;
	
	/**
	 * Vibrator object to start/stop phone's vibrator
	 */
	private Vibrator vibrator;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (alarmRunning) {
			// alarm is already running
			this.finish();
			return;
		}
		alarmRunning = true;

		// Get necessary information from intent
		final String name = getIntent().getExtras().getString("name");
		final int distance = getIntent().getExtras().getInt("distance");
		this.alarmId = getIntent().getExtras().getInt("id");

		// Get preferences
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		// Create MediaPlayer
		this.mp = new MediaPlayer();

		// Set alarm sound
		try {
			mp.setDataSource(this, this.getAlertUri(sharedPref));
		} catch (Exception e) {
		}

		// Get instance of AudioManager from current Context
		this.audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		// Get instance of Vibrator from current Context
		this.vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		// Check settings for vibrate / ringtone
		final String vibratePref = sharedPref.getString(SettingsFragment.KEY_PREF_VIBRATE, getString(R.string.pref_vibrate_val_default));
		final boolean shouldVibrate = !vibratePref.equals("never") && // if set to never, the alarm should not use vibrate
				(		vibratePref.equals("always") || // if set to always -> vibrate
						(this.audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE && vibratePref.equals("vibrate")) || // vibrate profile && vibrate in vibrate mode 
						(this.audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL && (vibratePref.equals("vibrate") || vibratePref.equals("normal"))) // normal profile && vibrate in vibrate or normal mode
						);
		final String soundPref = sharedPref.getString(SettingsFragment.KEY_PREF_SOUND, getString(R.string.pref_sound_val_default));
		final boolean shouldSound = !soundPref.equals("never") && // never sound means no sound
				(		soundPref.equals("always") || // always means always
						(this.audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL && soundPref.equals("normal")) // normal profile && sound in normal mode
						);


		// only peep if phone is not in vibrate or silent mode
		if (shouldSound && soundStarted == false) {
			Logger.d("ShowAlarmActivity", "Try to play sound");

			mp.setAudioStreamType(AudioManager.STREAM_ALARM);
			mp.setLooping(true);

			try {
				mp.prepare();
			} catch (Exception e) {
				Logger.e("ShowAlarmActivity", "Exception in mp.prepare(): " + e.toString());
			}

			Logger.d("ShowAlarmActivity", "Start alarm sound");
			mp.start();
			soundStarted = true;
		}

		// vibrate in silent/normal mode
		if (shouldVibrate && vibrateStarted == false) {
			// Start immediately
			// Vibrate for 200 milliseconds
			// Sleep for 100 milliseconds
			// Vibrate for 300 milliseconds
			// Sleep for 800 milliseconds
			final long[] pattern = { 0, 200, 100, 300, 800 };
			this.vibrator.vibrate(pattern, 0);
			vibrateStarted = true;
		}

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				String.format(getString(R.string.show_alarm_text), name,
						distance / 1000.f)).setCancelable(true)
				.setPositiveButton("Deaktivieren", this)
				.setOnCancelListener(this);

		AlertDialog alert = builder.create();
		alert.show();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	@Override
	public void onDestroy() {
		// stop alarm sound and vibration
		if (soundStarted && null != this.mp) {
			this.mp.stop();
			soundStarted = false;
		}
		if (vibrateStarted && null != this.vibrator) {
			this.vibrator.cancel();
			vibrateStarted = false;
		}

		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		super.onDestroy();
	}

	/**
	 * Get Uri for alarm sound.
	 * 
	 * @return
	 */
	private Uri getAlertUri(SharedPreferences sharedPref) {
		Uri u = Uri.parse(sharedPref.getString(SettingsFragment.KEY_PREF_SOUND_RINGTONE, getString(R.string.pref_sound_ringtone_val_default)));
		if (null != u)
			return u;
		
		final int[] alarmTypes = { RingtoneManager.TYPE_ALARM,
				RingtoneManager.TYPE_NOTIFICATION,
				RingtoneManager.TYPE_RINGTONE };

		for (final int type : alarmTypes) {
			final Uri alert = RingtoneManager.getDefaultUri(type);
			if (alert != null)
				return alert;
		}

		// should never happen
		return null;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		this.quit();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		this.quit();
	}

	/**
	 * Stop sound/vibration and deactivate alarm in database
	 */
	private void quit() {
		// stop alarm sound and vibration
		if (soundStarted) {
			this.mp.stop();
			soundStarted = false;
		}
		if (vibrateStarted) {
			this.vibrator.cancel();
			vibrateStarted = false;
		}

		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// deactivate this alarm
		StationManager stationManager = new StationManager(new DataBaseHelper(this));
		Station s = stationManager.get(this.alarmId);
		s.active = false;
		stationManager.save(s);
		
		alarmRunning = false;

		// start AlarmService again
		Intent service = new Intent(this, LocationMonitorService.class);
		this.startService(service);

		// quit
		Intent data = new Intent();
		setResult(RESULT_OK, data);
		this.finish();
	}
}

package org.wessner.android.stationAlarm;

import org.wessner.android.stationAlarm.data.DataBaseHelper;
import org.wessner.android.stationAlarm.data.Station;
import org.wessner.android.stationAlarm.data.StationManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;

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
	 * Is sound playing started?
	 */
	private static boolean soundStarted = false;
	
	private static boolean vibrateStarted = false;
	
	private static boolean alarmRunning = false;

	/**
	 * AudioManger for getting active RingerMode
	 */
	private AudioManager audioManager;
	
	/**
	 * Vibrator object to start/stop phone's vibrator
	 */
	private Vibrator vibrator;

	/**
	 * WakeLock while alarm is active
	 */
	private PowerManager.WakeLock wakeLock;

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

		// Create MediaPlayer
		this.mp = new MediaPlayer();

		// Set alarm sound
		try {
			mp.setDataSource(this, this.getAlertUri());
		} catch (Exception e) {
		}

		// Get instance of AudioManager from current Context
		this.audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		// Get instance of Vibrator from current Context
		this.vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		// only peep if phone is not in vibrate or silent mode
		if (this.audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL && soundStarted == false) {
			mp.setAudioStreamType(AudioManager.STREAM_ALARM);
			mp.setLooping(true);

			try {
				mp.prepare();
			} catch (Exception e) {
			}

			mp.start();
			soundStarted = true;
		}

		// vibrate in silent/normal mode
		if (vibrateStarted == false && (this.audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL
				|| this.audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE)) {
			// Start immediately
			// Vibrate for 200 milliseconds
			// Sleep for 100 milliseconds
			// Vibrate for 300 milliseconds
			// Sleep for 800 milliseconds
			final long[] pattern = { 0, 200, 100, 300, 800 };
			this.vibrator.vibrate(pattern, 0);
			vibrateStarted = true;
		}

		// Get wakeLock and turn display on
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, "ShowAlarmActivity");
		this.wakeLock.acquire();

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				String.format(getString(R.string.show_alarm_text), name,
						distance / 1000.f)).setCancelable(true)
				.setPositiveButton("Deaktivieren", this)
				.setOnCancelListener(this);

		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * Get Uri for alarm sound.
	 * 
	 * @return
	 */
	private Uri getAlertUri() {
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

		// release wakeLock
		this.wakeLock.release();

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

package org.wessner.android.stationAlarm;

import java.util.ArrayList;

import org.wessner.android.stationAlarm.data.DataBaseHelper;
import org.wessner.android.stationAlarm.data.Station;
import org.wessner.android.stationAlarm.data.StationManager;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

/**
 * Background service, which listens on GPS-position and informs the user if
 * specific station is reached. Look at MainAcitivity to add alarms.
 * 
 * @author Joseph Wessner <joseph@wessner.org>
 */
public class LocationMonitorService extends Service implements LocationListener {	
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	
	private static final int NOTIFICATION_ID = 33;
	
	private static boolean running = false;
	
	/// Minimum time to wait for location updates (currently 20s) [ms]
	private static final long MIN_UPDATE_WINDOW = 20 * 1000;
	
	/// Maximum time to wait for location updates (currently 40s) [ms]
	private static final long MAX_UPDATE_WINDOW = 40 * 1000;
	
	/// Threshold for deciding if location update is accurate enough (default: 100) [m]
	private static final float ACCURACY_THRESHOLD = 100.f;
	
	/// Maximum sleep window between two location requests (default: 5min) [ms]
	private static final long MAX_SLEEP_WINDOW = 5 * 60 * 1000;
	
	/// Time, when listing for location updates was started
	private long updateWindowStartTime;

	/**
	 * LocationManager for getting updates on changed locations
	 */
	private LocationManager locationManager = null;
	
	/**
	 * Handler for periodically calling GpsFinder
	 */
	private Handler handler = new Handler();

	/**
	 * Stores the last known location
	 */
	private Location lastLocation;

	/**
	 * WakeLock to prevent the phone to go into sleep mode
	 */
	private PowerManager.WakeLock wakeLock;
	
	private StationManager stationManager;
	
	/**
	 * Inform every available location provider to call us for location updates.
	 * Additionally use best last known location as current location.
	 */
	private void registerProviders() {
		java.util.List<String> providers = this.locationManager.getAllProviders();

		// get best last known location, and register itself for location changes
		for (String provider : providers) {
			this.locationManager.requestLocationUpdates(provider, 2000, 100,
					this);
			Location loc = this.locationManager.getLastKnownLocation(provider);
			
			if (this.lastLocation == null || isBetterLocation(loc, this.lastLocation))
				this.lastLocation = loc;
		}
		
		// Save the start time
		this.updateWindowStartTime = System.currentTimeMillis();
		
		// Call unregisterProviders after timeout
		this.handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				unregisterProviders();
			}
		}, MAX_UPDATE_WINDOW);
	}
	
	/**
	 * Unregister itself from getting location updates and check for alarms using best location estimate.
	 */
	private void unregisterProviders() {
		// don't get any updates und location changes anymore
		this.locationManager.removeUpdates(this);
		
		// check for alarms with best location estimate
		checkForAlarm();
	}
	
	public void onCreate() {
		// Initialize AlarmManger, LocationManager, PowerManger and WakeLock
		this.stationManager = new StationManager(new DataBaseHelper(this));
		this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlarmService");
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		// aquire wakeLock
		this.wakeLock.acquire();
		
		// PendingIntent to show app's main activity
		Intent resultIntent = new Intent(this, MainActivity.class);
		// Because clicking the notification opens a new ("special") activity, there's
		// no need to create an artificial back stack.
		PendingIntent resultPendingIntent =
		    PendingIntent.getActivity(
		    this,
		    0,
		    resultIntent,
		    PendingIntent.FLAG_UPDATE_CURRENT
		);
		
		// Get high priority and show icon in notification area
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.notification_icon)
				.setContentTitle("stationAlarm")
				.setContentText("stationAlarm is looking for locations!")
				.setContentIntent(resultPendingIntent);
		startForeground(NOTIFICATION_ID, mBuilder.build());
		
		// Set running status to true
		running = true;
		
		// Register for location updates
		registerProviders();
		
		// Inform android, that this service should run longer
		return START_STICKY;
	}
	
	public void onDestroy() {
		// don't get any updates und location changes anymore
		this.locationManager.removeUpdates(this);

		// release wakeLock
		this.wakeLock.release();

		super.onDestroy();
		
		running = false;
	}
	
	@Override
	public void onLocationChanged(Location location) {
		if (isBetterLocation(location, this.lastLocation))
			this.lastLocation = location;

		// if location is accurate enough and MIN_UPDATE_WINDOW time is already
		// past -> Unregister for location updates and check for alarms
		if (this.lastLocation.getAccuracy() < ACCURACY_THRESHOLD
				&& this.updateWindowStartTime + MIN_UPDATE_WINDOW < System
						.currentTimeMillis())
			unregisterProviders();
	}
	
	/**
	 * Cleanup and stopping itself (service)
	 */
	private void quit() {
		this.handler.removeCallbacksAndMessages(null);
		this.stopSelf();
	}
	
	/**
	 * Fire alarm and inform the user of active alarm.
	 * 
	 * @param distance current distance to alarmStation
	 * @param alarm Alarm Object
	 */
	private void alertUser(final float distance, final Station station) {
		final Intent dialogIntent = new Intent(getBaseContext(),
				ShowAlarmActivity.class);
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		dialogIntent.putExtra("name", station.name);
		dialogIntent.putExtra("distance", (int) distance);
		dialogIntent.putExtra("id", station._id);
		getApplication().startActivity(dialogIntent);
		this.stopSelf();
	}
	
	private void checkForAlarm()
	{
		if (null == this.lastLocation)
		{
			// Register again for location updates and do nothing for now ..
			registerProviders();
			return;
		}
		
		// Get all active stations
		ArrayList<Station> stations = this.stationManager.getAllActive();
		if (stations.size() < 1)
		{
			// No active station -> stop service
			this.quit();
			return;
		}
		
		// Save the closest distance to alarm
		float closest = Float.MAX_VALUE;
		for (final Station s : stations)
		{
			float[] results = new float[3];
			// Calculate distance
			Location.distanceBetween(
					this.lastLocation.getLatitude(), this.lastLocation.getLongitude(), 
					s.lat, s.lon, results);
			
			// Calculate how far away from alarm this stations is
			final float dist2alarm = results[0] - 1.1f * 1000.f; // Add 10% to be sure
			if (dist2alarm < 0.f)
			{
				// Station is less than 1000m away -> Call alarm
				alertUser(results[0], s);
				return;
			}
			
			if (dist2alarm < closest)
				closest = dist2alarm;
		}
		
		// Calculate the sleep time until next location queries
		// Assuming the user is traveling with 108km/h (= 30m/s),
		// in this case the alarm would be reached in:
		long timeToAlarm = (long) (closest*1000.f / 30.f);
		if (timeToAlarm > MAX_SLEEP_WINDOW)
			timeToAlarm = MAX_SLEEP_WINDOW; // Never sleep longer than MAX_SLEEP_WINDOW
		
		// No user alert happened, but at least one station is still active
		// -> Go to sleep and wait for new locations
		this.handler.postDelayed(new Runnable() {
			public void run() {
				registerProviders();
			}
		}, timeToAlarm);
	}

	@Override
	public void onProviderDisabled(java.lang.String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(java.lang.String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(java.lang.String provider, int status,
			Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 * @return true if the new Location is better
	 */
	public static boolean isBetterLocation(Location location,
			Location currentBestLocation) {
		// A new location is always better than no location
		if (currentBestLocation == null)
			return true;
		if (location == null)
			return false;

		// Check whether the new location fix is newer or older
		final long timeDelta = location.getTime()
				- currentBestLocation.getTime();
		final boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		final boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		final boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer)
			return true;
		else if (isSignificantlyOlder) // If the new location is more than two
										// minutes older, it must be worse
			return false;

		// Check whether the new location fix is more or less accurate
		final int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		final boolean isLessAccurate = accuracyDelta > 0;
		final boolean isMoreAccurate = accuracyDelta < 0;
		final boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		final boolean isFromSameProvider = isSameProvider(
				location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate)
			return true;
		else if (isNewer && !isLessAccurate)
			return true;
		else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
			return true;

		return false;
	}
	
	/** 
	 * Checks whether two providers are the same
	 * 
	 *  @param provider1		Name of the first provider
	 *  @param provider2		Name of the second provider
	 *  
	 *  @return					True if providers are the same
	 */
	private static boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static boolean isRunning() {
		return running;
	}
}

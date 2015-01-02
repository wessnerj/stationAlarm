package org.wessner.android.stationAlarm;

import java.util.ArrayList;

import org.wessner.android.stationAlarm.data.DataBaseHelper;
import org.wessner.android.stationAlarm.data.Station;
import org.wessner.android.stationAlarm.data.StationManager;

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
	 * Periodically called by handler for checking current location
	 */
	private Runnable GpsFinder = new Runnable() {
		public void run() {
			registerProviders();
			handler.postDelayed(GpsFinder, 20000); // register again to start after 20 seconds...
		}
	};
	
	/**
	 * Inform every available location provider to call us for location updates.
	 * Additionally use best last known location as current location.
	 */
	private void registerProviders() {
		java.util.List<String> providers = this.locationManager.getAllProviders();

		// get best last known location, and register itself for location changes
		Location bestLocation = null;
		for (String provider : providers) {
			this.locationManager.requestLocationUpdates(provider, 5000, 400,
					this);
			Location loc = this.locationManager.getLastKnownLocation(provider);
			if (isBetterLocation(loc, bestLocation))
				bestLocation = loc;
		}

		// save current location and check for active alarms
		this.lastLocation = bestLocation;
		// this.checkForAlarm(bestLocation);
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
		
		// register GpsFinder at handler
		this.handler.postDelayed(GpsFinder, 10000);// will start after 10 seconds
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.notification_icon)
		        .setContentTitle("stationAlarm")
		        .setContentText("stationAlarm is looking for locations!");
		startForeground(NOTIFICATION_ID, mBuilder.build());
		
		running = true;
		
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
		if (isBetterLocation(location, this.lastLocation)) {
			this.lastLocation = location;
			
			checkForAlarm();
		}
	}
	
	/**
	 * Cleanup and stopping itself (service)
	 */
	private void quit() {
		this.handler.removeCallbacks(GpsFinder);
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
			return; // Nothing to do without a location ..
		
		// Get all active stations
		ArrayList<Station> stations = this.stationManager.getAllActive();
		
		if (stations.size() < 1)
		{
			// No active station -> stop service
			this.quit();
			return;
		}
		
		for (final Station s : stations)
		{
			float[] results = new float[3];
			// Calculate distance
			Location.distanceBetween(
					this.lastLocation.getLatitude(), this.lastLocation.getLongitude(), 
					s.lat, s.lon, results);
			
			if (results[0] < 500.f)
			{
				// Station is less than 500m away -> Call alarm
				alertUser(results[0], s);
				return;
			}
		}
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

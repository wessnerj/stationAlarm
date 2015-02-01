package org.wessner.android.stationAlarm;

import java.util.List;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayManager;
import org.wessner.android.stationAlarm.data.DataBaseHelper;
import org.wessner.android.stationAlarm.data.Station;
import org.wessner.android.stationAlarm.data.StationManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class AddLocationActivity extends Activity implements OnClickListener,
		OnSeekBarChangeListener {
	/**
	 * Seekbar step size (default: 100) [m]
	 */
	private static int SEEKBAR_STEP = 100;
	/**
	 * Minimum seekbar value (default: 500) [m]
	 */
	private static int SEEKBAR_MIN = 500;
	/**
	 * Maximum seekbar value (default: 5000) [m]
	 */
	private static int SEEKBAR_MAX = 5000;

	/**
	 * Current seekbar value (starts with 1km) [m]
	 */
	private int currentSeekBarValue = 1000;

	/**
	 * Current (selected) location.
	 */
	private Location selectedLocation;

	/**
	 * Manager for accessing stations database
	 */
	private StationManager sm;

	/**
	 * Current Station (if any)
	 */
	private Station currentStation = null;

	/**
	 * EditText for stations' name
	 */
	private EditText etStationName;

	/**
	 * SeekBar for distance
	 */
	private SeekBar sbDistanceSB;

	/**
	 * TextView to show current distance value
	 */
	private TextView tvDistanceValue;

	/**
	 * Mapview with stations' location
	 */
	private MapView mvMap;

	/**
	 * MapController used for MapView
	 */
	private IMapController mapController;

	/**
	 * MapMarker for MapView
	 */
	private Marker mapMarker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_location);

		// Create StationManager
		this.sm = new StationManager(new DataBaseHelper(this));

		// Get current station (if any), is only set for editing stations
		this.currentStation = null;
		Bundle extras = getIntent().getExtras();
		if (null != extras) {
			int stationId = extras.getInt("stationId");
			if (stationId > 0)
				this.currentStation = this.sm.get(stationId);
		}

		// Get view elements
		this.etStationName = (EditText) findViewById(R.id.name_edit);
		this.sbDistanceSB = (SeekBar) findViewById(R.id.seekBar1);
		this.tvDistanceValue = (TextView) findViewById(R.id.value_distance);

		// On click listener for save button
		((Button) findViewById(R.id.save_button)).setOnClickListener(this);

		// SeekBar properties
		this.sbDistanceSB.setMax((SEEKBAR_MAX - SEEKBAR_MIN) / SEEKBAR_STEP);
		this.sbDistanceSB.setOnSeekBarChangeListener(this);

		// Set up MapView
		this.setupMapView();

		// Set values
		if (null == this.currentStation) {
			// Use default values for new station
			this.currentSeekBarValue = 1000;

			this.selectedLocation = getLastLocation();
		} else {
			// Use current stations'values
			this.etStationName.setText(this.currentStation.name);

			this.currentSeekBarValue = (int) this.currentStation.distance;

			this.selectedLocation = new Location("custom");
			this.selectedLocation.setLatitude(this.currentStation.lat);
			this.selectedLocation.setLongitude(this.currentStation.lon);

			this.setMapViewMarker(this.selectedLocation);
		}

		this.sbDistanceSB.setProgress((this.currentSeekBarValue - SEEKBAR_MIN)
				/ SEEKBAR_STEP);
		this.setMapViewLocation(this.selectedLocation);
	}

	/**
	 * Sets up the MapView
	 */
	private void setupMapView() {
		// Set MapView properties
		this.mvMap = (MapView) findViewById(R.id.mapview);
		this.mvMap.setTileSource(TileSourceFactory.MAPNIK);
		this.mvMap.setBuiltInZoomControls(true);
		this.mvMap.setMultiTouchControls(true);
		this.mvMap.setClickable(true);

		// Get Map controller
		this.mapController = this.mvMap.getController();

		// Set Location marker
		this.mapMarker = new Marker(this.mvMap);
		this.mapMarker.setAlpha(0);
		this.mapMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
		this.mvMap.getOverlays().add(this.mapMarker);

		// Setup touch overlay
		Overlay touchOverlay = new Overlay(this) {
			@Override
			protected void draw(Canvas arg0, MapView arg1, boolean arg2) {

			}

			@Override
			public boolean onLongPress(MotionEvent e, MapView mapView) {
				Projection proj = mapView.getProjection();
				GeoPoint loc = (GeoPoint) proj.fromPixels((int) e.getX(),
						(int) e.getY());

				if (null != selectedLocation) {
					selectedLocation.setLatitude(loc.getLatitude());
					selectedLocation.setLongitude(loc.getLongitude());
				}

				setMapViewMarker(selectedLocation);

				return true;
			}
		};

		OverlayManager om = this.mvMap.getOverlayManager();
		om.add(touchOverlay);
	}

	/**
	 * Sets the MapView's center to location
	 * 
	 * @param loc
	 */
	private void setMapViewLocation(Location loc) {
		if (null == loc)
			return;

		final GeoPoint gp = new GeoPoint(loc);
		this.mapController.setZoom(13);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mapController.animateTo(gp);
			}
		}, 250);

		this.mapController.animateTo(gp);
	}

	/**
	 * Sets the Marker to given location
	 * 
	 * @param loc
	 */
	private void setMapViewMarker(Location loc) {
		if (null == loc)
			return;

		GeoPoint gp = new GeoPoint(selectedLocation);
		this.mapMarker.setTitle(String.format(
				getString(R.string.add_location_marker_desc),
				loc.getLatitude(), loc.getLongitude()));
		this.mapMarker.setPosition(gp);
		this.mapMarker.setAlpha(1.0f);

		this.mvMap.invalidate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Get the last location of any location provider, or the center of Germany
	 * if no location can be acquired.
	 * 
	 * @return
	 */
	private Location getLastLocation() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);

		/*
		 * Loop over the array backwards, and if you get an accurate location,
		 * then break out the loop
		 */
		Location l = null;

		for (int i = providers.size() - 1; i >= 0; i--) {
			l = lm.getLastKnownLocation(providers.get(i));
			if (l != null)
				break;
		}

		if (l == null) {
			// Use center of Germany as default
			l = new Location("custom");
			l.setLongitude(10.447683);
			l.setLatitude(51.163375);
		}

		return l;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.save_button) {
			Station s;

			if (null == this.currentStation)
				s = new Station();
			else
				s = this.currentStation;

			s.name = ((EditText) findViewById(R.id.name_edit)).getText()
					.toString();
			s.lat = selectedLocation.getLatitude();
			s.lon = selectedLocation.getLongitude();
			s.distance = (float) this.currentSeekBarValue;

			// Save station
			this.sm.save(s);

			// Return to default activity
			Intent data = new Intent();
			setResult(RESULT_OK, data);
			finish();
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		this.currentSeekBarValue = SEEKBAR_MIN + (progress * SEEKBAR_STEP);
		this.tvDistanceValue.setText(String.format(
				getString(R.string.add_location_distance_value),
				((double) this.currentSeekBarValue / 1000.0)));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
}

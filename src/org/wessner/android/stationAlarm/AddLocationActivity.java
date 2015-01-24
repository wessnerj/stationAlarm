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

public class AddLocationActivity extends Activity implements OnClickListener {
	private Location selectedLocation;
	
	private StationManager sm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_location);
		
		// Create StationManager
		this.sm = new StationManager(new DataBaseHelper(this));
		
		// Get the map view
		final MapView v = (MapView) findViewById(R.id.mapview);
		v.setTileSource(TileSourceFactory.MAPNIK);
		v.setBuiltInZoomControls(true);
		v.setMultiTouchControls(true);
	    v.setClickable(true);
	    
	    // Get map controller
	    final IMapController mapController = v.getController();
	    this.selectedLocation = getLastLocation();
	    final GeoPoint gp = new GeoPoint(this.selectedLocation);
	    mapController.setZoom(13);
	    new Handler().postDelayed(new Runnable() {
	        @Override
	        public void run() {
	            mapController.animateTo(gp);
	        }
	    }, 250);

	    mapController.animateTo(gp);
	    
	    final Marker locationMarker = new Marker(v);
	    locationMarker.setAlpha(0);
	    locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
	    v.getOverlays().add(locationMarker);
	    
	    Overlay touchOverlay = new Overlay(this) {
			@Override
			protected void draw(Canvas arg0, MapView arg1, boolean arg2) {
								
			}
			
			@Override
		    public boolean onLongPress(MotionEvent e, MapView mapView) {
				Projection proj = mapView.getProjection();
				GeoPoint loc = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());
				
				if (null != selectedLocation)
				{ 
					selectedLocation.setLatitude(loc.getLatitude());
					selectedLocation.setLongitude(loc.getLongitude());
				}
				
				
				locationMarker.setTitle("Lat: " + loc.getLatitude() + "\nLon: " + loc.getLongitude());
				locationMarker.setPosition(loc);
				locationMarker.setAlpha(1.0f);
				v.invalidate();

				return true;
		    }
		};
		
		
		OverlayManager om = v.getOverlayManager();
		om.add(touchOverlay);
		
		Button saveButton = (Button) findViewById(R.id.save_button);
		saveButton.setOnClickListener(this);
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
	
	private Location getLastLocation() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  
		List<String> providers = lm.getProviders(true);
		
		/* Loop over the array backwards, and if you get an accurate location, then break out the loop*/
		Location l = null;
		
		for (int i=providers.size()-1; i>=0; i--) {
			l = lm.getLastKnownLocation(providers.get(i));
			if (l != null) break;
		}
		
		if (l == null)
		{
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
			Station s = new Station();
			s.name = ((EditText) findViewById(R.id.name_edit)).getText().toString();
			s.lat = selectedLocation.getLatitude();
			s.lon = selectedLocation.getLongitude();
			
			// Save station
			this.sm.save(s);
			
			// Return to default activity
			Intent data = new Intent();
			setResult(RESULT_OK, data);
			finish();
		}
	}
}

package org.wessner.android.stationAlarm;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

public class AddLocationActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_location);
		
		final MapView v = (MapView) findViewById(R.id.mapview);
		v.setTileSource(TileSourceFactory.MAPNIK);
		v.setBuiltInZoomControls(true);
		v.setMultiTouchControls(true);
	    v.setClickable(true);
	    
	    IMapController mapController = v.getController();
	    Location loc = getLastLocation();
	    GeoPoint gp = new GeoPoint(loc);
	    mapController.setZoom(10);
	    mapController.setCenter(gp);
	    
	    final Marker selectedLocation = new Marker(v);
	    selectedLocation.setAlpha(0);
	    selectedLocation.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
	    v.getOverlays().add(selectedLocation);
	    
	    Overlay touchOverlay = new Overlay(this) {
			@Override
			protected void draw(Canvas arg0, MapView arg1, boolean arg2) {
								
			}
			
			@Override
		    public boolean onLongPress(MotionEvent e, MapView mapView) {
				Projection proj = mapView.getProjection();
				GeoPoint loc = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());
				Log.d("Location", "Long: " + loc.getLongitude() + " Lat: " + loc.getLatitude());
				
				selectedLocation.setTitle("Long: " + loc.getLongitude() + " Lat: " + loc.getLatitude());
				selectedLocation.setPosition(loc);
				selectedLocation.setAlpha(1.0f);
				v.invalidate();

				return true;
		    }
		};
		
		
		OverlayManager om = v.getOverlayManager();
		om.add(touchOverlay);
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
		
		return l;
	}
}

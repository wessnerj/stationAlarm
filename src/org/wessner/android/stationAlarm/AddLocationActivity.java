package org.wessner.android.stationAlarm;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;
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

public class AddLocationActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_location);
		
		MapView v = (MapView) findViewById(R.id.mapview);
		v.setMultiTouchControls(true);
	    v.setClickable(true);
	    
	    Overlay touchOverlay = new Overlay(this) {
			@Override
			protected void draw(Canvas arg0, MapView arg1, boolean arg2) {
								
			}
			
			@Override
		    public boolean onLongPress(MotionEvent e, MapView mapView) {
				Projection proj = mapView.getProjection();
				GeoPoint loc = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());
				Log.d("Location", "Long: " + loc.getLongitude() + " Lat: " + loc.getLatitude());
			
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
}

package org.wessner.android.stationAlarm;

import org.wessner.android.stationAlarm.data.DataBaseHelper;
import org.wessner.android.stationAlarm.data.StationAdapter;
import org.wessner.android.stationAlarm.data.StationManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;


public class MainActivity extends Activity {
	
	/**
	 * Request code for switching to AddFavoriteActivity
	 */
	private static final int REQUEST_CODE = 10;
	
	private StationManager stationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        this.stationManager = new StationManager(new DataBaseHelper(this));
        
//        Button startButton = (Button) findViewById(R.id.button1);
//        startButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				startService();
//			}
//        });
        
        GridView v = (GridView) findViewById(R.id.gridView1);
        StationAdapter sa = new StationAdapter(this, this.stationManager.getAllCursor());
        v.setAdapter(sa);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add) {
        	Log.d("onOptionsItemSelected", "ADD");
        	
        	final Intent switchToAddFavourite = new Intent(
					this, AddLocationActivity.class);
			startActivityForResult(switchToAddFavourite, REQUEST_CODE);
			
//        	Intent intent = new Intent(this, AddLocationActivity.class);
//        	startActivity(intent);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void startService() {
    	Intent mServiceIntent = new Intent(this, LocationMonitorService.class);
    	this.startService(mServiceIntent);
    }
};

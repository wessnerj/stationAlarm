package org.wessner.android.stationAlarm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
//        Button startButton = (Button) findViewById(R.id.button1);
//        startButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				startService();
//			}
//        });
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
        	Intent intent = new Intent(this, AddLocationActivity.class);
        	startActivity(intent);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void startService() {
    	Intent mServiceIntent = new Intent(this, LocationMonitorService.class);
    	this.startService(mServiceIntent);
    }
};

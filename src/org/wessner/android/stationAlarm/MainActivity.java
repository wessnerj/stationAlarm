package org.wessner.android.stationAlarm;

import org.wessner.android.stationAlarm.data.DataBaseHelper;
import org.wessner.android.stationAlarm.data.Station;
import org.wessner.android.stationAlarm.data.StationAdapter;
import org.wessner.android.stationAlarm.data.StationManager;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;


public class MainActivity extends Activity implements OnItemLongClickListener {
	
	/**
	 * Request code for switching to AddFavoriteActivity
	 */
	private static final int REQUEST_CODE = 10;
	
	private static final int CONTEXT_ID_DE_ACTIVATE = 1;
	private static final int CONTEXT_ID_DELETE = 2;
	
	private StationManager stationManager;
	private StationAdapter stationAdapter;
	
	private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        this.stationManager = new StationManager(new DataBaseHelper(this));
        
        Button startButton = (Button) findViewById(R.id.button1);
        startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startService();
			}
        });
        
        this.gridView = (GridView) findViewById(R.id.gridView1);
        this.stationAdapter = new StationAdapter(this, this.stationManager.getAllCursor());
        this.gridView.setAdapter(this.stationAdapter);
        this.gridView.invalidate();
        // this.gridView.setOnItemLongClickListener(this);
        
        registerForContextMenu(this.gridView);
    }
    
    protected void onResume() {
    	super.onResume();
    	
        this.stationAdapter.changeCursor(this.stationManager.getAllCursor());
        this.stationAdapter.notifyDataSetChanged();
    	this.gridView.invalidate();
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
        	final Intent switchToAddFavourite = new Intent(
					this, AddLocationActivity.class);
			startActivityForResult(switchToAddFavourite, REQUEST_CODE);

            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void startService() {
    	Intent mServiceIntent = new Intent(this, LocationMonitorService.class);
    	this.startService(mServiceIntent);
    }

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		if (R.id.gridView1 == parent.getId()) {
			Log.d("Clicked", "gridView");
		}
		return false;
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.gridView1) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			Station s = this.stationAdapter.getItemByPosition(info.position);
			
			menu.setHeaderTitle(s.name);
			menu.add(Menu.NONE, CONTEXT_ID_DE_ACTIVATE, 1, s.active? getString(R.string.deactivate): getString(R.string.activate));
			menu.add(Menu.NONE, CONTEXT_ID_DELETE, 2, getString(R.string.delete));
		}
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Station station = this.stationAdapter.getItemByPosition(info.position);
		
		if (CONTEXT_ID_DE_ACTIVATE == item.getItemId())
		{
			// (De)activate alarm
			station.active = !station.active;
			this.stationManager.save(station);
		}
		else if (CONTEXT_ID_DELETE == item.getItemId())
		{
			// Delete alarm
			this.stationManager.delete(station._id);
		}
		
		this.stationAdapter.changeCursor(this.stationManager.getAllCursor());
		this.stationAdapter.notifyDataSetChanged();
    	this.gridView.invalidate();
		return true;
	}
};

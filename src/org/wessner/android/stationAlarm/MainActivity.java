package org.wessner.android.stationAlarm;

import org.wessner.android.stationAlarm.data.DataBaseHelper;
import org.wessner.android.stationAlarm.data.Station;
import org.wessner.android.stationAlarm.data.StationAdapter;
import org.wessner.android.stationAlarm.data.StationManager;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.Toast;


public class MainActivity extends Activity implements OnItemLongClickListener, OnClickListener {
	
	/**
	 * Request code for switching to AddStationActivity
	 */
	private static final int REQUEST_CODE = 10;
	
	/**
	 * Context menu id for (de)activate entry
	 */
	private static final int CONTEXT_ID_DE_ACTIVATE = 1;
	/**
	 * Context menu id for delete entry
	 */
	private static final int CONTEXT_ID_DELETE = 2;
	/**
	 * Context menu id for edit entry
	 */
	private static final int CONTEXT_ID_EDIT = 3;
	
	/**
	 * StationManager used for retrieving the saved stations
	 */
	private StationManager stationManager;
	/**
	 * StationAdapter as middle-ware between GridView and station Manager
	 */
	private StationAdapter stationAdapter;
	
	/**
	 * Activity's layout
	 */
	private View layout;
	
	/**
	 * GridView showing all stations
	 */
	private GridView gridView;
	
	/**
	 * Button for starting the alarm service
	 */
	private Button startButton;
	/**
	 * Button for stopping the alarm service
	 */
	private Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Get references to used view elements and register listeners       
        this.layout = findViewById(R.id.linear_layout);
        this.startButton = (Button) findViewById(R.id.button_start);
        this.startButton.setOnClickListener(this);
        this.stopButton = (Button) findViewById(R.id.button_stop);
        this.stopButton.setOnClickListener(this);
        
        this.gridView = (GridView) findViewById(R.id.gridView1);
        this.stationManager = new StationManager(new DataBaseHelper(this));
        this.stationAdapter = new StationAdapter(this, this.stationManager.getAllCursor());
        this.gridView.setAdapter(this.stationAdapter);
        this.gridView.invalidate();
        
        // Add app's context menu
        registerForContextMenu(this.gridView);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	// Update the grid view
    	this.updateGridView();
    	
    	// Change start/stop button accordingly
    	this.setServiceButtons();
    }
    
    /**
     * Sets the visibility of the start & end service button regarding the current service state.
     */
    private void setServiceButtons() {
    	this.setServiceButtons(LocationMonitorService.isRunning());
    }
    
    /**
     * Sets the visibility of the start & end service button regarding the current service state.
     * 
     * @param running		set to true if service is running
     */
    private void setServiceButtons(boolean running) {
    	if (running) {
    		this.startButton.setVisibility(View.GONE);
    		this.stopButton.setVisibility(View.VISIBLE);
    	}
    	else {
    		this.startButton.setVisibility(View.VISIBLE);
    		this.stopButton.setVisibility(View.GONE);
    	}
    	
    	this.layout.invalidate();
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
        	final Intent switchToAddStation = new Intent(
					this, AddLocationActivity.class);
			startActivityForResult(switchToAddStation, REQUEST_CODE);

            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    /**
     * Starts the Alarm service
     */
    private void startService() {
    	// Check if there is any active station:
    	if (this.stationManager.getAllActive().size() < 1) {
    		// Now active station found
    		Toast.makeText(this, getString(R.string.err_no_active_station), Toast.LENGTH_LONG).show();
    		return;
    	}
    	
    	Intent mServiceIntent = new Intent(this, LocationMonitorService.class);
    	this.startService(mServiceIntent);
    	
    	// update buttons visibility
    	this.setServiceButtons(true);
    }
    
    /**
     * Stops the Alarm service
     */
    private void stopService() {
    	Intent mServiceIntent = new Intent(this, LocationMonitorService.class);
    	this.stopService(mServiceIntent);
    	
    	// update buttons visibility
    	this.setServiceButtons(false);
    }
	
	/**
	 * Adds entries to context menu
	 */
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.gridView1) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			Station s = this.stationAdapter.getItemByPosition(info.position);
			
			menu.setHeaderTitle(s.name);
			menu.add(Menu.NONE, CONTEXT_ID_DE_ACTIVATE, 1, s.active? getString(R.string.deactivate): getString(R.string.activate));
			menu.add(Menu.NONE, CONTEXT_ID_EDIT, 1, getString(R.string.edit));
			menu.add(Menu.NONE, CONTEXT_ID_DELETE, 2, getString(R.string.delete));
		}
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Station station = this.stationAdapter.getItemByPosition(info.position);
		
		switch (item.getItemId()) {
		case CONTEXT_ID_DE_ACTIVATE:
			// (De)activate alarm
			station.active = !station.active;
			this.stationManager.save(station);
			break;
		case CONTEXT_ID_DELETE:
			// Delete alarm
			this.stationManager.delete(station._id);
			break;
		case CONTEXT_ID_EDIT:
			Intent switchToAddStation = new Intent(this,
					AddLocationActivity.class);
			switchToAddStation.putExtra("stationId", station._id);
			startActivity(switchToAddStation);
			break;
		default: // unkown code
			return true;
		}
		
		// Update grid view
		this.updateGridView();
    	
		return true;
	}
	
	/**
	 * Updates the grid view with current stations
	 */
	private void updateGridView() {
		this.stationAdapter.changeCursor(this.stationManager.getAllCursor());
		this.stationAdapter.notifyDataSetChanged();
    	this.gridView.invalidate();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_start:
			this.startService();
			break;
		case R.id.button_stop:
			this.stopService();
			break;
		default:

		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		return false;
	}
};

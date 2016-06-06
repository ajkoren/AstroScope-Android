package com.astro.scope;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.TextView;
import android.widget.Toast;

import android.view.MenuItem;
import android.view.Menu;
//import android.view.MenuInflater;


public class Selector extends Activity implements OnItemSelectedListener, OnClickListener
{
	private Menu configMenu;
	private static final int CONF_SAVED_MENU = 1;
	
	private EditText mName;
    private TextView mDateDisplay, mTimeDisplay, mLocationDisplay;
    private Button mPickDate, mPickTime, mPickLocation;
    private Button mNatal, mTransChart, mTransTable;
    private Button mOpen, mSave;
	//private Spinner natalSpinner;
    
    private int mYear, mMonth, mDay;
    private int mHour, mMin;
    
    static final int DATE_DIALOG_ID = 0;
    static final int TIME_DIALOG_ID = 1;
    
	private LocationManager mLocationManager;
	private MyLocationListener locListener;
	private String mLocationProvider;
	
	private static final int requestCodeLocator = 1;
	private static final int requestCodeListPeople = 2;
	
	private static double lat;
	private static double lng;
	private static double tz;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		// capture our View elements
        mName = (EditText) findViewById(R.id.nameEditText);
        mDateDisplay = (TextView) findViewById(R.id.dateDisplayText);
        mPickDate = (Button) findViewById(R.id.pickDateButton);
        mTimeDisplay = (TextView) findViewById(R.id.timeTextView);
        mPickTime = (Button) findViewById(R.id.pickTimeButton);
        mLocationDisplay = (TextView) findViewById(R.id.locationTextView);
        mPickLocation = (Button) findViewById(R.id.pickLocationButton);
        mNatal = (Button) findViewById(R.id.natalButton);
        //mTransChart = (Button) findViewById(R.id.transChartButton);
        mTransTable = (Button) findViewById(R.id.transTableButton);
        mOpen = (Button) findViewById(R.id.openButton);
        mSave = (Button) findViewById(R.id.saveButton);

        //natalSpinner = (Spinner) findViewById(R.id.planetSp);
       // natalSpinner.setOnItemSelectedListener(this);
        //natalSpinner.
        
        mPickDate.setOnClickListener(this);
        mPickTime.setOnClickListener(this);
		mPickLocation.setOnClickListener(this);
		mNatal.setOnClickListener(this);
		//mTransChart.setOnClickListener(this);
		mTransTable.setOnClickListener(this);
		mOpen.setOnClickListener(this);
		mSave.setOnClickListener(this);


		// Get the current location

		mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the location provider -> use
		Criteria criteria = new Criteria();
		mLocationProvider = mLocationManager.getBestProvider(criteria, false);
		locListener = new MyLocationListener();
		
		if ((lat == 0) && (lng == 0) && (tz == 0)) {
			try {
			mLocationManager.requestLocationUpdates(
				// min update time: 60 sec, min update dist: 10 km
				mLocationProvider, 60000, 10000, locListener);  
				mLocationManager.getLastKnownLocation(mLocationProvider);
			}
			catch (Exception ex) {
				Log.e("Astro", ex.toString());
			}
		}
		
        // Get the current date + time
		
        if ((mYear == 0) && (mMonth == 0) && (mDay == 0)) {
            Calendar today = Calendar.getInstance();
	        mYear = today.get(Calendar.YEAR);
	        mMonth = today.get(Calendar.MONTH)+1;
	        mDay = today.get(Calendar.DAY_OF_MONTH);
	        mHour = today.get(Calendar.HOUR_OF_DAY);
	        mMin = today.get(Calendar.MINUTE);
        }
        
        displayDateTime();
 
        // This (julian date) should be the same as the above calendar date...
	    //Jdate jd = new Jdate();
		//jdateNow = jd.getJD();
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){
        case R.id.pickDateButton:
        	showDialog(DATE_DIALOG_ID);
        	break;
        
        case R.id.pickTimeButton:
        	showDialog(TIME_DIALOG_ID);
        	break;
        
        case R.id.pickLocationButton:
            Intent intentLoc = new Intent("com.astro.scope.LOCATOR");
            startActivityForResult(intentLoc, requestCodeLocator);
            break;	
        
        case R.id.natalButton:
        	Intent natalIntent = new Intent("com.astro.scope.NATAL");
        	natalIntent.setClass(this, ShowNatal.class);
        	Bundle natalBundle = new Bundle();
        	natalBundle.putDouble("lat", lat);
        	natalBundle.putDouble("lng", lng);
        	natalBundle.putDouble("tz", tz);
        	natalBundle.putInt("year", mYear);
        	natalBundle.putInt("month", mMonth);
        	natalBundle.putInt("day", mDay);
        	double hourDbl = mHour + mMin / 60.0;
        	natalBundle.putDouble("hour", hourDbl);
        	natalIntent.putExtras(natalBundle);
            startActivity(natalIntent);
            break;	
       	/*
        case R.id.transChartButton:
        	Intent transChartIntent = new Intent("com.astro.scope.TRANSITIONS");
        	transChartIntent.setClass(this, ShowTransitions.class);
        	Bundle transChartBundle = new Bundle();
        	transChartBundle.putDouble("lat", lat);
        	transChartBundle.putDouble("lng", lng);
        	transChartBundle.putDouble("tz", tz);
        	transChartBundle.putInt("year", mYear);
        	transChartBundle.putInt("month", mMonth);
        	transChartBundle.putInt("day", mDay);
        	transChartBundle.putInt("hour", mHour);
        	transChartBundle.putInt("nim", mMin);
        	transChartIntent.putExtras(transChartBundle);
            startActivity(transChartIntent);
            break;	
		*/
        case R.id.transTableButton:
        	Log.i("Astro", "TransTable");
        	//Intent transTableIntent = new Intent("com.astro.scope.TRANSTABLE");
        	Intent transTableIntent = new Intent(
        		this.getApplicationContext(), TransitsTable.class);
        	//transTableIntent.setClass(this, TransTable.class);
        	Bundle transTableBundle = new Bundle();
        	transTableBundle.putInt("byear", mYear);
        	transTableBundle.putInt("bmonth", mMonth);
        	transTableBundle.putInt("bday", mDay);
        	double hourTrans = mHour + mMin / 60.0;
        	transTableBundle.putDouble("bhour", hourTrans);
        	transTableBundle.putDouble("blat", lat);
        	transTableBundle.putDouble("blng", lng);
        	transTableBundle.putDouble("btz", tz);
        	transTableIntent.putExtras(transTableBundle);
            startActivity(transTableIntent);
            break;
            
        case R.id.openButton:
        	Log.i("Astro", "openButton");
        	open();
            break;	
        
        case R.id.saveButton:
        	Log.i("Astro", "saveButton");
        	save();
            break;
        }	

    }
    
    private void save() {
    	Log.i("Astro", "save()");
    	String name = mName.getText().toString();
    	Log.i("Astro", "name=" + name);
    	if ((name == null) || (name.length() == 0)) {
        	Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();    		
    	} else if ((mYear == 0) || (mMonth == 0) || (mDay == 0)) {
        	Toast.makeText(this, "Please enter a valid date", Toast.LENGTH_SHORT).show();
    	} else if ((mHour < 0 ) || (mHour > 23 ) || (mMin < 0) || (mMin > 59)) {
        	Toast.makeText(this, "Please enter a valid time", Toast.LENGTH_SHORT).show();
    	} else {
        	DBAdapter dbAdapter = new DBAdapter(this);
        	dbAdapter.open();
	    	Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();
	    	dbAdapter.insertNatal(name, 
	    		mYear, mMonth, mDay, mHour, mMin, lat, lng, tz);
	    	dbAdapter.close();
    	}
    }

    private void open() {
    	Log.i("Astro", "open()");
    	
    	Intent listPeopleIntent = new Intent("com.astro.scope.LISTPEOPLE");
    	listPeopleIntent.setClass(this, ListPeople.class);
        startActivityForResult(listPeopleIntent, requestCodeListPeople);
    }
    
    private void displayDateTime() {
		mDateDisplay.setText(
			new StringBuilder()
				// Month is 0 based so add 1
				.append(mYear).append("-")
				.append(mMonth).append("-")
				.append(mDay).append(" "));
		mTimeDisplay.setText(
			new StringBuilder()
				.append(String.format("%02d:%02d", mHour, mMin)));
	}
	
	private void displayLocation(double lat, double lng, double tz) {
		char latSign = (lat >= 0) ? 'N' : 'S';
		char lngSign = (lng >= 0) ? 'E' : 'W';
		String latStr = String.format("%c %2.2f", latSign, Math.abs(lat));
		String lngStr = String.format("%c %3.2f", lngSign, Math.abs(lng));
		String tzStr = "";
		if (tz < 13) tzStr = "TZ:" + tz;
		mLocationDisplay.setText(latStr + ", " + lngStr + ", " + tzStr);
	}

	public void onActivityResult(int reqCode, int resCode, Intent intent)
    {
    	if (reqCode == requestCodeLocator) {
    		if (resCode == RESULT_OK) {
    			Bundle extras = intent.getExtras();
    			lat = extras.getDouble("latitude");
    	    	lng = extras.getDouble("longitude");
    	    	tz = extras.getDouble("timezone");
    			displayLocation(lat, lng, tz);
    		}
    	} else if (reqCode == requestCodeListPeople) {
        		if (resCode == RESULT_OK) {
        			Bundle extras = intent.getExtras();
        			mName.setText(extras.getString("name"));
        			mYear = extras.getInt("year");
        			mMonth = extras.getInt("month");
        			mDay = extras.getInt("day");
        			mHour = extras.getInt("hour");
        			mMin = extras.getInt("minute");
        			lat = extras.getDouble("latitude");
        	    	lng = extras.getDouble("longitude");
        	    	tz = extras.getDouble("timezone");
        	    	displayDateTime();
        			displayLocation(lat, lng, tz);
        		}
    	}
    }
	
    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, 
                                      int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear+1;
                    mDay = dayOfMonth;
                    displayDateTime();
                }
            };

    // the callback received when the user "sets" the time in the dialog
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {

                public void onTimeSet(TimePicker view, int hour, int min) {
                    mHour = hour;
                    mMin = min;
                    displayDateTime();
                }
            };

	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    case DATE_DIALOG_ID:
	        return new DatePickerDialog(this,
	                    mDateSetListener,
	                    mYear, mMonth-1, mDay);
	        
	    case TIME_DIALOG_ID:
	        return new TimePickerDialog(this,
	                    mTimeSetListener,
	                    mHour, mMin, false);
	    }
	    return null;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
	    //MenuInflater inflater = getMenuInflater();
	    //inflater.inflate(R.menu.config_menu, menu);
		
		MenuItem item;
		menu.add(0, CONF_SAVED_MENU, 0, "Delete From List");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case CONF_SAVED_MENU:
        	Log.i("Astro", "Delete From List");
        	Intent deleteIntent = new Intent("com.astro.scope.DELETAPEOPLE");
        	deleteIntent.setClass(this, DeletePeople.class);
        	startActivity(deleteIntent);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	private class MyLocationListener implements LocationListener
    {

		@Override
		public void onLocationChanged(Location location) {
			lat = location.getLatitude();
			lng = location.getLongitude();
			
			TimeZone tzHere = TimeZone.getDefault();		
			int rawOffset = tzHere.getRawOffset();
			tz = rawOffset / (double)3600000; // 3600000=(60*60*1000);

			displayLocation(lat, lng, tz);
		}
	
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	
		@Override
		public void onProviderEnabled(String provider) {
		}
	
		@Override
		public void onProviderDisabled(String provider) {
		}
	
	} // class MyLocationListener

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}

}
package com.astro.scope;

import android.app.Activity;
import android.content.Intent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class Locator extends Activity implements OnClickListener
{
    private double userLongitude, userLatitude, userTZ;
    private EditText etLongitude, etLatitude, geoName; 
    Spinner latNS, lngEW, tzSpinner;
    private TextView tvGeocodeOutput, tvLocator;
    private Button btnFindMyLocation, btnOK;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locator);

		// capture our View elements
        btnFindMyLocation = (Button) findViewById(R.id.findMyLocationButton);
        btnFindMyLocation.setOnClickListener(this);
        btnOK = (Button) findViewById(R.id.okButton);
        btnOK.setOnClickListener(this);
        
        etLongitude = (EditText) findViewById(R.id.lngEditText);
        etLatitude = (EditText) findViewById(R.id.latEditText);
        latNS = (Spinner) findViewById(R.id.latSignSpinner);
        lngEW = (Spinner) findViewById(R.id.lngSignSpinner);
        tzSpinner = (Spinner) findViewById(R.id.timezoneSpinner);
       
        tvLocator = (TextView) findViewById(R.id.locatorTextView);
        tvGeocodeOutput = (TextView) findViewById(R.id.geocodeOutputTextView);
        etLongitude = (EditText) findViewById(R.id.lngEditText);
        etLatitude = (EditText) findViewById(R.id.latEditText);
}
    
    @Override
    public void onClick(View v) {

        switch(v.getId()){
        case R.id.findMyLocationButton:
            // Following adapted from Conder and Darcey, pp.321 ff.		
            EditText placeText = (EditText) findViewById(R.id.geocodeEditText);			
            String placeName = placeText.getText().toString();
            // Break from execution if the user has not entered anything in the field
            if(placeName.compareTo("")==0) break;
            int numberOptions = 5;
            String [] optionArray = new String[numberOptions];
            Geocoder gcoder = new Geocoder(this);  
              
            // Note that the Geocoder uses synchronous network access, so in a serious application
            // it would be best to put it on a background thread to prevent blocking the main UI if network
            // access is slow. Here we are just giving an example of how to use it so, for simplicity, we
            // don't put it on a separate thread.
                                
            try {
                List<Address> results = gcoder.getFromLocationName(placeName,numberOptions);
                Iterator<Address> locations = results.iterator();
                String raw = "\nRaw String:\n";
                int opCount = 0;
                while(locations.hasNext()){
                    Address location = locations.next();
                    userLatitude = location.getLatitude();                        
                    userLongitude = location.getLongitude();

                    if (userLatitude >= 0) latNS.setSelection(0);
                    else latNS.setSelection(1);
                    if (userLongitude >= 0) lngEW.setSelection(0);
                    else lngEW.setSelection(1);
                    
        			String latStr = String.format("%2.2f", Math.abs(userLatitude));
        			etLatitude.setText(latStr);
        			String lngStr = String.format("%3.2f", Math.abs(userLongitude));
                    etLongitude.setText(lngStr);
                    
                    InputStream in = WebServices.OpenHttpConnection(
                    	"http://www.earthtools.org/timezone/" +
                    	userLatitude + "/" + userLongitude);
                    String text = WebServices.getText(in);
                    Log.i("Geocoder", "text=" + text);
                    String tzString = WebServices.getXmlValue(text, "offset");
                    userTZ = Double.valueOf(tzString);
                    Log.i("Geocoder", "userTZ=" + userTZ);
        			setTzSpinner(userTZ);

        			raw += location+"\n";
                    optionArray[opCount] = location.getAddressLine(0) + ", " + location.getCountryName();
                    opCount ++;
                }
                tvGeocodeOutput.setText(optionArray[0]);
                Log.i("Geocoder", raw);
                Log.i("Geocoder","\nOptions:\n");
                for(int i=0; i<opCount; i++) {
                    Log.i("Geocoder","("+(i+1)+") "+optionArray[i]);
                }
                    					
            } catch (IOException e){
                Log.e("Geocoder", "I/O Failure; is network available?",e);
            }			
            break;
            
        case R.id.okButton:
            userLatitude = getFromSpinnerAndText(latNS, etLatitude);
            userLongitude = getFromSpinnerAndText(lngEW, etLongitude);
			userTZ = getTzSpinner();
            //Log.i("Geocoder", 
            //	"lat=" + userLatitude + 
            //	", lng=" + userLongitude + 
            //	", tz=" + userTZ);
            Intent intent = new Intent();
            intent.putExtra("latitude", userLatitude);
            intent.putExtra("longitude", userLongitude);
            intent.putExtra("timezone", userTZ);
            setResult(RESULT_OK, intent);
            Log.i("Geocoder", "Returning from Locator");
            finish();

            break;
        }
    }

    private double getFromSpinnerAndText(Spinner spinner, EditText editText)
    {
		Double dblValue;
		 try {
			 dblValue = Double.parseDouble(editText.getText().toString());
		 } catch (NumberFormatException e) {
			 dblValue = (double)0;
		 }
		 if (spinner.getSelectedItemPosition() == 1)
			 return (-dblValue);
		 else
			 return (dblValue);
    }

    private double getTzSpinner()
    {
		Double doubleTZ = (double) 0;
		String itemString = (String)tzSpinner.getSelectedItem();
		int itemIndex = tzSpinner.getSelectedItemPosition();
		
		if ((itemIndex == 0) && (itemString.startsWith("UTC GMT"))) {
			doubleTZ = (double) 0;
			
		} else {
			if (itemString.startsWith("UTC+")) {
				int startIndex = itemString.indexOf('+');
				int hourMinIndex = itemString.indexOf(':');
				int endMinIndex = itemString.indexOf(' ');
                //Log.i("Geocoder", "start=" + startIndex +
                //		", hourMinIndex=" + hourMinIndex +
                //		", endMinIndex=" + endMinIndex);
				String hourOffsetStr = itemString.substring(startIndex+1, hourMinIndex);
				String minOffsetStr = itemString.substring(hourMinIndex+1, endMinIndex);
				doubleTZ = Double.valueOf(hourOffsetStr) + 
					Double.valueOf(minOffsetStr)/60;
			}
			if (itemString.startsWith("UTC-")) {
				int startIndex = itemString.indexOf('-');
				int hourMinIndex = itemString.indexOf(':');
				int endMinIndex = itemString.indexOf(' ');
				String hourOffsetStr = itemString.substring(startIndex+1, hourMinIndex);
				String minOffsetStr = itemString.substring(hourMinIndex+1, endMinIndex);
				doubleTZ = - (Double.valueOf(hourOffsetStr) + 
					Double.valueOf(minOffsetStr)/60);
			}
		}
		
		tvLocator.setText(Double.toString(doubleTZ));
		
		return doubleTZ;
    }
    
	private void setTzSpinner(double userTZ) {
		int spinnerIndex = 0;
		
        Log.i("Geocoder", "userTZ=" + userTZ);
        
		if (userTZ >= 0) {
			spinnerIndex = (int)userTZ;
			
			if (userTZ > 3) {
				spinnerIndex++;
			}
			if (userTZ > 5) {
				spinnerIndex++;
			}
			if (userTZ > 6) {
				spinnerIndex++;
			}
			if (userTZ > 7) {
				spinnerIndex++;
			}
			if (userTZ > 9) {
				spinnerIndex++;
			}
		}
		if (userTZ < 0) {
			spinnerIndex = 12 - (int)userTZ;
		}
		
		tzSpinner.setSelection(spinnerIndex);
	}

}

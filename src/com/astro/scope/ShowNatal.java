package com.astro.scope;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;

import swisseph.*;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class ShowNatal extends Activity implements OnItemSelectedListener, OnClickListener
{
	private TextView coordTV;
	private TextView aspectsTV;
	//private Spinner typeAstro;
	private Spinner planetSpinner;
	private Button expalnationBtn;
	
	private int planetIndex = 0; // 0=Sun
	private int iflag;
	private double tjd_ut;
	
	private static final int hsys = 'P'; // P=Plucidous K=Koch

	String[] planets = {
		"Sun ", "Moon", "Mercury", "Venus", "Mars",
		"Jupiter", "Saturn", "Uranus", "Neptune", "Pluto"
	};

	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.natal);
		
        coordTV = (TextView) findViewById(R.id.coordTV);
        aspectsTV = (TextView) findViewById(R.id.aspectsTV);
        
        //typeAstro = (Spinner) findViewById(R.id.tradShiftedSp);
        //typeAstro.setOnItemSelectedListener(this);

        planetSpinner = (Spinner) findViewById(R.id.planetSp);
        planetSpinner.setOnItemSelectedListener(this);
        planetSpinner.setSelection(planetIndex);
        
        expalnationBtn = (Button) findViewById(R.id.explanationBtn);
        expalnationBtn.setOnClickListener(this);

		planetCalcAndDisp (planetIndex);
		aspectsCalcAndDisp (planetIndex);
	}
        
    private void planetCalcAndDisp (int planetIndex) 
    {
    	Intent intent = this.getIntent();
		Bundle extras = intent.getExtras();
		double geolat = extras.getDouble("lat");
		double geolon = extras.getDouble("lng");
		double tz = extras.getDouble("tz");
		int year = extras.getInt("year");
		int month = extras.getInt("month");
		int day = extras.getInt("day");
		double hour = extras.getDouble("hour");
    	
       	Log.i("Astro", "planetCalcAndDisp: Lat: " + geolat + ", Lng: " + geolon + ", tz: " + tz);
       	Log.i("Astro", "planetCalcAndDisp: year: " + year + ", month: " + month + ", day: " + day);
       	Log.i("Astro", "planetCalcAndDisp: hour: " + hour);
       	Log.i("Astro", "planetNum: " + planetIndex);
       	
        double jut = -tz + hour;

        SwissEph se = new SwissEph("");
        tjd_ut = swisseph.SweDate.getJulDay(
            year, month, day, jut, swisseph.SweDate.SE_GREG_CAL);
        
        // do the coordinate calculations 
        
        StringBuffer serr = new StringBuffer();
        double[] planetPos = new double[6];
        iflag = swisseph.SweConst.SEFLG_SPEED;
        int iflgret;
        //double tjd_ut = tjd_et + SweDate.getDeltaT;
        
        int planetSwephIndex = SwissEphConst.planetIndexToSwephIndex(planetIndex);
        iflgret = se.swe_calc_ut(tjd_ut, planetSwephIndex, iflag, planetPos, serr);
        if (iflgret < 0) {
            Log.e("Astro", serr.toString());
        }
		
        Log.i("Astro", "RA: " + planetPos[0]);
        
        // Houses
           	
        double[] cusps = new double[13];
        double[] ascmc = new double[10];
        se.swe_houses(tjd_ut, 0, geolat, geolon, hsys, cusps, ascmc);
        		
        String coordText = String.format("Julian Day: %7.2f\n", tjd_ut);
        coordTV.setText(coordText);
        coordTV.append(String.format("Right Ascen: %5.2f° \n", planetPos[0]));
        for (int i = 1; i < 12; i++) {
        	if (planetInHouse(planetPos[0], cusps[i], cusps[i+1]))
        		coordTV.append(String.format("In House %d: %5.2f° - %5.2f° ", i, cusps[i], cusps[i+1]));
        }
    	if (planetInHouse(planetPos[0], cusps[12], cusps[1]))
    		coordTV.append(String.format("In House %d: %5.2f° - %5.2f° ", 12, cusps[12], cusps[1]));
        
    	coordTV.append("\n");
        

       	// Zodiac RA 
       	
       	ZodiacSign zSign = new ZodiacSign(planetPos[0]);
		String zodiacText = String.format("Zodiac: %s, %02d°%02d\'", 
	       	zSign.name, zSign.raSignInt, zSign.raSignMin);
		coordTV.append(zodiacText);
    } 

    private boolean planetInHouse(double planetPos, double cusps0, double cusps1) {
    	if (cusps1 < cusps0) {
    		if (planetPos < cusps1) planetPos += 360;
    		cusps1 += 360;
    	}
    	
    	if ((planetPos >= cusps0) && (planetPos < cusps1)) {
    		return true;
    	} else {
    		return false;
    	}
    }

   	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) 
	{
       	Log.i("Astro", "onItemSelected");
        int planetSwephIndex = SwissEphConst.planetIndexToSwephIndex(pos);
    	planetCalcAndDisp(planetSwephIndex);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub	
	}	
/*
   	public class ZodiacSign {
   		public String name;
   		public int raSignInt;
   		public int raSignMin;
   	}
   	
   	private ZodiacSign getZodiacSignFromRA(double raDeg)
   	{
   		ZodiacSign zSign = new ZodiacSign();
   		
   		int zodiacSignNum = (int) Math.floor(raDeg / 30);
   		int zodiacSignDeg = zodiacSignNum * 30;
   		zSign.raSignInt = (int) Math.floor(raDeg - zodiacSignDeg);
   		zSign.raSignMin = (int) (60 * (raDeg - zodiacSignDeg - zSign.raSignInt));
   		
   		zSign.name = Zodiac.names[zodiacSignNum];
  		
   		return zSign;
   	}
*/
    private void aspectsCalcAndDisp (int selectedPlanetIndex) 
    {
    	final int aspectTolerance = 4;
    	
    	int maxPlanets = planetSpinner.getCount();
    	
    	// Write names of planets horizontally
    	
    	aspectsTV.setText("Aspects: ");
    	for (int planetIndex = 0; planetIndex < maxPlanets; planetIndex++) {
    		String planetShortName = (planets[planetIndex].length() > 4)? 
    				planets[planetIndex].substring(0, 4) : planets[planetIndex];
    		String paddedPlanetName = (planetShortName + "   ").substring(0, 5);
    		aspectsTV.append(paddedPlanetName);
    	}
    	aspectsTV.append("\n");
    	
    	// write names of planets vertically + aspect symbols
    	
        SwissEph se = new SwissEph("");

        for (int planetIndex = 0; planetIndex < maxPlanets; planetIndex++) {
    		String planetShortName = (planets[planetIndex].length() > 3)? 
    				planets[planetIndex].substring(0, 4) : planets[planetIndex];
    	    String paddedPlanetName = (planetShortName + "    ").substring(0, 5);
    		aspectsTV.append(paddedPlanetName);
    		
    		// Calculate position for planet
    		
	        StringBuffer serr = new StringBuffer();
	        double[] planetPos = new double[6];
	        int planetSwephIndex = SwissEphConst.planetIndexToSwephIndex(planetIndex);
	        int iflgret = se.swe_calc_ut(tjd_ut, planetSwephIndex, iflag, planetPos, serr);
	        if (iflgret < 0) {
	            Log.e("Astro", serr.toString());
	        }

			String padding = "    ";

			for (int aspectingPlanet = 0; aspectingPlanet < maxPlanets; aspectingPlanet++) {
    			String aspectSign = " ";

    	        double[] aspectingPlanetPos = new double[6];
    	        int aspectingPlanetSwephIndex = SwissEphConst.planetIndexToSwephIndex(aspectingPlanet);
    	        int iflgretAspecting = se.swe_calc_ut(tjd_ut, 
    	        	aspectingPlanetSwephIndex, iflag, aspectingPlanetPos, serr);
    	        if (iflgretAspecting < 0) {
    	            Log.e("Astro", serr.toString());
    	        }

    			double aspect = planetPos[0] - aspectingPlanetPos[0];
	            if (planetIndex == 0) Log.i("Astro", "aspect=" + aspect);
    			
	            int specificAspectTol = aspectTolerance;
	            if ((planetIndex < 2) || (aspectingPlanet < 2)) 
	            	specificAspectTol = aspectTolerance + 2;
    			if (isSpecialAspect(aspect, 0, specificAspectTol)) aspectSign = "0";
    			if (isSpecialAspect(aspect, 60, specificAspectTol)) aspectSign = "*";
    			if (isSpecialAspect(aspect, 90, specificAspectTol)) aspectSign = "□";
    			if (isSpecialAspect(aspect, 120, specificAspectTol)) aspectSign = "∆";
    			if (isSpecialAspect(aspect, 180, specificAspectTol)) aspectSign = "/";
    			
    			aspectsTV.append(padding + aspectSign);
    		}
    		aspectsTV.append("\n");
    	}
    }

    private boolean isSpecialAspect(double aspect, double specialAspect, double aspectTolerance)
    {
    	if ((aspect > specialAspect - aspectTolerance) &&
    		(aspect < specialAspect + aspectTolerance))
    	{
    		return true;
    	}
    	
    	if ((aspect > (-specialAspect) - aspectTolerance) &&
    		(aspect < (-specialAspect) + aspectTolerance))
    	{
    		return true;
    	}
    	
    	return false;
    }

	@Override
	public void onClick(View v) {
        switch(v.getId()){
        case R.id.explanationBtn:
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.natal_dialog);
            dialog.setTitle("Natal Key");
            dialog.setCancelable(true);
        	dialog.show();
        	break;
        }
	}
}

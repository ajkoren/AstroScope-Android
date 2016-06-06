package com.astro.scope;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;


public class TransitsTable extends ListActivity implements Runnable, OnItemSelectedListener, OnClickListener
{
	private Button expBtn;
	private Spinner planetSpinner;
	private ProgressDialog pd;
    
	private int byear;
	private int bmonth;
	private int bday;
	private double bhour;
	private double blat;
	private double blng;
	private double btz;

	private int innerPlanet;
	private List<TransitData> tCalcResults;
    private List<Integer> displayListIdx;
    private double[] houseCusps;
    private TransitsCalc tCalc;
	
	private static final String[] planets = {
		"Sun", "Moon", "Mercury", "Venus", "Mars",
		"Jupiter", "Saturn", "Uranus", "Neptune", "Pluto"
	};
	
	private static final String[] zodiac = {
		"Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
		"Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
	};

	private static final int[] ruler = {
		4, 3, 2, 1, 0, 8, 9, 10, 11, 7
	};

	private static final int[] ruler2 = {
		-1, -1, 5, 6, -1, -1, -1, -1, -1, -1
	};

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transits_table);

        expBtn = (Button) findViewById(R.id.explanationBtn);
        expBtn.setOnClickListener(this);
        //listView = (ListView) findViewById(R.id.list);
        planetSpinner = (Spinner) findViewById(R.id.planetSp);
        planetSpinner.setOnItemSelectedListener(this);
        planetSpinner.setSelection(0);

    	Intent intent = this.getIntent();
		Bundle extras = intent.getExtras();
		byear = extras.getInt("byear");
		bmonth = extras.getInt("bmonth");
		bday = extras.getInt("bday");
		bhour = extras.getDouble("bhour");
		blat = extras.getDouble("blat");
		blng = extras.getDouble("blng");
		btz = extras.getDouble("btz");

		Log.i("Astro", "DT: bY=" + byear + " bM=" + bmonth + " bD=" + bday);
		tCalcResults = new ArrayList<TransitData>();
		
		tCalc = new TransitsCalc(byear, bmonth, bday, bhour, blat, blng, btz);
	    houseCusps = tCalc.houseCusps();
	    for (int i = 1; i < 13; i++) {
			Log.i("Astro", "Cusps:" + houseCusps[i]);
	    }

	    ListView listView = getListView();
    	listView.setTextFilterEnabled(true);
    }
        
	@Override
	public void onClick(View v) {
        switch(v.getId()){
        case R.id.explanationBtn:
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.transits_dialog);
            dialog.setTitle("Transits Key");
            dialog.setCancelable(true);
        	dialog.show();
        	break;
        }
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}
    
	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
       	Log.i("Astro", "onItemSelected, pos=" + pos);
        innerPlanet = SwissEphConst.planetIndexToSwephIndex(pos);
    	pd = ProgressDialog.show(this, "Working..", "Calculating Aspects", true, false);
		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
    public void run() {
    	tCalcResults = tCalc.calculate(innerPlanet);
    	handler.sendEmptyMessage(0);
    }
    
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            pd.dismiss();
            
            List<String> displayList = new ArrayList<String>();
            Iterator<TransitData> iter = tCalcResults.iterator();
            displayListIdx = new ArrayList<Integer>();
            int lastTransitingPlanet = -1;
            int i = 0;
            
            while (iter.hasNext()) {
            	TransitData transitData = new TransitData(iter.next());
            	if (transitData.outerPlanet != lastTransitingPlanet) {
            		lastTransitingPlanet = transitData.outerPlanet;
            		displayList.add("");
            		displayList.add(planets[transitData.innerPlanet] + " - " + 
            			planets[transitData.outerPlanet] + " transits");
                	displayListIdx.add(-1);
                	displayListIdx.add(-1);
            	}
            	            	
            	displayListIdx.add(i);
            	if (isMajor(displayList, i)) {
            		displayList.add("* " + transitData.aspect + "°: " + 
            				transitData.startDate + " - " + transitData.endDate);
            	} else {
            		displayList.add("   " + transitData.aspect + "°: " + 
            				transitData.startDate + " - " + transitData.endDate);
            	}
            	/*
                Log.i("Astro", "Adding: planets: " +  
                		planets[transitData.innerPlanet] +
                		" - " + planets[transitData.outerPlanet] + 
                		", aspect: " + transitData.aspect + " i=" + i);
                */
            	i++;
            }
            
    		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
    			TransitsTable.this, R.layout.list_item, displayList);

    		TransitsTable.this.setListAdapter(adapter);
        }
    };

    // A major aspect has at least one of the houses repeated
    private boolean isMajor(List<String> displayList, int index) {
    	boolean major = false;
    	
		TransitData transit = tCalcResults.get(index);
		
       	int zodiacNatal = ruler[transit.innerPlanet];
       	int zodiacNatal2 = ruler2[transit.innerPlanet];

       	// find house ruled
       	int natalRuledHouse = zodiacToHouse(zodiacNatal);
       	int natalRuledHouse2 = zodiacToHouse(zodiacNatal2);

       	int zodiacOuter = ruler[transit.outerPlanet];
       	int outerRuledHouse = zodiacToHouse(zodiacOuter);

       	if (
       		(planetRaToHouse(transit.raInnerPlanet) == planetRaToHouse(transit.raOuterPlanet)) ||
       		(planetRaToHouse(transit.raInnerPlanet) == natalRuledHouse) ||
       		(planetRaToHouse(transit.raInnerPlanet) == natalRuledHouse2) ||
       		(planetRaToHouse(transit.raInnerPlanet) == outerRuledHouse))
       	{
       		major = true;
       	} else if (
            (planetRaToHouse(transit.raOuterPlanet) == natalRuledHouse) ||
            (planetRaToHouse(transit.raOuterPlanet) == natalRuledHouse2) || 
            (planetRaToHouse(transit.raOuterPlanet) == outerRuledHouse))
        {
           	major = true;
       	} else if (
           	(natalRuledHouse == natalRuledHouse2) ||
            (natalRuledHouse == outerRuledHouse))
       	{
       		major = true;
       	} else if (
            (natalRuledHouse2 == outerRuledHouse))
        {
       		major = true;
       	} else {
       		major =false;
       	}
       	return major;
    }
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		int dli = displayListIdx.get(position);
        Log.i("Astro", "dli=" + dli);
        
        if (dli >= 0) {
    		TransitData transit = tCalcResults.get(dli);
    		
    		String innerPlanet = planets[transit.innerPlanet];
    		String outerPlanet = planets[transit.outerPlanet];
	        String aspect = transit.aspect + "°";
	        if (transit.aspect > 180) { 
	        	aspect += " / -" + (360 - transit.aspect) + "°"; 
	        }
	        String dates = transit.startDate + " - " + transit.endDate;
	        
	        
	       	int zodiacInner = ruler[transit.innerPlanet];
	       	int zodiacInner2 = ruler2[transit.innerPlanet];
	       	// 2nd ruler, if any
	        String ruler2Str = (zodiacInner2 >= 0) ? " & " + zodiac[zodiacInner2] + "\n" : "\n";
	        
	       	int innerRuledHouse = zodiacToHouse(zodiacInner);
	       	int innerRuledHouse2 = zodiacToHouse(zodiacInner2);
	       	// 2nd ruled house, if any
	        String innerRuledHouse2Str = (innerRuledHouse2 > 0) ? "," + innerRuledHouse2 + "\n" : "\n";

	       	int zodiacOuter = ruler[transit.outerPlanet];
	       	int outerRuledHouse = zodiacToHouse(zodiacOuter);

	       	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        Log.i("Astro", "innerPlanet:=" + innerPlanet);
	        Log.i("Astro", "outerPlanet:=" + outerPlanet);
	        Log.i("Astro", "raOuterPlanet:=" + transit.raOuterPlanet);
	        
	        builder.setTitle("Transit Details:")
	        	.setMessage("Aspect: " + aspect + "\n" +
	        		"Dates: " + dates + "\n" +
	        		"Natal " + innerPlanet + " in house " + planetRaToHouse(transit.raInnerPlanet) + "\n" +
	        		"Transiting " + outerPlanet + " in house " + planetRaToHouse(transit.raOuterPlanet) + "\n" +
	        		innerPlanet + " is ruler of " + zodiac[ruler[transit.innerPlanet]] + ruler2Str +
	        		"& therefore ruler of house " + innerRuledHouse + innerRuledHouse2Str +
	        		outerPlanet + " is ruler of " + zodiac[ruler[transit.outerPlanet]] + "\n" +
	        		"& therefore ruler of house " + outerRuledHouse)
	        	   .setCancelable(true);
	        AlertDialog alert = builder.create();
	        alert.show();
        }
	}
	
	private int planetRaToHouse(double planetRa) {
        Log.i("Astro", "planetRa:=" + planetRa);
		int houseNum = 0;
	    for (int i = 1; i < 12; i++) {
	    	if (planetInHouse(planetRa, houseCusps[i], houseCusps[i+1]))
	    		houseNum = i;
	    }
		if (planetInHouse(planetRa, houseCusps[12], houseCusps[1]))
			houseNum = 12;
		return houseNum;
	}
	
    private boolean planetInHouse(double planetRa, double cusps0, double cusps1) {
    	if (cusps1 < cusps0) {
    		if (planetRa < cusps1) planetRa += 360;
    		cusps1 += 360;
    	}
    	
    	if ((planetRa >= cusps0) && (planetRa < cusps1)) {
    		return true;
    	} else {
    		return false;
    	}
    }


	public int zodiacToHouse(int zodiacNum) {
		int houseNumber = 0;
        Log.i("Astro", "zodiacNum=" + zodiacNum);
        int zodiacRA = zodiacNum * 30;
		
		for (int cuspIdx = 1; cuspIdx < 12; cuspIdx++) {
	        Log.i("Astro", "houseCusps[" + cuspIdx + "]=" + houseCusps[cuspIdx]);

	        double loBounday = houseCusps[cuspIdx];
	        double hiBounday = houseCusps[cuspIdx+1];
	        if (hiBounday < loBounday) {
	        	hiBounday += 360;
	        }
	        	
			if ((loBounday >= zodiacRA) && (hiBounday <= zodiacRA+30) || 
				(loBounday >= zodiacRA) && (hiBounday <= zodiacRA+60) || 
				(loBounday <= zodiacRA) && (hiBounday >= zodiacRA+30))
			{
				houseNumber = cuspIdx;
				break;
			}
		}
		
		if (houseNumber == 0) {
	        double loBounday = houseCusps[12];
	        double hiBounday = houseCusps[1];
	        if (hiBounday < loBounday) {
	        	hiBounday += 360;
	        }
	        	
			if ((loBounday >= zodiacRA) && (hiBounday <= zodiacRA+30) || 
				(loBounday >= zodiacRA) && (hiBounday <= zodiacRA+60) || 
				(loBounday <= zodiacRA) && (hiBounday >= zodiacRA+30))
			{
		        Log.i("Astro", "houseCusps[" + 12 + "]=" + houseCusps[12]);
				houseNumber = 12;
			}
		}
		return houseNumber;
	}
}

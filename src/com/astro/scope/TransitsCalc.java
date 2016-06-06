package com.astro.scope;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import swisseph.*;

public class TransitsCalc 
{
	private static final int hsys = 'P'; // P=Plucidous K=Koch
	private static final int displayYears = 30;

	private SwissEph se;
	private double tjd_ut;
	int flags = swisseph.SweConst.SEFLG_TRANSIT_LONGITUDE;
    int iflag = swisseph.SweConst.SEFLG_SPEED;
	boolean backwards = false;
    double[] cusps = new double[13];
    double[] ascmc = new double[10];
	
	public TransitsCalc(
		int byear, int bmonth, int bday, double bhour, 
		double lat, double lng, double tz) 
	{
		Log.i("Astro", "TC: bY=" + byear + " bM=" + bmonth + " bD=" + bday);
		Log.i("Astro", "TC: bH=" + bhour);
		Log.i("Astro", "TC: lat=" + lat + " lng=" + lng + " tz=" + tz);

        double jut = -tz + bhour;
        tjd_ut = swisseph.SweDate.getJulDay(
            byear, bmonth, bday, jut, swisseph.SweDate.SE_GREG_CAL);

        // Houses
       	
        cusps = new double[13];
        ascmc = new double[10];
		se = new SwissEph("");
        se.swe_houses(tjd_ut, 0, lat, lng, hsys, cusps, ascmc);
        
        for (int c = 1; c < 13; c++) {
        	Log.i("Astro", "CUSPS: " + cusps[c]);
        }
        		
	}
	
	public double[] houseCusps() {
		return cusps;
	}
	
	public List<TransitData> calculate(int innerPlanet) 
	{
		List<TransitData> calcResults = new ArrayList<TransitData>(); // 5 (planets) * 5 (trans) * 10 (transits)
        Jdate nowJdate = new Jdate();
        double now_ut = nowJdate.getJD();

        for (int planetIndex = innerPlanet; planetIndex < innerPlanet+1; planetIndex++) 
		{
			//Log.i("Astro", "TC: planetIndex=" + planetIndex);
			
	    	// Get the inner planet position on the birthdate
			StringBuffer serr = new StringBuffer();
	        double[] planetPos = new double[6];
	        int pSwephIndex = SwissEphConst.planetIndexToSwephIndex(planetIndex);
	        int iflgret = se.swe_calc_ut(tjd_ut, pSwephIndex, iflag, planetPos, serr);
	        if (iflgret < 0) {
	            System.err.println(serr.toString());
	        }
	        
	        for (int aspectingPlanet = 5; aspectingPlanet < 10; aspectingPlanet++) {
				//Log.i("Astro", "TC: aspectingPlanet=" + aspectingPlanet);
				
		        int startJulDay = (int)(now_ut - 365.25 * displayYears/2);
				int endJulDay = (int)(now_ut + 365.25 * displayYears/2);
				
				// Display smaller number of aspects for Jupiter
				if (aspectingPlanet == 5) { 
			        startJulDay = (int)(now_ut - 365.25 * displayYears/5);
					endJulDay = (int)(now_ut + 365.25 * displayYears/5);
				}
		        if (startJulDay < tjd_ut) startJulDay = (int)tjd_ut;

				int apSwephIndex = 
    	        	SwissEphConst.planetIndexToSwephIndex(aspectingPlanet);
	    	        
				// Get the outer planet position on the first tested date: startJulDay
				StringBuffer serr2 = new StringBuffer();
		        double[] planetPos2 = new double[6];
		        int pSwephIndex2 = SwissEphConst.planetIndexToSwephIndex(aspectingPlanet);
		        int iflgret2 = se.swe_calc_ut(startJulDay, pSwephIndex2, iflag, planetPos2, serr2);
		        if (iflgret2 < 0) {
		            System.err.println(serr2.toString());
		        }
		        
				//Log.i("Astro", "TC: aspecting pos=" + planetPos2[0] + ", planet pos=" + planetPos[0]);

				// The starting aspect is the next 30 deg. mark after the outer planet's aspect on startJulDay
		        double startJulDayAspect = planetPos2[0] - planetPos[0];
		        int startAspect = getStartAspect(startJulDayAspect);
		        int nextJulStartDay = startJulDay;
		        int nextJulEndDay = endJulDay;

				// Sun & Moon: RA orb = 10 degs. Other celestial bodies: RA orb = 8
				int raOrb = 0;
				if (planetIndex <=1) { raOrb = 10; }
				else { raOrb = 8; }
				
				for (int aspectCount = 0; aspectCount < 12; aspectCount++) {
					//Log.i("Astro", "TC: aspect=" + aspect);
					
					int aspect = (startAspect + aspectCount * 30) % 360;
					double ra = planetPos[0] + aspect;
					if (ra >= 360) ra -= 360;
					
					// Calculate transit date for aspecting planet 
					backwards = false;
					TransitCalculator tcStart = 
						new TCPlanet(se, apSwephIndex, flags, ra - raOrb);
					int jdTransitStart = (int)se.getTransitUT(tcStart, nextJulStartDay, backwards);
					backwards = true;
					nextJulEndDay = getMaxJulEndDay(aspectingPlanet, jdTransitStart);
					TransitCalculator tcEnd = 
						new TCPlanet(se, apSwephIndex, flags, ra);
					int jdTransitEnd = (int)se.getTransitUT(tcEnd, nextJulEndDay, backwards);
					if (jdTransitStart > endJulDay) {
						if (aspectCount == 0) {
							continue;
						} else {
							break;
						}
					}
					
					if (jdTransitEnd < jdTransitStart) {
						if (aspectCount == 0) {
							continue;
						} else if (aspectCount == 11) {
							break;
						}
					}
					
					// Date calculations
					Jdate startDate = new Jdate(jdTransitStart);
					String startDateStr = startDate.toString();
					String transDateStart = startDateStr.substring(0, startDateStr.indexOf(" "));
					//Log.i("Astro", "TC: transDateStart=" + transDateStart);

					Jdate endDate = new Jdate(jdTransitEnd);
					String endDateStr = endDate.toString();
					String transDateEnd = endDateStr.substring(0, endDateStr.indexOf(" "));
					//Log.i("Astro", "TC: transDateStart=" + transDateStart);

					//calcResults.add(String.format("%3d°", aspect) + ": " + transDateStart + " - " + transDateEnd);
					
			    	// Get the outer planet position on the transit date
			        double endJD = endDate.getJD();
					StringBuffer serrT = new StringBuffer();
			        double[] planetPosT = new double[6];
			        int pSwephIndexT = SwissEphConst.planetIndexToSwephIndex(aspectingPlanet);
			        int iflgretT = se.swe_calc_ut(endJD, pSwephIndexT, iflag, planetPosT, serrT);
			        if (iflgretT < 0) {
			            System.err.println(serrT.toString());
			        }

			    	TransitData transitData = new TransitData(transDateStart, transDateEnd, aspect, 
						planetIndex, aspectingPlanet, planetPos[0], ra);
					calcResults.add(transitData);
				} // for (int aspectCount = 0...
			} // for (int aspectingPlanet = 5...
		} // for (int planetIndex = innerPlanet...
        return calcResults;
	}	

    private int getStartAspect(double startJulDayAspect) {
		int testAspect = 0;
		
		if (startJulDayAspect >= 0) {
			for (int i = 0; i < 12; i++) {
				testAspect = i * 30;
				if (testAspect >= startJulDayAspect) {
					break;
				}
			}
		} else {
			for (int i = 0; i < 12; i++) {
				testAspect = -i * 30;
				if (testAspect <= startJulDayAspect) {
					break;
				}
			}
			testAspect += 390;
		}
		return testAspect;
	}
	
	private int getMaxJulEndDay(int aspectingPlanet, int jdTransitStart) {
		int maxEndJulDay = jdTransitStart + 3650;
		int swissEphPlanet = SwissEphConst.planetIndexToSwephIndex(aspectingPlanet);
		
		switch (swissEphPlanet) {
		case swisseph.SweConst.SE_JUPITER:
			maxEndJulDay = jdTransitStart + 300;
			break;
		case swisseph.SweConst.SE_SATURN:
			maxEndJulDay = jdTransitStart + 900;
			break;
		case swisseph.SweConst.SE_URANUS:
			maxEndJulDay = jdTransitStart + 1800;
			break;
		case swisseph.SweConst.SE_NEPTUNE:
			maxEndJulDay = jdTransitStart + 3300;
			break;
		case swisseph.SweConst.SE_PLUTO:
			maxEndJulDay = jdTransitStart + 4400;
			break;
		}
		
		return maxEndJulDay;
	}
}



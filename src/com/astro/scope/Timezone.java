package com.astro.scope;

import java.util.Calendar;
//import java.util.Date;
//import java.util.GregorianCalendar;
import java.util.TimeZone;;

// Find timezone from longitude and latitude
// or find current user timezone
public class Timezone 
{
	Calendar calGMT, calHere, calAnywhere;
	TimeZone tzHere, tzGMT;

	public Timezone() {
		tzHere = TimeZone.getDefault();		
		tzGMT = TimeZone.getTimeZone("GMT");		
	}
	
	public int currentTZ()
	{
		  int rawOffset = tzHere.getRawOffset();
		  int hour = rawOffset / 3600000; // 3600000=(60*60*1000);
		  return hour;
	}

}

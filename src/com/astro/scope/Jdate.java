package com.astro.scope;

import java.util.*;
/**
	$Id: Jdate.java,v 1.1 2005/10/06 21:56:37 hiram Exp $ <br>
<br>
	Jdate - Astronomical Julian Date <p>
	Astronomical Julian Dates are always UTC (GMT), there is no timezone<p>
	There is a year zero between year -1 and year 1 <p>
	Julian day 0 is Noon on 01 January -4712 == -4712/01/01 12:00:00 <p>
	See also: http://www.cl.cam.ac.uk/~mgk25/iso-time.html <br>
	ISO-8601 definitions <br>
<br>
	@author Hiram Clawson jday at hiram.ws <br>
	@version 2.4
*/

public class Jdate {

//	The field definitions for set and get

/** Constant to refer to the year calendar field */
public static final int YEAR=0;
/** Constant to refer to the month calendar field */
public static final int MONTH=1;
/** Constant to refer to the day calendar field */
public static final int DAY=2;
/** Constant to refer to the hour calendar field */
public static final int HOUR=3;
/** Constant to refer to the minute calendar field */
public static final int MINUTE=4;
/** Constant to refer to the weekday calendar field */
public static final int WEEKDAY=5;
/** Constant to refer to the day_of_year calendar field */
public static final int DAY_OF_YEAR=6;

//	The Instance variables for Jdate class

/**	Valid Julian dates are 0.0 to 2147438064.499988	*/
private double JD;
/**	Valid year is from -4712 to 5874773	*/
private int year;
/**	month range is [1-12]	*/
private int month;
/**	day range is [1-31]	*/
private int day;
/**	hour range is [0-23]	*/
private int hour;
/**	minute range is [0-60]	*/
private int minute;
/**	second range is [0.-59.999]	*/
private double second;
/**	weekday range is [0-6] == [Sunday - Saturday]	*/
private int weekday;
/**	day_of_year range is [1-366] 	*/
private int day_of_year;
/**	internal signal to trigger recalculation of the calendar fields	*/
private boolean RecalcCalendar;
/**	internal signal to trigger recalculation of the julian date */
private boolean RecalcJD;

/**
	recalculates the calendar fields from the Julian Date, used
	internally by the get() methods
*/
private void CalDate()
{
	double frac;
	int jd;
	int ka;
	int kb;
	int kc;
	int kd;
	int ke;
	int ialp;
	double d_hour;
	double d_minute;

	jd = (int) (JD + 0.5);	/* integer julian date */
	frac = JD + 0.5 - (double) jd + 1.0e-10; /* day fraction */
	ka = jd;
	if ( jd >= 2299161 )
	{
		ialp = (int)( ((double) jd - 1867216.25 ) / ( 36524.25 ));
		ka = jd + 1 + ialp - ( ialp >> 2 );
	}
	kb = ka + 1524;
	kc =  (int) ( ((double) kb - 122.1 ) / 365.25 );
	kd = (int) ((double) kc * 365.25);
	ke = (int) ((double) ( kb - kd ) / 30.6001 );
	day = kb - kd - ((int) ( (double) ke * 30.6001 ));
	if ( ke > 13 )
		month = ke - 13;
	else
		month = ke - 1;
	if ( (month == 2) && (day > 28) )
		day = 29;
	if ( (month == 2) && (day == 29) && (ke == 3) )
		year = kc - 4716;
	else if ( month > 2 )
		year = kc - 4716;
	else
		year = kc - 4715;

	// hour with minute and second included as fraction
	d_hour = frac * 24.0;
	hour = (int) d_hour;				// integer hour
	// minute with second included as a fraction
	d_minute = ( d_hour - (double) hour ) * 60.0;
	minute = (int) d_minute;			// integer minute
	// weird: time fix
        int test = (hour % 3) * 100 + minute;
	int test_tbl[] = {0, 1, 2, 11, 12, 13, 22, 23, 24, 25, 34, 35, 36,
		45, 46, 47, 56, 57, 58, 107, 108, 109, 110, 119, 120, 121,
		130, 131, 132, 141, 142, 143, 152, 153, 154, 155, 204, 205,
		206, 215, 216, 217, 226, 227, 228, 237, 238, 239, 240, 249,
		250, 251};
        for (int i = 0; i < test_tbl.length; i++)
	{
	    if (test == test_tbl[i])
	    {
		frac += 0.000012;
		d_hour = frac * 24.0;
		hour = (int)d_hour;
		d_minute = (d_hour - (double)hour) * 60.0;
		minute = (int)d_minute;
		break;
	    }
        }

	second = ( d_minute - (double) minute ) * 60.0;
	weekday = (jd + 1) % 7;				// day of week
	if ( year == ((year >> 2) << 2) )
		day_of_year = ( ( 275 * month ) / 9)
			- ((month + 9) / 12) + day - 30;
	else
		day_of_year = ( ( 275 * month ) / 9)
			- (((month + 9) / 12) << 1) + day - 30;

	RecalcCalendar = false;
	return;
}	//	end of CalDate()

/**
	recalculates the Julian date from the calendar fields, used
	internally by the get() methods.
*/
private double JulDate()
{
	// weird: months fix 
        if ((month > 12) || (month < -12))
	{
	    month--;
	    int delta = month / 12;
	    year += delta;
	    month -= delta * 12;
	    month++;
	}

	/* decimal day fraction	*/
	double frac = (hour / 24.0) + (minute / 1440.0) + (second / 86400.0);
	double gyr = year + (0.01 * month) + (0.0001 * day)
			+ (0.0001 * frac) + 1.0e-9;
	/* conversion factors */
	int iy0;
	int im0;
	if ( month <= 2 )
	{
		iy0 = year - 1;
		im0 = month + 12;
	}
	else
	{
		iy0 = year;
		im0 = month;
	}
	int ia = iy0 / 100;
	int ib = 2 - ia + (ia >> 2);
	/* calculate julian date	*/
	int jd;
	if ( year <= 0 ) {
		jd = (int) ((365.25 * iy0) - 0.75)
			+ (int) (30.6001 * (im0 + 1) )
			+ (int) day + 1720994;
	} else {
		jd = (int) (365.25 * iy0)
			+ (int) (30.6001 * (im0 + 1))
			+ (int) day + 1720994;
	}
	if ( gyr >= 1582.1015 )	/* on or after 15 October 1582	*/
		jd += ib;
	JD = jd + frac + 0.5;
	jd = (int) (JD + 0.5);
	weekday = (jd + 1) % 7;
	if ( year == ((year >> 2) << 2) )
		day_of_year =
			( ( 275 * month ) / 9)
			- ((month + 9) / 12)
			+ day - 30;
	else
		day_of_year =
			( ( 275 * month ) / 9)
			- (((month + 9) / 12) << 1)
			+ day - 30;
	RecalcJD = false;
	return( JD );
}	//	end of JulDate()

/**
	returns the object initialized to UTC date/time of now
*/
public Jdate() {
	TimeZone tz = TimeZone.getTimeZone("GMT");
 	Calendar UTInstance = Calendar.getInstance(tz);
	long msSince1970;

	year = UTInstance.get(Calendar.YEAR);
	month = UTInstance.get(Calendar.MONTH) + 1;
	day = UTInstance.get(Calendar.DAY_OF_MONTH);
	hour = UTInstance.get(Calendar.HOUR_OF_DAY);
	minute = UTInstance.get(Calendar.MINUTE);
	msSince1970 = UTInstance.get(Calendar.MILLISECOND);
	second = UTInstance.get(Calendar.SECOND) + (double) msSince1970 / 1000;
	JD = JulDate();

	return;
}

/**
	returns the object initialized to the specified Julian Date
*/
public Jdate( double jd ) {
	JD = jd;
	RecalcCalendar = true;

	return;
}

public Jdate( final int year, int month, int day, int hour, int minute ) {
	TimeZone tz = TimeZone.getTimeZone("GMT");
	this.year = year;
	this.month = month;
	this.day = day;
	this.hour = hour;
	this.minute = minute;
	JD = JulDate();
	
	return;
}

		/**
	sets Julian Date to jd - calendar will be recalculated on a get()
*/
public void setJD( double jd ) {
	JD = jd;
	RecalcCalendar = true;

	return;
}

/**
	gets one of the integer fields of the date
	Possible fields are: YEAR, MONTH, DAY, HOUR, MINUTE
	all integers
*/
public int getField( final int field ) {
	if( RecalcCalendar ) {
		CalDate();
	}
	switch (field) {
	case YEAR: return(year);
	case MONTH: return(month);
	case DAY: return(day);
	case HOUR: return(hour);
	case MINUTE: return(minute);
	case WEEKDAY: return(weekday);
	case DAY_OF_YEAR: return(day_of_year);
	default: return( year );  //  in case field is incorrect, do something
	}
}

/**
	sets one of the integer fields of the date
	Possible fields are: YEAR, MONTH, DAY, HOUR, MINUTE
	all integers
	The Julian Date will be recalculated on any get() method
*/
public void setField( final int field, int value ) {
	switch (field) {
	case YEAR:
		year = value;
		break;
	case MONTH:
		month = value;
		break;
	case DAY:
		day = value;
		break;
	case HOUR:
		hour = value;
		break;
	case MINUTE:
		minute = value;
		break;
	}
	RecalcJD = true;
}

/**
	sets the second for the date, a double including fractions of a second
*/
public void setSecond( double sec ) {
	second = sec;
	RecalcJD = true;
	return;
}

/**
	returns the second of the date, a double including fractions of a second
*/
public double getSecond() {
	if ( RecalcCalendar ) {
		CalDate();
	}
	return second;
}

/**
	returns the Julian Date - will recalculate if any fields have been set
*/
public double getJD() {
	if ( RecalcJD ) {
		JD = JulDate();
	}
	return JD;
}

public String toString() {
	if( RecalcCalendar ) {
		CalDate();
	}
	StringBuffer sb = new StringBuffer();
	sb.append(year);
	sb.append("-");
	if ( month < 10 ) { sb.append("0"); }
	sb.append(month);
	sb.append("-");
	if ( day < 10 ) { sb.append("0"); }
	sb.append(day);
	sb.append(" ");
	if ( hour < 10 ) { sb.append("0"); }
	sb.append(hour);
	sb.append(":");
	if ( minute < 10 ) { sb.append("0"); }
	sb.append(minute);
	sb.append(":");
	int sec = (int) (second + 0.5);
	if ( sec < 10 ) { sb.append("0"); }
	sb.append(sec);
	return sb.toString();
}

protected static void main(String[] args) {
	int TestCount = 15;
	double[] TestJDs = new double[TestCount];
	String[] CalResults = new String[TestCount];
	Jdate TestJD = new Jdate();
	String TestResult;
	String OK;
	int TestRun = 0;
	int TestFail = 0;
	int TestSuccess = 0;


	TestJDs[0] = 0.0; CalResults[0] ="-4712-01-01 12:00:00";
	TestJDs[1] = 59.0; CalResults[1] ="-4712-02-29 12:00:00";
	TestJDs[2] = 366.0; CalResults[2] ="-4711-01-01 12:00:00";
	TestJDs[3] = 731.0; CalResults[3] ="-4710-01-01 12:00:00";
	TestJDs[4] = 1721058.0; CalResults[4] ="0-01-01 12:00:00";
	TestJDs[5] = 1721057.0; CalResults[5] ="-1-12-31 12:00:00";
	TestJDs[6] = 1721117.0; CalResults[6] ="0-02-29 12:00:00";
	TestJDs[7] = 1721118.0; CalResults[7] ="0-03-01 12:00:00";
	TestJDs[8] = 1721423.0; CalResults[8] ="0-12-31 12:00:00";
	TestJDs[9] = 1721424.0; CalResults[9] ="1-01-01 12:00:00";
	TestJDs[10] = 2440587.5; CalResults[10] ="1970-01-01 00:00:00";
	TestJDs[11] = 2451774.726007; CalResults[11] ="2000-08-18 05:25:27";
	TestJDs[12] = 2299160.499988; CalResults[12] ="1582-10-04 23:59:59";
	TestJDs[13] = 2299160.500000; CalResults[13] ="1582-10-15 00:00:00";
	TestJDs[14] = 2147438064.499988; CalResults[14] ="5874773-08-15 23:59:59";
	System.out.println("Testing Jdate class");
	System.out.println("Testing " + TestCount + " Julian Dates");

	for( int i = 0; i < TestCount; ++i ) {
		++TestRun;
		TestJD.setJD(TestJDs[i]);
		TestResult = TestJD.toString();
		if ( 0 == TestResult.compareTo(CalResults[i]) ) { OK = "OK"; }
		else { OK = "FAIL"; ++TestFail; }
		System.out.println("JD[ " + i + "]: " + TestJDs[i] +
		" = " + TestJD + " - " + OK);
	}
	TestSuccess = TestRun - TestFail;
	System.out.println("Tests Run: " + TestRun + " Tests OK: " +
		TestSuccess + " Tests Failed: " + TestFail );
	if ( 0 == TestFail ) {
		System.out.println("Jdate is OK *****************");
	} else {
		System.out.println("Jdate has problems **********");
	}
}

}	//	end public class Jdate

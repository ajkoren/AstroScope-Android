package com.astro.scope;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager; 

public class ShowTransits extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
        	WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, 
        	WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

    	Intent intent = this.getIntent();
		Bundle extras = intent.getExtras();
		int byear = extras.getInt("byear");
		int bmonth = extras.getInt("bmonth");
		int bday = extras.getInt("bday");
		int syear = extras.getInt("syear");
		int smonth = extras.getInt("smonth");
		int sday = extras.getInt("sday");

		setContentView(new TransitsView(this, 
			byear, bmonth, bday, syear, smonth, sday));
        //TransitionsCalc tCalc = new TransitionsCalc(this.getBaseContext());
    }
}
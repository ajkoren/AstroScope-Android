package com.astro.scope;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ListPeople extends ListActivity {
	List<Person> personList = new ArrayList<Person>();
	
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
    	DBAdapter dbAdapter = new DBAdapter(this);
    	dbAdapter.open();
    	List<String> nameList=new ArrayList<String>();
    	
    	Cursor c = dbAdapter.getAllNatals();
    	if (c.moveToFirst()) {
    		do {
    			String name = c.getString(1);
    			int year = c.getInt(2);
    			int month = c.getInt(3);
    			int day = c.getInt(4);
    			int hour = c.getInt(5);
    			int minute = c.getInt(6);
    			double lat = c.getDouble(7);
    			double lng = c.getDouble(8);
    			double tz = c.getDouble(9);
    			
    			String date = year + "/" + month + "/" + day;
    			nameList.add(name + " (" + date + ")");
    			
    			Person person = new Person(name, year, month, day, 
    				hour, minute, lat, lng, tz);
    			personList.add(person);
    			
    		} while (c.moveToNext());
    	}
    	
    	dbAdapter.close();
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, nameList);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//String item = (String) getListAdapter().getItem(position);
		Person person = personList.get(position);
		
        Intent intent = new Intent();
        intent.putExtra("name", person.name);
        intent.putExtra("year", person.year);
        intent.putExtra("month", person.month);
        intent.putExtra("day", person.day);
        intent.putExtra("hour", person.hour);
        intent.putExtra("minute", person.minute);
        intent.putExtra("latitude", person.lat);
        intent.putExtra("longitude", person.lng);
        intent.putExtra("timezone", person.tz);
        setResult(RESULT_OK, intent);
        
        Log.i("Astro", "Returning from ListPeople");
        finish();
	}
	
	private class Person {
		public String name;
		public int year;
		public int month;
		public int day;
		public int hour;
		public int minute;
		public double lat;
		public double lng;
		public double tz;
		
		public Person (String name, int year, int month, int day, 
				int hour, int minute, double lat, double lng, double tz) 
		{
			this.name = name;
			this.year = year;
			this.month = month;
			this.day = day;
			this.hour = hour;
			this.minute = minute;
			this.lat = lat;
			this.lng = lng;
			this.tz = tz;
		}
	}
}


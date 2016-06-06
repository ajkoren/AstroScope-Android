package com.astro.scope;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class DeletePeople extends ListActivity implements OnClickListener
{
	private Button deleteBtn;
	private DBAdapter dbAdapter;
	private List<Integer> idList = new ArrayList<Integer>();
	private List<Integer> deleteList = new ArrayList<Integer>();
	
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
        setContentView(R.layout.deletelist);
		
    	dbAdapter = new DBAdapter(this);
    	dbAdapter.open();
    	List<String> nameList = new ArrayList<String>();
    	
    	Cursor c = dbAdapter.getAllNatals();
    	if (c.moveToFirst()) {
    		do {
    			int id = c.getInt(0);
    			String name = c.getString(1);
    			int year = c.getInt(2);
    			int month = c.getInt(3);
    			int day = c.getInt(4);
    			
    			String date = year + "/" + month + "/" + day;
    			nameList.add(name + " (" + date + ")");
    			idList.add(id);
    			
    		} while (c.moveToNext());
    	}
    	
    	ListView listView = getListView();
    	listView.setChoiceMode(2);
    	listView.setTextFilterEnabled(true);
    	
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_checked, nameList);
		setListAdapter(adapter);
		
        deleteBtn = (Button) findViewById(R.id.deleteButton);
        deleteBtn.setOnClickListener(this);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		l.setItemChecked(position, l.isItemChecked(position));
        Log.i("Astro", "Add id=" + idList.get(position));
		deleteList.add(idList.get(position));
	}
	
	private void delete() {
	    Iterator<Integer> itr = deleteList.iterator();
	    while (itr.hasNext()){
	    	int id = itr.next();
	        Log.i("Astro", "Delete id=" + id);
	        try {
	        	if (dbAdapter.deleteNatal(id)) {
	    	        Log.i("Astro", "Deleted");
	        	}
	        } catch (Exception e) {
	            Log.e("Astro", "Exception: " + e.getMessage());
	        }
	    }
        Log.i("Astro", "ListPeople: Items deleted");
        finish();
	}
	
	public void onDestroy() {
    	dbAdapter.close();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {

	        switch(v.getId()){
	        case R.id.deleteButton:
	        	delete();
	        	break;
	        }
	}
}


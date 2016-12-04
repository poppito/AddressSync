package com.noni.embryio;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Settings;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity implements TabListener, OnPageChangeListener {
	   private static ViewPager viewPager;
	   private static TabsPagerAdapter mAdapter;
	   public static final String mPassedString = "passedString";
	   private int[] Tabs = { R.drawable.ic_actionbar_tab_download, R.drawable.ic_actionbar_tab_upload, R.drawable.ic_actionbar_tab_delete };
	   public ActionBar actionBar;
	   final static String TAG = "MainActivity";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = (ViewPager)findViewById(R.id.pager);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(actionBar.NAVIGATION_MODE_TABS);
        
        for (int tab_name: Tabs)
        {
        	actionBar.addTab(actionBar.newTab().setIcon(tab_name).setTabListener(this));
        }
        viewPager.setOnPageChangeListener(this);
        
        if(savedInstanceState != null ) {
        	actionBar.setSelectedNavigationItem(savedInstanceState.getInt("currentTab"));
        	mAdapter.onlyUpdatedSelected(viewPager.getCurrentItem());
        }
    }
   
    
	public static ArrayList<String> getUnsyncedList(ArrayList<String> retrievedContacts, ArrayList<String> phone_Contacts)
	{
	
			for (int j = 0; j<phone_Contacts.size(); j++)
			{
				if (retrievedContacts.contains(phone_Contacts.get(j)))
				{
					retrievedContacts.remove(phone_Contacts.get(j));
				}
			}
		return retrievedContacts;
	}
	
	public static ArrayList<String> getSyncedList(ArrayList<String> retrievedContacts, ArrayList<String> phone_Contacts)
	{
		ArrayList syncedContacts = phone_Contacts;
				for (int j = 0; j<retrievedContacts.size(); j++)
			{
				if (syncedContacts.contains(retrievedContacts.get(j)))
				{
					syncedContacts.remove(retrievedContacts.get(j));
				}
			}
		return syncedContacts;
	}
	
	public static int getDuplicates(ArrayList<String> retrievedContacts, ArrayList<String> displayedList)
	{
		int duplicateCount = 0;
		ArrayList<String >tempRetrievedContacts = new ArrayList<String>(retrievedContacts);
		ArrayList<String> tempDisplayedList = new ArrayList<String> (displayedList);
		
		
		for (int a=0; a<tempRetrievedContacts.size(); a++)
		{
			String temp = tempRetrievedContacts.get(a);
			temp = temp.toLowerCase();
			tempRetrievedContacts.set(a, temp);
			//Log.v(TAG, temp + " is the current retrievedContact which should be lower case");
		}
		
		for (int b=0; b<tempDisplayedList.size(); b++)
		{
			String temp = tempDisplayedList.get(b);
			temp = temp.toLowerCase();
			tempDisplayedList.set(b, temp);
		//	Log.v(TAG, temp + " is the current displayedContact which should be lower case");
		}
		
		for (int i = 0; i< tempDisplayedList.size(); i++)
		{
			String dupContact;
			dupContact = tempDisplayedList.get(i);
			if (tempRetrievedContacts.contains(dupContact))
			{
				//Log.e(TAG, "duplicate is " + displayedList.get(i));
				Log.v(TAG, "comparing " + dupContact + " to "+  tempRetrievedContacts.toString());
				duplicateCount++;
			}
		}
		return duplicateCount;
	}
	
	public static ArrayList<String> getPhoneContactNames(ContentResolver cr)
	{
		String[] proj = { RawContacts.DISPLAY_NAME_PRIMARY, RawContacts.DELETED} ;
		
		ArrayList<String> names = new ArrayList<String>();
		Cursor C = cr.query(RawContacts.CONTENT_URI, proj, null, null, null);
		while (C.moveToNext())
		{
			int deleted = C.getInt(C.getColumnIndex(RawContacts.DELETED));
			if (deleted != 1)
			{
				String currentContact = "";
				currentContact = C.getString(C.getColumnIndex(RawContacts.DISPLAY_NAME_PRIMARY));
				names.add(currentContact);

			}
			//Log.v(TAG, "deleted is " + deleted + " for contact " + C.getString(C.getColumnIndex(RawContacts.DISPLAY_NAME_PRIMARY));
		}
		C.close();
		return names;
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


	
    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		//get fragment first
		//viewPager.setCurrentItem(tab.getPosition());
		//actionBar.setSelectedNavigationItem(tab.getPosition());
		//mAdapter.notifyDataSetChanged();
	}


	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		viewPager.setCurrentItem(tab.getPosition());
		actionBar.setSelectedNavigationItem(tab.getPosition());
		mAdapter.notifyDataSetChanged();
	}


	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onPageScrollStateChanged(int position) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
	}


	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		Log.e(TAG, "hey, this is the current Fragment " + viewPager.getCurrentItem());
		mAdapter.onlyUpdatedSelected(viewPager.getCurrentItem());
		//Log.e(TAG, "ViewPager has " + viewPager.getCurrentItem());
		actionBar.setSelectedNavigationItem(position);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		Integer a = viewPager.getCurrentItem();
		Log.e(TAG, "a is " + a);
		if (a != null)
		{
			mAdapter.onlyUpdatedSelected(a);
		}
	}
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	   super.onSaveInstanceState(outState);
	   outState.putInt("currentTab", viewPager.getCurrentItem());
	}

    
}

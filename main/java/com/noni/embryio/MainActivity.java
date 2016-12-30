package com.noni.embryio;

import android.annotation.SuppressLint;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;

@SuppressLint("NewApi")
public class MainActivity extends AppCompatActivity implements TabListener, OnPageChangeListener, OnExecutionCompletionListener {
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private int[] Tabs = { R.drawable.ic_actionbar_tab_download, R.drawable.ic_actionbar_tab_upload, R.drawable.ic_actionbar_tab_delete };
    private android.support.v7.app.ActionBar actionBar;
    final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(android.app.ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        for (int tab_name : Tabs) {
            actionBar.addTab(actionBar.newTab().setIcon(tab_name).setTabListener(this));
        }
        viewPager.setOnPageChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
    public void onResume() {
        super.onResume();
        Integer a = viewPager.getCurrentItem();
        Log.e(TAG, "a is " + a);
        if (a != null) {
            mAdapter.onlyUpdatedSelected(a);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentTab", viewPager.getCurrentItem());
    }

    @Override
    public void onExecutionCompleted(String[] names) {

    }

    @Override
    public void onTabSelected(Tab tab, android.support.v4.app.FragmentTransaction ft) {
        viewPager.setCurrentItem(tab.getPosition());
        actionBar.setSelectedNavigationItem(tab.getPosition());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTabUnselected(Tab tab, android.support.v4.app.FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(Tab tab, android.support.v4.app.FragmentTransaction ft) {
    }
}

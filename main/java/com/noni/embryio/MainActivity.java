package com.noni.embryio;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

@SuppressLint("NewApi")
public class MainActivity extends AppCompatActivity implements TabListener, OnExecutionCompletionListener {
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = (ViewPager) findViewById(R.id.pager);
        TabLayout tabs = (TabLayout) findViewById(R.id.pager_tabs);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), getFragments());
        viewPager.setAdapter(mAdapter);
        tabs.setupWithViewPager(viewPager);
        initialiseTabs(tabs);
    }

    private ArrayList<Fragment> getFragments() {
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new FirstTab());
        fragments.add(new SecondTab());
        fragments.add(new ThirdTab());
        return fragments;
    }

    private void initialiseTabs(TabLayout tabs) {
        if (tabs.getTabAt(0) != null) {
            tabs.getTabAt(0).setText(getString(R.string.txt_title_tab1));
        }
        if (tabs.getTabAt(1) != null) {
            tabs.getTabAt(1).setText(getString(R.string.txt_title_tab2));
        }
        if (tabs.getTabAt(2) != null) {
            tabs.getTabAt(2).setText(getString(R.string.txt_title_tab3));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        int a = viewPager.getCurrentItem();
        mAdapter.onlyUpdatedSelected(a);
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
    public void onTabSelected(Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {

    }
}

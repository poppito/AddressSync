package com.noni.embryio;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity implements TabListener, OnPageChangeListener, OnExecutionCompletionListener {
	   private ViewPager viewPager;
	   private TabsPagerAdapter mAdapter;
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
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        CreateContactsContent createContactsContent = new CreateContactsContent(this.getApplicationContext(), this.getContentResolver(), ListOperations.getPhoneContactNames(this.getContentResolver()));
        createContactsContent.mListener = this;
        createContactsContent.execute();
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
        for (int tab_name: Tabs) {
            actionBar.addTab(actionBar.newTab().setIcon(tab_name).setTabListener(this));
        }
        viewPager.setOnPageChangeListener(this);
    }
}

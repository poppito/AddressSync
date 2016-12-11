package com.noni.embryio;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;


public class TabsPagerAdapter extends FragmentPagerAdapter {
    public FragmentManager fm;
    public final String TAG = "TabsPagerAdapter";
    private ArrayList<String> allPhoneContacts, duplicateContacts, syncedContacts;
    public Fragment FT = new FirstTab();
    public Fragment ST = new SecondTab();
    public Fragment TT = new ThirdTab();


    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void onlyUpdatedSelected(int position) {
        Fragment frag = getItem(position);
        Log.e(TAG, "fragment selected is " + frag.toString());
        if (frag instanceof UpdateableFragment) {
            ((UpdateableFragment) frag).update();
        }
    }

    @Override
    public Fragment getItem(int index) {
        // TODO Auto-generated method stub
        switch (index) {
            case 0:
                return FT;
            case 1:
                return ST;
            case 2:
                return TT;
        }
        return null;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return 3;
    }


}

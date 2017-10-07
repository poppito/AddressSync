package com.noni.embryio;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class TabsPagerAdapter extends FragmentPagerAdapter {
    public final String TAG = "TabsPagerAdapter";

    private ArrayList<Fragment> mFragments;


    public TabsPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        mFragments = (ArrayList<Fragment>) fragments;
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
        return mFragments.get(index);
    }

    @Override
    public int getCount() {
        if (mFragments != null) {
            return mFragments.size();
        } else {
            return 0;
        }
    }


}

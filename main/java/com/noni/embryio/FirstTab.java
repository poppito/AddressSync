package com.noni.embryio;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Collections;


public class FirstTab extends Fragment implements OnClickListener, UpdateableFragment, OnDropboxContactListReceivedListener {
    private ListView syncStatusList;
    private final String TAG = this.getClass().getSimpleName();
    private ArrayList<String> listViewContents = new ArrayList<>();
    private ArrayList<String> allPhoneContacts, unsyncedphoneContacts;
    private DropboxContactsList dbContactList;
    private TextView mEmptyPlaceHolder;
    private AdView mAdView;

    @Override
    public void update() {
        if (getActivity() != null) {
            dbContactList = new DropboxContactsList(getActivity());
            dbContactList.mListener = this;
            dbContactList.execute();
        }
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            update();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_first_tab, container, false);
        if (rootView instanceof  ViewGroup) {
            initialiseAds((ViewGroup) rootView);
            initialiseViews((ViewGroup)rootView);
        }
        dbContactList = new DropboxContactsList(getActivity());
        dbContactList.mListener = this;
        dbContactList.execute();
        return rootView;
    }

    private void initialiseAds(ViewGroup rootView) {
        mAdView = (AdView) rootView.findViewById(R.id.adView);
        MobileAds.initialize(getActivity().getApplicationContext(), getResources().getString(R.string.id_ad_first_tab));
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void initialiseViews(ViewGroup rootView) {
        syncStatusList = (ListView) rootView.findViewById(R.id.listcontacts1);
        Button mSelectAllButton = (Button) rootView.findViewById(R.id.selectall);
        Button mDeselectallButton = (Button) rootView.findViewById(R.id.deselectall);
        Button mDownloadContactsButton = (Button) rootView.findViewById(R.id.downloadContacts);
        mEmptyPlaceHolder = (TextView) rootView.findViewById(R.id.empty_placeholder_download_contact);
        mSelectAllButton.setOnClickListener(this);
        mDownloadContactsButton.setOnClickListener(this);
        mDeselectallButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.selectall:
                for (int i = 0; i < syncStatusList.getCount(); i++) {
                    syncStatusList.setItemChecked(i, true);
                }
                break;

            case R.id.deselectall:
                for (int i = 0; i < syncStatusList.getCount(); i++) {
                    syncStatusList.setItemChecked(i, false);
                }
                break;
            case R.id.downloadContacts:
                Log.v(TAG, "sync me button pressed!");
                runDownloadsForSelectedItems(syncStatusList);
        }

    }

    @Override
    public void dropboxContactListReceived(ArrayList<String> names) {
        listViewContents = names;
        allPhoneContacts = ListOperations.getPhoneContactNames(getActivity().getContentResolver());
        unsyncedphoneContacts = ListOperations.getUnsyncedList(listViewContents, allPhoneContacts);
        unsyncedphoneContacts = ListOperations.checkForNullSafety(unsyncedphoneContacts);
        if (unsyncedphoneContacts.size() == 0) {
            mEmptyPlaceHolder.setVisibility(View.VISIBLE);
            syncStatusList.setVisibility(View.GONE);
        } else {
            Collections.sort(unsyncedphoneContacts);
            ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, unsyncedphoneContacts);
            syncStatusList.setAdapter(mArrayAdapter);
            syncStatusList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            mEmptyPlaceHolder.setVisibility(View.GONE);
            syncStatusList.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            dbContactList = new DropboxContactsList(getActivity());
            dbContactList.mListener = this;
            dbContactList.execute();
        }
    }

    private void runDownloadsForSelectedItems(ListView listView) {
        ArrayList<String> selectedItemList = new ArrayList<>();
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        if (checked == null || checked.size() <= 0) {
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), getResources().getString(R.string.noContentToDownload), Snackbar.LENGTH_SHORT);
            snackbar.show();
            return;
        }

        for (int i = 0; i < checked.size(); i++) {
            int key = checked.keyAt(i);
            boolean value = checked.get(key);
            if (value) {
                Log.v(TAG, "adding " + listView.getItemAtPosition(key));
                selectedItemList.add((String) listView.getItemAtPosition(key));
            }
        }
        DownloadFile df = new DownloadFile(this, getActivity(), selectedItemList);
        df.execute();
    }
}


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
    private int totalContactCount;
    private Button selectall, deselectall, downloadContacts;
    public OnDropboxContactListReceivedListener mListener;
    private DropboxContactsList dbContactList;
    private ArrayAdapter mArrayAdapter;

    @Override
    public void update() {
        // TODO Auto-generated method stub
        if (getActivity() != null) {
            dbContactList = new DropboxContactsList(getActivity());
            dbContactList.mListener = this;
            dbContactList.execute();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_first_tab, container, false);
        MobileAds.initialize(getActivity().getApplicationContext(), getResources().getString(R.string.banner_ad_unit_id));
        AdView mAdView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        syncStatusList = (ListView) rootView.findViewById(R.id.listcontacts1);
        selectall = (Button) rootView.findViewById(R.id.selectall);
        deselectall = (Button) rootView.findViewById(R.id.deselectall);
        downloadContacts = (Button) rootView.findViewById(R.id.downloadContacts);
        selectall.setOnClickListener(this);
        downloadContacts.setOnClickListener(this);
        deselectall.setOnClickListener(this);
        dbContactList = new DropboxContactsList(getActivity());
        dbContactList.mListener = this;
        dbContactList.execute();
        return rootView;
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
        Collections.sort(unsyncedphoneContacts);
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, unsyncedphoneContacts);
        syncStatusList.setAdapter(mArrayAdapter);
        syncStatusList.setChoiceMode(syncStatusList.CHOICE_MODE_MULTIPLE);
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
        if (checked.size() <= 0) {
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


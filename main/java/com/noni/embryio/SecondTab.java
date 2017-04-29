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

public class SecondTab extends Fragment implements OnClickListener, UpdateableFragment, OnDropboxContactListReceivedListener, OnExecutionCompletionListener {

    private static String TAG = "SecondTab";
    private Button selectall, deselectall, syncme;
    private ListView listContacts;
    private ArrayList<String> selectedItemList = new ArrayList<>();
    private ArrayList<String> displayList = new ArrayList<>();
    private ArrayList<String> allPhoneContacts = new ArrayList<>();
    private ArrayList<String> syncedContacts = new ArrayList<>();
    private final static int TIMEOUT_MILLSEC = 1000;
    private DropboxContactsList dbContactList;
    private ArrayAdapter<String> mArrayAdapter;
    private TextView mPlaceholderView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_second_tab, container, false);
        MobileAds.initialize(getActivity().getApplicationContext(), getResources().getString(R.string.banner_ad_unit_id));
        AdView mAdView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        listContacts = (ListView) rootView.findViewById(R.id.listcontacts);
        Button selectall = (Button) rootView.findViewById(R.id.selectall);
        Button deselectall = (Button) rootView.findViewById(R.id.deselectall);
        Button backupContacts = (Button) rootView.findViewById(R.id.syncme);
        mPlaceholderView = (TextView) rootView.findViewById(R.id.empty_placeholder_send_contact);
        selectall.setOnClickListener(this);
        deselectall.setOnClickListener(this);
        backupContacts.setOnClickListener(this);
        dbContactList = new DropboxContactsList(getActivity());
        dbContactList.mListener = this;
        dbContactList.execute();
        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void update() {
        // TODO Auto-generated method stub
        dbContactList = new DropboxContactsList(getActivity());
        dbContactList.mListener = this;
        dbContactList.execute();
        selectedItemList.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        dbContactList = new DropboxContactsList(getActivity());
        dbContactList.mListener = this;
        dbContactList.execute();
        selectedItemList.clear();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case (R.id.deselectall):
                for (int i = 0; i < listContacts.getCount(); i++) {
                    listContacts.setItemChecked(i, false);
                }
                break;
            case (R.id.selectall):
                for (int i = 0; i < listContacts.getCount(); i++) {
                    listContacts.setItemChecked(i, true);
                }
                break;
            case (R.id.syncme):
                createContactsContentForSelected(selectedItemList, listContacts);
                break;
        }
    }

    @Override
    public void dropboxContactListReceived(ArrayList<String> names) {
        syncedContacts = names;
        allPhoneContacts = ListOperations.getPhoneContactNames(getActivity().getContentResolver()); //gets all contacts except ones marked for deletion
        displayList = ListOperations.getSyncedList(syncedContacts, allPhoneContacts); //compares synced contacts with unsynced ones to only show unsynced contacts
        if (displayList.size() > 0) {
            mArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, displayList);
            displayList = ListOperations.checkForNullSafety(displayList);
            Collections.sort(displayList);
            listContacts.setAdapter(mArrayAdapter);
            listContacts.setChoiceMode(listContacts.CHOICE_MODE_MULTIPLE);
            listContacts.setVisibility(View.VISIBLE);
            mPlaceholderView.setVisibility(View.GONE);
        } else {
            mPlaceholderView.setVisibility(View.VISIBLE);
            listContacts.setVisibility(View.GONE);
        }
    }


    private void createContactsContentForSelected(ArrayList<String> selectedItemList, ListView listContacts) {
        SparseBooleanArray mChecked = listContacts.getCheckedItemPositions();
        if (mChecked.size() <= 0) {
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), getResources().getString(R.string.noContentToSend), Snackbar.LENGTH_SHORT);
            snackbar.show();
            return;
        }
        for (int i = 0; i < mChecked.size(); i++) {
            int key = mChecked.keyAt(i);
            boolean value = mChecked.get(key);
            if (value) {
                selectedItemList.add((String) listContacts.getItemAtPosition(key));
                Log.v(TAG, String.valueOf(listContacts.getItemAtPosition(key)));
            }
        }
        CreateContactsContent createContactsContent = new CreateContactsContent(getActivity(), getActivity().getContentResolver(), selectedItemList);
        createContactsContent.mListener = this;
        createContactsContent.execute();
    }

    @Override
    public void onExecutionCompleted(String[] names) {
        ArrayList<String> namesList = new ArrayList<>();
        for (String name : names) {
            Log.v(TAG, name + " is name");
            namesList.add(name);
        }
        UploadFile uf = new UploadFile(this, getActivity(), namesList);
        uf.execute();
    }
}
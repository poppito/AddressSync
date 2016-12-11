package com.noni.embryio;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Collections;


public class FirstTab extends Fragment implements OnClickListener, UpdateableFragment, OnDropboxContactListReceivedListener {
    private ListView syncStatusList;
    private final String TAG = this.getClass().getSimpleName();
    private ArrayList<String> listViewContents = new ArrayList<>();
    private ArrayList<String> allPhoneContacts, unsyncedphoneContacts;
    private int totalContactCount;
    private Button uselectall, udeselectall, usyncme;
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

        View rootView = inflater.inflate(R.layout.get_sync_status, container, false);
        syncStatusList = (ListView) rootView.findViewById(R.id.listcontacts1);
        uselectall = (Button) rootView.findViewById(R.id.uselectall);
        udeselectall = (Button) rootView.findViewById(R.id.udeselectall);
        usyncme = (Button) rootView.findViewById(R.id.usyncme);
        uselectall.setOnClickListener(this);
        usyncme.setOnClickListener(this);
        udeselectall.setOnClickListener(this);
        dbContactList = new DropboxContactsList(getActivity());
        dbContactList.mListener = this;
        dbContactList.execute();
        //	Log.e(TAG, "contacts on phone " + allPhoneContacts.size() + " and contacts on server are " + listViewContents.size());
        return rootView;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.uselectall:
                for (int i = 0; i < syncStatusList.getCount(); i++) {
                    syncStatusList.setItemChecked(i, true);
                }
                break;

            case R.id.udeselectall:
                for (int i = 0; i < syncStatusList.getCount(); i++) {
                    syncStatusList.setItemChecked(i, false);
                }
                break;
            case R.id.usyncme:
                Log.v(TAG, "sync me button pressed!");
                ArrayList<String> selectedItemList = new ArrayList<>();
                SparseBooleanArray checked = syncStatusList.getCheckedItemPositions();
                for (int i = 0; i < checked.size(); i++) {
                    int key = checked.keyAt(i);
                    boolean value = checked.get(key);
                    if (value) {
                        Log.v(TAG, "adding " + syncStatusList.getItemAtPosition(key));
                        selectedItemList.add((String) syncStatusList.getItemAtPosition(key));
                    }
                }
                for (String name : selectedItemList) {
                    DownloadFile df = new DownloadFile(this, getActivity(), name, selectedItemList.size(), selectedItemList.indexOf(name) + 1);
                    df.execute();
                }
        }

    }

    @Override
    public void dropboxContactListReceived(ArrayList<String> names) {
        listViewContents = names;
        allPhoneContacts = ListOperations.getPhoneContactNames(getActivity().getContentResolver());
        totalContactCount = allPhoneContacts.size();
        unsyncedphoneContacts = ListOperations.getUnsyncedList(listViewContents, allPhoneContacts);
        Collections.sort(unsyncedphoneContacts);
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, unsyncedphoneContacts);
        syncStatusList.setAdapter(mArrayAdapter);
        syncStatusList.setChoiceMode(syncStatusList.CHOICE_MODE_MULTIPLE);
    }
}


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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class SecondTab extends Fragment implements OnClickListener, UpdateableFragment, OnDropboxContactListReceivedListener {

    private static String TAG = "SecondTab";
    private Button selectall, deselectall, syncme;
    private ListView listContacts;
    private ArrayList<String> selectedItemList = new ArrayList<String>();
    private ArrayList<String> displayList = new ArrayList<String>();
    private ArrayList<String> allPhoneContacts = new ArrayList<String>();
    private ArrayList<String> syncedContacts = new ArrayList<String>();
    private final static int TIMEOUT_MILLSEC = 1000;
    private DropboxContactsList dbContactList;
    private ArrayAdapter mArrayAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_get_contacts, container, false);
        listContacts = (ListView) rootView.findViewById(R.id.listcontacts);
        Button selectall = (Button) rootView.findViewById(R.id.selectall);
        Button deselectall = (Button) rootView.findViewById(R.id.deselectall);
        Button syncme = (Button) rootView.findViewById(R.id.syncme);
        selectall.setOnClickListener(this);
        deselectall.setOnClickListener(this);
        syncme.setOnClickListener(this);
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
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case (R.id.selectall):
                for (int i = 0; i < listContacts.getCount(); i++) {
                    listContacts.setItemChecked(i, true);
                }
                break;
            case (R.id.deselectall):
                for (int i = 0; i < listContacts.getCount(); i++) {
                    listContacts.setItemChecked(i, false);
                }
                break;

            case (R.id.syncme):
                SparseBooleanArray checked = listContacts.getCheckedItemPositions();
                for (int i = 0; i < checked.size(); i++) {
                    int key = checked.keyAt(i);
                    boolean value = checked.get(key);
                    if (value) {
                        if (selectedItemList.contains(listContacts.getItemAtPosition(key))) {
                            Log.v(TAG, listContacts.getItemAtPosition(key).toString() + " is a duplicate!!!!");

                        }
                        selectedItemList.add((String) listContacts.getItemAtPosition(key));
                    }
                }
                for (String name : selectedItemList) {
                    File file = new File(getActivity().getFilesDir().getPath() + "/" + name);
                    UploadFile uf = new UploadFile(getActivity(),name,file, selectedItemList.size(), selectedItemList.indexOf(name) + 1);
                    uf.execute();
                }
                selectedItemList.clear();
        }
    }

    @Override
    public void dropboxContactListReceived(ArrayList<String> names) {
        syncedContacts = names;
        allPhoneContacts = ListOperations.getPhoneContactNames(getActivity().getContentResolver()); //gets all contacts except ones marked for deletion
        displayList = ListOperations.getSyncedList(syncedContacts, allPhoneContacts); //compares synced contacts with unsynced ones to only show unsynced contacts
        mArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, displayList);
        Collections.sort(displayList);
        listContacts.setAdapter(mArrayAdapter);
        listContacts.setChoiceMode(listContacts.CHOICE_MODE_MULTIPLE);
    }
}
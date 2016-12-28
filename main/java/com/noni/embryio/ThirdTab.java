package com.noni.embryio;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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


public class ThirdTab extends Fragment implements OnClickListener, UpdateableFragment, OnDropboxContactListReceivedListener {

    public ListView embryioContacts;
    public Button selectall, deselectall, unsync;
    public ListView unsyncStatusList;
    public static final String TAG = "ThirdTab";
    public ArrayList<String> listViewContents = new ArrayList<String>();
    private ArrayList<String> removePhoneContacts = new ArrayList<String>();
    private DropboxContactsList dbContactList;
    private OnDropboxContactListReceivedListener mListener;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.third_tab, container, false);
        unsyncStatusList = (ListView) rootView.findViewById(R.id.embryiocontacts);
        selectall = (Button) rootView.findViewById(R.id.eselectall);
        selectall.setOnClickListener(this);
        deselectall = (Button) rootView.findViewById(R.id.edeselectall);
        deselectall.setOnClickListener(this);
        unsync = (Button) rootView.findViewById(R.id.unsyncme);
        unsync.setOnClickListener(this);
        dbContactList = new DropboxContactsList(getActivity());
        dbContactList.mListener = this;
        dbContactList.execute();
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        listViewContents.clear();
        removePhoneContacts.clear();
        dbContactList = new DropboxContactsList(getActivity());
        dbContactList.mListener = this;
        dbContactList.execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.eselectall:
                for (int i = 0; i < unsyncStatusList.getCount(); i++) {
                    unsyncStatusList.setItemChecked(i, true);
                }
                break;
            case (R.id.edeselectall):
                for (int i = 0; i < unsyncStatusList.getCount(); i++) {
                    unsyncStatusList.setItemChecked(i, false);
                }
                break;
            case (R.id.unsyncme):
                SparseBooleanArray checked = unsyncStatusList.getCheckedItemPositions();
                for (int i = 0; i < checked.size(); i++) {
                    int key = checked.keyAt(i);
                    boolean value = checked.get(key);
                    if (value) {
                        removePhoneContacts.add((String) unsyncStatusList.getItemAtPosition(key));
                    }
                }
                DeleteFile df = new DeleteFile(this, getActivity(), removePhoneContacts);
                df.execute();
                break;
        }

    }

    @Override
    public void update() {
        listViewContents.clear();
        removePhoneContacts.clear();
        dbContactList = new DropboxContactsList(getActivity());
        dbContactList.mListener = this;
        dbContactList.execute();
    }

    @Override
    public void dropboxContactListReceived(ArrayList<String> names) {
        listViewContents = names;
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String> (getActivity(), android.R.layout.simple_list_item_multiple_choice, listViewContents);
        Collections.sort(listViewContents);
        unsyncStatusList.setAdapter(mArrayAdapter);
        unsyncStatusList.setChoiceMode(unsyncStatusList.CHOICE_MODE_MULTIPLE);
    }
}
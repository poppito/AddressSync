package com.noni.embryio;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class FirstTab extends Fragment implements OnClickListener, UpdateableFragment, OnExecutionCompletionListener {
        private ListView syncStatusList;
        private final String TAG = this.getClass().getSimpleName();
        private ArrayList<String> listViewContents = new ArrayList<String>();
        private JSONArray values = new JSONArray();
        private ArrayList<String> allPhoneContacts, unsyncedphoneContacts;
        private Button uselectall, udeselectall, usyncme;

    @Override
    public void update() {
        // TODO Auto-generated method stub
        if (listViewContents != null) {
            listViewContents.clear();
        }
        if (unsyncedphoneContacts != null) {
            unsyncedphoneContacts.clear();
        }
        //populate the tab with data here;
        //This fragment gets all the synced contacts first.
        //Then it looks at all the phone contacts that are not synced.
        //If there are any contacts that are not synced it downloads them to the phone to be synced.
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
        allPhoneContacts = MainActivity.getPhoneContactNames(getActivity().getContentResolver()); //all contacts except ones marked for deletion
        Log.v(TAG, "size of all phone contacts is " + allPhoneContacts.size());
        CreateContactsContent createContactsContent = new CreateContactsContent(getActivity().getApplicationContext(), getActivity().getContentResolver(), allPhoneContacts);
        createContactsContent.mListener = this;
        createContactsContent.execute();
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
                        Log.v(TAG, "adding " + (String) syncStatusList.getItemAtPosition(key));
                        selectedItemList.add((String) syncStatusList.getItemAtPosition(key));
                    }
                }
                JSONArray sendArray = new JSONArray();
                try {
                    for (int j = 0; j < selectedItemList.size(); j++) {
                        JSONObject contactName = new JSONObject();
                        contactName.put("contactName", selectedItemList.get(j));
                        sendArray.put(contactName);
                    }
                    Log.v(TAG, "send array is " + sendArray.toString());
                    JSONObject sendObj = new JSONObject();
                    sendObj.put("contacts", sendArray.toString());
                    sendArray = null;
                    //Here we put all the contacts that are selected to be downloaded to the phone.
                    //then we can call insert unsynced contacts;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
    }

    @Override
    public void onExecutionCompleted(String[] names) {

        for (String name : names) {
            File file = new File(getActivity().getApplicationContext().getFilesDir().getPath() + "/" + name);
            UploadFile uf = new UploadFile(getActivity().getApplicationContext(),name,file);
            uf.execute();
        }
    }
}


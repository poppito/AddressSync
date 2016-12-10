package com.noni.embryio;

import android.app.ProgressDialog;
import android.content.Context;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ThirdTab extends Fragment implements OnClickListener, UpdateableFragment {

    public ListView embryioContacts;
    public Button selectall, deselectall, unsync;
    public ListView unsyncStatusList;
    public Button retrieveContacts, updatesAvailable;
    public static final String TAG = "ThirdTab";
    public static String testURL1 = Constants.SERVERURL + "getsyncedcontacts";
    public static String testURL2 = Constants.SERVERURL + "unsynccontacts";
    public int TIMEOUT_MILLSEC = 10000;
    public ArrayAdapter<String> mArrayAdapter = null;
    public ArrayList<String> listViewContents = new ArrayList<String>();
    public JSONObject syncStatus = null;
    public JSONArray values = null;
    private ArrayList<String> removePhoneContacts = new ArrayList<String>();
    public ProgressDialog mProgressDialog1;
    public ProgressDialog mProgressDialog2;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public JSONArray createJsonString(ArrayList<String> listofdetails) {

        JSONArray innerArray = new JSONArray();

        for (int i = 0; i < listofdetails.size(); i++) {

            try {

                JSONObject contact = new JSONObject();
                contact.put("contact_name", listofdetails.get(i));
                innerArray.put(contact);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                innerArray = null;
                e.printStackTrace();
            }

        }
        Log.e(TAG, "this is the JSON array" + innerArray.toString());
        return innerArray;
    }

    public JSONObject getObject(JSONArray jsonArray) {
        String usernameString = "haha";
        JSONObject outerJSONObject = new JSONObject();
        try {
            outerJSONObject.put("contacts", jsonArray);
            outerJSONObject.put("username", usernameString);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return outerJSONObject;
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Context context = getActivity().getApplicationContext();
        View rootView = inflater.inflate(R.layout.third_tab, container, false);
        unsyncStatusList = (ListView) rootView.findViewById(R.id.embryiocontacts);
        selectall = (Button) rootView.findViewById(R.id.eselectall);
        selectall.setOnClickListener(this);
        deselectall = (Button) rootView.findViewById(R.id.edeselectall);
        deselectall.setOnClickListener(this);
        unsync = (Button) rootView.findViewById(R.id.unsyncme);
        unsync.setOnClickListener(this);
        return rootView;

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
                    } else {

                    }

                }
                JSONArray jsonArray = createJsonString(removePhoneContacts);
                Log.e(TAG, "json array is " + jsonArray.toString());
                JSONObject sendObject = getObject(jsonArray);
                Log.e(TAG, "send object is " + sendObject.toString());
                //do something here.
                break;
        }

    }


    @Override
    public void update() {
        listViewContents.clear();
        removePhoneContacts.clear();
    }
}
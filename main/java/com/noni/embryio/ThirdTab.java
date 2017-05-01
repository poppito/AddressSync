package com.noni.embryio;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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


public class ThirdTab extends Fragment implements OnClickListener, UpdateableFragment, OnDropboxContactListReceivedListener {

    public ListView embryioContacts;
    public Button selectall, deselectall, unsync;
    public ListView unsyncStatusList;
    public static final String TAG = "ThirdTab";
    public ArrayList<String> listViewContents = new ArrayList<String>();
    private ArrayList<String> removePhoneContacts = new ArrayList<String>();
    private DropboxContactsList dbContactList;
    private OnDropboxContactListReceivedListener mListener;
    private TextView mPlaceholderText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_third_tab, container, false);
        MobileAds.initialize(getActivity().getApplicationContext(), getResources().getString(R.string.banner_ad_unit_id));
        AdView mAdView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        String spanString = getContext().getResources().getString(R.string.third_tab_delete_contacts_placeholder);
        unsyncStatusList = (ListView) rootView.findViewById(R.id.embryiocontacts);
        selectall = (Button) rootView.findViewById(R.id.eselectall);
        selectall.setOnClickListener(this);
        deselectall = (Button) rootView.findViewById(R.id.edeselectall);
        deselectall.setOnClickListener(this);
        unsync = (Button) rootView.findViewById(R.id.unsyncme);
        unsync.setOnClickListener(this);
        mPlaceholderText = (TextView) rootView.findViewById(R.id.empty_placeholder_delete_contact);
        dbContactList = new DropboxContactsList(getActivity());
        dbContactList.mListener = this;
        dbContactList.execute();
        initialiseClickableSpan();
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
                if (checked == null || checked.size() <= 0) {
                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), getResources().getString(R.string.nothingToDelete), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    break;
                }
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
        if (names.size() > 0) {
            ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, listViewContents);
            listViewContents = ListOperations.checkForNullSafety(listViewContents);
            Collections.sort(listViewContents);
            unsyncStatusList.setAdapter(mArrayAdapter);
            unsyncStatusList.setChoiceMode(unsyncStatusList.CHOICE_MODE_MULTIPLE);
            unsyncStatusList.setVisibility(View.VISIBLE);
            mPlaceholderText.setVisibility(View.GONE);
        } else {
            mPlaceholderText.setVisibility(View.VISIBLE);
            unsyncStatusList.setVisibility(View.GONE);
        }
    }


    private void initialiseClickableSpan() {
        String spanString = "start";
        SpannableString spannableString = new SpannableString(mPlaceholderText.getText().toString());

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (getActivity() instanceof MainActivity && ((MainActivity) getActivity()).getBar() != null) {
                    ((MainActivity) getActivity()).getBar().setSelectedNavigationItem(1);
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(getContext().getResources().getColor(R.color.textColor));
            }
        };

        int[] bounds = getIndexOfUrlKeywords(spanString, mPlaceholderText.getText().toString());
        if (bounds != null && bounds[0] > 0) {
            spannableString.setSpan(clickableSpan, bounds[0], bounds[1], SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        mPlaceholderText.setText(spannableString);
        mPlaceholderText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private int[] getIndexOfUrlKeywords(String keyword, String containingString) {
        int[] index = new int[2];
        index[0] = containingString.indexOf(keyword);
        index[1] = index[0] + keyword.length();
        return index;
    }
}
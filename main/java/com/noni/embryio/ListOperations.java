package com.noni.embryio;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;

public class ListOperations {


    private final String TAG = this.getClass().getSimpleName();
    private Context context;


    public static ArrayList<String> getUnsyncedList(ArrayList<String> retrievedContacts, ArrayList<String> phoneContacts) {

        for (int j = 0; j < phoneContacts.size(); j++) {
            if (retrievedContacts.contains(phoneContacts.get(j))) {
                retrievedContacts.remove(phoneContacts.get(j));
            }
        }
        return retrievedContacts;
    }


    public static ArrayList<String> getSyncedList(ArrayList<String> retrievedContacts, ArrayList<String> phone_Contacts) {
        ArrayList syncedContacts = phone_Contacts;
        for (int j = 0; j < retrievedContacts.size(); j++) {
            if (syncedContacts.contains(retrievedContacts.get(j))) {
                syncedContacts.remove(retrievedContacts.get(j));
            }
        }
        return syncedContacts;
    }

    public static int getDuplicates(ArrayList<String> retrievedContacts, ArrayList<String> displayedList) {
        int duplicateCount = 0;
        ArrayList<String> tempRetrievedContacts = new ArrayList<String>(retrievedContacts);
        ArrayList<String> tempDisplayedList = new ArrayList<String>(displayedList);


        for (int a = 0; a < tempRetrievedContacts.size(); a++) {
            String temp = tempRetrievedContacts.get(a);
            temp = temp.toLowerCase();
            tempRetrievedContacts.set(a, temp);
            //Log.v(TAG, temp + " is the current retrievedContact which should be lower case");
        }

        for (int b = 0; b < tempDisplayedList.size(); b++) {
            String temp = tempDisplayedList.get(b);
            temp = temp.toLowerCase();
            tempDisplayedList.set(b, temp);
            //	Log.v(TAG, temp + " is the current displayedContact which should be lower case");
        }

        for (int i = 0; i < tempDisplayedList.size(); i++) {
            String dupContact;
            dupContact = tempDisplayedList.get(i);
            if (tempRetrievedContacts.contains(dupContact)) {
                duplicateCount++;
            }
        }
        return duplicateCount;
    }


    public static ArrayList<String> getPhoneContactNames(ContentResolver cr) {
        String[] proj = {ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY, ContactsContract.RawContacts.DELETED};

        ArrayList<String> names = new ArrayList<>();
        Cursor C = cr.query(ContactsContract.RawContacts.CONTENT_URI, proj, null, null, null);
        while (C.moveToNext()) {
            int deleted = C.getInt(C.getColumnIndex(ContactsContract.RawContacts.DELETED));
            if (deleted != 1) {
                String currentContact = "";
                currentContact = C.getString(C.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
                names.add(currentContact);
            }
        }
        C.close();
        return names;
    }

    public static ArrayList<String> checkForNullSafety(ArrayList<String> in) {
        ArrayList<String> out = new ArrayList<>();
        for (int i=0; i<in.size(); i++) {
            if (in.get(i) != null) {
                out.add(in.get(i));
            }
        }
        return out;
    }

}
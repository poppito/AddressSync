package com.noni.embryio;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;

class ListOperations {

    static ArrayList<String> getUnsyncedList(ArrayList<String> retrievedContacts, ArrayList<String> phoneContacts) {

        for (int j = 0; j < phoneContacts.size(); j++) {
            if (retrievedContacts.contains(phoneContacts.get(j))) {
                retrievedContacts.remove(phoneContacts.get(j));
            }
        }
        return retrievedContacts;
    }


    static ArrayList<String> getSyncedList(ArrayList<String> retrievedContacts, ArrayList<String> phone_Contacts) {
        ArrayList<String> syncedContacts = new ArrayList<>(phone_Contacts);
        for (int j = 0; j < retrievedContacts.size(); j++) {
            if (syncedContacts.contains(retrievedContacts.get(j))) {
                syncedContacts.remove(retrievedContacts.get(j));
            }
        }
        return syncedContacts;
    }

    static int getDuplicates(ArrayList<String> retrievedContacts, ArrayList<String> displayedList) {
        int duplicateCount = 0;
        for (String retrievedContact : retrievedContacts ) {
            for (String displayedContact : displayedList) {
                if (retrievedContact.equalsIgnoreCase(displayedContact)) {
                    duplicateCount++;
                }
            }
        }
        return duplicateCount;
    }


    static ArrayList<String> getPhoneContactNames(ContentResolver cr) {
        String[] proj = {ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY, ContactsContract.RawContacts.DELETED};

        ArrayList<String> names = new ArrayList<>();
        Cursor C = cr.query(ContactsContract.RawContacts.CONTENT_URI, proj, null, null, null);
        if (C != null) {
            while (C.moveToNext()) {
                int deleted = C.getInt(C.getColumnIndex(ContactsContract.RawContacts.DELETED));
                if (deleted != 1) {
                    String currentContact = "";
                    currentContact = C.getString(C.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
                    names.add(currentContact);
                }
            }
            C.close();
        }
        return names;
    }

    static ArrayList<String> checkForNullSafety(ArrayList<String> in) {
        ArrayList<String> out = new ArrayList<>();
        for (int i=0; i<in.size(); i++) {
            if (in.get(i) != null) {
                out.add(in.get(i));
            }
        }
        return out;
    }

}
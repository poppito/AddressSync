package com.noni.embryio;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class CreateContactsContent extends AsyncTask<Void, String, String[]> {

    ArrayList<String> inputArrayList;
    private final String TAG = this.getClass().getSimpleName();
    public ContentResolver cr;
    public Context context;
    private int countingContacts = 1;
    private String name = null;
    public OnExecutionCompletionListener mListener = null;

    public CreateContactsContent(Context context, ContentResolver cr, ArrayList<String> inputArrayList) {

        this.context = context;
        this.cr = cr;
        this.inputArrayList = inputArrayList;

    }


    @Override
    protected String[] doInBackground(Void... params) {
        try {

            String accountName = null;
            String accountType = null;

            String[] proj = {ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY, ContactsContract.RawContacts.CONTACT_ID, ContactsContract.RawContacts.ACCOUNT_NAME, ContactsContract.RawContacts.ACCOUNT_TYPE, ContactsContract.RawContacts.DELETED};
            Cursor C = cr.query(ContactsContract.RawContacts.CONTENT_URI, proj, null, null, null);

            while (C.moveToNext()) {
                name = C.getString(C.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
                int deleted = C.getInt(C.getColumnIndex(ContactsContract.RawContacts.DELETED));
                if (deleted != 1) {
                    if ((C.getString(C.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME)) != null)
                            && (C.getString(C.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE)) != null)) {
                        accountName = C.getString(C.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
                        accountType = C.getString(C.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
                    }

                    JSONObject numObj = new JSONObject();
                    JSONObject emObj = new JSONObject();
                    JSONObject detailType = new JSONObject();
                    JSONObject IMobj = new JSONObject();
                    JSONObject orgObj = new JSONObject();
                    JSONObject addressObj = new JSONObject();
                    JSONObject websiteObj = new JSONObject();
                    JSONObject noteObj = new JSONObject();

                    if (inputArrayList.contains(name)) {
                        countingContacts++;
                        String contactID = C.getString(C.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
                        String[] filter = {contactID};

                        Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", filter, null);


                        while (phoneCursor.moveToNext()) {
                            int type = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                            String numType = "" + type;
                            String num = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            if ((num != null) && (numType != null)) {
                                numObj.put(num, numType);
                            }
                        }
                        detailType.put("phoneNumbers", numObj.toString());
                        Cursor emailCursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", filter, null);

                        while (emailCursor.moveToNext()) {
                            int type = emailCursor.getInt(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                            String emailType = "" + type;
                            String email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));

                            if ((email != null) && (emailType != null)) {

                                emObj.put(email, emailType);
                            }
                        }

                        detailType.put("emailAddresses", emObj.toString());

                        Cursor addressCursor = cr.query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                                null, ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + "=?", filter, null);

                        while (addressCursor.moveToNext()) {
                            String type = addressCursor.getString(addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
                            String address = addressCursor.getString(addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));

                            if ((address != null) && (type != null)) {

                                addressObj.put(address, type);
                            }
                        }


                        Cursor genericCursor = cr.query(ContactsContract.Data.CONTENT_URI,
                                null, ContactsContract.Data.CONTACT_ID + "=?", filter, null);


                        while (genericCursor.moveToNext()) {
                            String organisation = genericCursor.getString(genericCursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA1));
                            String title = genericCursor.getString(genericCursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA4));
                            String MIMETYPE_ORG = genericCursor.getString(genericCursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.MIMETYPE));


                            if ((organisation != null) && (title != null) && (MIMETYPE_ORG.equals("vnd.android.cursor.item/organization"))) {
                                orgObj.put(organisation, title);
                            }

                            String IMtype = genericCursor.getString(genericCursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
                            String IMvalue = genericCursor.getString(genericCursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA1));
                            String MIMETYPE_IM = genericCursor.getString(genericCursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.MIMETYPE));

                            if ((IMtype != null) && (IMvalue != null) && (MIMETYPE_IM.equals("vnd.android.cursor.item/im"))) {
                                IMobj.put(IMvalue, IMtype);
                            }

                            String websiteVal = genericCursor.getString(genericCursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL));
                            String MIMETYPE_URL = genericCursor.getString(genericCursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.MIMETYPE));

                            if ((websiteVal != null) && (MIMETYPE_URL.equals("vnd.android.cursor.item/website"))) {
                                websiteObj.put(websiteVal, "website");
                            }


                            String notesVal = genericCursor.getString(genericCursor.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
                            String MIMETYPE_NOTE = genericCursor.getString(genericCursor.getColumnIndex(ContactsContract.CommonDataKinds.Note.MIMETYPE));

                            if ((notesVal != null) && (MIMETYPE_NOTE.equals("vnd.android.cursor.item/note"))) {
                                noteObj.put(notesVal, "Note");
                            }
                        }
                        detailType.put("Address", addressObj.toString());
                        detailType.put("Note", noteObj.toString());
                        detailType.put("website", websiteObj.toString());
                        detailType.put("Organisation", orgObj.toString());
                        detailType.put("IM", IMobj.toString());
                        detailType.put("accountName", accountName);
                        detailType.put("accountType", accountType);
                        genericCursor.close();
                        addressCursor.close();
                        emailCursor.close();
                        phoneCursor.close();

                        String fileContent = detailType.toString();

                        FileOutputStream FOS = null;
                        try {
                            FOS = context.openFileOutput(name, context.MODE_PRIVATE);
                            try {
                                OutputStreamWriter OSW = new OutputStreamWriter(FOS);
                                OSW.write(fileContent);
                                OSW.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } catch (FileNotFoundException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return context.getFilesDir().list();
    }

    @Override
    protected void onPostExecute(String[] names) {
        super.onPostExecute(names);
        mListener.onExecutionCompleted(names);
    }
}
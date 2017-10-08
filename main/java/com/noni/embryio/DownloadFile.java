package com.noni.embryio;

import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class DownloadFile extends AsyncTask<Void, Integer, String> {
    private final String TAG = this.getClass().getSimpleName();
    private Context mContext;
    private ArrayList<String> fileNames;
    private ProgressDialog mProgressDialog;
    private int totalCount, currentCount;
    private UpdateableFragment frag;

    public DownloadFile(UpdateableFragment frag, Context c, ArrayList<String> fileNames) {
        this.mContext = c;
        SharedPreferences prefs = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        this.fileNames = fileNames;
        totalCount = fileNames.size();
        this.frag = frag;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            for (String name : fileNames) {
                currentCount = fileNames.indexOf(name) + 1;
                publishProgress(totalCount, currentCount);
                File file = new File(mContext.getFilesDir() + "/" + name);
                FileOutputStream mOutputStream = new FileOutputStream(file);
                insertUnsyncedContacts(file);
            }
        } catch (FileNotFoundException e) {
            //swallow
        }
        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        frag.update();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Please wait...");
        mProgressDialog.setMessage("Downloading contacts now.");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        mProgressDialog.setMessage("Downloading " + String.valueOf(values[1]) + " of " + String.valueOf(values[0]) + " contacts");
    }

    private void insertUnsyncedContacts(File file) {
        try {
            JSONObject obj = (JSONObject) new JSONTokener("").nextValue();
            String accountName;
            String accountType;

            ArrayList<ContentProviderOperation> Ops = new ArrayList<>();
            ContentProviderOperation.Builder op;
            Log.v(TAG, "names list check passed");

            if ((obj.getString("accountName").equals("")) || (obj.getString("accountType").equals(""))) {

                op = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .withValue(ContactsContract.RawContacts.DELETED, 0);


            } else {
                accountName = obj.getString("accountName");
                accountType = obj.getString("accountType");
                op = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, accountName)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountType)
                        .withValue(ContactsContract.RawContacts.DELETED, 0);
            }

            Ops.add(op.build());

            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, obj.getString("contactName"));

            Ops.add(op.build());


            if (!obj.getString("emailAddresses").equals("")) {
                String emailAddresses = obj.getString("emailAddresses");
                JSONObject emails = (JSONObject) new JSONTokener(emailAddresses).nextValue();
                Log.v(TAG, "email addresses are not null");
                Iterator iter = emails.keys();

                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    String value = emails.getString(key);

                    op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Email.TYPE, value)
                            .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, key);

                    Ops.add(op.build());
                }

            }

            if (!obj.getString("ims").equals("")) {
                String IMs = obj.getString("ims");
                JSONObject iM = (JSONObject) new JSONTokener(IMs).nextValue();
                Iterator iter = iM.keys();

                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    String value = iM.getString(key);

                    Log.v(TAG, "IM type " + key + " Protocol " + value);


                    op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Im.DATA1, key)
                            .withValue(ContactsContract.CommonDataKinds.Im.PROTOCOL, value);


                    Ops.add(op.build());
                }

            }


            if (!obj.getString("note").equals("")) {
                String notes = obj.getString("note");
                JSONObject Notes = (JSONObject) new JSONTokener(notes).nextValue();
                Iterator iter = Notes.keys();

                while (iter.hasNext()) {
                    String key = (String) iter.next();


                    Log.v(TAG, "Note is " + key);
                    op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Note.NOTE, key);


                    Ops.add(op.build());
                }

            }


            if (!obj.getString("address").equals("")) {
                String address = obj.getString("address");
                JSONObject Address = (JSONObject) new JSONTokener(address).nextValue();
                Iterator iter = Address.keys();

                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    String value = Address.getString(key);


                    Log.v(TAG, "Address " + key + " address type " + value);

                    op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, value)
                            .withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, key);


                    Ops.add(op.build());
                }
            }
            if (!obj.getString("website").equals("")) {
                String websiteString = obj.getString("website");
                JSONObject website = (JSONObject) new JSONTokener(websiteString).nextValue();
                Iterator iter = website.keys();

                while (iter.hasNext()) {
                    String key = (String) iter.next();

                    Log.v(TAG, "website is " + key);

                    op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Website.URL, key);


                    Ops.add(op.build());
                }
            }


            if (!obj.getString("organisation").equals("")) {
                String organisationString = obj.getString("organisation");
                JSONObject org = (JSONObject) new JSONTokener(organisationString).nextValue();
                Iterator iter = org.keys();

                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    String value = org.getString(key);

                    Log.v(TAG, "Org is " + key + " Title  " + value);


                    op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, value)
                            .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, key);


                    Ops.add(op.build());
                }
            }

            if (!obj.getString("nickname").equals("")) {
                String nicknameString = obj.getString("nickname");
                JSONObject nicknames = (JSONObject) new JSONTokener(nicknameString).nextValue();
                Iterator iter = nicknames.keys();

                while (iter.hasNext()) {
                    String key = (String) iter.next();

                    Log.v(TAG, "nickname is " + key);

                    op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Nickname.NAME, key);

                    Ops.add(op.build());
                }
            }

            if (!obj.getString("phoneNumbers").equals("")) {
                String phoneNumberString = obj.getString("phoneNumbers");
                JSONObject phoneNumbers = (JSONObject) new JSONTokener(phoneNumberString).nextValue();
                Iterator iter = phoneNumbers.keys();
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    String value = phoneNumbers.getString(key);
                    op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, value)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, key);
                    op.withYieldAllowed(true);
                    Ops.add(op.build());
                }
            }

            try {
                if (Ops != null) {
                    ContentProviderResult[] result = mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, Ops);
                    Log.v(TAG, "Content provider result is " + result[0]);
                }
            } catch (Exception e) {
                Log.v(TAG, "contact add didn't work! ");
                e.printStackTrace();

            }

        } catch (JSONException e) {
            Log.v(TAG, "json exception thrown" + e.getMessage());
        }
    }

}

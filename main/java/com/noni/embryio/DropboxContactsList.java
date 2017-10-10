package com.noni.embryio;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

import java.util.ArrayList;

public class DropboxContactsList extends AsyncTask<Void, Void, ArrayList<String>> {
    private Context context;
    private ArrayList<String> fileNames;
    public OnDropboxContactListReceivedListener mListener = null;
    private ProgressDialog mProgressDialog;
    private SharedPreferences mPrefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    private DbxRequestConfig mConfig = new DbxRequestConfig(BuildConfig.CLIENT_ID);

    public DropboxContactsList(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Please wait...");
        mProgressDialog.setMessage("Loading your contact list");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    protected ArrayList<String> doInBackground(Void... params) {
        String token = mPrefs.getString(LogonActivity.ACCESS_TOKEN, "");
        DbxClientV2 client = new DbxClientV2(mConfig, token);
        fileNames = new ArrayList<>();
        return fileNames;
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        mListener.dropboxContactListReceived(strings);
        if (mProgressDialog != null) mProgressDialog.dismiss();
    }
}
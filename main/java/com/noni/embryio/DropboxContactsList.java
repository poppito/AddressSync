package com.noni.embryio;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;


import java.util.ArrayList;

public class DropboxContactsList extends AsyncTask<Void, Void, ArrayList<String>> {
    private String response;
    private final String TAG = this.getClass().getSimpleName();
    private Context context;
    private AndroidAuthSession newSession;
    private DropboxAPI emboDBApi;
    private ArrayList<String> fileNames;
    public OnDropboxContactListReceivedListener mListener = null;
    private ProgressDialog mProgressDialog;

    public DropboxContactsList(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        newSession = new AndroidAuthSession(Constants.KEY_PAIR, prefs.getString("emboDBAccessToken", ""));
        emboDBApi = new DropboxAPI<>(newSession);
        this.context = context;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Loading...");
        mProgressDialog.setMessage("Just a second.");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    protected ArrayList<String> doInBackground(Void... params) {
        fileNames = new ArrayList<>();
        try {
            DropboxAPI.Account account = emboDBApi.accountInfo();
            DropboxAPI.Entry entry = emboDBApi.metadata("/", 10000, null, true, null);
            for (DropboxAPI.Entry ent : entry.contents) {
                String name = ent.path;
                name = name.substring(1, name.length());
                fileNames.add(name);
            }
        } catch (DropboxException e) {
            Log.v(TAG, e.getMessage());
        }
        return fileNames;
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        mListener.dropboxContactListReceived(strings);
        if (mProgressDialog != null) mProgressDialog.dismiss();
    }
}
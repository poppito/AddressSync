package com.noni.embryio;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.ArrayList;

public class DropboxContactsList extends AsyncTask<Void, Void, ArrayList<String>> {
    private Context context;
    private ArrayList<String> fileNames;
    public OnDropboxContactListReceivedListener mListener = null;
    private ProgressDialog mProgressDialog;

    public DropboxContactsList(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
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
        fileNames = new ArrayList<>();
        return fileNames;
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        mListener.dropboxContactListReceived(strings);
        if (mProgressDialog != null) mProgressDialog.dismiss();
    }
}
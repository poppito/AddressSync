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


public class DeleteFile extends AsyncTask<Void, Integer, Void> {


    private final String TAG = this.getClass().getSimpleName();
    private Context context;
    AndroidAuthSession newSession;
    private DropboxAPI emboDBApi;
    private int totalCount;
    private int currentCount;
    private ProgressDialog mProgressDialog;
    private UpdateableFragment frag;
    private ArrayList<String> fileNames;


    public DeleteFile(UpdateableFragment frag, Context c, ArrayList<String> fileNames) {
        this.context = c;
        SharedPreferences prefs = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        newSession = new AndroidAuthSession(Constants.KEY_PAIR, prefs.getString("emboDBAccessToken", ""));
        emboDBApi = new DropboxAPI<>(newSession);
        this.fileNames = fileNames;
        this.frag = frag;
        totalCount = fileNames.size();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            for (String name : fileNames) {
                currentCount = fileNames.indexOf(name);
                publishProgress(totalCount, currentCount);
                emboDBApi.delete(name);
            }
        } catch (DropboxException e) {
            Log.v(TAG, "Dropbox exception thrown");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Please wait...");
        mProgressDialog.setMessage("Deleting contacts from Dropbox");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        frag.update();
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        String progressUpdate = "Deleting " + String.valueOf(values[1]) + " of " + String.valueOf(values[0]) + " contacts";
        mProgressDialog.setMessage(progressUpdate);
    }
}

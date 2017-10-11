package com.noni.embryio;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.ArrayList;


public class DeleteFile extends AsyncTask<Void, Integer, Void> {

    private Context context;
    private int totalCount;
    private int currentCount;
    private ProgressDialog mProgressDialog;
    private UpdateableFragment frag;
    private ArrayList<String> fileNames;


    public DeleteFile(UpdateableFragment frag, Context c, ArrayList<String> fileNames) {
        this.context = c;
        SharedPreferences prefs = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);
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
                //delete stuff
            }
        } catch (Exception e) {
            //swallow
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

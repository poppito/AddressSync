package com.noni.embryio;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;


public class UploadFile extends AsyncTask<Void, Integer, String> {

    private final String TAG = this.getClass().getSimpleName();
    private Context context;
    private ArrayList<String> fileNames;
    private int totalCount;
    private int currentCount;
    private ProgressDialog mProgressDialog;
    private UpdateableFragment frag;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Send contacts for sync");
        mProgressDialog.setMessage("Just a second.");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }


    public UploadFile(UpdateableFragment frag, Context c, ArrayList<String> fileNames) {
        this.context = c;
        SharedPreferences prefs = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        this.fileNames = fileNames;
        this.frag = frag;
        totalCount = fileNames.size();
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            for (String name : fileNames) {
                currentCount = fileNames.indexOf(name) + 1;
                publishProgress(totalCount, currentCount);
                File file = new File(context.getFilesDir() + "/" + name);
                FileInputStream inputStream = new FileInputStream(file);
            }

        } catch (FileNotFoundException e) {
            //swallow
        }
        return "";
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        String progressUpdate = "Sending " + String.valueOf(values[1]) + " of " + String.valueOf(values[0]) + " contacts";
        mProgressDialog.setMessage(progressUpdate);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        frag.update();
    }
}

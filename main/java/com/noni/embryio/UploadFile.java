package com.noni.embryio;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;


public class UploadFile extends AsyncTask<Void, Integer, String> {

    private final String TAG = this.getClass().getSimpleName();
    private Context context;
    AndroidAuthSession newSession;
    private DropboxAPI emboDBApi;
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
        newSession = new AndroidAuthSession(Constants.KEY_PAIR, prefs.getString("emboDBAccessToken", ""));
        emboDBApi = new DropboxAPI<>(newSession);
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
                DropboxAPI.Entry response = emboDBApi.putFile(name, inputStream, file.length(), null, null);
                if (response != null) {
                    file.delete();
                }
                Log.v(TAG, "The uploaded file's revision number is " + response.rev.toString());
            }

        } catch (FileNotFoundException e) {
            Log.v(TAG, "file not found exception thrown");
            e.printStackTrace();
        } catch (DropboxException e) {
            Log.v(TAG, "Dropbox exception thrown");
            e.printStackTrace();
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

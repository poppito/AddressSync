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


public class UploadFile extends AsyncTask<Void, Integer, String> {

    private final String TAG = this.getClass().getSimpleName();
    public Context context;
    AndroidAuthSession newSession;
    private DropboxAPI emboDBApi;
    private String fileName;
    private File file;
    private int totalCount;
    private int currentCount;
    private ProgressDialog mProgressDialog;

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


    public UploadFile(Context c, String fileName, File file, int totalCount, int currentCount) {
        this.context = c;
        SharedPreferences prefs = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        newSession = new AndroidAuthSession(Constants.KEY_PAIR, prefs.getString("emboDBAccessToken", ""));
        emboDBApi = new DropboxAPI<>(newSession);
        this.fileName = fileName;
        this.file = file;
        this.totalCount = totalCount;
        this.currentCount = currentCount;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            publishProgress(totalCount, currentCount);
            FileInputStream inputStream = new FileInputStream(file);
            DropboxAPI.Entry response = emboDBApi.putFile(fileName, inputStream, file.length(), null, null);
            Log.v(TAG, "The uploaded file's revision number is " + response.rev.toString());
            publishProgress(totalCount, currentCount);
            return response.rev.toString();

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
    }
}

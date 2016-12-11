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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class DownloadFile extends AsyncTask<Void, Integer, String> {
    private final String TAG = this.getClass().getSimpleName();
    public Context context;
    AndroidAuthSession newSession;
    private DropboxAPI emboDBApi;
    private String fileName;
    private DropboxAPI.DropboxFileInfo mFileInfo;
    private ProgressDialog mProgressDialog;
    private int totalCount, currentCount;

    public DownloadFile(Context c, String fileName, int totalCount, int currentCount) {
        this.context = c;
        SharedPreferences prefs = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        newSession = new AndroidAuthSession(Constants.KEY_PAIR, prefs.getString("emboDBAccessToken", ""));
        emboDBApi = new DropboxAPI<>(newSession);
        this.fileName = fileName;
        this.totalCount = totalCount;
        this.currentCount = currentCount;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            publishProgress(totalCount, currentCount);
            File file = new File(context.getFilesDir() + "/" + fileName);
            FileOutputStream mOutputStream = new FileOutputStream(file);
            mFileInfo = emboDBApi.getFile(fileName, null, mOutputStream, null);
        } catch (DropboxException e) {
            Log.v(TAG, e.getMessage());
        } catch (FileNotFoundException e) {
            Log.v(TAG, e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

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

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        mProgressDialog.setMessage("Downloading " + String.valueOf(values[1]) + " of " + String.valueOf(values[0]) + " contacts");
    }
}

package com.noni.embryio;

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


public class UploadFile extends AsyncTask<Void, Void, String> {

    private final String TAG = this.getClass().getSimpleName();
    public Context context;
    AndroidAuthSession newSession;
    private DropboxAPI emboDBApi;
    private String fileName;
    private File file;

    public UploadFile(Context c, String fileName, File file) {
        this.context = c;
        SharedPreferences prefs = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        newSession = new AndroidAuthSession(Constants.KEY_PAIR, prefs.getString("emboDBAccessToken", ""));
        emboDBApi = new DropboxAPI<>(newSession);
        this.fileName = fileName;
        this.file = file;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            DropboxAPI.Entry response = emboDBApi.putFile(fileName, inputStream, file.length(), null, null);
            Log.v(TAG, "The uploaded file's revision number is " + response.rev.toString());
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
}

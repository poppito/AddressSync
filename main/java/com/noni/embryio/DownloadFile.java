package com.noni.embryio;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;


public class DownloadFile extends AsyncTask<String, Void, String> {
    private final String TAG = this.getClass().getSimpleName();
    public Context context;
    AndroidAuthSession newSession;
    private DropboxAPI emboDBApi;
    private String fileName;

    public DownloadFile(Context c, String fileName) {
        this.context = c;
        SharedPreferences prefs = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        newSession = new AndroidAuthSession(Constants.KEY_PAIR, prefs.getString("emboDBAccessToken", ""));
        emboDBApi = new DropboxAPI<>(newSession);
        this.fileName = fileName;
    }


    @Override
    protected String doInBackground(String... params) {
        String responseRead = "";
        String filename = params[0];
      /*  try {
            //file downloads go here.
        } catch (FileNotFoundException e) {
            Log.v(TAG, "file not found exception thrown");
            e.printStackTrace();
        } catch (DropboxException e) {
            Log.v(TAG, "Dropbox exception thrown");
            e.printStackTrace();
        }
*/
        return responseRead;
    }

    @Override
    protected void onPostExecute(String response) {

        if (!response.equals("")) {
            Log.v(TAG, "response from file upload: " + response.toString());
        } else {
            Log.v(TAG, "file upload failed");
        }
    }


}

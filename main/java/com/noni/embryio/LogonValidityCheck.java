package com.noni.embryio;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

public class LogonValidityCheck extends AsyncTask<Void, Void, String> {
    private DropboxAPI<AndroidAuthSession> dbApi;
    private String response;
    private final String TAG = this.getClass().getSimpleName();
    private Context context;

    public LogonValidityCheck(DropboxAPI<AndroidAuthSession> dbApi, Context context) {
        this.dbApi = dbApi;
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {

        try {
            DropboxAPI.Account account = dbApi.accountInfo();
            return account.displayName;
        } catch (DropboxException e) {
            //swallow
        }
        return "";
    }
}

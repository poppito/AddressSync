package com.noni.embryio;

import android.content.Context;
import android.os.AsyncTask;

public class LogonValidityCheck extends AsyncTask<Void, Void, String> {
    private String response;
    private final String TAG = this.getClass().getSimpleName();
    private Context context;

    public LogonValidityCheck(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        return "";
    }
}

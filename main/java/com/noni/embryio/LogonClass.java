package com.noni.embryio;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;


public class LogonClass extends FragmentActivity implements OnClickListener {

    private SharedPreferences prefs;
    private String TAG = "LogonClass.java";
    private Button logonButton;
    private TextView logonstatus;
    private final String LOGON_URL = Constants.SERVERURL + "login_mobile";
    int TIMEOUT_MILLSEC = 10000;
    public static MyHttpClient Client = new MyHttpClient(null);
    private ProgressDialog mProgressDialog;
    private ActionBar actionBar;
    private final static AppKeyPair emboAppKeys = new AppKeyPair(Constants.API_KEY, Constants.API_KEY);
    private DropboxAPI<AndroidAuthSession> emboDBApi;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidAuthSession newSession = new AndroidAuthSession(emboAppKeys);
        emboDBApi = new DropboxAPI<>(newSession);
        actionBar = getActionBar();
        actionBar.hide();
        setContentView(R.layout.logonview);
        logonButton = (Button) findViewById(R.id.logonbutton);
        logonButton.setOnClickListener(this);
        prefs = this.getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString("emboDBAccessToken", "");
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "On Destroy called");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "On Pause called");
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (emboDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                emboDBApi.getSession().finishAuthentication();

                String accessToken = emboDBApi.getSession().getOAuth2AccessToken();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("emboDBAccessToken", accessToken);
                editor.apply();
                Log.v("SomeTag", prefs.getString())
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        switch (view.getId()) {
            case R.id.logonbutton: {
                Log.e(TAG, "logon button pressed");
                if (!checkIfDBUnlinked()) {
                    Log.v(TAG, "Db unlinked eh");
                    startSessionWhenUnlinked(this);
                } else {
                    //retrieve token and build session
                    Log.v(TAG, "db still linked");
                    AndroidAuthSession newSession = new AndroidAuthSession(emboAppKeys, prefs.getString("emboDBAccessToken", ""));
                    //build API
                    emboDBApi = new DropboxAPI<>(newSession);

                    FileOperations fp = new FileOperations(getApplicationContext(), emboDBApi);
                    fp.execute("test");
                }
                break;
            }
        }
    }

    public boolean tokenExists() {
        Log.v(TAG, "checking that token exists");

        if (prefs.getString("emboDBAccessToken", "").equals("")) {
            Log.v(TAG, "Nah token don't exist bruv");
            return false;
        } else {
            Log.v(TAG, "Yah token exists");
            return true;
        }
    }

    public void startSessionWhenUnlinked(Context context) {
        emboDBApi.getSession().startOAuth2Authentication(context);
    }

    public boolean checkIfDBUnlinked() {
        AndroidAuthSession newSession = new AndroidAuthSession(emboAppKeys, prefs.getString("emboDBAccessToken", ""));
        emboDBApi = new DropboxAPI<AndroidAuthSession>(newSession);

        if (!tokenExists()) {
            return false;
        }
        return verifyAuth();
    }

    private boolean verifyAuth() {
        VerifyAuthing va = new VerifyAuthing(emboDBApi);
        if (va.execute().equals("")) {
            return false;
        } else {
            return true;
        }
    }

    public class VerifyAuthing extends AsyncTask<Void, Void, String> {

        private DropboxAPI<AndroidAuthSession> dbApi;

        public VerifyAuthing(DropboxAPI<AndroidAuthSession> dbApi) {
            this.dbApi = dbApi;
        }


        @Override
        protected String doInBackground(Void... params) {
            String response = "";
            try {
                DropboxAPI.Account account = dbApi.accountInfo();
                response = account.displayName;
            } catch (DropboxException e) {
                e.printStackTrace();
            }

            return response;
        }
    }
}





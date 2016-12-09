package com.noni.embryio;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;


public class LogonClass extends FragmentActivity implements OnClickListener {

    private SharedPreferences prefs;
    private final String TAG = this.getClass().getSimpleName();
    private Button logonButton;
    private ActionBar actionBar;
    private DropboxAPI<AndroidAuthSession> emboDBApi;
    private AndroidAuthSession newSession = new AndroidAuthSession(Constants.KEY_PAIR);
    public static MyHttpClient Client = new MyHttpClient(null);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        emboDBApi = new DropboxAPI<>(newSession);
        actionBar = getActionBar();
        actionBar.hide();
        setContentView(R.layout.logonview);
        logonButton = (Button) findViewById(R.id.logonbutton);
        logonButton.setOnClickListener(this);
        prefs = this.getSharedPreferences("prefs", MODE_PRIVATE);
        if (isDBLinked() == false) {
            startSessionWhenUnlinked(this);
        } else {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (emboDBApi.getSession().authenticationSuccessful()) {
            try {
                if (!tokenExists()) {
                    emboDBApi.getSession().finishAuthentication();
                    String accessToken = emboDBApi.getSession().getOAuth2AccessToken();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("emboDBAccessToken", accessToken);
                    editor.apply();
                    Intent i = new Intent(this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            } catch (IllegalStateException e) {
                Log.v(TAG, "Error authenticating", e);
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
                Log.v(TAG, "logon button pressed");
                if (!isDBLinked()) {
                    Log.v(TAG, "Db unlinked eh");
                    startSessionWhenUnlinked(this);
                } else {
                    //retrieve token and build session
                    Log.v(TAG, "db still linked");
                }
                break;
            }
        }
    }

    public boolean tokenExists() {
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

    public boolean isDBLinked() {
        AndroidAuthSession newSession = new AndroidAuthSession(Constants.KEY_PAIR, prefs.getString("emboDBAccessToken", ""));
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
            Log.v(TAG, response.toString() + " is response. We are ok.");
            return response;
        }
    }
}





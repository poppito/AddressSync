package com.noni.embryio;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;


public class LogonClass extends FragmentActivity implements OnClickListener {

    private SharedPreferences prefs;
    private final String TAG = this.getClass().getSimpleName();
    private DropboxAPI<AndroidAuthSession> emboDBApi;
    private AndroidAuthSession newSession = new AndroidAuthSession(Constants.KEY_PAIR);
    private Boolean buttonPressed = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        emboDBApi = new DropboxAPI<>(newSession);
        ActionBar actionBar = getActionBar();
        actionBar.hide();
        setContentView(R.layout.logonview);
        Button logonButton = (Button) findViewById(R.id.logonbutton);
        logonButton.setOnClickListener(this);
        prefs = this.getSharedPreferences("prefs", MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onresume called");
        if (buttonPressed) {
            buttonPressed = false;
            if (emboDBApi.getSession().authenticationSuccessful()) {
                try {
                    emboDBApi.getSession().finishAuthentication();
                    String accessToken = emboDBApi.getSession().getOAuth2AccessToken();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("emboDBAccessToken", accessToken);
                    editor.apply();
                    Intent i = new Intent(this, MainActivity.class);
                    startActivity(i);
                    finish();
                } catch (IllegalStateException e) {
                    Log.v(TAG, "Error authenticating", e);
                }
            }
            if (!tokenExists()) {
                startSessionWhenUnlinked(this);
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
                buttonPressed = true;
                if (!isDBLinked()) {
                    Log.v(TAG, "Db unlinked eh");
                    startSessionWhenUnlinked(this);
                } else {
                    //retrieve token and build session
                    Intent i = new Intent(this, MainActivity.class);
                    startActivity(i);
                    finish();
                    Log.v(TAG, "db still linked");
                }
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
        emboDBApi = new DropboxAPI<AndroidAuthSession>(newSession);
        emboDBApi.getSession().startOAuth2Authentication(context);
    }

    public boolean isDBLinked() {
        String response;
        if (!tokenExists()) {
            return false;
        }
        AndroidAuthSession newSession = new AndroidAuthSession(Constants.KEY_PAIR, prefs.getString("emboDBAccessToken", ""));
        emboDBApi = new DropboxAPI<AndroidAuthSession>(newSession);
        LogonValidityCheck va = new LogonValidityCheck(emboDBApi, this);
        try {
            response = va.execute().get();
            if (!response.equals("") && (response != null)) {
                return true;
            }
        }
        catch (Exception e) {
            Log.v(TAG, e.getMessage());
        }
        return false;
    }
}





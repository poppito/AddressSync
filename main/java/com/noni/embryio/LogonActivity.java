package com.noni.embryio;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;


public class LogonActivity extends AppCompatActivity implements OnClickListener {

    private SharedPreferences prefs;
    private final String TAG = this.getClass().getSimpleName();
    private DropboxAPI<AndroidAuthSession> emboDBApi;
    private AndroidAuthSession newSession = new AndroidAuthSession(Constants.KEY_PAIR);
    private Boolean buttonPressed = false;
    private ProgressDialog mProgressDialog;


    protected void onCreate(Bundle savedInstanceState) {
        String termsURL = Constants.TERMS_URL;
        String whyDropboxURL = Constants.WHY_DROPBOX_URL;
        final String licenseSpan = "License Terms";
        super.onCreate(savedInstanceState);
        emboDBApi = new DropboxAPI<>(newSession);
        setContentView(R.layout.logonview);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Button logonButton = (Button) findViewById(R.id.logonbutton);
        TextView whyDropboxView = (TextView) findViewById(R.id.explainWhy);
        initialiseClickableSpan(this, whyDropboxView, whyDropboxURL, getResources().getString(R.string.whyDropbox));
        TextView licenseAgreementView = (TextView) findViewById(R.id.licenseAgreement);
        initialiseClickableSpan(this, licenseAgreementView, termsURL, licenseSpan);
        logonButton.setOnClickListener(this);
        prefs = this.getSharedPreferences("prefs", MODE_PRIVATE);
    }

    private void initialiseClickableSpan(final Context context, TextView tv, final String url, String spannedString) {

        SpannableString span = new SpannableString(tv.getText().toString());

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(browserIntent);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };


        int[] bounds = getIndexOfUrlKeywords(spannedString, tv.getText().toString());
        if (bounds != null && bounds[0] >= 0) {
            span.setSpan(clickableSpan, bounds[0], bounds[1], SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tv.setText(span);
        tv.setMovementMethod(LinkMovementMethod.getInstance());

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
            } else {
                dismissLogonLoader(mProgressDialog);
                Snackbar sb = Snackbar.make(findViewById(android.R.id.content), "Logon failed", Snackbar.LENGTH_SHORT);
                sb.show();
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
        switch (view.getId()) {
            case R.id.logonbutton: {
                Log.v(TAG, "logon button pressed");
                buttonPressed = true;
                mProgressDialog = showLogonLoader();
                mProgressDialog.show();
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
        emboDBApi = new DropboxAPI<>(newSession);
        emboDBApi.getSession().startOAuth2Authentication(context);
    }

    public boolean isDBLinked() {
        String response;
        if (!tokenExists()) {
            return false;
        }
        AndroidAuthSession newSession = new AndroidAuthSession(Constants.KEY_PAIR, prefs.getString("emboDBAccessToken", ""));
        emboDBApi = new DropboxAPI<>(newSession);
        LogonValidityCheck va = new LogonValidityCheck(emboDBApi, this);
        try {
            response = va.execute().get();
            if ((response != null) && !response.equals("")) {
                return true;
            }
        }
        catch (Exception e) {
            Log.v(TAG, e.getMessage());
        }
        return false;
    }


    private int[] getIndexOfUrlKeywords(String keyword, String containingString) {
        int[] index = new int[2];
        index[0] = containingString.indexOf(keyword);
        index[1] = index[0] + keyword.length();
        return index;
    }

    public ProgressDialog showLogonLoader() {
        ProgressDialog mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Please wait...");
        mProgressDialog.setMessage("Logging you in..");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        return mProgressDialog;
    }

    public void dismissLogonLoader(ProgressDialog progressDialog) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}





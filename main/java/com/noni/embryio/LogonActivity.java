package com.noni.embryio;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;


public class LogonActivity extends AppCompatActivity implements OnClickListener {

    private SharedPreferences mPrefs;
    private final String TAG = this.getClass().getSimpleName();
    private Boolean buttonPressed = false;
    private ProgressDialog mProgressDialog;

    public static final String EXTRA_AUTH_URL = "authurl";
    public static final int REQUEST_AUTH = 10001;
    private String mAuthCode;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logonview);

        //initialise sharedPres
        mPrefs = getSharedPreferences("mPrefs", MODE_PRIVATE);

        //initialise views
        initialiseViews();

        //initialise ads
        initialiseAds();
    }

    private void initialiseClickableSpan(TextView tv, final String url, String spannedString) {
        final Context context = this;
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

    private void initialiseAds() {
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.id_ad_logon));
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }


    private String getAuthUrl() {
        DbxAppInfo appInfo = new DbxAppInfo(BuildConfig.API_KEY, BuildConfig.API_PASS);
        DbxRequestConfig config = new DbxRequestConfig(BuildConfig.CLIENT_ID);
        DbxWebAuth auth = new DbxWebAuth(config, appInfo);
        DbxWebAuth.Request request = DbxWebAuth.newRequestBuilder()
                .withDisableSignup(true)
                .withNoRedirect()
                .build();
        return auth.authorize(request);
    }

    private void showAuthDialog() {
        String authUrl = getAuthUrl();
        final Intent webViewIntent = new Intent(this, WebViewActivity.class);
        webViewIntent.putExtra(EXTRA_AUTH_URL, authUrl);
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_launcher)
                .setTitle(getResources().getString(R.string.title_db_auth_dialog))
                .setMessage(getResources().getString(R.string.body_db_auth_dialog))
                .setPositiveButton(getResources().getString(R.string.btn_ok_db_auth_dialog), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(webViewIntent, REQUEST_AUTH);
                    }
                }).show();
    }

    private void initialiseViews() {
        final String licenseSpan = "License Terms";
        Button logonButton = (Button) findViewById(R.id.logonbutton);
        TextView whyDropboxView = (TextView) findViewById(R.id.explainWhy);
        initialiseClickableSpan(whyDropboxView, BuildConfig.WHY_DROPBOX_URL, getResources().getString(R.string.whyDropbox));
        TextView licenseAgreementView = (TextView) findViewById(R.id.licenseAgreement);
        initialiseClickableSpan(licenseAgreementView, BuildConfig.TERMS_URL, licenseSpan);
        TextView privacyPolicy = (TextView) findViewById(R.id.privacyPolicy);
        initialiseClickableSpan(privacyPolicy, BuildConfig.PRIVACY_POLICY_URL, getResources().getString(R.string.privacyPolicy));
        logonButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                //show dialog
                showAuthDialog();
            }
        }
    }

    public boolean tokenExists() {
        if (mPrefs.getString("emboDBAccessToken", "").equals("")) {
            Log.v(TAG, "Nah token don't exist bruv");
            return false;
        } else {
            Log.v(TAG, "Yah token exists");
            return true;
        }
    }

    public void startSessionWhenUnlinked(Context context) {

    }

    public boolean isDBLinked() {
        if (!tokenExists()) {
            return false;
        }
        return false;
    }


    private int[] getIndexOfUrlKeywords(String keyword, String containingString) {
        int[] index = new int[2];
        index[0] = containingString.indexOf(keyword);
        index[1] = index[0] + keyword.length();
        return index;
    }

    public void showLogonLoader() {
        ProgressDialog mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Please wait...");
        mProgressDialog.setMessage("Logging you in..");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    public void dismissLogonLoader(ProgressDialog progressDialog) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_AUTH) {
            if (resultCode == RESULT_OK) {
                mAuthCode = data.getStringExtra(WebViewActivity.AUTH_CODE_WEBVIEW);
            }
        }
    }
}





package com.noni.embryio;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class WebViewActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener {

    private String mAuthUrl;
    private WebView mAuthWebView;
    private Button mOkButton;
    private EditText mAuthEditText;
    private ProgressBar mProgressBar;
    public static final String AUTH_CODE_WEBVIEW = "authcode";
    private String mAuthCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_auth_activity);
        initialiseToolbar(toolbar);
        mOkButton = (Button) findViewById(R.id.btn_ok);
        mOkButton.setOnClickListener(this);
        mAuthEditText = (EditText) findViewById(R.id.et_auth_code);
        mAuthEditText.addTextChangedListener(this);
        mAuthWebView = (WebView) findViewById(R.id.view_webview_auth);
        mProgressBar = (ProgressBar) findViewById(R.id.auth_progressbar);
        mAuthUrl = getIntent().getStringExtra(LogonActivity.EXTRA_AUTH_URL);
        initialiseWebView();
    }

    private void initialiseToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initialiseWebView() {
        WebSettings settings = mAuthWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mAuthWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mProgressBar.setVisibility(View.GONE);
                mAuthWebView.setVisibility(View.VISIBLE);
            }
        });
        mAuthWebView.loadUrl(mAuthUrl);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() > 0) {
            mOkButton.setEnabled(true);
            mAuthCode = s.toString();
        } else {
            mOkButton.setEnabled(false);
            mAuthCode = null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_ok) {
            Intent intent = new Intent();
            intent.putExtra(AUTH_CODE_WEBVIEW, mAuthCode);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }
}

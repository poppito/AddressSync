package com.noni.embryio;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

public class WebViewActivity extends AppCompatActivity {

    private String mAuthUrl;
    private WebView mAuthWebView;
    private Button mOkButton;
    private EditText mAuthEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_auth_activity);
        mOkButton = (Button) findViewById(R.id.btn_ok);
        mAuthEditText = (EditText) findViewById(R.id.et_auth_code);
        initialiseToolbar(toolbar);
        mAuthWebView = (WebView) findViewById(R.id.view_webview_auth);
        initialiseWebView();
        mAuthUrl = getIntent().getStringExtra(LogonActivity.EXTRA_AUTH_URL);
    }

    private void initialiseToolbar(Toolbar toolbar) {
        if (getSupportActionBar() != null) {
            setSupportActionBar(toolbar);
        }
    }

    private void initialiseWebView() {
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
            }
        });
        mAuthWebView.loadUrl(mAuthUrl);
    }

}

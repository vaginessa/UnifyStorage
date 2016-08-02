package org.cryse.unifystorage.providers.onedrive;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.cryse.unifystorage.credential.Credential;
import org.cryse.unifystorage.utils.oauth2.DeviceType;
import org.cryse.unifystorage.providers.onedrive.utils.OneDriveAuthorizationCodeGrant;
import org.cryse.unifystorage.utils.oauth2.OAuth2AuthorizationCodeGrant;
import org.cryse.unifystorage.utils.oauth2.ScreenSize;
import org.cryse.unifystorage.utils.oauth2.OAuth2AccessToken;

import java.util.Locale;

import okhttp3.OkHttpClient;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class OneDriveAuthenticateActivity extends AppCompatActivity {
    private String mAccountType;
    private String mAccountName;
    private String mClientId;
    private String[] mScopes;
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private static final String ONEDRIVE_SDK_PREFS_NAME = "com.microsoft.live";
    OneDriveAuthorizationCodeGrant mGrant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_authenticate);
        Intent intent = getIntent();
        if (intent.hasExtra(OneDriveConst.PARCELABLE_NAME_ACCOUNT_TYPE) &&
                intent.hasExtra(OneDriveConst.PARCELABLE_NAME_CLIENT_ID) &&
                intent.hasExtra(OneDriveConst.PARCELABLE_NAME_SCOPES)) {
            mAccountType = intent.getStringExtra(OneDriveConst.PARCELABLE_NAME_ACCOUNT_TYPE);
            mClientId = intent.getStringExtra(OneDriveConst.PARCELABLE_NAME_CLIENT_ID);
            mScopes = intent.getStringArrayExtra(OneDriveConst.PARCELABLE_NAME_SCOPES);
        } else {
            setResult(100); // Failed
            finish();
        }

        ScreenSize screenSize = ScreenSize.determineScreenSize(this);
        DeviceType deviceType = screenSize.getDeviceType();

        String displayParameter = deviceType.getDisplayParameter().toString().toLowerCase(Locale.US);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < mScopes.length; i++)
            builder.append(mScopes[i]).append(' ');
        if (builder.charAt(builder.length() - 1) == ' ')
            builder.deleteCharAt(builder.length() - 1);
        mGrant = new OneDriveAuthorizationCodeGrant(
                new OkHttpClient(),
                mClientId,
                builder.toString(),
                OneDriveConst.OAUTH2_DESKTOP_URI.toString(),
                displayParameter
        );

        mWebView = (WebView) findViewById(R.id.webview_auth);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_auth);

        clearCookies(mWebView);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mProgressBar.setVisibility(View.INVISIBLE);

                // Tell the grant we loaded an url
                mGrant.onUrlLoaded(Uri.parse(url));
            }
        });


        // Start the authorization process
        mGrant.setOnAuthorizationCallback(new OAuth2AuthorizationCodeGrant.OnAuthorizationCallback() {
            @Override
            public void onStartExchangeAccessToken() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onSuccess(final OAuth2AccessToken token) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        OneDriveCredential oneDriveCredential = new OneDriveCredential(
                                mAccountName,
                                mAccountType,
                                token);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(Credential.RESULT_KEY, oneDriveCredential);
                        setResult(RESULT_OK, resultIntent);
                        cleanUp();
                        finish();
                    }
                });
            }

            @Override
            public void onError(final Throwable throwable) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(Credential.RESULT_KEY, throwable.getMessage());
                        setResult(100, resultIntent); // Failed
                        cleanUp();
                        finish();
                    }
                });
            }
        });

        // Load the authorization url once build
        mWebView.loadUrl(mGrant.buildAuthorizationUri().toString());
    }

    public void clearCookies(WebView view) {
        view.clearCache(true);
        view.clearHistory();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            //Log.d(C.TAG, "Using ClearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            //Log.d(C.TAG, "Using ClearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(view.getContext());
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    private void cleanUp() {
        clearCookies(mWebView);
    }
}

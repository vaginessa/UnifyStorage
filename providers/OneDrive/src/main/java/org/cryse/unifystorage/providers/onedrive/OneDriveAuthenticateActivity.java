package org.cryse.unifystorage.providers.onedrive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.microsoft.services.msa.InternalOneDriveAuthenticator;
import com.onedrive.sdk.authentication.ClientAuthenticatorException;
import com.onedrive.sdk.authentication.IAccountInfo;
import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.concurrency.IExecutors;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.core.DefaultClientConfig;
import com.onedrive.sdk.core.IClientConfig;
import com.onedrive.sdk.core.OneDriveErrorCodes;
import com.onedrive.sdk.logger.LoggerLevel;

import org.cryse.unifystorage.credential.Credential;

public class OneDriveAuthenticateActivity extends AppCompatActivity {
    private String mAccountType;
    private String mAccountName;
    private String mClientId;
    private String[] mScopes;
    private static final String ONEDRIVE_SDK_PREFS_NAME = "com.microsoft.live";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if(intent.hasExtra(OneDriveConst.PARCELABLE_NAME_ACCOUNT_TYPE) &&
                intent.hasExtra(OneDriveConst.PARCELABLE_NAME_CLIENT_ID) &&
                intent.hasExtra(OneDriveConst.PARCELABLE_NAME_SCOPES)) {
            mAccountType = intent.getStringExtra(OneDriveConst.PARCELABLE_NAME_ACCOUNT_TYPE);
            mClientId = intent.getStringExtra(OneDriveConst.PARCELABLE_NAME_CLIENT_ID);
            mScopes = intent.getStringArrayExtra(OneDriveConst.PARCELABLE_NAME_SCOPES);
        } else {
            setResult(100); // Failed
            finish();
        }
        startAuthenticate();
    }

    private void cleanUp() {
        SharedPreferences preferences =
               getSharedPreferences(ONEDRIVE_SDK_PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
    }

    private void startAuthenticate() {
        final InternalOneDriveAuthenticator internalOneDriveAuthenticator = new InternalOneDriveAuthenticator() {
            @Override
            public String getClientId() {
                return mClientId;
            }

            @Override
            public String[] getScopes() {
                return mScopes;
            }
        };
        final IClientConfig config = DefaultClientConfig.createWithAuthenticator(internalOneDriveAuthenticator);
        config.getLogger().setLoggingLevel(LoggerLevel.Debug);
        final ICallback<Integer> callback = new ICallback<Integer>() {
            @Override
            public void success(Integer aVoid) {
                OneDriveCredential oneDriveCredential = new OneDriveCredential(
                        mAccountName,
                        mAccountType,
                        internalOneDriveAuthenticator.getSession());
                Intent resultIntent = new Intent();
                resultIntent.putExtra(Credential.RESULT_KEY, oneDriveCredential);
                setResult(RESULT_OK, resultIntent);
                cleanUp();
                finish();
            }

            @Override
            public void failure(ClientException ex) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(Credential.RESULT_KEY, ex.getMessage());
                setResult(100, resultIntent); // Failed
                cleanUp();
                finish();
            }
        };

        config.getExecutors().performOnBackground(new Runnable() {
            @Override
            public void run() {
                final IExecutors executors = config.getExecutors();
                try {
                    executors.performOnForeground(login(OneDriveAuthenticateActivity.this, internalOneDriveAuthenticator, config), callback);
                } catch (final ClientException e) {
                    executors.performOnForeground(e, callback);
                }
            }
        });
    }

    private Integer login(final Activity activity, InternalOneDriveAuthenticator internalOneDriveAuthenticator, IClientConfig config) throws ClientException {
        internalOneDriveAuthenticator.init(
                config.getExecutors(),
                config.getHttpProvider(),
                activity,
                config.getLogger());

        IAccountInfo silentAccountInfo = null;
        try {
            silentAccountInfo = internalOneDriveAuthenticator.loginSilent();
        } catch (final Exception ignored) {
        }

        if (silentAccountInfo == null
                && internalOneDriveAuthenticator.login(null) == null) {
            throw new ClientAuthenticatorException("Unable to authenticate silently or interactively",
                    OneDriveErrorCodes.AuthenticationFailure);
        }
        return 0;
    }
}

package org.cryse.unifystorage.providers.onedrive;

import android.app.Activity;
import android.app.Fragment;

import com.microsoft.services.msa.InternalOneDriveAuthenticator;
import com.onedrive.sdk.authentication.MSAAccountInfo;
import com.onedrive.sdk.core.DefaultClientConfig;
import com.onedrive.sdk.core.IClientConfig;
import com.onedrive.sdk.logger.LoggerLevel;

import org.cryse.unifystorage.StorageAuthenticator;
import org.cryse.unifystorage.credential.Credential;

import java.util.List;

public class OneDriveAuthenticator extends StorageAuthenticator {
    private String mClientId;
    private String[] mScopes;
    private static final String ACCOUNT_TYPE = "OneDrive";

    public OneDriveAuthenticator(String clientId, String[] scopes) {
        this.mClientId = clientId;
        this.mScopes = scopes;
    }

    @Override
    public String getAccountType() {
        return ACCOUNT_TYPE;
    }

    @Override
    public String getAccountName() {
        return ACCOUNT_TYPE;
    }

    @Override
    public void startAuthenticate(Activity activity, int requestCode) {
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
        internalOneDriveAuthenticator.init(
                config.getExecutors(),
                config.getHttpProvider(),
                activity,
                config.getLogger());
        MSAAccountInfo accountInfo = (MSAAccountInfo) internalOneDriveAuthenticator.login(null);
        if(accountInfo != null) {
            OneDriveCredential oneDriveCredential = new OneDriveCredential(
                    getAccountName(),
                    getAccountType(),
                    internalOneDriveAuthenticator.getSession());
        }
    }

    @Override
    public void startAuthenticate(Fragment fragment, int requestCode) {

    }

    @Override
    public void startAuthenticate(android.support.v4.app.Fragment fragment, int requestCode) {

    }

    @Override
    public void refreshAuthenticate(Credential credential) {

    }
}

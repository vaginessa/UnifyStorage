package org.cryse.unifystorage.providers.onedrive;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import org.cryse.unifystorage.StorageAuthenticator;
import org.cryse.unifystorage.credential.Credential;

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

    private Intent buildIntent(Activity activity) {
        Intent intent = new Intent(activity, OneDriveAuthenticateActivity.class);
        intent.putExtra(OneDriveConst.PARCELABLE_NAME_ACCOUNT_TYPE, getAccountType());
        intent.putExtra(OneDriveConst.PARCELABLE_NAME_CLIENT_ID, mClientId);
        intent.putExtra(OneDriveConst.PARCELABLE_NAME_SCOPES, mScopes);
        return intent;
    }

    @Override
    public void startAuthenticate(final Activity activity, final int requestCode) {
        activity.startActivityForResult(buildIntent(activity), requestCode);
    }

    @Override
    public void startAuthenticate(Fragment fragment, int requestCode) {
        fragment.startActivityForResult(buildIntent(fragment.getActivity()), requestCode);
    }

    @Override
    public void startAuthenticate(android.support.v4.app.Fragment fragment, int requestCode) {
        fragment.startActivityForResult(buildIntent(fragment.getActivity()), requestCode);
    }

    @Override
    public void refreshAuthenticate(Credential credential) {

    }
}

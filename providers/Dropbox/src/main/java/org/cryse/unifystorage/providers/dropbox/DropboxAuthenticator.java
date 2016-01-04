package org.cryse.unifystorage.providers.dropbox;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

import org.cryse.unifystorage.StorageAuthenticator;
import org.cryse.unifystorage.credential.Credential;

public class DropboxAuthenticator extends StorageAuthenticator {
    private String mAppKey;
    public DropboxAuthenticator(String appKey) {
        this.mAppKey = appKey;
    }

    @Override
    public String getAccountType() {
        return DropboxConst.NAME_ACCOUNT_TYPE;
    }

    private Intent buildIntent(Activity activity) {
        Intent intent = new Intent(activity, DropboxAuthenticateActivity.class);
        intent.putExtra(DropboxConst.PARCELABLE_NAME_ACCOUNT_TYPE, getAccountType());
        intent.putExtra(DropboxConst.PARCELABLE_NAME_APP_KEY, mAppKey);
        return intent;
    }

    @Override
    public void startAuthenticate(Activity activity, int requestCode) {
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

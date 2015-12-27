package org.cryse.unifystorage;

import android.app.Activity;
import android.app.Fragment;

import org.cryse.unifystorage.credential.Credential;

public abstract class StorageAuthenticator {
    public abstract String getAccountType();

    public abstract String getAccountName();

    public abstract void startAuthenticate(Activity activity, int requestCode);

    public abstract void startAuthenticate(Fragment fragment, int requestCode);

    public abstract void startAuthenticate(android.support.v4.app.Fragment fragment, int requestCode);

    public abstract void refreshAuthenticate(Credential credential);
}

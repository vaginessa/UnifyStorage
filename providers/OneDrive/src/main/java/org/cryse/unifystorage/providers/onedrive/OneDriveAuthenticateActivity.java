package org.cryse.unifystorage.providers.onedrive;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.cryse.unifystorage.credential.Credential;

public class OneDriveAuthenticateActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.hasExtra(OneDriveStorageProviderConstants.CREDENTIAL_PARCELABLE_NAME)) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Credential.RESULT_KEY, intent.getParcelableExtra(OneDriveStorageProviderConstants.CREDENTIAL_PARCELABLE_NAME));
            setResult(RESULT_OK);
            finish();
        } else {
            setResult(100); // Failed
            finish();
        }
    }
}

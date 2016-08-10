# UnifyStorage

[![GitHub license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/crysehillmes/UnifyStorage/blob/master/LICENSE.txt)

## Introduction

UnifyStorage provides unified API for different Storage (local, Dropbox, OneDrive, Google Drive .etc) without third-party services.

## Sample Projects

## Gradle Dependency

### Repository

Not uploaded yet.

### Core

Not uploaded yet.

### Dropbox 

Not uploaded yet.

### OneDrive

Not uploaded yet.

### Google Drive

Not uploaded yet.

## Structure

Each local/cloud storage support are based on 3 parts: Authenticator, StorageProvider, RemoteFile.

## Usage

Authenticate:
```java
// OneDrive
OneDriveAuthenticator oneDriveAuthenticator = new OneDriveAuthenticator(
        DataContract.ClientIds.OneDriveClientId,
        DataContract.ClientIds.OneDriveScopes
);
oneDriveAuthenticator.startAuthenticate(MainActivity.this, RC_AUTHENTICATE_ONEDRIVE);

// Dropbox
DropboxAuthenticator dropboxAuthenticator = new DropboxAuthenticator(
        DataContract.ClientIds.DropboxAppKey
);
dropboxAuthenticator.startAuthenticate(MainActivity.this, RC_AUTHENTICATE_DROPBOX);

// Receive authentication result from onActivityResult
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(resultCode == RESULT_OK && data != null && data.hasExtra(Credential.RESULT_KEY)) {
        if(requestCode == RC_AUTHENTICATE_ONEDRIVE) {
            OneDriveCredential credential = data.getParcelableExtra(Credential.RESULT_KEY);
        } else if(requestCode == RC_AUTHENTICATE_DROPBOX) {
            DropboxCredential credential = data.getParcelableExtra(Credential.RESULT_KEY);
        }
    } else {
        new MaterialDialog.Builder(this)
                .title("Something is wrong")
                .content("Something is wrong")
                .positiveText(android.R.string.ok)
                .show();
    }
}
```

Initialize StorageProvider:
```java
StorageProvider localStorageProvider = new LocalStorageProvider(context, mLocalPath);
StorageProvider storageProvider = new DropboxStorageProvider(mOkHttpClient, dropboxCredential, clientIdentifier);
StorageProvider storageProvider = new OneDriveStorageProvider(mOkHttpClient, oneDriveCredential, clientId);
```

List files:
```java
DirectoryInfo directory = storageProvider.list();
// If has more file to load:
DirectoryInfo directory = storageProvider.list(directory);
// List root 
DirectoryInfo directory = storageProvider.list("/");
```

License
-------
Apache License 2.0
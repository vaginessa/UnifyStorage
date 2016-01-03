package org.cryse.unifystorage.providers.dropbox;

import android.os.Parcel;
import android.text.TextUtils;

import com.dropbox.core.android.Auth;

import org.cryse.unifystorage.credential.OAuth2Credential;
import org.cryse.unifystorage.utils.JsonUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

public class DropboxCredential extends OAuth2Credential {
    private String accessSecret;
    public DropboxCredential() {
    }

    public DropboxCredential(String savedCredential) {
        super(savedCredential);
    }

    public DropboxCredential(String accountName, String accountType) {
        super(accountName, accountType);
    }

    public DropboxCredential(
            String accountName,
            String accountType,
            String accessToken,
            String accessSecret,
            String userId) {
        super(
                accountName,
                accountType,
                null,
                null,
                accessToken,
                null,
                null,
                null,
                userId
        );
        this.accessSecret = accessSecret;
    }

    @Override
    public boolean isAvailable() {
        return !TextUtils.isEmpty(accessToken);
    }

    @Override
    public String persist() {
        JSONObject object = new JSONObject();
        try {
            JsonUtils.addIfNotNull(object, "accountName", accountName);
            JsonUtils.addIfNotNull(object, "accountType", accountType);
            JsonUtils.addIfNotNull(object, "tokenType", tokenType);
            JsonUtils.addIfNotNull(object, "authenticationToken", authenticationToken);
            JsonUtils.addIfNotNull(object, "accessToken", accessToken);
            JsonUtils.addIfNotNull(object, "refreshToken", refreshToken);
            JsonUtils.addIfNotNull(object, "userId", userId);
            JsonUtils.addIfNotNull(object, "expiresIn", expiresIn);
            JsonUtils.addIfNotNull(object, "scopes", scopes);
            JsonUtils.addIfNotNull(object, "accessSecret", accessSecret);
        } catch (JSONException ignored) {

        }
        return object.toString();
    }

    @Override
    public void restore(String stored) {
        try {
            JSONObject object = new JSONObject(stored);
            accountName = JsonUtils.readIfExists(object, "accountName", String.class);
            accountType = JsonUtils.readIfExists(object, "accountType", String.class);
            tokenType = JsonUtils.readIfExists(object, "tokenType", String.class);
            authenticationToken = JsonUtils.readIfExists(object, "authenticationToken", String.class);
            accessToken = JsonUtils.readIfExists(object, "accessToken", String.class);
            refreshToken = JsonUtils.readIfExists(object, "refreshToken", String.class);
            userId = JsonUtils.readIfExists(object, "userId", String.class);
            expiresIn = JsonUtils.readIfExists(object, "expiresIn", Date.class);
            scopes = JsonUtils.readSetIfExists(object, "scopes", String.class);
            accessSecret = JsonUtils.readIfExists(object, "accessSecret", String.class);
        } catch (JSONException ignored) {
        }
    }

    @Override
    public String getAccessToken() {
        return accessSecret;
    }

    public String getAccessSecret() {
        return accessSecret;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.accessSecret);
    }

    protected DropboxCredential(Parcel in) {
        super(in);
        this.accessSecret = in.readString();
    }

    public static final Creator<DropboxCredential> CREATOR = new Creator<DropboxCredential>() {
        public DropboxCredential createFromParcel(Parcel source) {
            return new DropboxCredential(source);
        }

        public DropboxCredential[] newArray(int size) {
            return new DropboxCredential[size];
        }
    };
}

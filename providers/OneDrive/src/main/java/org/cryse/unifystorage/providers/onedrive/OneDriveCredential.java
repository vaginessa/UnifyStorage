package org.cryse.unifystorage.providers.onedrive;

import android.os.Parcel;
import android.text.TextUtils;

import org.cryse.unifystorage.credential.OAuth2Credential;
import org.cryse.unifystorage.utils.JsonUtils;
import org.cryse.unifystorage.utils.oauth2.OAuth2AccessToken;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Set;

public class OneDriveCredential extends OAuth2Credential {

    public OneDriveCredential(String savedCredential) {
        super(savedCredential);
    }

    public OneDriveCredential(String accountName, String accountType) {
        super(accountName, accountType);
    }

    public OneDriveCredential(
            String accountName,
            String accountType,
            String tokenType,
            String authenticationToken,
            String accessToken,
            String refreshToken,
            Date expiresIn,
            Set<String> scopes,
            String userId
    ) {
        super(
                accountName,
                accountType,
                tokenType,
                authenticationToken,
                accessToken,
                refreshToken,
                expiresIn,
                scopes,
                userId
        );
        this.userId = userId;
    }

    public OneDriveCredential(
            String accountName,
            String accountType,
            OAuth2AccessToken token) {
        super(accountName, accountType);
        this.tokenType = token.mTokenType;
        this.accessToken = token.mAccessToken;
        this.refreshToken = token.mRefreshToke;
        this.expiresIn = token.mExpiresDate.getTime();
        this.setScopes(token.mScope, " ");
    }

    @Override
    public boolean isAvailable() {
        return !TextUtils.isEmpty(accessToken) &&
                !TextUtils.isEmpty(refreshToken);
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
        } catch (JSONException ignored) {
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected OneDriveCredential(Parcel in) {
        super(in);
    }

    public static final Creator<OneDriveCredential> CREATOR = new Creator<OneDriveCredential>() {
        public OneDriveCredential createFromParcel(Parcel source) {
            return new OneDriveCredential(source);
        }

        public OneDriveCredential[] newArray(int size) {
            return new OneDriveCredential[size];
        }
    };
}

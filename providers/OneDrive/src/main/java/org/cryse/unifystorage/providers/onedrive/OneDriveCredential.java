package org.cryse.unifystorage.providers.onedrive;

import android.os.Parcel;
import android.text.TextUtils;

import com.microsoft.services.msa.LiveConnectSession;

import org.cryse.unifystorage.credential.OAuth2Credential;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class OneDriveCredential extends OAuth2Credential {
    private String userId;

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
        super(accountName, accountType, tokenType, authenticationToken, accessToken, refreshToken, expiresIn, scopes);
        this.userId = userId;
    }

    public OneDriveCredential(
            String accountName,
            String accountType,
            LiveConnectSession session) {
        super(accountName, accountType);
        this.tokenType = session.getTokenType();
        this.authenticationToken = session.getAuthenticationToken();
        this.accessToken = session.getAccessToken();
        this.refreshToken = session.getRefreshToken();
        this.expiresIn = session.getExpiresIn();
        this.setScopes(session.getScopes());
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean isAvailable() {
        return !TextUtils.isEmpty(accessToken) &&
                !TextUtils.isEmpty(refreshToken) &&
                expiresIn.before(new Date());
    }

    @Override
    public String persist() {
        return null;
    }

    @Override
    public void restore(String stored) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.accountName);
        dest.writeString(this.accountType);
        dest.writeString(this.authenticationToken);
        dest.writeString(this.accessToken);
        dest.writeString(this.refreshToken);
        dest.writeLong(this.expiresIn.getTime());
        dest.writeStringArray(this.scopes.toArray(new String[this.scopes.size()]));
        dest.writeString(this.userId);
    }

    protected OneDriveCredential(Parcel in) {
        this.accountName = in.readString();
        this.accountType = in.readString();
        this.authenticationToken = in.readString();
        this.accessToken = in.readString();
        this.refreshToken = in.readString();
        this.expiresIn = new Date(in.readLong());
        String[] scopesArray = in.createStringArray();
        this.scopes = new HashSet<>(scopesArray.length);
        for(String scope : scopesArray) {
            this.scopes.add(scope);
        }
        this.userId = in.readString();
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

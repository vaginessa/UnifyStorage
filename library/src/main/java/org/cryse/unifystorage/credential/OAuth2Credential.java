package org.cryse.unifystorage.credential;

import android.os.Parcel;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public abstract class OAuth2Credential extends Credential {
    protected String tokenType;
    protected String authenticationToken;
    protected String accessToken;
    protected String refreshToken;
    protected Date expiresIn;
    protected Set<String> scopes;
    protected String userId;

    protected OAuth2Credential() {

    }

    public OAuth2Credential(String savedCredential) {
        super(savedCredential);
    }

    public OAuth2Credential(String accountName, String accountType) {
        super(accountName, accountType);
    }

    public OAuth2Credential(
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
        super(accountName, accountType);
        this.tokenType = tokenType;
        this.authenticationToken = authenticationToken;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.scopes = scopes;
        this.userId = userId;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public void setAuthenticationToken(String authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Date getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Date expiresIn) {
        this.expiresIn = expiresIn;
    }

    public void setScopes(Iterable<String> scopes) {
        final Iterable<String> oldValue = this.scopes;

        // Defensive copy
        this.scopes = new HashSet<String>();
        if (scopes != null) {
            for (String scope : scopes) {
                this.scopes.add(scope);
            }
        }

        this.scopes = Collections.unmodifiableSet(this.scopes);
    }

    public Iterable<String> getScopes() {
        return this.scopes;
    }

    public String[] getScopesArray() {
        return scopes.toArray(new String[scopes.size()]);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }



    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.accountName);
        dest.writeString(this.accountType);
        dest.writeString(this.tokenType);
        dest.writeString(this.authenticationToken);
        dest.writeString(this.accessToken);
        dest.writeString(this.refreshToken);
        dest.writeLong(this.expiresIn == null ? 0l : this.expiresIn.getTime());
        if(this.scopes == null)
            dest.writeStringArray(new String[0]);
        else
            dest.writeStringArray(this.scopes.toArray(new String[this.scopes.size()]));
        dest.writeString(this.userId);
    }

    protected OAuth2Credential(Parcel in) {
        this.accountName = in.readString();
        this.accountType = in.readString();
        this.tokenType = in.readString();
        this.authenticationToken = in.readString();
        this.accessToken = in.readString();
        this.refreshToken = in.readString();
        long expiresInDateLong = in.readLong();
        this.expiresIn = expiresInDateLong == 0 ? null : new Date(expiresInDateLong);
        String[] scopesArray = in.createStringArray();
        if(scopesArray.length == 0) {
            this.scopes = new HashSet<>();
        } else {
            this.scopes = new HashSet<>(scopesArray.length);
            Collections.addAll(this.scopes, scopesArray);
        }
        this.userId = in.readString();
    }
}

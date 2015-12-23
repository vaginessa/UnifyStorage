package org.cryse.unifystorage.credential;

import java.util.Date;
import java.util.Set;

public abstract class OAuth2Credential extends Credential {

    protected String applicationKey;
    protected String applicationSecret;
    protected String accessToken;
    protected String refreshToken;
    protected Date expiresIn;
    protected Set<String> scopes;

    public OAuth2Credential(String savedCredential) {
        super(savedCredential);
    }

    public OAuth2Credential(String accountName, String accountType) {
        super(accountName, accountType);
    }

    public OAuth2Credential(
            String accountName,
            String accountType,
            String applicationKey,
            String applicationSecret,
            String accessToken,
            String refreshToken,
            Date expiresIn,
            Set<String> scopes
    ) {
        super(accountName, accountType);
        this.applicationKey = applicationKey;
        this.applicationSecret = applicationSecret;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.scopes = scopes;
    }

    public String getApplicationKey() {
        return applicationKey;
    }

    public void setApplicationKey(String applicationKey) {
        this.applicationKey = applicationKey;
    }

    public String getApplicationSecret() {
        return applicationSecret;
    }

    public void setApplicationSecret(String applicationSecret) {
        this.applicationSecret = applicationSecret;
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

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }
}

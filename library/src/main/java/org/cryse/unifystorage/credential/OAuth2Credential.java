package org.cryse.unifystorage.credential;

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
            Set<String> scopes
    ) {
        super(accountName, accountType);
        this.tokenType = tokenType;
        this.authenticationToken = authenticationToken;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.scopes = scopes;
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
}

package org.cryse.unifystorage.utils.oauth2;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class OAuth2AccessToken {
    public static final String DEFAULT_ACCESS_TOKEN_NAME = "access_token";
    public static final String DEFAULT_REFRESH_TOKEN_NAME = "refresh_token";
    public static final String DEFAULT_TOKEN_TYPE_NAME = "token_type";
    public static final String DEFAULT_EXPIRES_IN_NAME = "expires_in";
    public static final String DEFAULT_SCOPE_NAME = "scope";

    private String mAccessTokenName = DEFAULT_ACCESS_TOKEN_NAME;
    private String mRefreshTokenName = DEFAULT_REFRESH_TOKEN_NAME;
    private String mTokenTypeName = DEFAULT_TOKEN_TYPE_NAME;
    private String mExpiresInName = DEFAULT_EXPIRES_IN_NAME;
    private String mScopeName = DEFAULT_SCOPE_NAME;

    public String mAccessToken = "";
    public String mRefreshToke = "";
    public String mTokenType = "";
    public Calendar mExpiresDate = null;
    public String mScope = "";

    public OAuth2AccessToken() {

    }

    public OAuth2AccessToken(
            String accessTokenName,
            String refreshTokenName,
            String tokenTypeName,
            String expiresInName,
            String scopeName
    ) {
        this.mAccessTokenName = accessTokenName;
        this.mRefreshTokenName = refreshTokenName;
        this.mTokenTypeName = tokenTypeName;
        this.mExpiresInName = expiresInName;
        this.mScopeName = scopeName;
    }

    public void parse(String json) throws JSONException {
        JSONObject object = new JSONObject(json);
        if(object.has(mAccessTokenName))
            mAccessToken = object.getString(mAccessTokenName);
        else
            throw new IllegalArgumentException("No accessToken avaliable");
        if(object.has(mRefreshTokenName))
            mRefreshToke = object.getString(mRefreshTokenName);
        if(object.has(mTokenTypeName))
            mTokenType = object.getString(mTokenTypeName);
        if(object.has(mExpiresInName)) {
            mExpiresDate = Calendar.getInstance();
            mExpiresDate.add(Calendar.SECOND, object.getInt(mExpiresInName));
        }
        if(object.has(mScopeName))
            mScope = object.getString(mScopeName);
    }

    public boolean isExpired() {
        return mExpiresDate != null && Calendar.getInstance().after(mExpiresDate);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof OAuth2AccessToken)) {
            return false;
        }

        OAuth2AccessToken otherToken = (OAuth2AccessToken) other;

        return mAccessToken.equals(otherToken.mAccessToken) && mTokenType.equals(otherToken.mTokenType);
    }

    @Override
    public int hashCode() {
        int result = mTokenType.hashCode();
        result = 31 * result + mAccessToken.hashCode();

        return result;
    }
}

package org.cryse.unifystorage.utils.oauth2;

import android.net.Uri;
import android.text.TextUtils;

/**
 * Class representing the Authorization Code Grant as described in https://tools.ietf.org/html/rfc6749#section-4.1.
 *
 * @param <TAccessToken> The access token type.
 */
public abstract class OAuth2AuthorizationCodeGrant<TAccessToken extends OAuth2AccessToken> implements OAuth2Grant<TAccessToken> {

    // Constants

    /**
     * REQUIRED
     * The "response_type" which MUST be "code".
     */
    public final static String RESPONSE_TYPE = "code";

    /**
     * REQUIRED
     * The "grant_type" which MUST be "authorization_code".
     */
    public final static String GRANT_TYPE = "authorization_code";

    // Properties

    /**
     * REQUIRED
     * The client identifier as described in https://tools.ietf.org/html/rfc6749#section-2.2.
     */
    public String clientId;

    /**
     * OPTIONAL
     * As described in https://tools.ietf.org/html/rfc6749#section-3.1.2.
     */
    public String redirectUri;

    /**
     * OPTIONAL
     * The scope of the access request as described in https://tools.ietf.org/html/rfc6749#section-3.3.
     */
    public String scope;

    /**
     * RECOMMENDED
     * An opaque value used by the client to maintain
     * state between the request and callback. The authorization
     * server includes this value when redirecting the user-agent back
     * to the client. The parameter SHOULD be used for preventing
     * cross-site request forgery as described in https://tools.ietf.org/html/rfc6749#section-10.12.
     */
    public String state;

    // Public Api
    public OnAuthorizationCallback mOnAuthorizationCallback;
    public void setOnAuthorizationCallback(OnAuthorizationCallback callback) {
        this.mOnAuthorizationCallback = callback;
    }

    // Abstract Api

    /**
     * Called when the grant needs the authorization uri.
     */
    public abstract Uri buildAuthorizationUri();

    /**
     * Called when the grant was able to grab the code and it wants to exchange it for an access token.
     */
    public abstract void exchangeTokenUsingCode(String code);

    // Members

    // OAuth2AccessToken
    public void onUrlLoaded(Uri uri) {
        String code = uri.getQueryParameter(RESPONSE_TYPE);
        if(!TextUtils.isEmpty(code)) {
            exchangeTokenUsingCode(code);
        }
    }

    public interface OnAuthorizationCallback {
        void onStartExchangeAccessToken();
        void onSuccess(OAuth2AccessToken token);
        void onError(Throwable throwable);
    }
}

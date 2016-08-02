package org.cryse.unifystorage.providers.onedrive.utils;

import android.net.Uri;

import org.cryse.unifystorage.providers.onedrive.OneDriveConst;
import org.cryse.unifystorage.utils.oauth2.OAuth;
import org.cryse.unifystorage.utils.oauth2.OAuth2AccessToken;
import org.cryse.unifystorage.utils.oauth2.OAuth2AuthorizationCodeGrant;
import org.json.JSONException;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OneDriveAuthorizationCodeGrant extends OAuth2AuthorizationCodeGrant<OAuth2AccessToken> {
    private OkHttpClient mOkHttpClient;
    private String mDisplayParameter;

    public OneDriveAuthorizationCodeGrant(OkHttpClient okHttpClient, String clientId, String scope, String redirectUri, String displayParameter) {
        this.mOkHttpClient = okHttpClient;
        this.clientId = clientId;
        this.scope = scope;
        this.redirectUri = redirectUri;
        this.mDisplayParameter = displayParameter;
    }

    @Override
    public Uri buildAuthorizationUri() {
        String locale = Locale.getDefault().toString();
        final Uri.Builder requestUriBuilder = Uri.parse("https://login.live.com/oauth20_authorize.srf")
                .buildUpon()
                .appendQueryParameter(OAuth.CLIENT_ID, clientId)
                .appendQueryParameter(OAuth.SCOPE, scope)
                .appendQueryParameter(OAuth.DISPLAY, mDisplayParameter)
                .appendQueryParameter(OAuth.RESPONSE_TYPE, RESPONSE_TYPE)
                .appendQueryParameter(OAuth.LOCALE, locale)
                .appendQueryParameter(OAuth.REDIRECT_URI, redirectUri);
        return requestUriBuilder.build();
    }

    @Override
    public void exchangeTokenUsingCode(final String code) {
        if (mOnAuthorizationCallback != null) {
            mOnAuthorizationCallback.onStartExchangeAccessToken();
        }
        RequestBody formBody = new FormBody.Builder()
                .add(OAuth.CLIENT_ID, clientId)
                .add(OAuth.CODE, code)
                .add(OAuth.REDIRECT_URI, OneDriveConst.OAUTH2_DESKTOP_URI.toString())
                .add(OAuth.GRANT_TYPE, OAuth.GrantType.AUTHORIZATION_CODE.toString().toLowerCase(Locale.US))
                .build();
        Request request = new Request.Builder()
                .url("https://login.live.com/oauth20_token.srf")
                .post(formBody)
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mOnAuthorizationCallback != null) {
                    mOnAuthorizationCallback.onError(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken();
                try {
                    oAuth2AccessToken.parse(responseString);
                    if (mOnAuthorizationCallback != null) {
                        mOnAuthorizationCallback.onSuccess(oAuth2AccessToken);
                    }
                } catch (JSONException e) {
                    if (mOnAuthorizationCallback != null) {
                        mOnAuthorizationCallback.onError(e);
                    }
                }
            }
        });
    }
}

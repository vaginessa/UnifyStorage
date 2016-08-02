package org.cryse.unifystorage.providers.onedrive;

import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OneDriveService {
    String SUBDOMAIN_API = "api.onedrive.com/";
    String SUBDOMAIN_VERSION = "v1.0/";

    @FormUrlEncoded
    @POST("//login.live.com/oauth20_token.srf")
    Call<JsonObject> refreshToken(
            @Field("client_id") String clientId,
            @Field("redirect_uri") String redirect_uri,
            @Field("client_secret") String client_secret,
            @Field("refresh_token") String refresh_token,
            @Field("grant_type") String grant_type
    );

    @GET("drive/root")
    @Headers("Accept: application/json")
    Call<JsonObject> getDriveRoot(@Header("Authorization") String authorization);

    @GET("drive/items/{item-id}/children")
    @Headers({
            "Accept: application/json"
    })
    Call<JsonObject> listChildrenById(@Header("Authorization") String authorization, @Path("item-id") String id);

    @GET("drive/root:/{item-path}:/children")
    @Headers({
            "Accept: application/json"
    })
    Call<JsonObject> listChildrenByPath(@Header("Authorization") String authorization, @Path("item-path") String path);

    @GET("drive/items/{item-id}")
    @Headers({
            "Accept: application/json"
    })
    Call<JsonObject> getMetaDataById(@Header("Authorization") String authorization, @Path("item-id") String id);

    @GET("drive/root:/{item-path}")
    @Headers({
            "Accept: application/json"
    })
    Call<JsonObject> getMetaDataByPath(@Header("Authorization") String authorization, @Path("item-path") String path);

    @POST("drive/items/{parent-id}/children")
    @Headers({
            "Content-Type: application/json"
    })
    Call<JsonObject> createFolder(@Header("Authorization") String authorization, @Path("parent-id") String path, @Body JsonObject body);

    @GET("drive/items/{item-id}/content")
    Call<ResponseBody> downloadById(@Header("Authorization") String authorization, @Path("item-id") String itemId);

    @GET("drive/root:/{item-path}:/content")
    Call<ResponseBody> downloadByPath(@Header("Authorization") String authorization, @Path("item-path") String itemPath);

    @DELETE("drive/items/{item-id}")
    @Headers({
            "Accept: application/json"
    })
    Call<JsonObject> deleteById(@Header("Authorization") String authorization, @Path("item-id") String id);

    @DELETE("drive/root:/{item-path}")
    @Headers({
            "Accept: application/json"
    })
    Call<JsonObject> deleteByPath(@Header("Authorization") String authorization, @Path("item-path") String path);
}

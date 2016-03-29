package org.cryse.unifystorage.providers.dropbox;

import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface DropboxService {
    String SUBDOMAIN_API = "api.dropboxapi.com";
    String SUBDOMAIN_CONTENT = "content.dropboxapi.com";
    @POST("2/files/list_folder")
    @Headers({
            "Content-Type: application/json"
    })
    Call<JsonObject> listFolders(@Header("Authorization") String authorization, @Body JsonObject body);

    @POST("2/files/list_folder/continue")
    Call<JsonObject> listFoldersContinue(@Header("Authorization") String authorization, @Body JsonObject body);

    @POST("2/files/get_metadata")
    @Headers({
            "Content-Type: application/json"
    })
    Call<JsonObject> getMetaData(@Header("Authorization") String authorization, @Body JsonObject body);

    @POST("2/files/create_folder")
    @Headers({
            "Content-Type: application/json"
    })
    Call<JsonObject> createFolder(@Header("Authorization") String authorization, @Body JsonObject body);

    @POST("2/files/delete")
    @Headers({
            "Content-Type: application/json"
    })
    Call<JsonObject> delete(@Header("Authorization") String authorization, @Body JsonObject body);

    @POST("//" + SUBDOMAIN_CONTENT + "/2/files/download")
    Call<ResponseBody> download(@Header("Authorization") String authorization, @Header("Dropbox-API-Arg") String argAPI);
}
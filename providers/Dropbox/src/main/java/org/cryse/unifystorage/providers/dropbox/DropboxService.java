package org.cryse.unifystorage.providers.dropbox;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface DropboxService {
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
}
package org.cryse.unifystorage.providers.dropbox;

import com.google.gson.JsonObject;

public class DropboxRequestDataBuilder {
    JsonObject jsonObject;
    public DropboxRequestDataBuilder() {
        jsonObject = new JsonObject();
    }

    public JsonObject build() {
        return jsonObject;
    }

    public DropboxRequestDataBuilder listFolder(String path) {
        jsonObject.addProperty("path", path);
        jsonObject.addProperty("recursive", false);
        jsonObject.addProperty("include_media_info", false);
        jsonObject.addProperty("include_deleted", false);
        return this;
    }

    public DropboxRequestDataBuilder listFolderContinue(String cursor) {
        jsonObject.addProperty("cursor", cursor);
        return this;
    }

    public DropboxRequestDataBuilder createFolder(String path) {
        jsonObject.addProperty("path", path);
        return this;
    }

    public DropboxRequestDataBuilder getMetaData(String path) {
        jsonObject.addProperty("path", path);
        jsonObject.addProperty("include_media_info", false);
        jsonObject.addProperty("include_deleted", false);
        return this;
    }

    public DropboxRequestDataBuilder download(String path) {
        jsonObject.addProperty("path", path);
        return this;
    }

    public DropboxRequestDataBuilder delete(String path) {
        jsonObject.addProperty("path", path);
        return this;
    }
}

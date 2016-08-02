package org.cryse.unifystorage.providers.onedrive;

import com.google.gson.JsonObject;

public class RequestDataBuilder {
    JsonObject jsonObject;
    public RequestDataBuilder() {
        jsonObject = new JsonObject();
    }

    public JsonObject build() {
        return jsonObject;
    }

    public RequestDataBuilder createFolder(String name, String conflictBehavior) {
        jsonObject.addProperty("name", name);
        JsonObject emptyObject = new JsonObject();
        jsonObject.add("folder", emptyObject);
        jsonObject.addProperty("@name.conflictBehavior", conflictBehavior);
        return this;
    }

    public RequestDataBuilder delete(String path) {
        jsonObject.addProperty("path", path);
        return this;
    }
}

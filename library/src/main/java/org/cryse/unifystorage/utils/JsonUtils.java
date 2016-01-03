package org.cryse.unifystorage.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class JsonUtils {
    public static <T> void addIfNotNull(JSONObject object, String name, T value) throws JSONException {
        if(value != null) {
            if(value instanceof Date)
                object.put(name, ((Date)value).getTime());
            else if(value instanceof Iterable) {
                JSONArray array = new JSONArray();
                for (Object scope : (Iterable)value) {
                    array.put(scope);
                }
                object.put(name, array);
            }
            else
                object.put(name, value);
        }
    }

    public static <T> T readIfExists(JSONObject object, String name, Class<T> tClass) throws JSONException {
        if (object.has(name)) {
            if(tClass == String.class)
                return tClass.cast(object.getString(name));
            else if(tClass == Date.class)
                return tClass.cast(new Date(object.getLong(name)));
            else
                return null;
        }
        return null;
    }

    public static <T> Set<T> readSetIfExists(JSONObject object, String name, Class<T> tClass) throws JSONException {
        if (object.has(name)) {
            Set<T> set = new HashSet<T>();
            JSONArray array = object.getJSONArray(name);
            for (int i = 0; i < array.length(); i++) {
                set.add(tClass.cast(array.get(i)));
            }
            return set;
        }
        return null;
    }
}

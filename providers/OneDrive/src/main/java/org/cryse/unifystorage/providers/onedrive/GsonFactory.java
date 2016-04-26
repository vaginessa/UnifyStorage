package org.cryse.unifystorage.providers.onedrive;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Calendar;

public class GsonFactory {
    public static Gson getGsonInstance() {

        final JsonSerializer<Calendar> dateJsonSerializer = new JsonSerializer<Calendar>() {
            @Override
            public JsonElement serialize(final Calendar src,
                                         final Type typeOfSrc,
                                         final JsonSerializationContext context) {
                if (src == null) {
                    return null;
                }
                try {
                    return new JsonPrimitive(CalendarSerializer.serialize(src));
                } catch (final Exception e) {
                    //logger.logError("Parsing issue on " + src, e);
                    return null;
                }
            }
        };

        final JsonDeserializer<Calendar> dateJsonDeserializer = new JsonDeserializer<Calendar>() {
            @Override
            public Calendar deserialize(final JsonElement json,
                                        final Type typeOfT,
                                        final JsonDeserializationContext context) throws JsonParseException {
                if (json == null) {
                    return null;
                }
                try {
                    return CalendarSerializer.deserialize(json.getAsString());
                } catch (final ParseException e) {
                    //logger.logError("Parsing issue on " + json.getAsString(), e);
                    return null;
                }
            }
        };

        return new GsonBuilder()
                .registerTypeAdapter(Calendar.class, dateJsonSerializer)
                .registerTypeAdapter(Calendar.class, dateJsonDeserializer)
                .create();
    }
}

package io.boxcar.publisher.client.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jpcarlino on 10/02/15.
 */
public class DateTimeConverter implements JsonDeserializer<Date>, JsonSerializer<Date> {
    public static final DateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static {
        DATE_FORMAT.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
    }

    /**
     * Converts strings of this format "yyyy-MM-ddTHH:mm:ssZ" to date
     * instances.
     * @param json
     * @param typeOfT
     * @param context
     * @return
     * @throws com.google.gson.JsonParseException
     */
    public Date deserialize(JsonElement json, Type typeOfT,
                            JsonDeserializationContext context) throws JsonParseException {
        String dateTime = json.getAsJsonPrimitive().getAsString();
        try {
            return DATE_FORMAT.parse(dateTime);
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
    }

    /**
     * Converts date instances into strings like "yyyy-MM-ddTHH:mm:ssZ"
     */
	public JsonElement serialize(Date date, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(DATE_FORMAT.format(date));
	}

}

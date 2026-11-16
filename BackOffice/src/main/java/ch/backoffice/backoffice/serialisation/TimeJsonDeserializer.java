package ch.backoffice.backoffice.serialisation;

import com.fasterxml.jackson.core.JsonParseException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class TimeJsonDeserializer {

    public static Integer deserialize(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

        try {
            // "required": ["timestamp"]
            if (!obj.has("UnixTimeStamp")) {
                throw new JsonParseException("timestamp manquant");
            }
            JsonElement el = obj.get("UnixTimeStamp");

            // "type": "integer" (rejeter booléens et non-nombres)
            if (!el.isJsonPrimitive() || !el.getAsJsonPrimitive().isNumber()) {
                throw new JsonParseException("timestamp doit être un entier");
            }
            return el.getAsInt();
        } catch (JsonParseException e) {
            return null;
        }
    }
}

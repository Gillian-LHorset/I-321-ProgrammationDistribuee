package ch.backoffice.backoffice.serialisation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class TimeJsonDeserializer {

    private TimeJsonDeserializer() {
    }

    public static Integer deserialize(String json) {
        JsonObject obj;
        try {
            obj = JsonParser.parseString(json).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }

        if (hasTimePayload(obj)) {
            JsonObject payload = obj.getAsJsonObject("payload");
            Integer timestamp = asInteger(payload.get("timestamp"));
            if (timestamp == null) {
                return null;
            }
            Integer offset = asInteger(payload.get("offset"));
            return timestamp + (offset != null ? offset : 0);
        }

        // Fallback : ancien format plat
        Integer flat = asInteger(obj.get("UnixTimeStamp"));
        if (flat == null) {
            flat = asInteger(obj.get("timestamp"));
        }
        return flat;
    }

    /**
     * @return true si le JSON est une enveloppe SERV-Time portant payload.timestamp
     */
    public static boolean isServTimeEnvelope(String json) {
        try {
            return hasTimePayload(JsonParser.parseString(json).getAsJsonObject());
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean hasTimePayload(JsonObject obj) {
        return obj.has("payload")
                && obj.get("payload").isJsonObject()
                && obj.getAsJsonObject("payload").has("timestamp");
    }

    private static Integer asInteger(JsonElement el) {
        if (el == null || !el.isJsonPrimitive() || !el.getAsJsonPrimitive().isNumber()) {
            return null;
        }
        try {
            return el.getAsInt();
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

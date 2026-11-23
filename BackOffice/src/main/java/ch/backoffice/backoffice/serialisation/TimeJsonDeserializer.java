package ch.backoffice.backoffice.serialisation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class TimeJsonDeserializer {

    private TimeJsonDeserializer() {
    }

    /**
     * Désérialise un message du topic umddcmid3b/time.
     *
     * Format réellement publié par SERV-Time (enveloppe) :
     * {"v":1,"id":"...","from":"SERV-time","to":"*","ts":"2026-11-23T10:15:58Z",
     *  "type":"time","payload":{"timestamp":1795432558}}
     *
     * Le timestamp simulé (offset déjà appliqué côté serveur) se trouve dans
     * payload.timestamp. Si un champ "offset" est présent dans le payload,
     * il est ajouté au timestamp (time + offset).
     *
     * Fallback : ancien format plat {"UnixTimeStamp": 1795428811} ou {"timestamp": ...}.
     *
     * @return le timestamp Unix en secondes, ou null si le message n'est pas une heure valide
     */
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

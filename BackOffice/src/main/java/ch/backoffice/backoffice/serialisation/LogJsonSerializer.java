package ch.backoffice.backoffice.serialisation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.SpecVersion.VersionFlag;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Construit les messages de log publies sur MQTT.
 *
 * L'enveloppe suit le meme contrat que SERV-Time (voir JSON/time.json) :
 * { v, id, from, to, ts, type:"log", payload:{...} }
 * Le payload { level, text } est valide contre schemas/log.schema.json.
 */
public final class LogJsonSerializer {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonSchema LOG_SCHEMA = chargerSchema();

    private LogJsonSerializer() {
    }

    /**
     * @param from  identifiant de l'emetteur (Id du back-office, ex. "COM-1522")
     * @param level "info", "warn" ou "error" (voir log.schema.json)
     * @param text  message, ex. "La bp Au MontSQL est ouvert"
     * @return l'enveloppe JSON prete a etre publiee
     */
    public static String serialize(String from, String level, String text) {
        JsonObject payload = new JsonObject();
        payload.addProperty("level", level);
        payload.addProperty("text", text);
        validerPayload(payload);

        JsonObject enveloppe = new JsonObject();
        enveloppe.addProperty("v", 1);
        enveloppe.addProperty("id", UUID.randomUUID().toString());
        enveloppe.addProperty("from", from);
        enveloppe.addProperty("to", "*");
        enveloppe.addProperty("ts", Instant.now().toString());
        enveloppe.addProperty("type", "log");
        enveloppe.add("payload", payload);
        return enveloppe.toString();
    }

    private static void validerPayload(JsonObject payload) {
        try {
            JsonNode node = MAPPER.readTree(payload.toString());
            Set<ValidationMessage> erreurs = LOG_SCHEMA.validate(node);
            if (!erreurs.isEmpty()) {
                throw new IllegalArgumentException("Payload log invalide : " + erreurs);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Payload log non serialisable", e);
        }
    }

    private static JsonSchema chargerSchema() {
        try (InputStream in = LogJsonSerializer.class.getResourceAsStream("/schemas/log.schema.json")) {
            if (in == null) {
                throw new IllegalStateException("schemas/log.schema.json introuvable sur le classpath");
            }
            // $id relatif ("contrat/log") non resolvable par le validateur : on le retire
            com.fasterxml.jackson.databind.node.ObjectNode node =
                    (com.fasterxml.jackson.databind.node.ObjectNode) MAPPER.readTree(in);
            node.remove("$id");
            return JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(node);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}

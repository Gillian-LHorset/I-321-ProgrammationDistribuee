package ch.backoffice.backoffice.serialisation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogJsonSerializerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void enveloppeConformeAuContratSERV() throws Exception {
        String json = LogJsonSerializer.serialize("COM-1522", "info", "La bp Au MontSQL est ouvert");
        JsonObject env = JsonParser.parseString(json).getAsJsonObject();

        assertEquals(1, env.get("v").getAsInt());
        assertEquals("log", env.get("type").getAsString());
        assertEquals("COM-1522", env.get("from").getAsString());
        assertEquals("*", env.get("to").getAsString());
        // id = UUID valide, ts = instant ISO-8601 parseable
        assertDoesNotThrow(() -> UUID.fromString(env.get("id").getAsString()));
        assertDoesNotThrow(() -> Instant.parse(env.get("ts").getAsString()));

        JsonObject payload = env.getAsJsonObject("payload");
        assertEquals("info", payload.get("level").getAsString());
        assertEquals("La bp Au MontSQL est ouvert", payload.get("text").getAsString());
    }

    @Test
    void payloadValideContreLogSchema() throws Exception {
        String json = LogJsonSerializer.serialize("COM-1522", "warn", "La bp Au MontSQL est fermé");
        JsonObject payload = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("payload");

        try (InputStream in = getClass().getResourceAsStream("/schemas/log.schema.json")) {
            com.fasterxml.jackson.databind.node.ObjectNode schemaNode =
                    (com.fasterxml.jackson.databind.node.ObjectNode) MAPPER.readTree(in);
            schemaNode.remove("$id"); // $id relatif non resolvable par le validateur
            JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaNode);
            JsonNode node = MAPPER.readTree(payload.toString());
            assertTrue(schema.validate(node).isEmpty(), "le payload doit valider log.schema.json");
        }
    }

    @Test
    void levelInvalideEstRejete() {
        assertThrows(IllegalArgumentException.class,
                () -> LogJsonSerializer.serialize("COM-1522", "debug", "texte"));
    }

    @Test
    void textVideEstRejete() {
        assertThrows(IllegalArgumentException.class,
                () -> LogJsonSerializer.serialize("COM-1522", "info", ""));
    }
}

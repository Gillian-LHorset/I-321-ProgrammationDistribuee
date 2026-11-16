package ch.backoffice.backoffice.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageSchemaTest {

    private static final String SCHEMA_RESOURCE = "/schemas/message.schema.json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static JsonSchema schema;

    @BeforeAll
    static void chargerSchema() throws Exception {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        try (InputStream in = MessageSchemaTest.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            assertNotNull(in, "Ressource introuvable : " + SCHEMA_RESOURCE);
            schema = factory.getSchema(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void payloadValideEstAccepte() throws Exception {
        String payload = """
                {
                  "Id": "COM-1522",
                  "Name": "Au MontSQL",
                  "Logo": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg==",
                  "Special": {
                    "Id": "PROD-1522",
                    "Name": "Macaron ON Cascade",
                    "Picture": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg==",
                    "Price": 2550
                  }
                }
                """;
        Set<ValidationMessage> erreurs = schema.validate(MAPPER.readTree(payload));
        assertTrue(erreurs.isEmpty(), () -> "Payload valide rejeté : " + erreurs);
    }

    @Test
    void payloadSansChampObligatoireEstRejete() throws Exception {
        // Le champ obligatoire "Name" est absent à la racine.
        String payload = """
                {
                  "Id": "COM-1522",
                  "Logo": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg==",
                  "Special": {
                    "Id": "PROD-1522",
                    "Name": "Macaron ON Cascade",
                    "Picture": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg==",
                    "Price": 2550
                  }
                }
                """;
        Set<ValidationMessage> erreurs = schema.validate(MAPPER.readTree(payload));
        assertFalse(erreurs.isEmpty(), "Un payload sans le champ obligatoire 'Name' doit être rejeté");
        assertTrue(
                erreurs.stream().anyMatch(m -> m.getMessage().contains("Name")),
                () -> "L'erreur doit signaler le champ manquant 'Name' : " + erreurs
        );
    }
}

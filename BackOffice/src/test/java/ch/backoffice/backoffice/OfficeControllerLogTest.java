package ch.backoffice.backoffice;

import ch.backoffice.backoffice.config.Horaires;
import ch.backoffice.backoffice.models.bpModel;
import ch.backoffice.backoffice.models.produit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifie que seules les transitions ouvert/ferme declenchent un log MQTT
 * au format "La bp <nom> est ouvert/ferme", avec un payload conforme a log.schema.json.
 */
class OfficeControllerLogTest {

    @BeforeAll
    static void demarrerToolkit() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException dejaDemarre) {
            // toolkit deja initialise par un autre test
        }
    }

    private static int ts(int annee, int mois, int jour, int heure, int minute) {
        return (int) ZonedDateTime.of(annee, mois, jour, heure, minute, 0, 0,
                ZoneId.of("Europe/Zurich")).toEpochSecond();
    }

    @Test
    void publieUniquementSurTransition() throws Exception {
        List<String> publies = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/ch/backoffice/backoffice/office-view.fxml"));
                loader.load();
                OfficeController controller = loader.getController();
                controller.setBackOfficeData(new bpModel("COM-1522", "Au MontSQL", null,
                        new produit("PROD-1", "Produit test", null, 1.0)));
                controller.setHoraires(Horaires.charger(Path.of("config.yml")));
                controller.setLogPublisher(publies::add);

                controller.setTime(ts(2025, 1, 6, 10, 0));   // lundi 10:00 -> premier etat ouvert, pas de log
                controller.setTime(ts(2025, 1, 6, 10, 1));   // toujours ouvert -> pas de log
                controller.setTime(ts(2025, 1, 6, 20, 0));   // lundi 20:00 -> transition ferme
                controller.setTime(ts(2025, 1, 7, 7, 0));    // mardi 07:00 -> transition ouvert
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        });
        latch.await(15, TimeUnit.SECONDS);

        assertEquals(2, publies.size(), "seules les 2 transitions doivent etre publiees");

        JsonObject ferme = JsonParser.parseString(publies.get(0)).getAsJsonObject();
        JsonObject ouvert = JsonParser.parseString(publies.get(1)).getAsJsonObject();

        assertEquals("log", ferme.get("type").getAsString());
        assertEquals("COM-1522", ferme.get("from").getAsString());
        assertEquals("La bp Au MontSQL est fermé", ferme.getAsJsonObject("payload").get("text").getAsString());
        assertEquals("info", ferme.getAsJsonObject("payload").get("level").getAsString());
        assertEquals("La bp Au MontSQL est ouvert", ouvert.getAsJsonObject("payload").get("text").getAsString());

        // chaque payload publie doit valider log.schema.json
        try (InputStream in = getClass().getResourceAsStream("/schemas/log.schema.json")) {
            ObjectMapper mapper = new ObjectMapper();
            com.fasterxml.jackson.databind.node.ObjectNode schemaNode =
                    (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(in);
            schemaNode.remove("$id"); // $id relatif non resolvable par le validateur
            JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaNode);
            for (String json : publies) {
                JsonObject payload = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("payload");
                assertTrue(schema.validate(mapper.readTree(payload.toString())).isEmpty(),
                        "payload invalide : " + payload);
            }
        }
    }
}

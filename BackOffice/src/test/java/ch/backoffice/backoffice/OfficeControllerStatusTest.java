package ch.backoffice.backoffice;

import ch.backoffice.backoffice.config.Horaires;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Charge le vrai FXML et verifie que setTime (appele a chaque message MQTT)
 * met a jour dynamiquement le label Status Ouvert/Ferme.
 */
class OfficeControllerStatusTest {

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

    private String statusApresSetTime(int timestamp) throws Exception {
        AtomicReference<String> status = new AtomicReference<>();
        AtomicReference<String> heure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/ch/backoffice/backoffice/office-view.fxml"));
                loader.load();
                OfficeController controller = loader.getController();
                controller.setHoraires(Horaires.charger(Path.of("config.yml")));
                controller.setTime(timestamp);
                status.set(((Label) loader.getNamespace().get("Status")).getText());
                heure.set(((Label) loader.getNamespace().get("Time")).getText());
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        });
        latch.await(15, TimeUnit.SECONDS);
        System.out.println("Time=" + heure.get() + " Status=" + status.get());
        return status.get();
    }

    @Test
    void afficheOuvertPendantLesHoraires() throws Exception {
        // lundi 06.01.2025 10:00 Europe/Zurich -> ouvert (06:00-17:00)
        assertEquals("Ouvert", statusApresSetTime(ts(2025, 1, 6, 10, 0)));
    }

    @Test
    void afficheFermeEnDehorsDesHoraires() throws Exception {
        // lundi 06.01.2025 20:00 Europe/Zurich -> ferme
        assertEquals("Fermé", statusApresSetTime(ts(2025, 1, 6, 20, 0)));
    }

    @Test
    void afficheFermeLeDimanche() throws Exception {
        // dimanche 12.01.2025 10:00 -> non configure -> ferme
        assertEquals("Fermé", statusApresSetTime(ts(2025, 1, 12, 10, 0)));
    }

    @Test
    void afficheFermeLeSamediApresMidi() throws Exception {
        // samedi 11.01.2025 13:00 -> ferme (08:00-12:00)
        assertEquals("Fermé", statusApresSetTime(ts(2025, 1, 11, 13, 0)));
    }
}

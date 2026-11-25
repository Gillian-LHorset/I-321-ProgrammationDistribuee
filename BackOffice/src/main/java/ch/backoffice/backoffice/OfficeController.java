package ch.backoffice.backoffice;

import ch.backoffice.backoffice.config.Horaires;
import ch.backoffice.backoffice.models.bpModel;
import ch.backoffice.backoffice.mqtt.MqttService;
import ch.backoffice.backoffice.serialisation.LogJsonSerializer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.function.Consumer;

public class OfficeController {
    @FXML
    public Label Id;
    public Label Name;
    public ImageView  Logo;
    public Label ProductId;
    public Label ProductName;
    public ImageView  ProductImage;
    public Label Time;
    public Label Status;

    private Horaires horaires;

    /** Dernier etat connu (null tant qu'aucun horaire MQTT recu). */
    private Boolean dernierEtatOuvert = null;

    /** Publication des logs (MQTT par defaut, remplacable pour les tests). */
    private Consumer<String> logPublisher = json -> MqttService.publish(MqttService.TOPIC_LOG, json);

    public void setHoraires(Horaires horaires) {
        this.horaires = horaires;
    }

    public void setLogPublisher(Consumer<String> logPublisher) {
        this.logPublisher = logPublisher;
    }

    @FXML
    public void setBackOfficeData(bpModel bp) {
        Id.setText(bp.ID);
        Name.setText(bp.Name);
        Logo.setImage(bp.Logo);
        ProductId.setText(bp.Special.ID);
        ProductName.setText(bp.Special.Name);
        ProductImage.setImage(bp.Special.Picture);
    }

    public void onTestButtonClick(ActionEvent event) throws IOException {

    }
    public void setTime(Integer ts) {
        ZonedDateTime maintenant = Instant.ofEpochSecond(ts)
                .atZone(ZoneId.of("Europe/Zurich"));
        Time.setText(maintenant.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        boolean ouvert = horaires != null && horaires.estOuvert(maintenant.toLocalDateTime());
        Status.setText(ouvert ? "Ouvert" : "Fermé");
        Status.getStyleClass().setAll("status", ouvert ? "status-ouvert" : "status-ferme");
        publierTransition(ouvert);
    }

    /**
     * Publie un log MQTT uniquement lorsqu'on detecte une transition
     * (ouverture ou fermeture). Le premier etat recu sert de reference
     * et ne declenche pas de publication.
     */
    private void publierTransition(boolean ouvert) {
        if (dernierEtatOuvert != null) {
            if (dernierEtatOuvert == ouvert) {
                return;
            }
            String text = "La bp " + Name.getText() + " est " + (ouvert ? "ouvert" : "fermé");
            String json = LogJsonSerializer.serialize(Id.getText(), "info", text);
            try {
                logPublisher.accept(json);
            } catch (RuntimeException e) {
                System.err.println("Publication du log MQTT impossible : " + e.getMessage());
            }
        }
        dernierEtatOuvert = ouvert;
    }
}

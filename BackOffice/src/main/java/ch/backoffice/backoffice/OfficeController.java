package ch.backoffice.backoffice;

import ch.backoffice.backoffice.config.Horaires;
import ch.backoffice.backoffice.models.bpModel;
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

    public void setHoraires(Horaires horaires) {
        this.horaires = horaires;
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
    }
}

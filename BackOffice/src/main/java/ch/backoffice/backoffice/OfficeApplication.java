package ch.backoffice.backoffice;

import ch.backoffice.backoffice.models.bpModel;
import ch.backoffice.backoffice.mqtt.MqttService;
import ch.backoffice.backoffice.serialisation.BpJsonDeserializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class OfficeApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(OfficeApplication.class.getResource("office-view.fxml"));

        bpModel myOffice;
        try {
            myOffice = BpJsonDeserializer.deserialize(
                    Files.readString(Path.of("C:/Users/pq60soi/Desktop/I-321-ProgrammationDistribuee/JSON/default.json"))
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Fichier JSON illisible", e);
        }
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);

        OfficeController controller = fxmlLoader.getController();
        controller.setBackOfficeData(myOffice);


        stage.setTitle("Au MontSQL");
        stage.setScene(scene);
        stage.show();
    }
}

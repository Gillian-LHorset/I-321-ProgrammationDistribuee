package ch.backoffice.backoffice;

import ch.backoffice.backoffice.mqtt.MqttService;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        MqttService.start();
        Application.launch(OfficeApplication.class, args);
    }
}

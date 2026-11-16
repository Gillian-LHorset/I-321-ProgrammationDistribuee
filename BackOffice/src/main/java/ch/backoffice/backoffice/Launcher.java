package ch.backoffice.backoffice;

import ch.backoffice.backoffice.mqtt.MqttService;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        Thread mqttThread = new Thread(MqttService::start);
        mqttThread.setDaemon(true);
        mqttThread.start();
        Application.launch(OfficeApplication.class, args);
    }
}

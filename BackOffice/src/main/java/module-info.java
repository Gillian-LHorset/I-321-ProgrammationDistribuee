module ch.backoffice.backoffice {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.desktop;
    requires com.google.gson;
    requires org.eclipse.paho.client.mqttv3;
    requires com.networknt.schema;
    requires com.fasterxml.jackson.databind;
    requires java.sql;

    opens ch.backoffice.backoffice to javafx.fxml;
    exports ch.backoffice.backoffice;
}
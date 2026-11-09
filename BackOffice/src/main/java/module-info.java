module ch.backoffice.backoffice {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.desktop;

    opens ch.backoffice.backoffice to javafx.fxml;
    exports ch.backoffice.backoffice;
}
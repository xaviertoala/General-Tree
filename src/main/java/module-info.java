module com.espoch.arbolgeneral {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens ec.edu.espoch.app to javafx.fxml;

    exports ec.edu.espoch.app;
    exports ec.edu.espoch.controller;
    opens ec.edu.espoch.controller to javafx.fxml;
    exports ec.edu.espoch.modelo;
    opens ec.edu.espoch.modelo to javafx.fxml;
}

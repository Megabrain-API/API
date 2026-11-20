module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.sql;
    requires java.desktop;
    requires mysql.connector.j;
    requires javafx.graphics;
    requires javafx.base;

    opens org.example to javafx.fxml, com.google.gson;
    exports org.example;
    exports org.example.controllers.profControllers;
    opens org.example.controllers.profControllers to javafx.fxml, com.google.gson;
    exports org.example.controllers.alunoControllers;
    opens org.example.controllers.alunoControllers to javafx.fxml, com.google.gson;
    exports org.example.entity.profEntity;
    opens org.example.entity.profEntity to javafx.fxml, com.google.gson;
    exports org.example.controllers;
    opens org.example.controllers to com.google.gson, javafx.fxml;
    exports org.example.entity;
    opens org.example.entity to com.google.gson, javafx.fxml;
}

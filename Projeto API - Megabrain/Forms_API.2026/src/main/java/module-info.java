module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens org.example to javafx.fxml, com.google.gson;
    exports org.example;
    exports org.example.controllers.profControllers;
    opens org.example.controllers.profControllers to javafx.fxml, com.google.gson;
    exports org.example.controllers.alunoControllers;
    opens org.example.controllers.alunoControllers to javafx.fxml, com.google.gson;
    exports org.example.entity.profEntity;
    opens org.example.entity.profEntity to javafx.fxml, com.google.gson;
}

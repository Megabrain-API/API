module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens org.example to javafx.fxml;
    exports org.example;
    exports org.example.profControllers;
    opens org.example.profControllers to javafx.fxml;
    exports org.example.alunoControllers;
    opens org.example.alunoControllers to javafx.fxml;
}

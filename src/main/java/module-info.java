module com.example.splayerbeta {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires com.almasb.fxgl.all;

    opens com.example.splayerbeta to javafx.fxml;
    exports com.example.splayerbeta;
}
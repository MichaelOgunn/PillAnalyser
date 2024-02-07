module com.example.pillanalyser {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.pillanalyser to javafx.fxml;
    exports com.example.pillanalyser;
}
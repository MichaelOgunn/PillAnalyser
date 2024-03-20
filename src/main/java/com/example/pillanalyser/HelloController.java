package com.example.pillanalyser;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HelloController {
    @FXML
    private Button getStarted;

    @FXML
    protected void onButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("menu - Copy.fxml"));
            Parent root = loader.load();
            Scene nextScene = new Scene(root);


            Stage stage = (Stage) getStarted.getScene().getWindow();
            stage.setScene(nextScene);
            stage.show();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
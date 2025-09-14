package com.caslanqa.assetsmanagement;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Locale.setDefault(new Locale("tr", "TR"));
        Parent root = FXMLLoader.load(getClass().getResource("/com/caslanqa/assetsmanagement/login.fxml"));
        stage.setTitle("Assets Management - Login");
        stage.setScene(new Scene(root, 1200, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
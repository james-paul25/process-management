package com.processmanagement;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(getClass().getResource("/com/processmanagement/main.fxml")));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1150, 740);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/com/processmanagement/style.css"))
                        .toExternalForm());

        primaryStage.setTitle("Process Management Simulator");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(980);
        primaryStage.setMinHeight(650);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
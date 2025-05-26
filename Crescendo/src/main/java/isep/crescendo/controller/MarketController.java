package isep.crescendo.controller;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MarketController extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Minha Aplicação");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

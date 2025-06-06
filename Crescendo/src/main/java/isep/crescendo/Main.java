package isep.crescendo;

import isep.crescendo.model.*;
import isep.crescendo.Repository.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL fxmlLocation = getClass().getResource("/isep/crescendo/login-view.fxml");
        System.out.println(fxmlLocation);

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Scene scene = new Scene(fxmlLoader.load(), 1350, 900);

        URL cssLocation = getClass().getResource("/isep/crescendo/styles/login.css");
        if (cssLocation != null) {
            scene.getStylesheets().add(cssLocation.toExternalForm());
        } else {
            System.err.println("Arquivo CSS não encontrado!");
        }

        stage.setTitle("Login");
      
        stage.setScene(scene);
        stage.show();
        new OrdemRepo();
        new TransacaoRepo();
    }

    public static void main(String[] args) {

        launch(args);
    }
}


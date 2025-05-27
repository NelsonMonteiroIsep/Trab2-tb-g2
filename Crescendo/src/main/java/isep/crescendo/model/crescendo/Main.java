package isep.crescendo.model.crescendo;

import isep.crescendo.model.crescendo.model.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

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
        new UserRepository();
        new CarteiraRepository();
        new TransacaoRepository();
        new CriptomoedaRepository();
        new HistoricoValorRepository();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


package isep.crescendo.model.crescendo.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneSwitcher {

    public static void switchScene(String fxmlPath, String cssPath, String windowTitle, Control anyControlInCurrentScene) {
        try {
            URL fxmlLocation = SceneSwitcher.class.getResource(fxmlPath);
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Parent root = fxmlLoader.load();

            Scene scene = new Scene(root);

            if (cssPath != null) {
                URL cssLocation = SceneSwitcher.class.getResource(cssPath);
                if (cssLocation != null) {
                    scene.getStylesheets().add(cssLocation.toExternalForm());
                } else {
                    System.err.println("CSS não encontrado: " + cssPath);
                }
            }

            Stage stage = (Stage) anyControlInCurrentScene.getScene().getWindow();

            double width = stage.getWidth();
            double height = stage.getHeight();

            stage.setTitle(windowTitle);
            stage.setScene(scene);
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setResizable(true);
            stage.show();

        } catch (IOException e) {
            System.err.println("Erro ao mudar de ecrã.");
            e.printStackTrace();
        }
    }
}

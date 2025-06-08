// isep.crescendo.Main.java

package isep.crescendo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.scene.image.Image; // Importe esta classe para trabalhar com imagens

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            // Carrega o FXML da MainView
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/isep/crescendo/view/MainView.fxml"));

            Parent root = loader.load();

            // Cria uma nova Scene com o root carregado
            Scene scene = new Scene(root);

            // Adiciona o stylesheet CSS à cena
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/style.css")).toExternalForm());

            // Define a Scene no Stage principal
            primaryStage.setScene(scene);

            // Define o título da janela
            primaryStage.setTitle("Crescendo Market");

            // *** NOVAS LINHAS: Definir o ícone da aplicação ***

            // 1. Carrega a imagem do logo a partir dos recursos
            // O caminho é relativo ao pacote (isep.crescendo)
            Image applicationIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/isep/crescendo/images/crescendo-logo.png")));
            // 2. Adiciona o ícone ao Stage
            primaryStage.getIcons().add(applicationIcon);

            // *** FIM DAS NOVAS LINHAS ***

            // Maximiza a janela
            primaryStage.setMaximized(true);

            // Definir o tamanho mínimo da janela
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double minWidth = bounds.getWidth() * (2.0 / 3.0);
            double minHeight = bounds.getHeight() * (2.0 / 3.0);
            primaryStage.setMinWidth(minWidth);
            primaryStage.setMinHeight(minHeight);

            // Mostra o Stage
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao carregar MainView.fxml ou recursos: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
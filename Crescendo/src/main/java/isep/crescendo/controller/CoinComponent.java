package isep.crescendo.controller;

import com.fasterxml.jackson.core.json.DupDetector;
import isep.crescendo.model.Criptomoeda;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.awt.event.MouseEvent;
import java.io.IOException;


public class CoinComponent {
    @FXML private Label nomeLabel;
    @FXML private Label simboloLabel;
    @FXML private Label descricaoLabel;
    @FXML private ImageView imagemView;
    private Criptomoeda moeda;

    @FXML
    private Label coinLabel;

    String name;

        public void setCoinName(String name) {
            coinLabel.setText(name);
        }
    @FXML
    public void handleClick(javafx.scene.input.MouseEvent mouseEvent) {
        try {

            Parent newPage = FXMLLoader.load(getClass().getResource("/isep/crescendo/coin-view.fxml"));

            Scene newScene = new Scene(newPage);

            newScene.getStylesheets().add(getClass().getResource("/isep/crescendo/styles/login.css").toExternalForm());

            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();

            // 5. Define a nova Scene no Stage
            stage.setScene(newScene);

            // 6. Mostra o Stage (atualiza a janela)
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao carregar a cena ou o CSS.");
            System.err.println("Verifique os caminhos do FXML e do CSS.");
        }
    }


    public void setCriptomoeda(Criptomoeda moeda) {
        this.moeda = moeda;

        nomeLabel.setText(moeda.getNome());
        simboloLabel.setText(moeda.getSimbolo());
        descricaoLabel.setText(moeda.getDescricao());


        if (moeda.getImagemUrl() != null && !moeda.getImagemUrl().isEmpty()) {
            imagemView.setImage(new Image(moeda.getImagemUrl(), true));
        }
    }

}


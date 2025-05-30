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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/isep/crescendo/coin-view.fxml"));
            Parent newPage = loader.load();

            // Passa a moeda selecionada para o CoinController
            CoinController controller = loader.getController();
            controller.setCriptomoeda(this.moeda);

            // Troca a cena
            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(newPage));
            stage.show();


        } catch (IOException e) {
            e.printStackTrace(); // Ou melhor: logar o erro
        }
    }


    public void setCriptomoeda(Criptomoeda moeda) {
        this.moeda = moeda;

        nomeLabel.setText(moeda.getNome());
        simboloLabel.setText(moeda.getSimbolo());
        descricaoLabel.setText(moeda.getDescricao());


        String imagemUrl = moeda.getImagemUrl();

        if (imagemUrl != null && !imagemUrl.trim().isEmpty()) {
            try {
                Image imagem = new Image(imagemUrl, true);
                imagemView.setImage(imagem);
            } catch (IllegalArgumentException e) {
                System.err.println("Imagem inválida, não será carregada: " + imagemUrl);
                imagemView.setImage(null); // ou simplesmente ignora
            }
        } else {
            imagemView.setImage(null);
        }
    }

}


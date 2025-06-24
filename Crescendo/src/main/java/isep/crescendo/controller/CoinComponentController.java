package isep.crescendo.controller;

import isep.crescendo.model.Criptomoeda;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CoinComponentController {

    @FXML private Label nomeLabel;
    @FXML private Label simboloLabel;
    @FXML private ImageView imagemView;
    @FXML private Label cryptoPriceLabel; // Ensure this matches fx:id in FXML

    public void setCriptomoeda(Criptomoeda moeda) {
        if (moeda != null) {
            nomeLabel.setText(moeda.getNome());
            simboloLabel.setText(moeda.getSimbolo());
            // Coloca o preco

            String imagemUrl = moeda.getImagemUrl();
            if (imagemUrl != null && !imagemUrl.trim().isEmpty()) {
                try {
                    Image imagem = new Image(imagemUrl, true); // carrega no background
                    imagemView.setImage(imagem);
                } catch (IllegalArgumentException e) {
                    System.err.println("Imagem inválida, não será carregada: " + imagemUrl);
                    imagemView.setImage(null);
                }
            } else {
                imagemView.setImage(null);
            }
        } else {
            // Limpas os campos
            nomeLabel.setText("");
            simboloLabel.setText("");
            imagemView.setImage(null);
        }
    }
}

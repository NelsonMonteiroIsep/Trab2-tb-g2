package isep.crescendo.controller;

import com.fasterxml.jackson.core.json.DupDetector;
import isep.crescendo.Repository.HistoricoValorRepository;
import isep.crescendo.model.Criptomoeda;
import isep.crescendo.model.HistoricoValor;
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
import java.util.Comparator;
import java.util.List;


public class CoinComponent {
    @FXML private Label nomeLabel;
    @FXML private Label simboloLabel;
    @FXML private Label descricaoLabel;
    @FXML private Label precoLabel;
    @FXML private Label variacaoLabel;
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


            Scene newScene = new Scene(newPage);


            newScene.getStylesheets().add(getClass().getResource("/isep/crescendo/styles/login.css").toExternalForm());


            CoinController controller = loader.getController();


            controller.setCriptomoeda(this.moeda);


            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();


            stage.setScene(newScene);


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
                imagemView.setImage(new Image(imagemUrl, true));
            } catch (IllegalArgumentException e) {
                System.err.println("Imagem inválida: " + imagemUrl);
                imagemView.setImage(null);
            }
        } else {
            imagemView.setImage(null);
        }

        // Lógica para mostrar preço e variação
        HistoricoValorRepository historicoRepo = new HistoricoValorRepository();
        List<HistoricoValor> historico = historicoRepo.listarPorCripto(moeda.getId());

        if (historico.size() >= 1) {
            historico.sort(Comparator.comparing(HistoricoValor::getData)); // garante ordem cronológica

            double ultimoValor = historico.get(historico.size() - 1).getValor();
            precoLabel.setText(String.format("%.2f €", ultimoValor));

            if (historico.size() >= 2) {
                double penultimo = historico.get(historico.size() - 2).getValor();
                if (penultimo != 0) {
                    double variacao = ((ultimoValor - penultimo) / penultimo) * 100;
                    variacaoLabel.setText(String.format("%+.2f%%", variacao));
                    variacaoLabel.setStyle("-fx-text-fill: " + (variacao >= 0 ? "#4CAF50" : "#F44336"));
                } else {
                    variacaoLabel.setText("0.00%");
                    variacaoLabel.setStyle("-fx-text-fill: #999999;");
                }
            } else {
                variacaoLabel.setText("N/A");
                variacaoLabel.setStyle("-fx-text-fill: #999999;");
            }
        } else {
            precoLabel.setText("Sem dados");
            variacaoLabel.setText("N/A");
            variacaoLabel.setStyle("-fx-text-fill: #999999;");
        }
    }


}


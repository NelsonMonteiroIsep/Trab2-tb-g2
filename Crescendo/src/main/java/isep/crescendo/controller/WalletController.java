package isep.crescendo.controller;

import isep.crescendo.Repository.CarteiraRepository;
import isep.crescendo.model.MoedaSaldo;
import isep.crescendo.model.User;
import isep.crescendo.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import isep.crescendo.model.Carteira;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.event.ActionEvent;
import java.time.format.DateTimeFormatter;


public class WalletController {
    @FXML
    public Label saldoLabel;
    @FXML
    private Label userNameLabel;

    private User loggedInUser;
    @FXML
    private ListView<MoedaSaldo> cryptoListView;
    @FXML
    private Label saldoInvestidoLabel;
    @FXML
    private void initialize() {
        loggedInUser = SessionManager.getCurrentUser();
        double saldo = CarteiraRepository.calcularSaldoInvestido(loggedInUser.getId());
        if (loggedInUser != null) {
            userNameLabel.setText("Bem-vindo, " + loggedInUser.getNome());
            atualizarSaldoLabel();
            int carteiraId = SessionManager.getCurrentUser().getId();
            configurarCellFactory();
            cryptoListView.setItems(CarteiraRepository.listarMoedasCarteira(carteiraId));

            saldoInvestidoLabel.setText(String.format(" %.2f €", saldo));
        } else {
            userNameLabel.setText("Bem-vindo, visitante!");
        }





    }

    private void configurarCellFactory() {
        cryptoListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(MoedaSaldo moeda, boolean empty) {
                super.updateItem(moeda, empty);
                if (empty || moeda == null) {
                    setGraphic(null);
                    return;
                }

                // Imagem
                ImageView imagem = new ImageView(carregarImagemCripto(moeda.getImagemUrl()));
                imagem.setFitWidth(40);
                imagem.setFitHeight(40);
                imagem.setPreserveRatio(true);

                // Labels horizontais
                Label nomeLabel = new Label("Nome: " + moeda.getNome());
                Label quantidadeLabel = new Label("Qtd: " + String.format("%.6f", moeda.getQuantidade()));
                Label precoMedioLabel = new Label("Média: € " + String.format("%.4f", moeda.getPrecoMedioCompra()));

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String dataUltCompraStr = moeda.getDataUltimaCompra() != null
                        ? moeda.getDataUltimaCompra().toLocalDateTime().format(formatter)
                        : "N/A";
                Label ultimaCompraLabel = new Label("Última: " + dataUltCompraStr);

                // Estilo (opcional)
                for (Label lbl : new Label[]{nomeLabel, quantidadeLabel, precoMedioLabel, ultimaCompraLabel}) {
                    lbl.setStyle("-fx-text-fill: white;");
                }

                // Agrupar os dados horizontalmente
                HBox infoBox = new HBox(20, nomeLabel, quantidadeLabel, precoMedioLabel, ultimaCompraLabel);
                infoBox.setAlignment(Pos.CENTER_LEFT);

                // Linha final com imagem + info
                HBox linha = new HBox(15, imagem, infoBox);
                linha.setPadding(new Insets(5));
                linha.setAlignment(Pos.CENTER_LEFT);
                linha.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 8;");

                setGraphic(linha);
            }
        });
    }

    private void atualizarSaldoLabel() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            Carteira carteira = CarteiraRepository.procurarPorUserId(currentUser.getId());

            if (carteira != null) {
                saldoLabel.setText(String.format("%.2f €", carteira.getSaldo()));
            } else {
                saldoLabel.setText("0.00 €");
            }
        }
    }

    private Image carregarImagemCripto(String imagemUrl) {
        try {
            // Corrige barras invertidas para barras normais
            String caminhoCorrigido = imagemUrl.replace("\\", "/");
            if (!caminhoCorrigido.startsWith("/")) {
                caminhoCorrigido = "/" + caminhoCorrigido;
            }
            return new Image(getClass().getResourceAsStream(caminhoCorrigido));
        } catch (Exception e) {
            return new Image(getClass().getResourceAsStream("/isep/crescendo/images/default.png"));
        }
    }
}

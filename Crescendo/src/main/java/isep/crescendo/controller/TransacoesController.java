package isep.crescendo.controller;

import isep.crescendo.Repository.OrdemRepo;
import isep.crescendo.Repository.TransacaoRepository;
import isep.crescendo.model.Ordem;
import isep.crescendo.model.OrdemResumo;
import isep.crescendo.model.Transacao;
import isep.crescendo.model.User;
import isep.crescendo.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransacoesController {

    @FXML
    private ListView<OrdemResumo> ordensListView;

    private OrdemRepo ordemRepo = new OrdemRepo();
    private TransacaoRepository transacaoRepo = new TransacaoRepository();

    @FXML
    private void initialize() {
        int carteiraId = SessionManager.getCurrentUser().getId();

        List<OrdemResumo> ordensResumo = ordemRepo.listarOrdensComResumo(carteiraId);
        Map<Integer, List<Transacao>> transacoesPorOrdem = transacaoRepo.listarTodasTransacoesDaCarteira(carteiraId);

        // Atribuir as transações já carregadas
        for (OrdemResumo resumo : ordensResumo) {
            List<Transacao> trans = transacoesPorOrdem.getOrDefault(resumo.getOrdem().getId(), new ArrayList<>());
            resumo.setTransacoes(trans);
        }

        ordensListView.setItems(FXCollections.observableArrayList(ordensResumo));

        ordensListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(OrdemResumo resumo, boolean empty) {
                super.updateItem(resumo, empty);
                if (empty || resumo == null) {
                    setGraphic(null);
                    return;
                }

                Ordem ordem = resumo.getOrdem();

                Label tipoLabel = new Label("Tipo: " + ordem.getTipo());
                Label idLabel = new Label("ID: " + ordem.getId());
                Label qtdLabel = new Label("Qtd Executada: " + String.format("%.6f", resumo.getQuantidadeExecutada()));
                Label mediaLabel = new Label("Valor Médio: € " + String.format("%.4f", resumo.getValorMedio()));
                Label dataLabel = new Label("Data: " + ordem.getDataHora().toLocalDate().toString());

                for (Label lbl : new Label[]{tipoLabel, idLabel, qtdLabel, mediaLabel, dataLabel}) {
                    lbl.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                }

                VBox transacoesBox = new VBox();
                transacoesBox.setSpacing(3);
                for (Transacao t : resumo.getTransacoes()) {
                    Label transLabel = new Label("Transação " + t.getId() + " | Qtd: " + String.format("%.6f", t.getQuantidade()) +
                            " | Preço: € " + String.format("%.4f", t.getValorUnitario()) +
                            " | Data: " + t.getDataHora().toLocalDate());
                    transLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");
                    transacoesBox.getChildren().add(transLabel);
                }

                VBox content = new VBox(tipoLabel, idLabel, qtdLabel, mediaLabel, dataLabel, transacoesBox);
                content.setSpacing(5);
                content.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 8; -fx-padding: 10;");

                setGraphic(content);
            }
        });
    }
}

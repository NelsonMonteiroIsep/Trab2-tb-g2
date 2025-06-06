package isep.crescendo.controller;

import isep.crescendo.Repository.CarteiraRepository;
import isep.crescendo.Repository.OrdemRepo;
import isep.crescendo.Repository.TransacaoRepository;
import isep.crescendo.model.Ordem;
import isep.crescendo.model.OrdemResumo;
import isep.crescendo.model.Transacao;
import isep.crescendo.model.User;
import isep.crescendo.util.OrdemService;
import isep.crescendo.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransacoesController {

    @FXML
    private ListView<OrdemResumo> ordensListView;
    @FXML
    private ComboBox<String> estadoComboBox;
    @FXML
    private Button exportCsvButton;
    @FXML
    private DatePicker dataPicker;

    private OrdemRepo ordemRepo = new OrdemRepo();
    private CarteiraRepository carteiraRepo = new CarteiraRepository();
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

        // Aqui o FilteredList
        ObservableList<OrdemResumo> ordensResumoList = FXCollections.observableArrayList(ordensResumo);
        FilteredList<OrdemResumo> filteredList = new FilteredList<>(ordensResumoList, p -> true);
        ordensListView.setItems(filteredList);

        // Inicia com "Todos" selecionado
        estadoComboBox.setValue("Todos");

        // Listeners para filtros
        estadoComboBox.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltro(filteredList));
        dataPicker.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltro(filteredList));

        // Restante código da ListCell (o teu updateItem normal aqui)
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
                Label estadoLabel = new Label("Estado: " + ordem.getStatus());
                estadoLabel.setStyle("-fx-text-fill: lightgreen; -fx-font-size: 14px;");

                Label idLabel = new Label("ID: " + ordem.getId());
                Label qtdLabel;

                double quantidadeExecutada = resumo.getQuantidadeExecutada();
                double quantidadeRestante = ordem.getQuantidade();
                double quantidadeTotal;

                if (ordem.getStatus().equalsIgnoreCase("executada")) {
                    quantidadeTotal = quantidadeExecutada;
                    quantidadeRestante = 0.0; // se executada, o restante deve aparecer como 0
                } else {
                    quantidadeTotal = quantidadeExecutada + quantidadeRestante;
                }

                if (ordem.getTipo().equalsIgnoreCase("venda")) {
                    if (ordem.getStatus().equalsIgnoreCase("executada")) {
                        qtdLabel = new Label("Qtd Total: " + String.format("%.6f", quantidadeTotal)
                                + " (Vendida)");
                    } else {
                        qtdLabel = new Label("Qtd Total: " + String.format("%.6f", quantidadeTotal)
                                + " | Vendida: " + String.format("%.6f", quantidadeExecutada)
                                + " | Restante: " + String.format("%.6f", quantidadeRestante));
                    }
                } else { // compra
                    if (ordem.getStatus().equalsIgnoreCase("executada")) {
                        qtdLabel = new Label("Qtd Total: " + String.format("%.6f", quantidadeTotal)
                                + " (Comprada)");
                    } else {
                        qtdLabel = new Label("Qtd Total: " + String.format("%.6f", quantidadeTotal)
                                + " | Comprada: " + String.format("%.6f", quantidadeExecutada)
                                + " | Restante: " + String.format("%.6f", quantidadeRestante));
                    }
                }

                Label mediaLabel;

                if (ordem.getTipo().equalsIgnoreCase("venda")) {
                    mediaLabel = new Label("Valor (Venda): € " + String.format("%.4f", ordem.getValor()));
                } else {
                    mediaLabel = new Label("Valor Médio: € " + String.format("%.4f", resumo.getValorMedio()));
                }

                Label dataLabel = new Label("Data: " + ordem.getDataHora().toLocalDate().toString());

                for (Label lbl : new Label[]{tipoLabel, idLabel, qtdLabel, mediaLabel, dataLabel}) {
                    lbl.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                }



                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                HBox topBox;

                if (ordem.getStatus().equalsIgnoreCase("pendente")) {
                    Button cancelarButton = new Button("Cancelar");
                    cancelarButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5;");

                    cancelarButton.setOnAction(e -> {
                        OrdemService ordemService = new OrdemService();
                        ordemService.cancelarOrdem(ordem);
                        atualizarListaOrdens();
                    });

                    topBox = new HBox(tipoLabel, spacer, estadoLabel, cancelarButton);
                } else {
                    topBox = new HBox(tipoLabel, spacer, estadoLabel);
                }

                topBox.setSpacing(10);

                // Transações
                VBox transacoesBox = new VBox();
                transacoesBox.setSpacing(3);
                for (Transacao t : resumo.getTransacoes()) {
                    Label transLabel = new Label("Transação " + t.getId() + " | Qtd: " + String.format("%.6f", t.getQuantidade()) +
                            " | Preço: € " + String.format("%.4f", t.getValorUnitario()) +
                            " | Data: " + t.getDataHora().toLocalDate());
                    transLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");
                    transacoesBox.getChildren().add(transLabel);
                }

                // VBox final
                VBox content = new VBox(topBox, idLabel, qtdLabel, mediaLabel, dataLabel, transacoesBox);
                content.setSpacing(5);
                content.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 8; -fx-padding: 10;");

                setGraphic(content);
            }
        });
        exportCsvButton.setOnAction(e -> exportarParaCsv());
    }


    private void aplicarFiltro(FilteredList<OrdemResumo> filteredList) {
        String estadoSelecionado = estadoComboBox.getValue();
        LocalDate dataSelecionada = dataPicker.getValue();

        filteredList.setPredicate(ordemResumo -> {
            // Filtro por estado
            boolean estadoOk = (estadoSelecionado == null || estadoSelecionado.equals("Todos") ||
                    ordemResumo.getOrdem().getStatus().equalsIgnoreCase(estadoSelecionado));

            // Filtro por data (se usar DatePicker de data específica)
            boolean dataOk = (dataSelecionada == null ||
                    ordemResumo.getOrdem().getDataHora().toLocalDate().isEqual(dataSelecionada));

            return estadoOk && dataOk;
        });
    }

    private void atualizarListaOrdens() {
        int carteiraId = SessionManager.getCurrentUser().getId();

        List<OrdemResumo> ordensResumo = ordemRepo.listarOrdensComResumo(carteiraId);
        Map<Integer, List<Transacao>> transacoesPorOrdem = transacaoRepo.listarTodasTransacoesDaCarteira(carteiraId);

        for (OrdemResumo resumo : ordensResumo) {
            List<Transacao> trans = transacoesPorOrdem.getOrDefault(resumo.getOrdem().getId(), new ArrayList<>());
            resumo.setTransacoes(trans);
        }

        ObservableList<OrdemResumo> ordensResumoList = FXCollections.observableArrayList(ordensResumo);
        FilteredList<OrdemResumo> filteredList = new FilteredList<>(ordensResumoList, p -> true);
        ordensListView.setItems(filteredList);

        estadoComboBox.setValue("Todos");
        estadoComboBox.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltro(filteredList));
        dataPicker.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltro(filteredList));
    }

    private void exportarParaCsv() {
        List<OrdemResumo> listaParaExportar = ordensListView.getItems();

        if (listaParaExportar.isEmpty()) {
            System.out.println("Nenhuma ordem para exportar.");
            return;
        }

        // Caminho do ficheiro — para teste podes pôr fixo, depois podes fazer com FileChooser
        String nomeFicheiro = "export_ordens.csv";

        try (PrintWriter writer = new PrintWriter(nomeFicheiro)) {

            // Cabeçalho
            writer.println("ID;Tipo;Estado;Qtd Total;Qtd Executada;Qtd Restante;Valor Médio/Valor Venda;Data");

            for (OrdemResumo resumo : listaParaExportar) {
                Ordem ordem = resumo.getOrdem();

                double quantidadeExecutada = resumo.getQuantidadeExecutada();
                double quantidadeRestante = ordem.getQuantidade();
                double quantidadeTotal;

                if (ordem.getStatus().equalsIgnoreCase("executada")) {
                    quantidadeTotal = quantidadeExecutada;
                    quantidadeRestante = 0.0;
                } else {
                    quantidadeTotal = quantidadeExecutada + quantidadeRestante;
                }

                String valorStr = ordem.getTipo().equalsIgnoreCase("venda") ?
                        String.format("%.4f", ordem.getValor()) :
                        String.format("%.4f", resumo.getValorMedio());

                writer.println(ordem.getId() + ";" +
                        ordem.getTipo() + ";" +
                        ordem.getStatus() + ";" +
                        String.format("%.6f", quantidadeTotal) + ";" +
                        String.format("%.6f", quantidadeExecutada) + ";" +
                        String.format("%.6f", quantidadeRestante) + ";" +
                        valorStr + ";" +
                        ordem.getDataHora().toLocalDate()
                );
            }

            System.out.println("Exportação CSV concluída: " + nomeFicheiro);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao exportar para CSV.");
        }
    }

}

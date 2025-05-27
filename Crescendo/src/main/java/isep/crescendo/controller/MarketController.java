package isep.crescendo.controller;

import isep.crescendo.Repository.Carteira;
import isep.crescendo.Repository.Criptomoeda;
import isep.crescendo.Repository.HistoricoValor;
import isep.crescendo.model.*;
import isep.crescendo.util.SceneSwitcher;
import isep.crescendo.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MarketController implements Initializable {
    @FXML
    private Label userNameLabel;

    private User loggedInUser;

    @FXML
    private Label saldoLabel;

    @FXML
    private TextField saldoField;

    private double saldo = 0.0;

    @FXML private ComboBox<String> moedaSelecionadaBox;
    @FXML private LineChart<String, Number> lineChart;
    @FXML private CategoryAxis eixoX;
    @FXML private NumberAxis eixoY;
    @FXML private Label infoLabel;
    @FXML private TextField campoPesquisaMoeda;
    private ContextMenu sugestoesPopup = new ContextMenu();
    private final Criptomoeda criptoRepo = new Criptomoeda();
    private final HistoricoValor historicoRepo = new HistoricoValor();
    @FXML private ComboBox<String> intervaloSelecionadoBox;

    private isep.crescendo.model.Criptomoeda criptoSelecionada;
    @FXML private ComboBox<String> periodoSelecionadoBox;
    @FXML private ImageView coinLogo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loggedInUser = SessionManager.getCurrentUser();
        intervaloSelecionadoBox.getItems().addAll("Minutos", "Horas", "Dias", "Meses", "Anos");
        intervaloSelecionadoBox.setValue("Horas");
        periodoSelecionadoBox.getItems().addAll("Último dia", "Última semana", "Último mês", "Último ano");
        periodoSelecionadoBox.setValue("Última semana");


        if (loggedInUser != null) {
            userNameLabel.setText("Bem-vindo, " + loggedInUser.getNome());
        } else {
            userNameLabel.setText("Bem-vindo, visitante!");
        }

        atualizarSaldoLabel();




        if (saldoLabel != null) {
            saldoLabel.setText(String.format("%.2f €", isep.crescendo.model.Carteira.getSaldo()));
        }


        campoPesquisaMoeda.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() < 1) {
                sugestoesPopup.hide();
                return;
            }

            List<isep.crescendo.model.Criptomoeda> correspondencias = criptoRepo.getAllCriptomoedas().stream()
                    .filter(c -> c.getNome().toLowerCase().contains(newText.toLowerCase()) ||
                            c.getSimbolo().toLowerCase().contains(newText.toLowerCase()))
                    .toList();

            if (correspondencias.isEmpty()) {
                sugestoesPopup.hide();
                return;
            }

            List<MenuItem> items = correspondencias.stream().map(c -> {
                MenuItem item = new MenuItem(c.getNome() + " (" + c.getSimbolo() + ")");
                item.setOnAction(e -> {
                    campoPesquisaMoeda.setText(c.getSimbolo());
                    sugestoesPopup.hide();
                    criptoSelecionada = c;
                    atualizarGraficoComCripto(criptoSelecionada);
                });
                return item;
            }).toList();

            sugestoesPopup.getItems().setAll(items);
            if (!sugestoesPopup.isShowing()) {
                sugestoesPopup.show(campoPesquisaMoeda, Side.BOTTOM, 0, 0);
            }
        });

    }

    @FXML
    private void handleAdicionarSaldo() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Adicionar Saldo");
        dialog.setHeaderText(null);
        dialog.setContentText("Insira o valor a adicionar:");

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(valorStr -> {
            try {
                double valor = Double.parseDouble(valorStr);
                if (valor <= 0) {
                    mostrarErro("Insira um valor positivo.");
                    return;
                }

                int userId = SessionManager.getCurrentUser().getId(); // Assumindo SessionManager
                Carteira carteiraRepo = new Carteira();
                isep.crescendo.model.Carteira carteira = carteiraRepo.procurarPorUserId(userId);

                if (carteira != null) {
                    double novoSaldo = carteira.getSaldo() + valor;
                    carteiraRepo.atualizarSaldo(userId, novoSaldo);
                    atualizarSaldoLabel();
                } else {
                    mostrarErro("Carteira não encontrada para o utilizador.");
                }

            } catch (NumberFormatException e) {
                mostrarErro("Valor inválido. Insira um número.");
            }
        });
    }

    @FXML
    private void atualizarGrafico() {
        if (criptoSelecionada == null) {
            infoLabel.setText("Selecione ou pesquise uma criptomoeda.");
            return;
        }

        atualizarGraficoComCripto(criptoSelecionada);
    }

    @FXML


    private void atualizarSaldoLabel() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            isep.crescendo.model.Carteira carteira = Carteira.procurarPorUserId(currentUser.getId());

            if (carteira != null) {
                saldoLabel.setText(String.format("%.2f €", carteira.getSaldo()));
            } else {
                saldoLabel.setText("0.00 €");
            }
        }
    }
    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    private void handlePesquisarMoeda() {
        String termo = campoPesquisaMoeda.getText().trim().toLowerCase();
        if (termo.isEmpty()) {
            infoLabel.setText("Insira o nome ou símbolo da moeda.");
            return;
        }

        criptoSelecionada = criptoRepo.getAllCriptomoedas()
                .stream()
                .filter(c -> c.getNome().toLowerCase().contains(termo) || c.getSimbolo().equalsIgnoreCase(termo))
                .findFirst()
                .orElse(null);

        if (criptoSelecionada == null) {
            infoLabel.setText("Moeda não encontrada.");
            return;
        }

        atualizarGraficoComCripto(criptoSelecionada);
    }

    private void atualizarGraficoComCripto(isep.crescendo.model.Criptomoeda cripto) {
        List<isep.crescendo.model.HistoricoValor> historico = historicoRepo.listarPorCripto(cripto.getId());
        String intervalo = intervaloSelecionadoBox.getValue();
        String periodo = periodoSelecionadoBox.getValue();


        if (cripto != null && cripto.getImagemUrl() != null && !cripto.getImagemUrl().isBlank()) {
            try {
                coinLogo.setImage(new Image(cripto.getImagemUrl(), true));
            } catch (Exception e) {
                coinLogo.setImage(null);
            }
        } else {
            coinLogo.setImage(null);
        }
        // Filtro por período
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limite;
        switch (periodo) {
            case "Último dia" -> limite = agora.minusDays(1);
            case "Última semana" -> limite = agora.minusWeeks(1);
            case "Último mês" -> limite = agora.minusMonths(1);
            case "Último ano" -> limite = agora.minusYears(1);
            default -> limite = agora.minusWeeks(1);
        }

        List<isep.crescendo.model.HistoricoValor> filtrados = historico.stream()
                .filter(hv -> hv.getData().isAfter(limite))
                .toList();

        // Agrupamento por intervalo
        DateTimeFormatter formatter;
        switch (intervalo) {
            case "Minutos" -> formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
            case "Horas" -> formatter = DateTimeFormatter.ofPattern("dd/MM HH:00");
            case "Dias" -> formatter = DateTimeFormatter.ofPattern("dd/MM");
            case "Meses" -> formatter = DateTimeFormatter.ofPattern("MM/yyyy");
            case "Anos" -> formatter = DateTimeFormatter.ofPattern("yyyy");
            default -> formatter = DateTimeFormatter.ofPattern("dd/MM");
        }

        Map<String, Double> dadosFiltrados = new LinkedHashMap<>();
        for (isep.crescendo.model.HistoricoValor hv : filtrados) {
            String chave = hv.getData().format(formatter);
            dadosFiltrados.putIfAbsent(chave, hv.getValor()); // mantém o primeiro valor do grupo
        }

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName(cripto.getSimbolo() + " (" + intervalo + ", " + periodo + ")");

        for (Map.Entry<String, Double> entry : dadosFiltrados.entrySet()) {
            serie.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        lineChart.getData().clear();
        lineChart.getData().add(serie);

        if (!filtrados.isEmpty()) {
            double ultimo = filtrados.get(filtrados.size() - 1).getValor();
            double penultimo = filtrados.size() > 1 ? filtrados.get(filtrados.size() - 2).getValor() : ultimo;
            double variacao = ((ultimo - penultimo) / penultimo) * 100;
            infoLabel.setText(String.format("Último valor: %.2f € (%.2f%%)", ultimo, variacao));
        } else {
            infoLabel.setText("Sem dados no período selecionado.");
        }
    }



    }






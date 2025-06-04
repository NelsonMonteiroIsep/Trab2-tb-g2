// src/main/java/isep/crescendo/controller/CoinController.java
package isep.crescendo.controller;

import isep.crescendo.model.*;
import isep.crescendo.Repository.CriptomoedaRepository;
import isep.crescendo.Repository.CarteiraRepository;
import isep.crescendo.Repository.HistoricoValorRepository;
import isep.crescendo.util.SceneSwitcher;
import isep.crescendo.util.SessionManager;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class CoinController implements Initializable {

    @FXML public Label navBarAnyControl;
    @FXML private Label userNameLabel;
    @FXML private Label saldoLabel;
    @FXML private TextField saldoField;

    @FXML private LineChart<String, Number> lineChart;
    @FXML private CategoryAxis eixoX;
    @FXML private NumberAxis eixoY;
    @FXML private Label infoLabel;

    @FXML private TextField campoPesquisaMoeda;
    @FXML private ComboBox<String> intervaloSelecionadoBox;
    @FXML private ComboBox<String> periodoSelecionadoBox;

    @FXML private ImageView coinLogo;
    @FXML private Label nomeLabel;
    @FXML private Label simboloLabel;
    @FXML private Label descricaoLabel;

    private User loggedInUser;
    private Criptomoeda criptoSelecionada;

    private final CriptomoedaRepository criptoRepo = new CriptomoedaRepository();
    private final HistoricoValorRepository historicoRepo = new HistoricoValorRepository();

    private ContextMenu sugestoesPopup = new ContextMenu();

    private XYChart.Series<String, Number> dataSeries;

    private CriptoAlgoritmo criptoAlgoritmoAtivo;
    private Timeline graficoRealtimeUpdater;
    private static final int REALTIME_UPDATE_INTERVAL_MS = 1000;

    private List<HistoricoValor> allChartData = new ArrayList<>();
    // private int realtimeStartIndex = -1; // Não é estritamente necessário para uma única série

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loggedInUser = SessionManager.getCurrentUser();
        if (loggedInUser != null) {
            userNameLabel.setText("Bem-vindo, " + loggedInUser.getNome());
        } else {
            userNameLabel.setText("Bem-vindo, visitante!");
        }

        intervaloSelecionadoBox.getItems().addAll("Minutos", "Horas", "Dias", "Meses", "Anos");
        intervaloSelecionadoBox.setValue("Horas");
        periodoSelecionadoBox.getItems().addAll("Último dia", "Última semana", "Último mês", "Último ano");
        periodoSelecionadoBox.setValue("Última semana");

        intervaloSelecionadoBox.valueProperty().addListener((obs, oldVal, newVal) -> atualizarGrafico());
        periodoSelecionadoBox.valueProperty().addListener((obs, oldVal, newVal) -> atualizarGrafico());

        atualizarSaldoLabel();

        lineChart.setTitle("Histórico de Preço");
        eixoX.setLabel("Tempo");
        eixoY.setLabel("Valor (€)");
        lineChart.setCreateSymbols(false);
        lineChart.setAnimated(false);
        lineChart.setLegendVisible(false);

        // *** IMPORTANTE: A série DEVE ser inicializada e adicionada ao gráfico UMA VEZ. ***
        dataSeries = new XYChart.Series<>();
        dataSeries.setName("Preço da Criptomoeda");
        lineChart.getData().add(dataSeries); // Garante que a série está no gráfico

        campoPesquisaMoeda.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() < 1) {
                sugestoesPopup.hide();
                return;
            }

            List<Criptomoeda> correspondencias = criptoRepo.getAllCriptomoedas().stream()
                    .filter(c -> c.getNome().toLowerCase().contains(newText.toLowerCase()) ||
                            c.getSimbolo().toLowerCase().contains(newText.toLowerCase()))
                    .collect(Collectors.toList());

            if (correspondencias.isEmpty()) {
                sugestoesPopup.hide();
                return;
            }

            List<MenuItem> items = correspondencias.stream().map(c -> {
                MenuItem item = new MenuItem(c.getNome() + " (" + c.getSimbolo() + ")");
                item.setOnAction(e -> {
                    campoPesquisaMoeda.setText(c.getSimbolo());
                    sugestoesPopup.hide();
                    setCriptomoeda(c);
                });
                return item;
            }).collect(Collectors.toList());

            sugestoesPopup.getItems().setAll(items);
            if (!sugestoesPopup.isShowing()) {
                sugestoesPopup.show(campoPesquisaMoeda, Side.BOTTOM, 0, 0);
            }
        });
    }

    /**
     * Define a criptomoeda a ser exibida e inicia a simulação/gráfico.
     * @param moeda A criptomoeda selecionada.
     */
    public void setCriptomoeda(Criptomoeda moeda) {
        this.criptoSelecionada = moeda;
        disposeSimulationAndChart(); // Limpa e para tudo para uma nova moeda

        if (moeda != null) {
            nomeLabel.setText(moeda.getNome());
            simboloLabel.setText(moeda.getSimbolo());
            descricaoLabel.setText(moeda.getDescricao());

            if (moeda.getImagemUrl() != null && !moeda.getImagemUrl().isEmpty()) {
                try {
                    coinLogo.setImage(new Image(moeda.getImagemUrl(), true));
                } catch (IllegalArgumentException e) {
                    coinLogo.setImage(null);
                    System.err.println("Erro ao carregar imagem para " + moeda.getNome() + ": " + e.getMessage());
                }
            } else {
                coinLogo.setImage(null);
            }

            infoLabel.setText("Carregando dados...");

            // 1. Carregar histórico e iniciar simulação
            carregarHistoricoDoDBEIniciarSimulacao(moeda);

            // 2. Iniciar o atualizador do gráfico em tempo real
            graficoRealtimeUpdater = new Timeline(new KeyFrame(Duration.millis(REALTIME_UPDATE_INTERVAL_MS), e -> {
                updateRealtimeChart();
            }));
            graficoRealtimeUpdater.setCycleCount(Timeline.INDEFINITE);
            graficoRealtimeUpdater.play();

            System.out.println("CoinController: Atualizador de gráfico em tempo real iniciado para " + moeda.getNome());

        } else {
            nomeLabel.setText("");
            simboloLabel.setText("");
            descricaoLabel.setText("");
            coinLogo.setImage(null);
            infoLabel.setText("Nenhuma criptomoeda selecionada.");
        }
    }

    /**
     * Carrega o histórico de valores da base de dados, popula a lista allChartData
     * e inicia a simulação do CriptoAlgoritmo.
     * @param cripto A criptomoeda para a qual carregar o histórico.
     */
    private void carregarHistoricoDoDBEIniciarSimulacao(Criptomoeda cripto) {
        System.out.println("CoinController: carregarHistoricoDoDBEIniciarSimulacao para " + cripto.getNome());
        allChartData.clear(); // Limpa a lista de dados internos antes de carregar novo histórico

        List<HistoricoValor> historicoDB = historicoRepo.listarPorCripto(cripto.getId());
        System.out.println("CoinController: " + historicoDB.size() + " registros históricos encontrados para Cripto ID " + cripto.getId());

        String periodo = periodoSelecionadoBox.getValue();
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limite;

        switch (periodo) {
            case "Último dia" -> limite = agora.minusDays(1);
            case "Última semana" -> limite = agora.minusWeeks(1);
            case "Último mês" -> limite = agora.minusMonths(1);
            case "Último ano" -> limite = agora.minusYears(1);
            default -> limite = agora.minusWeeks(1);
        }

        List<HistoricoValor> filtrados = historicoDB.stream()
                .filter(hv -> hv.getData().isAfter(limite))
                .collect(Collectors.toList());

        System.out.println("CoinController: " + filtrados.size() + " registros históricos filtrados para o período '" + periodo + "'.");

        allChartData.addAll(filtrados); // Adiciona os dados filtrados à lista interna

        Double initialPriceForSimulation = null;
        if (!allChartData.isEmpty()) {
            initialPriceForSimulation = allChartData.get(allChartData.size() - 1).getValor();
            System.out.println("CoinController: Último valor do histórico DB para iniciar simulação: " + String.format("%.2f", initialPriceForSimulation));
        } else {
            System.out.println("CoinController: Histórico DB vazio. CriptoAlgoritmo usará valor padrão.");
        }

        // Inicia ou reinicia a simulação
        if (criptoAlgoritmoAtivo != null) {
            criptoAlgoritmoAtivo.stopSimulation();
        }
        criptoAlgoritmoAtivo = new CriptoAlgoritmo(cripto.getId(), initialPriceForSimulation);
        criptoAlgoritmoAtivo.startSimulation();
        System.out.println("CoinController: Simulação iniciada para: " + cripto.getNome() + " (ID: " + cripto.getId() + ")");

        // Garante que o gráfico é atualizado imediatamente após carregar o histórico
        updateChartDataSeries();
    }


    /**
     * Atualiza o gráfico em tempo real com os dados da simulação.
     * Adiciona novos pontos à única série de dados.
     */
    private void updateRealtimeChart() {
        if (criptoAlgoritmoAtivo == null || criptoSelecionada == null || dataSeries == null) {
            System.out.println("CoinController: updateRealtimeChart: Condições de execução não atendidas.");
            return;
        }

        List<HistoricoValor> historicoMemoriaSimulacao = criptoAlgoritmoAtivo.getHistoricoEmMemoria();
        if (historicoMemoriaSimulacao.isEmpty()) {
            // System.out.println("CoinController: updateRealtimeChart: Histórico de simulação em memória vazio.");
            return;
        }

        // Adiciona apenas os NOVOS pontos da simulação à lista `allChartData`
        // Para isso, precisamos saber quantos pontos já tínhamos e adicionar apenas os que vieram depois.
        // O `CriptoAlgoritmo` sempre retorna o histórico COMPLETO em memória.
        // Comparar o tamanho é a forma mais simples de adicionar apenas os novos.
        int currentSizeOfAllChartData = allChartData.size();
        int newPointsCount = historicoMemoriaSimulacao.size() - (currentSizeOfAllChartData - historicoRepo.listarPorCripto(criptoSelecionada.getId()).stream().filter(hv -> hv.getData().isAfter(LocalDateTime.now().minusWeeks(1))).collect(Collectors.toList()).size());

        // A maneira mais robusta é adicionar o último ponto que a simulação gerou
        // e que ainda não está na allChartData.
        HistoricoValor ultimoSimulado = historicoMemoriaSimulacao.get(historicoMemoriaSimulacao.size() - 1);
        if (allChartData.isEmpty() || !allChartData.get(allChartData.size() - 1).getData().equals(ultimoSimulado.getData())) {
            allChartData.add(ultimoSimulado);
            // System.out.println("CoinController: Adicionado novo ponto simulado: " + ultimoSimulado.getValor() + " em " + ultimoSimulado.getData());
        }

        // Mantém o número de pontos do gráfico gerenciável
        int MAX_TOTAL_POINTS = 500; // Por exemplo, 500 pontos no total (histórico + tempo real)
        while (allChartData.size() > MAX_TOTAL_POINTS) {
            allChartData.remove(0); // Remove o ponto mais antigo
        }

        updateChartDataSeries(); // Atualiza a série do gráfico com a nova lista de dados

        HistoricoValor ultimoPonto = allChartData.get(allChartData.size() - 1);
        infoLabel.setText(String.format("Último valor (em tempo real): %.2f €", ultimoPonto.getValor()));
    }

    /**
     * Popula a `dataSeries` do gráfico com os dados da lista `allChartData`.
     * Este método é chamado tanto para carregar o histórico quanto para atualizar em tempo real.
     */
    private void updateChartDataSeries() {
        if (dataSeries == null) {
            System.err.println("CoinController: dataSeries é nula. Gráfico não pode ser atualizado.");
            return;
        }
        dataSeries.getData().clear(); // Limpa os dados atuais da série
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss"); // Formato consistente

        if (allChartData.isEmpty()) {
            System.out.println("CoinController: allChartData está vazia, nenhum ponto para o gráfico.");
            return;
        }

        for (HistoricoValor hv : allChartData) {
            dataSeries.getData().add(new XYChart.Data<>(hv.getData().format(formatter), hv.getValor()));
        }
        System.out.println("CoinController: Gráfico atualizado com " + dataSeries.getData().size() + " pontos.");
    }


    @FXML
    private void atualizarGrafico() {
        System.out.println("CoinController: atualizarGrafico chamado.");
        if (criptoSelecionada == null) {
            infoLabel.setText("Selecione ou pesquise uma criptomoeda para atualizar o gráfico.");
            return;
        }
        // Ao atualizar as caixas de seleção, reinicializamos o gráfico e a simulação
        setCriptomoeda(criptoSelecionada);
    }


    @FXML
    private void handleLogout() {
        dispose();
        SessionManager.setCurrentUser(null);
        SceneSwitcher.switchScene("/isep/crescendo/login-view.fxml", "/isep/crescendo/styles/login.css", "Login", navBarAnyControl);
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

                int userId = SessionManager.getCurrentUser().getId();
                CarteiraRepository carteiraRepo = new CarteiraRepository();
                Carteira carteira = carteiraRepo.procurarPorUserId(userId);

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
            disposeSimulationAndChart();
            return;
        }

        criptoSelecionada = criptoRepo.getAllCriptomoedas()
                .stream()
                .filter(c -> c.getNome().toLowerCase().contains(termo) || c.getSimbolo().equalsIgnoreCase(termo))
                .findFirst()
                .orElse(null);

        if (criptoSelecionada == null) {
            infoLabel.setText("Moeda não encontrada.");
            disposeSimulationAndChart();
            return;
        }

        setCriptomoeda(criptoSelecionada);
    }

    private void disposeSimulationAndChart() {
        System.out.println("CoinController: disposeSimulationAndChart chamado. Parando simulação e limpando gráfico.");
        if (criptoAlgoritmoAtivo != null) {
            criptoAlgoritmoAtivo.stopSimulation();
            criptoAlgoritmoAtivo = null;
        }
        if (graficoRealtimeUpdater != null) {
            graficoRealtimeUpdater.stop();
            graficoRealtimeUpdater = null;
        }
        // É importante limpar os dados da série, não a série do gráfico.
        if (dataSeries != null) {
            dataSeries.getData().clear();
        }
        allChartData.clear(); // Limpa os dados internos
        // realtimeStartIndex = -1; // Reset o índice de início do tempo real, se usado

        nomeLabel.setText("");
        simboloLabel.setText("");
        descricaoLabel.setText("");
        coinLogo.setImage(null);
    }

    public void dispose() {
        disposeSimulationAndChart();
        System.out.println("CoinController: Timelines de simulação e atualização de gráfico parados.");
    }
}
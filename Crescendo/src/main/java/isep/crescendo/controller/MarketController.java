package isep.crescendo.controller;

import isep.crescendo.model.Criptomoeda;
import isep.crescendo.model.HistoricoValor;
import isep.crescendo.Repository.CriptomoedaRepository;
import isep.crescendo.Repository.HistoricoValorRepository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.beans.value.ChangeListener; // Import necessary for ChangeListener
import javafx.scene.image.Image;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
// Make sure your CyclePhase enum is in the same package or imported
// For example: import isep.crescendo.controller.CriptoAlgoritmo.CyclePhase;
// Or, if CyclePhase is its own file in the same package: import isep.crescendo.controller.CyclePhase;

public class MarketController implements Initializable {

    // --- FXML Injections (componentes do MarketView.fxml) ---
    @FXML
    private VBox marketVBox;
    @FXML
    private ImageView coinLogo;
    @FXML
    private Label nameLabel;
    @FXML
    private Label symbolLabel;
    @FXML
    private ComboBox<String> intervaloSelecionadoBox;
    @FXML
    private ComboBox<String> periodoSelecionadoBox;
    @FXML
    private LineChart<String, Number> lineChart;
    @FXML
    private CategoryAxis eixoX;
    @FXML
    private NumberAxis eixoY;
    @FXML
    private Label infoLabel;

    private Criptomoeda criptomoedaSelecionada;


    @FXML
    private VBox coinContainer;

    // --- Repositórios e Dados ---
    private CriptomoedaRepository criptoRepo;
    private HistoricoValorRepository historicoRepo;
    private CriptoAlgoritmo criptoAlgoritmoAtual; // The current algorithm instance for the selected coin
    private Criptomoeda moedaSelecionada; // The currently selected cryptocurrency

    // We need to keep a reference to the listener to be able to remove it later
    private ChangeListener<Number> currentPriceChangeListener;

    // --- Constructor ---
    public MarketController() {
        this.criptoRepo = new CriptomoedaRepository();
        this.historicoRepo = new HistoricoValorRepository();
        System.out.println("DEBUG MC: MarketController constructor called.");
    }

    // --- JavaFX Initialization Method ---
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("DEBUG MC: initialize method called.");
        // Ensure repositories are initialized
        if (this.criptoRepo == null) {
            this.criptoRepo = new CriptomoedaRepository();
        }
        if (this.historicoRepo == null) {
            this.historicoRepo = new HistoricoValorRepository();
        }

        // Initial chart setup
        lineChart.setCreateSymbols(false);
        eixoX.setLabel("Data/Hora");
        eixoY.setLabel("Preço (€)");
        lineChart.setTitle("Performance da Moeda");
        infoLabel.setText("Selecione uma moeda para ver o gráfico.");

        // Populate ComboBoxes
        ObservableList<String> intervalos = FXCollections.observableArrayList(
                "15 minutos", "30 minutos", "1 hora", "4 horas", "1 dia"
        );
        intervaloSelecionadoBox.setItems(intervalos);
        intervaloSelecionadoBox.getSelectionModel().selectFirst();

        ObservableList<String> periodos = FXCollections.observableArrayList(
                "24 horas", "7 dias", "30 dias", "6 meses", "1 ano"
        );
        periodoSelecionadoBox.setItems(periodos);
        periodoSelecionadoBox.getSelectionModel().selectFirst();
    }

    /**
     * Updates the market UI with the data of a selected cryptocurrency.
     * This method is called by MainController when a cryptocurrency is clicked in the RightBar.
     * @param cripto The cryptocurrency to display.
     * @param initialPrice The initial or current price of the cryptocurrency.
     */
    public void updateMarketUI(Criptomoeda cripto, double initialPrice) {
        this.moedaSelecionada = cripto;
        System.out.println("DEBUG MC: updateMarketUI called for: " + (cripto != null ? cripto.getNome() : "null coin"));

        if (criptoAlgoritmoAtual != null) {
            criptoAlgoritmoAtual.stopSimulation();
            if (this.currentPriceChangeListener != null) {
                criptoAlgoritmoAtual.currentPriceProperty().removeListener(this.currentPriceChangeListener);
                // CHANGE THIS LINE:
                System.out.println("DEBUG MC: Removed listener from previous algorithm (ID: " + criptoAlgoritmoAtual.getCriptoIdParaSimulacao() + ")");
                // TO THIS:
                // System.out.println("DEBUG MC: Removed listener from previous algorithm (ID: " + criptoAlgoritmoAtual.getCriptoIdParaSimulacao() + ")");
            }
        }

        if (moedaSelecionada == null) {
            infoLabel.setText("Nenhuma moeda selecionada.");
            clearChart();
            nameLabel.setText("Nome da Moeda");
            symbolLabel.setText("SIMB");
            coinLogo.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/isep/crescendo/images/default_coin.png")));
            return;
        }

        nameLabel.setText(moedaSelecionada.getNome());
        symbolLabel.setText(moedaSelecionada.getSimbolo());

        // Load coin image (universal version)
        if (moedaSelecionada.getImagemUrl() != null && !moedaSelecionada.getImagemUrl().isEmpty()) {
            try {
                coinLogo.setImage(new Image(moedaSelecionada.getImagemUrl(), true));
            } catch (Exception e) {
                System.err.println("ERROR MC: Failed to load image for " + moedaSelecionada.getNome() + ": " + e.getMessage());
                coinLogo.setImage(new Image(getClass().getResourceAsStream("/isep/crescendo/images/default_coin.png")));
            }
        } else {
            coinLogo.setImage(new Image(getClass().getResourceAsStream("/isep/crescendo/images/default_coin.png")));
        }

        // --- Initialize and Control the Simulation Algorithm ---
        // Create a new instance of YOUR CriptoAlgoritmo for the selected coin
        this.criptoAlgoritmoAtual = new CriptoAlgoritmo(moedaSelecionada.getId(), initialPrice);
        System.out.println("DEBUG MC: New CriptoAlgoritmo instance created for ID: " + moedaSelecionada.getId() + " with initial price: " + initialPrice);

        // Load initial chart data (from DB and algorithm's in-memory history)
        handlePeriodoSelection();

        // Connect the chart to the algorithm's price property for real-time updates
        connectChartToAlgorithmPrice();

        // Start the simulation AFTER everything is set up
        criptoAlgoritmoAtual.startSimulation();
    }

    /**
     * Loads initial chart data: first from DB (if any), then from the algorithm's in-memory history.
     * @param criptoId The ID of the cryptocurrency.
     */
    private void loadInitialChartData(int criptoId, LocalDateTime dataInicial) {
        clearChart(); // Clear old data

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(moedaSelecionada.getSimbolo() + " Preço");
        lineChart.getData().add(series); // Add the series to the chart

        // Decide o formato do eixo X com base no Intervalo
        String selectedInterval = intervaloSelecionadoBox.getSelectionModel().getSelectedItem();
        DateTimeFormatter formatter;


// --- NOVO BLOCO AQUI ---
        eixoX.setTickLabelRotation(0); // Opcional: manter as labels direitas

        if (selectedInterval == null) selectedInterval = "1 hora"; // fallback padrão

        switch (selectedInterval) {
            case "15 minutos":
                formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
                eixoX.setLabel("Hora (15 min)");
                break;
            case "30 minutos":
                formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
                eixoX.setLabel("Hora (30 min)");
                break;
            case "1 hora":
                formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
                eixoX.setLabel("Hora (1h)");
                break;
            case "4 horas":
                formatter = DateTimeFormatter.ofPattern("dd/MM");
                eixoX.setLabel("Dia (4h)");
                break;
            case "1 dia":
                formatter = DateTimeFormatter.ofPattern("dd/MM");
                eixoX.setLabel("Dia");
                break;
            default:
                formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
                eixoX.setLabel("Hora/Minuto");
                break;
        }
// --- FIM DO NOVO BLOCO ---

        // 1. Load from DB (filtered by period)
        List<HistoricoValor> historicoDB = historicoRepo.listarPorCripto(criptoId, dataInicial);
        if (historicoDB != null && !historicoDB.isEmpty()) {
            System.out.println("DEBUG MC: Loading " + historicoDB.size() + " points from DB history for chart.");
            // Agrupar por intervalo escolhido
            Map<LocalDateTime, List<Double>> bucketToValues = new TreeMap<>();

            for (HistoricoValor hv : historicoDB) {
                LocalDateTime bucketTime = truncateDateTime(hv.getData(), selectedInterval);

                bucketToValues.putIfAbsent(bucketTime, new ArrayList<>());
                bucketToValues.get(bucketTime).add(hv.getValor());
            }

// Agora para cada bucket, calcula média (ou último valor, se preferires)
            for (Map.Entry<LocalDateTime, List<Double>> entry : bucketToValues.entrySet()) {
                LocalDateTime bucketTime = entry.getKey();
                List<Double> valores = entry.getValue();

                // Cálculo da média (podes mudar para último valor se quiseres)
                double media = valores.stream().mapToDouble(Double::doubleValue).average().orElse(0);

                series.getData().add(new XYChart.Data<>(bucketTime.format(formatter), media));
            }
        } else {
            System.out.println("DEBUG MC: No history found in DB for Cripto ID " + criptoId + ".");
        }

        if (criptoAlgoritmoAtual != null) {
            List<HistoricoValor> historicoMemoria = criptoAlgoritmoAtual.getHistoricoEmMemoria();
            if (historicoMemoria != null && !historicoMemoria.isEmpty()) {
                System.out.println("DEBUG MC: Adding " + historicoMemoria.size() + " points from algorithm's IN-MEMORY history to chart.");

                // Descobre o timestamp do último ponto do BD
                LocalDateTime ultimaDataBD = dataInicial;
                if (!series.getData().isEmpty()) {
                    XYChart.Data<String, Number> ultimoPontoBD = series.getData().get(series.getData().size() - 1);
                    String ultimaDataStr = ultimoPontoBD.getXValue();
                    try {
                        ultimaDataBD = LocalDateTime.parse(ultimaDataStr, formatter);
                    } catch (Exception e) {
                        // fallback: mantém dataInicial
                    }
                }

                // Adiciona só os pontos da memória que são mais recentes que o último ponto do BD
                for (HistoricoValor hv : historicoMemoria) {
                    if (hv.getData().isAfter(ultimaDataBD)) {
                        series.getData().add(new XYChart.Data<>(hv.getData().format(formatter), hv.getValor()));
                    }
                }
            }
        }

        // Limit points on initial load
        int maxDataPointsOnLoad = 500;
        if (series.getData().size() > maxDataPointsOnLoad) {
            series.getData().remove(0, series.getData().size() - maxDataPointsOnLoad);
            System.out.println("DEBUG MC: Chart truncated to last " + maxDataPointsOnLoad + " points on initial load.");
        }

        // Update infoLabel
        if (series.getData().isEmpty()) {
            infoLabel.setText("Nenhum dado disponível para o período selecionado.");
        } else {
            infoLabel.setText("");
        }
    }

    /**
     * Connects the LineChart to the CriptoAlgoritmo's price property for real-time updates.
     */
    private void connectChartToAlgorithmPrice() {
        if (criptoAlgoritmoAtual == null || lineChart.getData().isEmpty()) {
            System.err.println("ERROR MC: Algorithm or chart series not initialized for connection.");
            return;
        }

        XYChart.Series<String, Number> series = (XYChart.Series<String, Number>) lineChart.getData().get(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // The listener is declared as a field and removed in updateMarketUI, so no need to remove here again.

        // Add a NEW listener for the CURRENT algorithm
        // This listener will be invoked every time currentPriceProperty changes in CriptoAlgoritmo
        this.currentPriceChangeListener = (obs, oldPrice, newPrice) -> {
            // This block runs on the JavaFX Application Thread, safe for UI updates.
            LocalDateTime currentTime = LocalDateTime.now();
            series.getData().add(new XYChart.Data<>(currentTime.format(formatter), newPrice.doubleValue()));

            // Limit points on the graph to prevent it from becoming too dense
            int maxDataPointsLive = 60; // Display the last 60 points in real-time
            if (series.getData().size() > maxDataPointsLive) {
                series.getData().remove(0);
            }
            // You can add more debug here to confirm real-time updates
            // System.out.println("DEBUG MC: Live point added: " + String.format("%.2f", newPrice.doubleValue()) + " at " + currentTime.format(formatter) + ". Total points: " + series.getData().size());
        };
        criptoAlgoritmoAtual.currentPriceProperty().addListener(this.currentPriceChangeListener);
        System.out.println("DEBUG MC: New ChangeListener ADDED to current algorithm's price property.");
    }

    /**
     * Clears the chart's data.
     */
    private void clearChart() {
        lineChart.getData().clear();
        System.out.println("DEBUG MC: Chart data cleared.");
    }

    // --- Methods for UI interactions (ComboBoxes) ---
    @FXML
    private void handleIntervaloSelection() {
        String selectedInterval = intervaloSelecionadoBox.getSelectionModel().getSelectedItem();
        System.out.println("DEBUG MC: Intervalo selected: " + selectedInterval);

        // Força reload com o mesmo período atual
        handlePeriodoSelection();
    }

    @FXML
    private void handlePeriodoSelection() {
        String selectedPeriod = periodoSelecionadoBox.getSelectionModel().getSelectedItem();
        System.out.println("DEBUG MC: Período selected: " + selectedPeriod);

        if (moedaSelecionada != null && criptoAlgoritmoAtual != null) {
            LocalDateTime dataInicial;

            switch (selectedPeriod) {
                case "24 horas":
                    dataInicial = LocalDateTime.now().minusHours(24);
                    break;
                case "7 dias":
                    dataInicial = LocalDateTime.now().minusDays(7);
                    break;
                case "30 dias":
                    dataInicial = LocalDateTime.now().minusDays(30);
                    break;
                case "6 meses":
                    dataInicial = LocalDateTime.now().minusMonths(6);
                    break;
                case "1 ano":
                    dataInicial = LocalDateTime.now().minusYears(1);
                    break;
                default:
                    dataInicial = LocalDateTime.now().minusDays(7);
                    break;
            }

            loadInitialChartData(moedaSelecionada.getId(), dataInicial);
        }
    }

    // --- Method to populate the active coins list (bottom section) ---
    public void updateActiveCoinsDisplay() {
        if (coinContainer != null) {
            coinContainer.getChildren().clear();
            System.out.println("DEBUG MC: Populating active coins display...");

            List<Criptomoeda> allCriptos = criptoRepo.getAllCriptomoedasAtivas();
            System.out.println("DEBUG MC: Found " + allCriptos.size() + " active cryptocurrencies.");

            for (Criptomoeda cripto : allCriptos) {
                HistoricoValor ultimoValor = historicoRepo.getUltimoValorPorCripto(cripto.getId());
                double initialPrice = (ultimoValor != null) ? ultimoValor.getValor() : 100.0;
                System.out.println("DEBUG MC: Creating CoinComponent for " + cripto.getNome() + " with price: " + initialPrice);

                CoinComponent coinComp = new CoinComponent(cripto, initialPrice);
                coinContainer.getChildren().add(coinComp);
            }
            System.out.println("DEBUG MC: " + allCriptos.size() + " CoinComponents added to coinContainer.");
        } else {
            System.err.println("ERROR MC: coinContainer is null. Cannot populate active coins display.");
        }
    }

    public void setCriptomoedaSelecionada(Criptomoeda moeda) {
        this.criptomoedaSelecionada = moeda;

        HistoricoValor ultimoValor = historicoRepo.getUltimoValorPorCripto(moeda.getId());
        double initialPrice = (ultimoValor != null) ? ultimoValor.getValor() : 100.0;

        updateMarketUI(moeda, initialPrice);
    }

    private LocalDateTime truncateDateTime(LocalDateTime dateTime, String selectedInterval) {
        switch (selectedInterval) {
            case "15 minutos":
                int minute15 = (dateTime.getMinute() / 15) * 15;
                return dateTime.withMinute(minute15).withSecond(0).withNano(0);

            case "30 minutos":
                int minute30 = (dateTime.getMinute() / 30) * 30;
                return dateTime.withMinute(minute30).withSecond(0).withNano(0);

            case "1 hora":
                return dateTime.withMinute(0).withSecond(0).withNano(0);

            case "4 horas":
                int hour4 = (dateTime.getHour() / 4) * 4;
                return dateTime.withHour(hour4).withMinute(0).withSecond(0).withNano(0);

            case "1 dia":
                return dateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);

            default:
                // fallback → 1 hora
                return dateTime.withMinute(0).withSecond(0).withNano(0);
        }
    }

}
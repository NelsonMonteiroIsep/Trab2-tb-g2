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

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

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

        // Load coin image (as it was)
        if (moedaSelecionada.getImagemUrl() != null && !moedaSelecionada.getImagemUrl().isEmpty()) {
            try (java.io.InputStream is = getClass().getResourceAsStream(moedaSelecionada.getImagemUrl())) {
                if (is != null) {
                    coinLogo.setImage(new javafx.scene.image.Image(is));
                } else {
                    System.err.println("ERROR MC: Image resource not found for " + moedaSelecionada.getNome() + " at path: " + moedaSelecionada.getImagemUrl());
                    coinLogo.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/isep/crescendo/images/default_coin.png")));
                }
            } catch (Exception e) {
                System.err.println("ERROR MC: Failed to load image for " + moedaSelecionada.getNome() + ": " + e.getMessage());
                coinLogo.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/isep/crescendo/images/default_coin.png")));
            }
        } else {
            coinLogo.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/isep/crescendo/images/default_coin.png")));
        }

        // --- Initialize and Control the Simulation Algorithm ---
        // Create a new instance of YOUR CriptoAlgoritmo for the selected coin
        this.criptoAlgoritmoAtual = new CriptoAlgoritmo(moedaSelecionada.getId(), initialPrice);
        System.out.println("DEBUG MC: New CriptoAlgoritmo instance created for ID: " + moedaSelecionada.getId() + " with initial price: " + initialPrice);

        // Load initial chart data (from DB and algorithm's in-memory history)
        loadInitialChartData(moedaSelecionada.getId());

        // Connect the chart to the algorithm's price property for real-time updates
        connectChartToAlgorithmPrice();

        // Start the simulation AFTER everything is set up
        criptoAlgoritmoAtual.startSimulation();
    }

    /**
     * Loads initial chart data: first from DB (if any), then from the algorithm's in-memory history.
     * @param criptoId The ID of the cryptocurrency.
     */
    private void loadInitialChartData(int criptoId) {
        clearChart(); // Clear old data

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(moedaSelecionada.getSimbolo() + " Preço");
        lineChart.getData().add(series); // Add the series to the chart

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss"); // More detailed format for debugging

        // 1. Try to load history from DB (for older/persisted data)
        List<HistoricoValor> historicoDB = historicoRepo.listarPorCripto(criptoId);
        if (historicoDB != null && !historicoDB.isEmpty()) {
            System.out.println("DEBUG MC: Loading " + historicoDB.size() + " points from DB history for chart.");
            for (HistoricoValor hv : historicoDB) {
                series.getData().add(new XYChart.Data<>(hv.getData().format(formatter), hv.getValor()));
            }
        } else {
            System.out.println("DEBUG MC: No history found in DB for Cripto ID " + criptoId + ".");
        }

        // 2. Add IN-MEMORY history from the algorithm (this is crucial for detailed real-time data)
        if (criptoAlgoritmoAtual != null) {
            List<HistoricoValor> historicoMemoria = criptoAlgoritmoAtual.getHistoricoEmMemoria();
            if (historicoMemoria != null && !historicoMemoria.isEmpty()) {
                System.out.println("DEBUG MC: Adding " + historicoMemoria.size() + " points from algorithm's IN-MEMORY history to chart.");
                // Add only points that are not already in the chart (if there's overlap)
                // For simplicity, we'll just add all of them, but you could check for duplicates
                for (HistoricoValor hv : historicoMemoria) {
                    series.getData().add(new XYChart.Data<>(hv.getData().format(formatter), hv.getValor()));
                }
            } else {
                System.out.println("DEBUG MC: Algorithm's in-memory history (Cripto ID " + criptoId + ") is EMPTY. This is normal at startup or if it just started.");
            }
        }

        // Add an initial point if the chart is still empty (e.g., brand new coin or no data yet)
        if (series.getData().isEmpty() && criptoAlgoritmoAtual != null) {
            double initialPriceFromAlg = criptoAlgoritmoAtual.getCurrentPrice();
            series.getData().add(new XYChart.Data<>(LocalDateTime.now().format(formatter), initialPriceFromAlg));
            infoLabel.setText("Nenhum histórico disponível. Gerando preços em tempo real.");
            System.out.println("DEBUG MC: Chart still EMPTY, adding initial point from algorithm: " + String.format("%.2f", initialPriceFromAlg));
        } else {
            infoLabel.setText(""); // Remove the selection message if data is loaded
            System.out.println("DEBUG MC: Chart loaded with " + series.getData().size() + " total points after initial load.");
        }

        // Limit points on initial load to avoid too dense a graph
        int maxDataPointsOnLoad = 60; // Display the last 60 points on initial load
        if (series.getData().size() > maxDataPointsOnLoad) {
            series.getData().remove(0, series.getData().size() - maxDataPointsOnLoad);
            System.out.println("DEBUG MC: Chart truncated to last " + maxDataPointsOnLoad + " points on initial load.");
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
        // For simulation, we'll just reload the chart with current algorithm data
        if (moedaSelecionada != null && criptoAlgoritmoAtual != null) {
            loadInitialChartData(moedaSelecionada.getId()); // Reload with current algorithm's history
        }
    }

    @FXML
    private void handlePeriodoSelection() {
        String selectedPeriod = periodoSelecionadoBox.getSelectionModel().getSelectedItem();
        System.out.println("DEBUG MC: Período selected: " + selectedPeriod);
        // Similar to interval, reload with current algorithm's history
        if (moedaSelecionada != null && criptoAlgoritmoAtual != null) {
            loadInitialChartData(moedaSelecionada.getId()); // Reload with current algorithm's history
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
}
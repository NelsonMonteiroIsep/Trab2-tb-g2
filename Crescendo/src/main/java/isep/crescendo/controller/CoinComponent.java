package isep.crescendo.controller;

import isep.crescendo.model.Criptomoeda;
import isep.crescendo.Repository.HistoricoValorRepository;
import isep.crescendo.model.HistoricoValor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.InputStream; // Adicionar este import
import java.text.DecimalFormat;
import java.util.List;

public class CoinComponent extends VBox {

    @FXML
    private ImageView coinIcon;
    @FXML
    private Label coinNameLabel;
    @FXML
    private Label coinSymbolLabel;
    @FXML
    private Label currentPriceLabel;
    @FXML
    private Label priceChangeLabel;
    @FXML
    private LineChart<Number, Number> priceChart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    private Criptomoeda criptomoeda;
    private final HistoricoValorRepository historicoRepo = new HistoricoValorRepository();
    private XYChart.Series<Number, Number> series;
    private DecimalFormat df = new DecimalFormat("#,##0.00");

    public CoinComponent(Criptomoeda criptomoeda, double initialPrice) {
        this.criptomoeda = criptomoeda;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/isep/crescendo/view/CoinComponent.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            System.err.println("Erro ao carregar componente de moeda: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha ao carregar CoinComponent.fxml", e);
        }

        initializeComponent(initialPrice);
    }

    private void initializeComponent(double initialPrice) {
        if (criptomoeda != null) {
            coinNameLabel.setText(criptomoeda.getNome());
            coinSymbolLabel.setText(criptomoeda.getSimbolo());
            updatePriceDisplay(initialPrice, 0.0);

            // ALTERADO: Carregamento de imagem usando getResourceAsStream
            if (criptomoeda.getImagemUrl() != null && !criptomoeda.getImagemUrl().isEmpty()) {
                try (InputStream is = getClass().getResourceAsStream(criptomoeda.getImagemUrl())) {
                    if (is != null) {
                        coinIcon.setImage(new Image(is));
                    } else {
                        System.err.println("Erro: Recurso de imagem não encontrado para " + criptomoeda.getNome() + " no caminho: " + criptomoeda.getImagemUrl());
                        // Tentar carregar a imagem padrão se o recurso não for encontrado
                        try (InputStream defaultIs = getClass().getResourceAsStream("/isep/crescendo/images/default_coin.png")) {
                            if (defaultIs != null) {
                                coinIcon.setImage(new Image(defaultIs));
                            } else {
                                System.err.println("Erro fatal: default_coin.png também não encontrado.");
                            }
                        } catch (Exception ex) {
                            System.err.println("Erro ao carregar default_coin.png em CoinComponent: " + ex.getMessage());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao carregar imagem para " + criptomoeda.getNome() + " (caminho: " + criptomoeda.getImagemUrl() + "): " + e.getMessage());
                    // Fallback para default_coin.png em caso de qualquer outra exceção de carregamento
                    try (InputStream defaultIs = getClass().getResourceAsStream("/isep/crescendo/images/default_coin.png")) {
                        if (defaultIs != null) {
                            coinIcon.setImage(new Image(defaultIs));
                        } else {
                            System.err.println("Erro fatal: default_coin.png também não encontrado.");
                        }
                    } catch (Exception ex) {
                        System.err.println("Erro ao carregar default_coin.png em CoinComponent (fallback): " + ex.getMessage());
                    }
                }
            } else {
                // Se a URL da imagem estiver nula ou vazia na criptomoeda
                try (InputStream defaultIs = getClass().getResourceAsStream("/isep/crescendo/images/default_coin.png")) {
                    if (defaultIs != null) {
                        coinIcon.setImage(new Image(defaultIs));
                    } else {
                        System.err.println("Erro fatal: default_coin.png também não encontrado (URL vazia).");
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao carregar default_coin.png em CoinComponent (URL nula/vazia): " + e.getMessage());
                }
            }

            series = new XYChart.Series<>();
            series.setName(criptomoeda.getSimbolo() + " Preço");
            priceChart.getData().add(series);
            priceChart.setLegendVisible(false);
            priceChart.setCreateSymbols(false);
            priceChart.setHorizontalGridLinesVisible(false);
            priceChart.setVerticalGridLinesVisible(false);

            xAxis.setLabel("Tempo");
            xAxis.setTickUnit(1);
            xAxis.setMinorTickVisible(false);
            xAxis.setForceZeroInRange(false);

            yAxis.setLabel("Preço (€)");

            loadHistoricalData();
        }
    }

    // Atualiza a exibição do preço e da percentagem de variação
    public void updatePriceDisplay(double newPrice, double priceChange) {
        currentPriceLabel.setText(df.format(newPrice) + " €");
        if (priceChange > 0) {
            priceChangeLabel.setText("+" + df.format(priceChange) + "%");
            priceChangeLabel.setStyle("-fx-text-fill: #4CAF50;"); // Verde
        } else if (priceChange < 0) {
            priceChangeLabel.setText(df.format(priceChange) + "%");
            priceChangeLabel.setStyle("-fx-text-fill: #F44336;"); // Vermelho
        } else {
            priceChangeLabel.setText("0.00%");
            priceChangeLabel.setStyle("-fx-text-fill: #b0b0b0;"); // Cinza
        }
    }

    // Adiciona novo ponto ao gráfico de linha
    public void addDataToChart(double newPrice) {
        int nextTimePoint = series.getData().size();
        series.getData().add(new XYChart.Data<>(nextTimePoint, newPrice));

        int maxDataPoints = 50;
        if (series.getData().size() > maxDataPoints) {
            series.getData().remove(0);
        }

        if (nextTimePoint > xAxis.getUpperBound()) {
            xAxis.setUpperBound(nextTimePoint + 10);
            xAxis.setLowerBound(Math.max(0, nextTimePoint - maxDataPoints + 1));
        }
        yAxis.setAutoRanging(true);
    }

    // Carrega dados históricos do repositório e desenha no gráfico
    private void loadHistoricalData() {
        if (criptomoeda != null) {
            List<HistoricoValor> historicalRecords = historicoRepo.listarPorCripto(criptomoeda.getId());
            if (historicalRecords != null && !historicalRecords.isEmpty()) {
                System.out.println("DEBUG (CoinComponent): " + historicalRecords.size() + " valores carregados para " + criptomoeda.getNome());
                series.getData().clear();
                for (int i = 0; i < historicalRecords.size(); i++) {
                    series.getData().add(new XYChart.Data<>(i, historicalRecords.get(i).getValor()));
                }
                xAxis.setLowerBound(0);
                xAxis.setUpperBound(historicalRecords.size() + 10);
                yAxis.setAutoRanging(true);
            } else {
                System.out.println("DEBUG (CoinComponent): Sem histórico para " + criptomoeda.getNome());
            }
        }
    }

    // Atualiza o gráfico com novo preço, calculando a variação
    public void updateChartWithNewPrice(double newPrice) {
        double oldPrice = newPrice;
        if (!series.getData().isEmpty()) {
            oldPrice = series.getData().get(series.getData().size() - 1).getYValue().doubleValue();
        }

        double priceChange = ((newPrice - oldPrice) / oldPrice) * 100;

        updatePriceDisplay(newPrice, priceChange);
        addDataToChart(newPrice);
    }

    // Getter para obter a instância da criptomoeda
    public Criptomoeda getCriptomoeda() {
        return criptomoeda;
    }
}
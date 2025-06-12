package isep.crescendo.controller;


import isep.crescendo.Repository.TransacaoRepository;
import isep.crescendo.Repository.CriptomoedaRepository;
import isep.crescendo.model.Criptomoeda;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    // Gráfico de total investido
    @FXML
    private PieChart investimentoPieChart;

    // Top 3 moedas com mais transações
    @FXML
    private ImageView top1Image;
    @FXML
    private Label top1NameLabel;
    @FXML
    private Label top1TransacoesLabel;

    @FXML
    private ImageView top2Image;
    @FXML
    private Label top2NameLabel;
    @FXML
    private Label top2TransacoesLabel;

    @FXML
    private ImageView top3Image;
    @FXML
    private Label top3NameLabel;
    @FXML
    private Label top3TransacoesLabel;

    // Repositórios (podes adaptar se usares service layer)
    private final TransacaoRepository transacaoRepository = new TransacaoRepository();
    private final CriptomoedaRepository criptoRepository = new CriptomoedaRepository();
    @FXML
    private BarChart<String, Number> volumePorUtilizadorBarChart;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        carregarInvestimentoPorMoeda();
        carregarTop3MoedasTransacoes();
        updateBarChart();
    }

    public void updateBarChart() {
        volumePorUtilizadorBarChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Top Utilizadores");

        Map<String, Double> volumePorUtilizador = transacaoRepository.getVolumePorUtilizador();

        for (Map.Entry<String, Double> entry : volumePorUtilizador.entrySet()) {
            String nomeUtilizador = entry.getKey();
            double volume = entry.getValue();

            series.getData().add(new XYChart.Data<>(nomeUtilizador, volume));
        }

        volumePorUtilizadorBarChart.getData().add(series);
    }

    private void carregarInvestimentoPorMoeda() {
        // Suponho que já tenhas um método no teu TransacaoRepository:
        // Map<String, Double> getTotalInvestidoPorMoeda()

        Map<String, Double> totalInvestidoPorMoeda = transacaoRepository.getTotalInvestidoPorMoeda();

        investimentoPieChart.getData().clear();

        for (Map.Entry<String, Double> entry : totalInvestidoPorMoeda.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
            investimentoPieChart.getData().add(slice);
        }

        investimentoPieChart.setLegendVisible(true);
        investimentoPieChart.setLabelsVisible(true);
    }

    private void carregarTop3MoedasTransacoes() {
        // Suponho que já tenhas um método no teu TransacaoRepository:
        // List<Object[]> getTop3MoedasMaisTransacoes() → cada Object[]: [String nomeMoeda, Long numTransacoes, String imagemUrl]

        List<Object[]> top3 = transacaoRepository.getTop3MoedasMaisTransacoes();

        if (top3.size() >= 1) {
            Object[] moeda1 = top3.get(0);
            atualizarMoeda(top1Image, top1NameLabel, top1TransacoesLabel, moeda1);
        }
        if (top3.size() >= 2) {
            Object[] moeda2 = top3.get(1);
            atualizarMoeda(top2Image, top2NameLabel, top2TransacoesLabel, moeda2);
        }
        if (top3.size() >= 3) {
            Object[] moeda3 = top3.get(2);
            atualizarMoeda(top3Image, top3NameLabel, top3TransacoesLabel, moeda3);
        }
    }

    private void atualizarMoeda(ImageView imageView, Label nameLabel, Label transacoesLabel, Object[] moedaData) {
        String nome = (String) moedaData[0];
        Long numTransacoes = (Long) moedaData[1];
        String imagemUrl = (String) moedaData[2];

        nameLabel.setText(nome);
        transacoesLabel.setText(numTransacoes + " transações");

        // Carregar imagem (corrigido)
        try {
            if (imagemUrl != null && !imagemUrl.isEmpty()) {

                if (getClass().getResourceAsStream("/"+imagemUrl) != null) {
                    imageView.setImage(new Image(getClass().getResourceAsStream("/"+imagemUrl)));
                } else {
                    System.err.println("Imagem não encontrada para " + nome + " → fallback.");
                    imageView.setImage(new Image(getClass().getResourceAsStream("/isep/crescendo/images/default_coin.png")));
                }
            } else {
                imageView.setImage(new Image(getClass().getResourceAsStream("/isep/crescendo/images/default_coin.png")));
            }
        } catch (Exception e) {
            System.err.println("Erro a carregar imagem para " + nome + ": " + e.getMessage());
            imageView.setImage(new Image(getClass().getResourceAsStream("/isep/crescendo/images/default_coin.png")));
        }
    }
}


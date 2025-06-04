package isep.crescendo.controller;

import isep.crescendo.Repository.TransacaoRepo;
import isep.crescendo.model.*;
import isep.crescendo.util.OrdemService;
import isep.crescendo.util.SceneSwitcher;
import isep.crescendo.util.SessionManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CoinController implements Initializable {
    public Label navBarAnyControl;
    @FXML private Label userNameLabel;
    private User loggedInUser;
    private Criptomoeda moeda;
    private int idMoedaAtual;
    private int carteiraId;
    @FXML private Label saldoLabel;
    @FXML private TextField saldoField;
    private double saldo = 0.0;
    @FXML private ComboBox<String> moedaSelecionadaBox;
    @FXML private LineChart<String, Number> lineChart;
    @FXML private CategoryAxis eixoX;
    @FXML private NumberAxis eixoY;
    @FXML private Label infoLabel;
    @FXML private TextField campoPesquisaMoeda;
    private ContextMenu sugestoesPopup = new ContextMenu();
    private final isep.crescendo.Repository.Criptomoeda criptoRepo = new isep.crescendo.Repository.Criptomoeda();
    private final isep.crescendo.Repository.HistoricoValor historicoRepo = new isep.crescendo.Repository.HistoricoValor();
    private final isep.crescendo.Repository.Criptomoeda criptomoedaRepository = new isep.crescendo.Repository.Criptomoeda();
    @FXML private ListView<String> listaSugestoes;
    private Criptomoeda criptoSelecionada;
    @FXML private ComboBox<String> intervaloSelecionadoBox;
    @FXML private ComboBox<String> periodoSelecionadoBox;
    @FXML private ImageView coinLogo;
    @FXML private Label nomeLabel;
    @FXML private Label simboloLabel;
    @FXML private Label descricaoLabel;
    @FXML private ImageView imagemView;
    @FXML private VBox rightContainer;
    @FXML private TextField quantidadeCompraField;
    @FXML private TextField precoCompraField;
    @FXML private TextField quantidadeVendaField;
    @FXML private TextField precoVendaField;
    private final OrdemService ordemService = new OrdemService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        intervaloSelecionadoBox.getItems().addAll("Minutos", "Horas", "Dias", "Meses", "Anos");
        intervaloSelecionadoBox.setValue("Horas");
        periodoSelecionadoBox.getItems().addAll("Último dia", "Última semana", "Último mês", "Último ano");
        periodoSelecionadoBox.setValue("Última semana");

        loggedInUser = SessionManager.getCurrentUser();
        if (loggedInUser != null) {
            userNameLabel.setText("Bem-vindo, " + loggedInUser.getNome());
            isep.crescendo.Repository.Carteira carteiraRepo = new isep.crescendo.Repository.Carteira();
            isep.crescendo.model.Carteira carteira = carteiraRepo.procurarPorUserId(loggedInUser.getId());
            if (carteira != null) {
                carteiraId = carteira.getId();
            }
        } else {
            userNameLabel.setText("Bem-vindo, visitante!");
        }

        atualizarSaldoLabel();

        campoPesquisaMoeda.textProperty().addListener((obs, oldText, newText) -> atualizarSugestoes(newText));

        saldoLabel.setText(String.format("%.2f €", isep.crescendo.model.Carteira.getSaldo()));

        campoPesquisaMoeda.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() < 1) {
                sugestoesPopup.hide();
                return;
            }

            List<Criptomoeda> correspondencias = criptoRepo.getAllCriptomoedas().stream()
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
                    setCriptomoeda(c);
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
    private void handleLogout() {
        SessionManager.setCurrentUser(null);
        SceneSwitcher.switchScene("/isep/crescendo/login-view.fxml", "/isep/crescendo/styles/login.css", "Login", userNameLabel);
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
                isep.crescendo.Repository.Carteira carteiraRepo = new isep.crescendo.Repository.Carteira();
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
            Carteira carteira = isep.crescendo.Repository.Carteira.procurarPorUserId(currentUser.getId());
            saldoLabel.setText(String.format("%.2f €", carteira != null ? carteira.getSaldo() : 0.0));
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
    private void listarMoedas() throws IOException {
        ObservableList<Criptomoeda> moedas = criptomoedaRepository.getAllCriptomoedas();
        for (Criptomoeda moeda : moedas) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/isep/crescendo/coin-componente.fxml"));
            VBox coinComponent = loader.load();
            CoinListViewController controller = loader.getController();
            controller.setCriptomoeda(moeda);
            rightContainer.getChildren().add(coinComponent);
        }
    }

    @FXML
    private void handlePesquisarMoeda() {
        String termo = campoPesquisaMoeda.getText().trim().toLowerCase();
        if (termo.isEmpty()) {
            infoLabel.setText("Insira o nome ou símbolo da moeda.");
            return;
        }

        criptoSelecionada = criptoRepo.getAllCriptomoedas().stream()
                .filter(c -> c.getNome().toLowerCase().contains(termo) || c.getSimbolo().equalsIgnoreCase(termo))
                .findFirst()
                .orElse(null);

        if (criptoSelecionada == null) {
            infoLabel.setText("Moeda não encontrada.");
        } else {
            atualizarGraficoComCripto(criptoSelecionada);
        }
    }

    @FXML
    private void atualizarGraficoComCripto(Criptomoeda cripto) {
        List<HistoricoValor> historico = historicoRepo.listarPorCripto(cripto.getId());
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

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limite = switch (periodo) {
            case "Último dia" -> agora.minusDays(1);
            case "Última semana" -> agora.minusWeeks(1);
            case "Último mês" -> agora.minusMonths(1);
            case "Último ano" -> agora.minusYears(1);
            default -> agora.minusWeeks(1);
        };

        List<HistoricoValor> filtrados = historico.stream()
                .filter(hv -> hv.getData().isAfter(limite))
                .toList();

        DateTimeFormatter formatter = switch (intervalo) {
            case "Minutos" -> DateTimeFormatter.ofPattern("dd/MM HH:mm");
            case "Horas" -> DateTimeFormatter.ofPattern("dd/MM HH:00");
            case "Dias" -> DateTimeFormatter.ofPattern("dd/MM");
            case "Meses" -> DateTimeFormatter.ofPattern("MM/yyyy");
            case "Anos" -> DateTimeFormatter.ofPattern("yyyy");
            default -> DateTimeFormatter.ofPattern("dd/MM");
        };

        Map<String, Double> dadosFiltrados = new LinkedHashMap<>();
        for (HistoricoValor hv : filtrados) {
            String chave = hv.getData().format(formatter);
            dadosFiltrados.putIfAbsent(chave, hv.getValor());
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

    private void atualizarSugestoes(String termo) {
        if (termo.isBlank()) {
            listaSugestoes.getItems().clear();
            return;
        }

        List<String> resultados = criptoRepo.getAllCriptomoedas().stream()
                .filter(c -> c.getNome().toLowerCase().contains(termo.toLowerCase()) ||
                        c.getSimbolo().toLowerCase().contains(termo.toLowerCase()))
                .map(c -> c.getNome() + " (" + c.getSimbolo() + ")")
                .toList();

        listaSugestoes.getItems().setAll(resultados);
    }

    public void setCriptomoeda(Criptomoeda moeda) {
        this.moeda = moeda;
        this.criptoSelecionada = moeda;
        if (moeda != null) {
            idMoedaAtual = moeda.getId();
            saldoLabel.setText("Saldo: 0.00 " + moeda.getSimbolo());
            infoLabel.setText(moeda.getNome());

            if (moeda.getImagemUrl() != null && !moeda.getImagemUrl().isEmpty()) {
                try {
                    coinLogo.setImage(new Image(moeda.getImagemUrl(), true));
                } catch (IllegalArgumentException e) {
                    coinLogo.setImage(null);
                }
            } else {
                coinLogo.setImage(null);
            }
        }
        atualizarGrafico();
    }

    @FXML
    public void handleComprar() {
        try {
            double quantidade = Double.parseDouble(quantidadeCompraField.getText());
            double preco = Double.parseDouble(precoCompraField.getText());

            Ordem ordemCompra = new Ordem(carteiraId, idMoedaAtual, quantidade, preco, "compra");
            ordemService.processarOrdemCompra(ordemCompra);

            atualizarSaldoLabel();
            System.out.println("Ordem de compra enviada com sucesso!");

        } catch (NumberFormatException e) {
            System.out.println("Erro: campos inválidos para compra.");
        }
    }

    @FXML
    public void handleVender() {
        try {
            double quantidade = Double.parseDouble(quantidadeVendaField.getText());
            double preco = Double.parseDouble(precoVendaField.getText());

            if (quantidade <= 0 || preco <= 0) {
                System.out.println("Quantidade e preço devem ser maiores que zero.");
                return;
            }

            int userId = SessionManager.getCurrentUser().getId();
            Carteira carteira = isep.crescendo.Repository.Carteira.procurarPorUserId(userId);
            if (carteira == null) {
                System.out.println("Carteira não encontrada.");
                return;
            }

            int carteiraId = carteira.getId();
            int idMoedaAtual = criptoSelecionada.getId();

            isep.crescendo.Repository.Carteira carteiraRepo = new isep.crescendo.Repository.Carteira();
            boolean podeVender = carteiraRepo.podeVender(carteiraId, idMoedaAtual, quantidade);

            if (!podeVender) {
                System.out.println("Saldo insuficiente ou já comprometido em ordens abertas.");
                return;
            }

            Ordem ordemVenda = new Ordem(carteiraId, idMoedaAtual, quantidade, preco, "venda");
            ordemService.processarOrdemVenda(ordemVenda);

            atualizarSaldoLabel();
            System.out.println("Ordem de venda enviada com sucesso!");

        } catch (NumberFormatException e) {
            System.out.println("Erro: campos inválidos para venda.");
        }
    }


}

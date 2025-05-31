// src/main/java/isep/crescendo/controller/CoinController.java
package isep.crescendo.controller;

import isep.crescendo.model.*; // Importa Criptomoeda, HistoricoValor
import isep.crescendo.model.CriptomoedaRepository; // Presume que você tem um repositório para Criptomoedas
import isep.crescendo.model.CarteiraRepository; // Presume que você tem um repositório para Carteira
import isep.crescendo.model.HistoricoValorRepository; // Presume que você tem um repositório para HistoricoValor (opcional para DB)
import isep.crescendo.util.SceneSwitcher; // Seu utilitário para trocar de cena
import isep.crescendo.util.SessionManager; // Seu utilitário para gerenciar a sessão do utilizador

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
import java.util.stream.Collectors; // Para usar Collectors.toList()

public class CoinController implements Initializable {

    // Variáveis FXML (ligadas aos elementos do coin-view.fxml)
    @FXML public Label navBarAnyControl; // Para o título da navbar ou controle de cena
    @FXML private Label userNameLabel; // Para exibir o nome do utilizador logado
    @FXML private Label saldoLabel; // Para exibir o saldo do utilizador
    @FXML private TextField saldoField; // (Não usado diretamente neste código, mas pode ser para input de saldo)

    @FXML private LineChart<String, Number> lineChart; // O gráfico de linha
    @FXML private CategoryAxis eixoX; // Eixo X do gráfico (tempo)
    @FXML private NumberAxis eixoY; // Eixo Y do gráfico (valor)
    @FXML private Label infoLabel; // Para exibir informações em tempo real ou mensagens de status

    @FXML private TextField campoPesquisaMoeda; // Campo para pesquisar outras moedas
    @FXML private ComboBox<String> intervaloSelecionadoBox; // ComboBox para selecionar intervalo de tempo
    @FXML private ComboBox<String> periodoSelecionadoBox; // ComboBox para selecionar período de tempo

    @FXML private ImageView coinLogo; // Imagem/logo da moeda
    @FXML private Label nomeLabel; // Nome da moeda
    @FXML private Label simboloLabel; // Símbolo da moeda
    @FXML private Label descricaoLabel; // Descrição da moeda

    // Variáveis internas do controlador
    private User loggedInUser; // O utilizador atualmente logado
    private Criptomoeda criptoSelecionada; // A criptomoeda que está a ser exibida nesta tela

    // Repositórios para aceder aos dados (certifique-se de que existem ou adapte)
    private final CriptomoedaRepository criptoRepo = new CriptomoedaRepository();
    private final HistoricoValorRepository historicoRepo = new HistoricoValorRepository(); // Opcional, para DB

    private ContextMenu sugestoesPopup = new ContextMenu(); // Para sugestões de pesquisa

    private CriptoAlgoritmo criptoAlgoritmoAtivo; // A instância do simulador de preço para a moeda atual
    private Timeline graficoRealtimeUpdater; // Timeline para atualizar o gráfico em tempo real
    private static final int REALTIME_UPDATE_INTERVAL_MS = 1000; // Intervalo de atualização do gráfico (1 seg)

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicialização do utilizador logado
        loggedInUser = SessionManager.getCurrentUser();
        if (loggedInUser != null) {
            userNameLabel.setText("Bem-vindo, " + loggedInUser.getNome());
        } else {
            userNameLabel.setText("Bem-vindo, visitante!");
        }

        // Inicialização dos ComboBoxes do gráfico
        intervaloSelecionadoBox.getItems().addAll("Minutos", "Horas", "Dias", "Meses", "Anos");
        intervaloSelecionadoBox.setValue("Horas"); // Valor padrão
        periodoSelecionadoBox.getItems().addAll("Último dia", "Última semana", "Último mês", "Último ano");
        periodoSelecionadoBox.setValue("Última semana"); // Valor padrão

        // Configuração do gráfico
        lineChart.setTitle("Histórico de Preço");
        eixoX.setLabel("Tempo");
        eixoY.setLabel("Valor (€)");
        lineChart.setCreateSymbols(false); // Não mostra pontos nos dados
        lineChart.setAnimated(false); // Desativa animações para melhor desempenho em tempo real
        lineChart.setLegendVisible(false); // Oculta a legenda

        // Listener para o campo de pesquisa de moedas
        campoPesquisaMoeda.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() < 1) { // Esconde sugestões se o campo estiver vazio
                sugestoesPopup.hide();
                return;
            }

            // Filtra criptomoedas com base no texto de pesquisa
            List<Criptomoeda> correspondencias = criptoRepo.getAllCriptomoedas().stream()
                    .filter(c -> c.getNome().toLowerCase().contains(newText.toLowerCase()) ||
                            c.getSimbolo().toLowerCase().contains(newText.toLowerCase()))
                    .collect(Collectors.toList());

            if (correspondencias.isEmpty()) { // Esconde se não houver correspondências
                sugestoesPopup.hide();
                return;
            }

            // Cria itens de menu para as sugestões
            List<MenuItem> items = correspondencias.stream().map(c -> {
                MenuItem item = new MenuItem(c.getNome() + " (" + c.getSimbolo() + ")");
                item.setOnAction(e -> { // Ao selecionar uma sugestão
                    campoPesquisaMoeda.setText(c.getSimbolo()); // Preenche o campo de pesquisa
                    sugestoesPopup.hide(); // Esconde o popup
                    setCriptomoeda(c); // Define a criptomoeda selecionada e atualiza a UI
                });
                return item;
            }).collect(Collectors.toList());

            sugestoesPopup.getItems().setAll(items); // Adiciona os itens ao ContextMenu
            if (!sugestoesPopup.isShowing()) {
                sugestoesPopup.show(campoPesquisaMoeda, Side.BOTTOM, 0, 0); // Mostra o popup
            }
        });

        // Atualiza o saldo inicial
        atualizarSaldoLabel();
    }

    /**
     * Método público para definir a criptomoeda a ser exibida e iniciar a simulação/gráfico.
     * Este método será chamado pelo CoinComponent quando uma moeda for clicada.
     * @param moeda A criptomoeda selecionada.
     */
    public void setCriptomoeda(Criptomoeda moeda) {
        this.criptoSelecionada = moeda; // Armazena a moeda atual

        // 1. Parar simulação e atualização de gráfico anteriores, se existirem
        if (criptoAlgoritmoAtivo != null) {
            criptoAlgoritmoAtivo.stopSimulation();
            criptoAlgoritmoAtivo = null;
            System.out.println("Simulação anterior parada.");
        }
        if (graficoRealtimeUpdater != null) {
            graficoRealtimeUpdater.stop();
            graficoRealtimeUpdater = null;
            System.out.println("Atualizador de gráfico em tempo real parado.");
        }

        // 2. Atualizar UI com os dados da nova moeda
        if (moeda != null) {
            nomeLabel.setText(moeda.getNome());
            simboloLabel.setText(moeda.getSimbolo());
            descricaoLabel.setText(moeda.getDescricao());

            // Carrega a imagem da logo da moeda
            if (moeda.getImagemUrl() != null && !moeda.getImagemUrl().isEmpty()) {
                try {
                    coinLogo.setImage(new Image(moeda.getImagemUrl(), true));
                } catch (IllegalArgumentException e) {
                    coinLogo.setImage(null); // Define como nulo em caso de URL inválida
                    System.err.println("Erro ao carregar imagem para " + moeda.getNome() + ": " + e.getMessage());
                }
            } else {
                coinLogo.setImage(null); // Define como nulo se a URL for vazia
            }

            infoLabel.setText("Carregando histórico..."); // Mensagem de status

            // 3. Iniciar uma nova simulação para a criptomoeda selecionada
            criptoAlgoritmoAtivo = new CriptoAlgoritmo(moeda.getId());
            criptoAlgoritmoAtivo.startSimulation();
            System.out.println("Simulação de gravação iniciada para: " + moeda.getNome() + " (ID: " + moeda.getId() + ")");

            // 4. Inicializar e iniciar o atualizador do gráfico em tempo real
            lineChart.getData().clear(); // Limpa dados antigos do gráfico
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(moeda.getSimbolo()); // Nome da série de dados
            lineChart.getData().add(series); // Adiciona a série ao gráfico

            graficoRealtimeUpdater = new Timeline(new KeyFrame(Duration.millis(REALTIME_UPDATE_INTERVAL_MS), e -> {
                updateRealtimeChart(); // Chama o método para atualizar o gráfico
            }));
            graficoRealtimeUpdater.setCycleCount(Timeline.INDEFINITE); // Atualiza indefinidamente
            graficoRealtimeUpdater.play(); // Inicia o atualizador

            System.out.println("Atualizador de gráfico em tempo real iniciado.");

            // Carrega o histórico inicial do DB (se aplicável)
            carregarHistoricoDoDBEExibir(moeda);

        } else {
            // Limpa a UI se nenhuma moeda for selecionada (caso de erro ou inicialização)
            nomeLabel.setText("");
            simboloLabel.setText("");
            descricaoLabel.setText("");
            coinLogo.setImage(null);
            infoLabel.setText("Nenhuma criptomoeda selecionada.");
            lineChart.getData().clear();
        }
    }

    /**
     * Atualiza o gráfico em tempo real com os dados da simulação.
     */
    private void updateRealtimeChart() {
        if (criptoAlgoritmoAtivo == null || criptoSelecionada == null) {
            return; // Não faz nada se a simulação não estiver ativa
        }

        // Obtém o histórico recente da simulação em memória
        List<HistoricoValor> historicoMemoria = criptoAlgoritmoAtivo.getHistoricoEmMemoria();

        // Pega a série de dados do gráfico
        XYChart.Series<String, Number> series = (XYChart.Series<String, Number>) lineChart.getData().get(0);
        series.getData().clear(); // Limpa os dados antigos da série

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss"); // Formato para o eixo X

        // Adiciona os novos dados ao gráfico
        for (HistoricoValor hv : historicoMemoria) {
            series.getData().add(new XYChart.Data<>(hv.getData().format(formatter), hv.getValor()));
        }

        // Atualiza o label de informação com o último valor
        if (!historicoMemoria.isEmpty()) {
            HistoricoValor ultimo = historicoMemoria.get(historicoMemoria.size() - 1);
            infoLabel.setText(String.format("Último valor (em tempo real): %.2f €", ultimo.getValor()));
        }
    }

    /**
     * Carrega o histórico de valores da base de dados (se implementado) e exibe no gráfico.
     * Esta é a parte para carregar dados persistentes, não em tempo real.
     * @param cripto A criptomoeda para a qual carregar o histórico.
     */
    private void carregarHistoricoDoDBEExibir(Criptomoeda cripto) {
        // Esta parte assume que você tem um HistoricoValorRepository e uma DB.
        // Se não tiver, esta função pode ser simples ou fazer nada por enquanto.
        List<HistoricoValor> historico = historicoRepo.listarPorCripto(cripto.getId()); // Exemplo de chamada ao repositório

        String intervalo = intervaloSelecionadoBox.getValue();
        String periodo = periodoSelecionadoBox.getValue();

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limite;

        // Calcula o limite de tempo com base no período selecionado
        switch (periodo) {
            case "Último dia" -> limite = agora.minusDays(1);
            case "Última semana" -> limite = agora.minusWeeks(1);
            case "Último mês" -> limite = agora.minusMonths(1);
            case "Último ano" -> limite = agora.minusYears(1);
            default -> limite = agora.minusWeeks(1); // Padrão
        }

        // Filtra os dados históricos que estão dentro do período selecionado
        List<HistoricoValor> filtrados = historico.stream()
                .filter(hv -> hv.getData().isAfter(limite))
                .collect(Collectors.toList());

        // Define o formato da data para o eixo X com base no intervalo
        DateTimeFormatter formatter;
        switch (intervalo) {
            case "Minutos" -> formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
            case "Horas" -> formatter = DateTimeFormatter.ofPattern("dd/MM HH:00");
            case "Dias" -> formatter = DateTimeFormatter.ofPattern("dd/MM");
            case "Meses" -> formatter = DateTimeFormatter.ofPattern("MM/yyyy");
            case "Anos" -> formatter = DateTimeFormatter.ofPattern("yyyy");
            default -> formatter = DateTimeFormatter.ofPattern("dd/MM");
        }

        // Agrupa os dados para exibição (ex: um ponto por hora, um por dia)
        // Este LinkedHashMap mantém a ordem dos dados no gráfico.
        Map<String, Double> dadosAgrupados = new LinkedHashMap<>();
        for (HistoricoValor hv : filtrados) {
            String chave = hv.getData().format(formatter);
            // Para simplificar, pega o primeiro valor para cada chave agrupada
            // Ou pode fazer uma média, etc.
            dadosAgrupados.putIfAbsent(chave, hv.getValor());
        }

        // Limpa o gráfico atual e adiciona os dados históricos
        lineChart.getData().clear();
        XYChart.Series<String, Number> historicoSeries = new XYChart.Series<>();
        historicoSeries.setName("Histórico " + cripto.getSimbolo());

        for (Map.Entry<String, Double> entry : dadosAgrupados.entrySet()) {
            historicoSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        lineChart.getData().add(historicoSeries);

        // Nota: O infoLabel será atualizado pelo updateRealtimeChart, que tem prioridade.
        // if (!filtrados.isEmpty()) {
        //     double ultimo = filtrados.get(filtrados.size() - 1).getValor();
        //     double penultimo = filtrados.size() > 1 ? filtrados.get(filtrados.size() - 2).getValor() : ultimo;
        //     double variacao = ((ultimo - penultimo) / penultimo) * 100;
        //     // Pode exibir a variação aqui se quiser
        // }
    }

    /**
     * Método FXML para atualizar o gráfico com base nas seleções de intervalo/período.
     */
    @FXML
    private void atualizarGrafico() {
        if (criptoSelecionada == null) {
            infoLabel.setText("Selecione ou pesquise uma criptomoeda para atualizar o gráfico.");
            return;
        }
        // Recarrega o histórico do DB com base nas novas seleções
        carregarHistoricoDoDBEExibir(criptoSelecionada);
    }

    /**
     * Lida com o logout do utilizador.
     */
    @FXML
    private void handleLogout() {
        dispose(); // Para a simulação e atualizadores
        SessionManager.setCurrentUser(null); // Limpa a sessão
        // Troca para a tela de login
        SceneSwitcher.switchScene("/isep/crescendo/login-view.fxml", "/isep/crescendo/styles/login.css", "Login", navBarAnyControl);
    }

    /**
     * Lida com a adição de saldo à carteira do utilizador.
     */
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
                CarteiraRepository carteiraRepo = new CarteiraRepository(); // Cria uma nova instância do repositório
                Carteira carteira = carteiraRepo.procurarPorUserId(userId);

                if (carteira != null) {
                    double novoSaldo = carteira.getSaldo() + valor;
                    carteiraRepo.atualizarSaldo(userId, novoSaldo); // Atualiza o saldo na DB
                    atualizarSaldoLabel(); // Atualiza o label na UI
                } else {
                    mostrarErro("Carteira não encontrada para o utilizador.");
                }

            } catch (NumberFormatException e) {
                mostrarErro("Valor inválido. Insira um número.");
            }
        });
    }

    /**
     * Atualiza o label do saldo na UI buscando o saldo da carteira do utilizador logado.
     */
    private void atualizarSaldoLabel() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            Carteira carteira = CarteiraRepository.procurarPorUserId(currentUser.getId()); // Busca a carteira

            if (carteira != null) {
                saldoLabel.setText(String.format("%.2f €", carteira.getSaldo()));
            } else {
                saldoLabel.setText("0.00 €");
            }
        }
    }

    /**
     * Exibe uma mensagem de erro em um Alert.
     * @param mensagem A mensagem de erro.
     */
    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    /**
     * Lida com a pesquisa de uma criptomoeda pelo campo de texto.
     */
    @FXML
    private void handlePesquisarMoeda() {
        String termo = campoPesquisaMoeda.getText().trim().toLowerCase();
        if (termo.isEmpty()) {
            infoLabel.setText("Insira o nome ou símbolo da moeda.");
            disposeSimulationAndChart(); // Para a simulação e limpa o gráfico
            return;
        }

        // Procura a moeda pelo termo
        criptoSelecionada = criptoRepo.getAllCriptomoedas()
                .stream()
                .filter(c -> c.getNome().toLowerCase().contains(termo) || c.getSimbolo().equalsIgnoreCase(termo))
                .findFirst()
                .orElse(null);

        if (criptoSelecionada == null) {
            infoLabel.setText("Moeda não encontrada.");
            disposeSimulationAndChart(); // Para a simulação e limpa o gráfico
            return;
        }

        // Se encontrada, define a criptomoeda para exibir
        setCriptomoeda(criptoSelecionada);
    }

    /**
     * Para a simulação e o atualizador de gráfico em tempo real, e limpa o gráfico.
     * Usado ao trocar de moeda ou sair da tela.
     */
    private void disposeSimulationAndChart() {
        if (criptoAlgoritmoAtivo != null) {
            criptoAlgoritmoAtivo.stopSimulation();
            criptoAlgoritmoAtivo = null;
        }
        if (graficoRealtimeUpdater != null) {
            graficoRealtimeUpdater.stop();
            graficoRealtimeUpdater = null;
        }
        lineChart.getData().clear();
        nomeLabel.setText("");
        simboloLabel.setText("");
        descricaoLabel.setText("");
        coinLogo.setImage(null);
    }


    /**
     * Método para limpar recursos quando o controlador não for mais necessário.
     * É importante chamar este método quando a cena do CoinController for fechada ou substituída.
     */
    public void dispose() {
        disposeSimulationAndChart(); // Chama o método auxiliar
        System.out.println("CoinController: Timelines de simulação e atualização de gráfico parados.");
        // Pode adicionar outras limpezas aqui, se necessário.
    }
}
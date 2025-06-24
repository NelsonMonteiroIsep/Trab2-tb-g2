package isep.crescendo.controller;

// Importações necessárias para controlo de UI e manipulação de dados
import isep.crescendo.Repository.CarteiraRepository;
import isep.crescendo.Repository.TransacaoRepository;
import isep.crescendo.Repository.UserRepository;
import isep.crescendo.util.SceneSwitcher;
import isep.crescendo.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import isep.crescendo.Repository.CriptomoedaRepository;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Controlador da view de Admin, gerindo tanto a dashboard como a gestão de utilizadores e criptomoedas
public class AdminController {

    // Repositórios para acesso a dados
    private final CriptomoedaRepository criptoRepo = new CriptomoedaRepository();
    private final UserRepository userRepositoryRepo = new UserRepository();

    // Listas observáveis para popular tabelas
    private final ObservableList<isep.crescendo.model.Criptomoeda> criptomoedas = FXCollections.observableArrayList();
    private final ObservableList<isep.crescendo.model.User> users = FXCollections.observableArrayList();

    // === Componentes da view (injetados via FXML) ===

    // Tabela de utilizadores e colunas
    @FXML private TableView<isep.crescendo.model.User> userTable;
    @FXML private TableColumn<isep.crescendo.model.User, Integer> idColumn;
    @FXML private TableColumn<isep.crescendo.model.User, String> nomeColumn;
    @FXML private TableColumn<isep.crescendo.model.User, String> emailColumn;
    @FXML private TableColumn<isep.crescendo.model.User, Boolean> isAdminColumn;

    // Botões de gestão
    @FXML private Button btnUser;
    @FXML private Button btnCriar, btnDesativar;

    // Filtros de data e modo
    @FXML private DatePicker datePickerInicio;
    @FXML private DatePicker datePickerFim;

    // Gráficos para análise de dados
    @FXML private LineChart<String, Number> lineChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private LineChart<String, Number> lineChartVolumeGlobal;
    @FXML private BarChart<String, Number> barChartTopUsers;
    @FXML private PieChart pieChartTop3Moedas;
    @FXML private PieChart pieChartVolumePorMoeda;

    // Indicadores de atividade
    @FXML private ProgressBar progressAtividade;
    @FXML private Label labelAtividadePercent;
    @FXML private Label labelTotalInvestido;
    @FXML private Label labelTotalUsers;
    @FXML private Label labelTotalMoedas;

    // Pesquisa e tabela de criptomoedas
    @FXML private TextField searchField;
    @FXML private TableView<isep.crescendo.model.Criptomoeda> listaCriptomoedas;
    @FXML private TableColumn<isep.crescendo.model.Criptomoeda, Integer> idCriptoColumn;
    @FXML private TableColumn<isep.crescendo.model.Criptomoeda, String> nomeCriptoColumn;
    @FXML private TableColumn<isep.crescendo.model.Criptomoeda, String> simboloColumn;
    @FXML private TableColumn<isep.crescendo.model.Criptomoeda, String> descricaoColumn;
    @FXML private TableColumn<isep.crescendo.model.Criptomoeda, Boolean> ativoColumn;

    // Combobox para selecionar modos de visualização de gráficos
    @FXML private ComboBox<String> comboSaldoModo;
    @FXML private ComboBox<String> comboTransacoesModo;

    // Lista auxiliar de utilizadores
    private ObservableList<isep.crescendo.model.User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Ao iniciar o controlador, carregam-se os dados da dashboard, tabelas e gráficos

        // Tentativa de carregar dados da dashboard
        try {
            if (labelTotalUsers != null) {
                int totalUsers = userRepositoryRepo.countUsers();
                labelTotalUsers.setText(String.valueOf(totalUsers));
            }

            // Configurações dos comboboxes de modo
            if (comboSaldoModo != null) {
                comboSaldoModo.getItems().setAll("Valor em Euros", "Quantidade de Moeda");
                comboSaldoModo.getSelectionModel().selectFirst();
                comboSaldoModo.setButtonCell(getStyledCell());
                comboSaldoModo.setCellFactory(listView -> getStyledCell());
            }

            if (comboTransacoesModo != null) {
                comboTransacoesModo.getItems().setAll("Volume em Euros", "Número de Transações");
                comboTransacoesModo.getSelectionModel().selectFirst();
                comboTransacoesModo.setButtonCell(getStyledCell());
                comboTransacoesModo.setCellFactory(listView -> getStyledCell());
            }

            // Total investido em todas as moedas
            if (labelTotalInvestido != null) {
                double totalInvestido = new TransacaoRepository().getTotalInvestidoPorMoeda().values().stream().mapToDouble(Double::doubleValue).sum();
                labelTotalInvestido.setText(String.format("%.2f €", totalInvestido));
            }

            // Atualiza indicador de atividade
            if (progressAtividade != null && labelAtividadePercent != null) {
                updateAtividade();
            }

            // Carrega gráficos da dashboard
            if (lineChartVolumeGlobal != null) carregarLineChartVolumeGlobal();
            if (barChartTopUsers != null) carregarBarChartTopUsers();
            if (pieChartTop3Moedas != null) carregarPieChartTop3Moedas();
            if (pieChartVolumePorMoeda != null) carregarPieChartVolumePorMoeda();

        } catch (Exception e) {
            System.out.println("Dashboard não presente nesta view.");
        }

        // Configuração da tabela de utilizadores
        if (userTable != null) {
            userTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            if (idColumn != null && nomeColumn != null && emailColumn != null && isAdminColumn != null) {
                idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
                nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
                emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
                isAdminColumn.setCellValueFactory(new PropertyValueFactory<>("admin"));
                carregarUtilizadores();
            }
        }

        // Configuração da tabela de criptomoedas
        if (listaCriptomoedas != null) {
            idCriptoColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nomeCriptoColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
            simboloColumn.setCellValueFactory(new PropertyValueFactory<>("simbolo"));
            descricaoColumn.setCellValueFactory(new PropertyValueFactory<>("descricao"));
            ativoColumn.setCellValueFactory(new PropertyValueFactory<>("ativo"));

            // Customiza o texto da coluna "ativo" (sim/não)
            ativoColumn.setCellFactory(column -> new TableCell<isep.crescendo.model.Criptomoeda, Boolean>() {
                @Override
                protected void updateItem(Boolean ativo, boolean empty) {
                    super.updateItem(ativo, empty);
                    setText(empty || ativo == null ? null : ativo ? "Sim" : "Não");
                }
            });

            carregarCriptomoedas();
        }
    }


    // Carrega todos os utilizadores da base de dados e define-os na tabela
    private void carregarUtilizadores() {
        users.setAll(userRepositoryRepo.listarTodos());
        userTable.setItems(users);
    }

    // Carrega todas as criptomoedas da base de dados e define-as na tabela
    private void carregarCriptomoedas() {
        criptomoedas.clear();
        criptomoedas.addAll(criptoRepo.getAllCriptomoedas());
        listaCriptomoedas.setItems(criptomoedas);
    }

    // Método central que chama todos os outros métodos de carga para atualizar a dashboard
    private void carregarDashboard() {
        carregarVolumeGlobalPorDia();
        carregarTop5UsersPorVolume();
        carregarTop3MoedasPorTransacoes();
        carregarDistribuicaoVolumePorMoeda();
        carregarAtividade();
        carregarTotalInvestido();
    }

    // Carrega o gráfico de linha com o volume global de transações por dia
    private void carregarVolumeGlobalPorDia() {
        lineChartVolumeGlobal.getData().clear();
        var volumePorDia = isep.crescendo.Repository.TransacaoRepository.getVolumeGlobalPorDia();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Volume Global");

        for (var entry : volumePorDia.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        lineChartVolumeGlobal.getData().add(series);
    }

    // Carrega o gráfico de barras com os top 5 utilizadores por volume de transações
    private void carregarTop5UsersPorVolume() {
        barChartTopUsers.getData().clear();
        var topUsers = isep.crescendo.Repository.TransacaoRepository.getTop5UsersPorVolume();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Top 5 Utilizadores");

        for (var entry : topUsers.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChartTopUsers.getData().add(series);
    }

    // Carrega o gráfico circular com as 3 criptomoedas com mais transações
    private void carregarTop3MoedasPorTransacoes() {
        pieChartTop3Moedas.getData().clear();
        var topMoedas = isep.crescendo.Repository.TransacaoRepository.getTop3MoedasPorTransacoes();

        for (var entry : topMoedas.entrySet()) {
            pieChartTop3Moedas.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
    }

    // Carrega o gráfico circular com a distribuição de volume por moeda
    private void carregarDistribuicaoVolumePorMoeda() {
        pieChartVolumePorMoeda.getData().clear();
        var volumePorMoeda = isep.crescendo.Repository.TransacaoRepository.getDistribuicaoVolumePorMoeda();

        for (var entry : volumePorMoeda.entrySet()) {
            pieChartVolumePorMoeda.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
    }

    // Atualiza o progresso de atividade com base nas transações dos últimos 30 dias
    private void carregarAtividade() {
        double atividadePercent = isep.crescendo.Repository.TransacaoRepository.getPercentAtividadeUltimos30Dias();
        progressAtividade.setProgress(atividadePercent);
        labelAtividadePercent.setText((int)(atividadePercent * 100) + "%");
    }

    // Calcula e apresenta o total investido pelos utilizadores em todas as criptomoedas
    private void carregarTotalInvestido() {
        double totalInvestido = isep.crescendo.Repository.TransacaoRepository.getTotalInvestido();
        labelTotalInvestido.setText(String.format("%.2f €", totalInvestido));
    }


// === MÉTODOS DE AÇÃO (HANDLERS) ===

    // Edita o nome do utilizador selecionado na tabela
    @FXML
    private void handleEditarNome() {
        isep.crescendo.model.User selecionado = userTable.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            TextInputDialog dialog = new TextInputDialog(selecionado.getNome());
            dialog.setTitle("Editar Nome");
            dialog.setHeaderText("Editar nome do utilizador");
            dialog.setContentText("Novo nome:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(novoNome -> {
                selecionado.setNome(novoNome);
                userRepositoryRepo.atualizar(selecionado);
                carregarUtilizadores(); // Recarrega a lista para refletir alterações
                userTable.refresh();
            });
        } else {
            mostrarAlerta("Selecione um utilizador primeiro.");
        }
    }

    // Promove um utilizador a administrador, desde que ele não tenha saldo na carteira
    @FXML
    private void handleTornarAdmin() {
        isep.crescendo.model.User selecionado = userTable.getSelectionModel().getSelectedItem();
        if (selecionado != null && !selecionado.isAdmin()) {

            isep.crescendo.model.Carteira carteira = CarteiraRepository.procurarPorUserId(selecionado.getId());

            if (carteira != null && carteira.getSaldo() > 0) {
                mostrarAlerta("Não é possível tornar admin. O utilizador tem saldo na carteira.");
                return;
            }

            // Se a carteira estiver vazia, remove-a
            if (carteira != null && carteira.getSaldo() == 0) {
                CarteiraRepository.apagarPorUserId(selecionado.getId());
            }

            selecionado.setAdmin(true);
            userRepositoryRepo.atualizarAdmin(selecionado);
            carregarUtilizadores();
            mostrarAlerta("Utilizador promovido a admin com sucesso.");
        } else {
            mostrarAlerta("Selecione um utilizador primeiro.");
        }
    }

    // Apaga um utilizador após confirmação
    @FXML
    private void handleApagarUtilizador() {
        isep.crescendo.model.User selecionado = userTable.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            Alert confirm = new Alert(AlertType.CONFIRMATION, "Tem a certeza que quer apagar este utilizador?", ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Confirmar remoção");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                userRepositoryRepo.apagar(selecionado.getId());
                carregarUtilizadores();
            }
        } else {
            mostrarAlerta("Selecione um utilizador primeiro.");
        }
    }

    // Edita o email do utilizador após validar o novo formato
    @FXML
    private void handleEditarEmail() {
        isep.crescendo.model.User selecionado = userTable.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            TextInputDialog dialog = new TextInputDialog(selecionado.getEmail());
            dialog.setTitle("Editar Email");
            dialog.setHeaderText("Editar email do utilizador");
            dialog.setContentText("Novo email:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(novoEmail -> {
                if (!novoEmail.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                    mostrarAlerta("Email inválido.");
                    return;
                }

                selecionado.setEmail(novoEmail);
                userRepositoryRepo.atualizar(selecionado);
                carregarUtilizadores();
                userTable.refresh();
            });
        } else {
            mostrarAlerta("Selecione um utilizador primeiro.");
        }
    }

    // Mostra uma janela de alerta com a mensagem recebida
    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setContentText(mensagem);
        alert.showAndWait();
    }


    // Abre a vista de gestão de utilizadores (admin-user-management-view)
    @FXML
    private void handleUserManagement(ActionEvent event) {
        SceneSwitcher.switchScene(
                "/isep/crescendo/view/admin-user-management-view.fxml",
                "/isep/crescendo/styles/login.css",
                "Marketplace",
                (Control) event.getSource()
        );
    }

    // Efetua logout e redireciona para o ecrã de login
    @FXML
    private void handleLogout() {
        SessionManager.setCurrentUser(null);
        SceneSwitcher.switchScene("/isep/crescendo/view/login-view.fxml", "/isep/crescendo/styles/login.css", "Login",searchField );
    }

    // Abre a vista de gestão de criptomoedas (admin-cripto-view)
    @FXML
    private void handleCripto() {
        SceneSwitcher.switchScene(
                "/isep/crescendo/view/admin-cripto-view.fxml",
                "/isep/crescendo/styles/login.css",
                "Admin Cripto",
                searchField
        );
    }

    // Abre o dashboard principal (admin-view)
    @FXML
    private void handleDash() {
        SceneSwitcher.switchScene(
                "/isep/crescendo/view/admin-view.fxml",
                "/isep/crescendo/styles/login.css",
                "Admin Cripto",
                searchField
        );
    }


    // Abre um diálogo para criar nova criptomoeda e guarda no repositório
    @FXML
    private void handleCriar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/isep/crescendo/view/cripto-criar-dialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Criar Criptomoeda");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(root));

            CriptoCriarDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isConfirmado()) {
                isep.crescendo.model.Criptomoeda cripto = controller.getNovaCripto();

                // Atualiza a lista observável ligada à tabela
                criptomoedas.add(cripto);
                listaCriptomoedas.refresh();

                // Salva no banco
                criptoRepo.adicionar(cripto);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Ativa ou desativa uma criptomoeda selecionada
    @FXML
    private void handleToggleAtivo() {
        isep.crescendo.model.Criptomoeda selecionada = listaCriptomoedas.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            boolean estadoAtual = selecionada.isAtivo();
            selecionada.setAtivo(!estadoAtual);

            // Atualiza o banco de dados
            criptoRepo.atualizar(selecionada);

            listaCriptomoedas.refresh();

            // Atualiza o texto do botão
            if (selecionada.isAtivo()) {
                btnDesativar.setText("Desativar Criptomoeda");
            } else {
                btnDesativar.setText("Ativar Criptomoeda");
            }
        } else {
            mostrarAlerta("Selecione uma criptomoeda primeiro.");
        }
    }

    // Filtra os utilizadores na tabela conforme o texto introduzido no campo de pesquisa
    @FXML
    private void handlePesquisar() {
        String filtro = searchField.getText().toLowerCase();

        FilteredList<isep.crescendo.model.User> filteredData = new FilteredList<>(users, user -> {
            if (filtro == null || filtro.isEmpty()) {
                return true;
            }
            return user.getNome().toLowerCase().contains(filtro) ||
                    user.getEmail().toLowerCase().contains(filtro);
        });

        SortedList<isep.crescendo.model.User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sortedData);
    }


    // === GRÁFICO DE SALDOS POR UTILIZADOR ===
    @FXML
    private void handleVerGraficoSaldos() {
        // Verifica se os componentes necessários estão disponíveis
        if (userTable == null || comboSaldoModo == null || lineChart == null || datePickerInicio == null || datePickerFim == null) {
            mostrarAlerta("Esta funcionalidade só está disponível na Gestão de Utilizadores.");
            return;
        }

        // Verifica se o utilizador selecionou pelo menos um utilizador e intervalo de datas
        var selecionados = userTable.getSelectionModel().getSelectedItems();
        if (selecionados.isEmpty() || datePickerInicio.getValue() == null || datePickerFim.getValue() == null) {
            mostrarAlerta("Selecione pelo menos um utilizador e um intervalo de datas.");
            return;
        }

        // Determina se o gráfico deve mostrar valores em euros
        boolean mostrarEmEuros = "Valor em Euros".equals(comboSaldoModo.getValue());
        lineChart.getData().clear();
        xAxis.setLabel("Data");
        yAxis.setLabel(mostrarEmEuros ? "Valor em Euros" : "Quantidade de Moeda");

        String[] cores = {"#FF5733", "#33C1FF", "#75FF33", "#F933FF", "#FFC133", "#33FFB5", "#FF33A8"};
        int corIndex = 0;

        for (var user : selecionados) {
            var historico = isep.crescendo.Repository.TransacaoRepository.getHistoricoSaldoPorCripto(
                    user.getId(),
                    datePickerInicio.getValue(),
                    datePickerFim.getValue()
            );

            for (var entry : historico.entrySet()) {
                String nomeCripto = entry.getKey();
                var saldoPorDia = entry.getValue();

                XYChart.Series<String, Number> serie = new XYChart.Series<>();
                serie.setName(user.getNome() + " - " + nomeCripto);

                for (var saldoEntry : saldoPorDia.entrySet()) {
                    String dataStr = saldoEntry.getKey();
                    double valor = saldoEntry.getValue();

                    if (mostrarEmEuros) {
                        valor *= isep.crescendo.Repository.HistoricoValorRepository.getValorByCriptoNomeAndData(
                                nomeCripto, dataStr);
                    }

                    XYChart.Data<String, Number> data = new XYChart.Data<>(dataStr, valor);
                    serie.getData().add(data);

                    // Tooltip melhorado para cada ponto do gráfico
                    data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            Tooltip tooltip = new Tooltip("Data: " + data.getXValue() + "\nValor: " + String.format("%.2f", data.getYValue().doubleValue()) + (mostrarEmEuros ? " €" : ""));
                            tooltip.setShowDelay(Duration.millis(100));
                            Tooltip.install(newNode, tooltip);
                            newNode.setStyle("-fx-background-color: white, #00ffcc; -fx-background-insets: 0, 2;");
                        }
                    });
                }

                // Estilo da linha (cor e espessura)
                final int finalCorIndex = corIndex;
                serie.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        String cor = cores[finalCorIndex % cores.length];
                        newNode.setStyle("-fx-stroke: " + cor + "; -fx-stroke-width: 2px;");
                    }
                });

                lineChart.getData().add(serie);

                // Aplica a mesma cor à legenda
                final String corLegenda = cores[finalCorIndex % cores.length];
                Platform.runLater(() -> {
                    for (Node node : lineChart.lookupAll(".chart-legend-item")) {
                        if (node instanceof Label label && label.getText().equals(serie.getName())) {
                            label.setStyle("-fx-text-fill: " + corLegenda + ";");
                        }
                    }
                });
                corIndex++;
            }
        }

        // Estilo do gráfico (modo escuro)
        lineChart.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");
        xAxis.setTickLabelFill(javafx.scene.paint.Color.WHITE);
        yAxis.setTickLabelFill(javafx.scene.paint.Color.WHITE);
    }


    @FXML
    private void handleVerGraficoTransacoes() {
        if (userTable == null || comboTransacoesModo == null || lineChart == null || datePickerInicio == null || datePickerFim == null) {
            mostrarAlerta("Esta funcionalidade só está disponível na Gestão de Utilizadores.");
            return;
        }

        var selecionados = userTable.getSelectionModel().getSelectedItems();
        if (selecionados.isEmpty() || datePickerInicio.getValue() == null || datePickerFim.getValue() == null) {
            mostrarAlerta("Selecione pelo menos um utilizador e um intervalo de datas.");
            return;
        }

        boolean mostrarVolumeEuros = "Volume em Euros".equals(comboTransacoesModo.getValue());

        lineChart.setCreateSymbols(true); // necessário para mostrar os pontos (nodes)
        lineChart.getData().clear();
        xAxis.setLabel("Data");
        yAxis.setLabel(mostrarVolumeEuros ? "Volume em Euros" : "Número de Transações");

        var historico = isep.crescendo.Repository.TransacaoRepository.getVolumeTransacoesPorDia(
                selecionados.stream().map(u -> u.getId()).toList(),
                datePickerInicio.getValue(),
                datePickerFim.getValue(),
                mostrarVolumeEuros
        );

        String[] cores = {"#FF5733", "#33C1FF", "#75FF33", "#F933FF", "#FFC133", "#33FFB5", "#FF33A8"};
        int corIndex = 0;

        for (var entry : historico.entrySet()) {
            int userId = entry.getKey();
            String userName = users.stream().filter(u -> u.getId() == userId).findFirst().map(u -> u.getNome()).orElse("User " + userId);

            var transacoesPorDia = entry.getValue();
            XYChart.Series<String, Number> serie = new XYChart.Series<>();
            serie.setName(userName);

            List<XYChart.Data<String, Number>> dataList = new ArrayList<>();
            for (var transEntry : transacoesPorDia.entrySet()) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(transEntry.getKey(), transEntry.getValue());
                dataList.add(data);
            }
            serie.getData().addAll(dataList);

// Só instala os tooltips depois de os nodes estarem visíveis
            Platform.runLater(() -> {
                for (XYChart.Data<String, Number> data : dataList) {
                    Node node = data.getNode();
                    if (node != null) {
                        String tooltipText = userName + "\nData: " + data.getXValue() + "\nValor: " + data.getYValue() + (mostrarVolumeEuros ? " €" : " transações");

                        Tooltip tooltip = new Tooltip(tooltipText);
                        tooltip.setShowDelay(Duration.millis(100));
                        Tooltip.install(node, tooltip);
                        node.setStyle("-fx-cursor: hand; -fx-background-radius: 4px; -fx-background-color: #00ffcc, white;");
                    }
                }
            });

            // Cor e espessura da linha
            final int finalCorIndex = corIndex;
            serie.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    String cor = cores[finalCorIndex % cores.length];
                    newNode.setStyle("-fx-stroke: " + cor + "; -fx-stroke-width: 2px;");
                }
            });

            lineChart.getData().add(serie);
            final String corLegenda = cores[finalCorIndex % cores.length];

// Aguarda render da cena para aplicar estilo à legenda
            Platform.runLater(() -> {
                for (Node node : lineChart.lookupAll(".chart-legend-item")) {
                    if (node instanceof Label label && label.getText().equals(serie.getName())) {
                        label.setStyle("-fx-text-fill: " + corLegenda + ";");
                    }
                }
            });
            corIndex++;
        }

        // Estilo do gráfico (opcional)
        lineChart.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");
        xAxis.setTickLabelFill(javafx.scene.paint.Color.WHITE);
        yAxis.setTickLabelFill(javafx.scene.paint.Color.WHITE);
    }

    @FXML
    private void handleExportarCSV() {
        if (lineChart == null) {
            mostrarAlerta("Esta funcionalidade só está disponível na Gestão de Utilizadores.");
            return;
        }

        StringBuilder csv = new StringBuilder();
        csv.append("Utilizador, Série, Data, Valor\n");

        for (var series : lineChart.getData()) {
            String serieName = series.getName();
            for (var dataPoint : series.getData()) {
                csv.append("\"").append(serieName).append("\",")
                        .append(",").append(dataPoint.getXValue()).append(",")
                        .append(dataPoint.getYValue()).append("\n");
            }
        }

        try {
            java.nio.file.Files.write(java.nio.file.Paths.get("export_grafico.csv"), csv.toString().getBytes());
            mostrarAlerta("Exportação concluída com sucesso: export_grafico.csv");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro ao salvar o ficheiro.");
        }
    }

    private void updateAtividade() {
        var map = new TransacaoRepository().getNumeroTransacoesPorData(LocalDate.now().minusDays(30).atStartOfDay(), LocalDate.now().atTime(23,59,59));
        int diasComTransacoes = (int) map.values().stream().filter(v -> v > 0).count();
        double percent = diasComTransacoes / 30.0;
        progressAtividade.setProgress(percent);
        labelAtividadePercent.setText(String.format("%.0f%%", percent * 100));
    }

    private void carregarLineChartVolumeGlobal() {
        lineChartVolumeGlobal.getData().clear();
        var series = new XYChart.Series<String, Number>();
        series.setName("Volume de Transações");

        new TransacaoRepository().getNumeroTransacoesPorData(
                LocalDate.now().minusDays(30).atStartOfDay(),
                LocalDate.now().atTime(23,59,59)
        ).forEach((dia, valor) -> {
            XYChart.Data<String, Number> data = new XYChart.Data<>(dia, valor);
            series.getData().add(data);
            addTooltipToData(data, "Volume", "€");
        });

        lineChartVolumeGlobal.getData().add(series);
    }

    private void carregarBarChartTopUsers() {
        barChartTopUsers.getData().clear();
        var series = new XYChart.Series<String, Number>();
        series.setName("Volume em Euros");

        new TransacaoRepository().getVolumePorUtilizador()
                .forEach((user, valor) -> {
                    XYChart.Data<String, Number> data = new XYChart.Data<>(user, valor);
                    series.getData().add(data);
                    addTooltipToData(data, user, "€");
                });

        barChartTopUsers.getData().add(series);
    }

    private void carregarPieChartTop3Moedas() {
        pieChartTop3Moedas.getData().clear();
        var list = new TransacaoRepository().getTop3MoedasMaisTransacoes();

        for (var row : list) {
            String nomeMoeda = (String) row[0];
            long numTransacoes = (long) row[1];

            PieChart.Data data = new PieChart.Data(nomeMoeda, numTransacoes);
            pieChartTop3Moedas.getData().add(data);
            addTooltipToPieDataT(data, nomeMoeda, "");
    }
    }

    private void carregarPieChartVolumePorMoeda() {
        pieChartVolumePorMoeda.getData().clear();
        var repo = new CriptomoedaRepository();
        new TransacaoRepository().getVolumePorMoeda()
                .forEach((id, valor) -> {
                    String nome = repo.getNomeById(id);
                    PieChart.Data data = new PieChart.Data(nome, valor);
                    pieChartVolumePorMoeda.getData().add(data);
                    addTooltipToPieData(data, nome, "€");
                });
    }

    // ————— Método genérico para tooltips em XYChart.Data —————

    private void addTooltipToData(XYChart.Data<String, Number> data, String nome, String unidade) {
        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                Platform.runLater(() -> {
                    String tooltipText = nome + ": " + data.getYValue() + " " + unidade;

                    Tooltip tooltip = new Tooltip(tooltipText);
                    tooltip.setShowDelay(Duration.millis(100));
                    Tooltip.install(newNode, tooltip);

                    newNode.setStyle("-fx-cursor: hand; -fx-background-radius: 4px; -fx-background-color: #00ffcc, white;");
                });
            }
        });
    }

    // ————— Método genérico para tooltips em PieChart.Data —————



    private void addTooltipToPieData(PieChart.Data data, String nome, String unidade) {
        Platform.runLater(() -> {
            double total = pieChartVolumePorMoeda.getData().stream()
                    .mapToDouble(PieChart.Data::getPieValue)
                    .sum();

            double percent = total > 0 ? (data.getPieValue() / total) * 100 : 0;

            String tooltipText = nome + ": " + String.format("%.2f", data.getPieValue()) +
                    " " + unidade + " (" + String.format("%.1f", percent) + "%)";

            Tooltip tooltip = new Tooltip(tooltipText);
            tooltip.setShowDelay(Duration.millis(100));
            Tooltip.install(data.getNode(), tooltip);
            data.getNode().setStyle("-fx-cursor: hand;");
        });
    }

    private void addTooltipToPieDataT(PieChart.Data data, String nome, String unidade) {
        Platform.runLater(() -> {
            double total = pieChartVolumePorMoeda.getData().stream()
                    .mapToDouble(PieChart.Data::getPieValue)
                    .sum();

            double percent = total > 0 ? (data.getPieValue() / total) * 100 : 0;

            String tooltipText = nome + ": " + String.format("%.2f", data.getPieValue()) +
                    " " + unidade;

            Tooltip tooltip = new Tooltip(tooltipText);
            tooltip.setShowDelay(Duration.millis(100));
            Tooltip.install(data.getNode(), tooltip);
            data.getNode().setStyle("-fx-cursor: hand;");
        });
    }

    private ListCell<String> getStyledCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white; -fx-background-color: #3c3c3c;");
                }
            }
        };
    }

}

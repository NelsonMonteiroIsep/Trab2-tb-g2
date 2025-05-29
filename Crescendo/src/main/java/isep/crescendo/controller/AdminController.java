package isep.crescendo.controller;

import isep.crescendo.Repository.Carteira;
import isep.crescendo.Repository.User;
import isep.crescendo.util.SceneSwitcher;
import isep.crescendo.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import isep.crescendo.Repository.Criptomoeda;

import java.io.IOException;
import java.util.Optional;

public class AdminController {

    private final Criptomoeda criptoRepo = new Criptomoeda();
    private final ObservableList<isep.crescendo.model.Criptomoeda> criptomoedas = FXCollections.observableArrayList();
    @FXML private TableView<isep.crescendo.model.User> userTable;
    @FXML private TableColumn<isep.crescendo.model.User, Integer> idColumn;
    @FXML private TableColumn<isep.crescendo.model.User, String> nomeColumn;
    @FXML private TableColumn<isep.crescendo.model.User, String> emailColumn;
    @FXML private TableColumn<isep.crescendo.model.User, Boolean> isAdminColumn;
    @FXML private Button btnUser;
    @FXML
    private Button btnCriar, btnDesativar;

    @FXML
    private Label labelTotalUsers;

    @FXML
    private Label labelTotalMoedas;


    private final User userRepo = new User();
    private final ObservableList<isep.crescendo.model.User> users = FXCollections.observableArrayList();
    @FXML
    private TextField searchField;


    @FXML
    private TableView<isep.crescendo.model.Criptomoeda> listaCriptomoedas;

    @FXML private TableColumn<isep.crescendo.model.Criptomoeda, Integer> idCriptoColumn;
    @FXML private TableColumn<isep.crescendo.model.Criptomoeda, String> nomeCriptoColumn;
    @FXML private TableColumn<isep.crescendo.model.Criptomoeda, String> simboloColumn;
    @FXML private TableColumn<isep.crescendo.model.Criptomoeda, String> descricaoColumn;
    @FXML private TableColumn<isep.crescendo.model.Criptomoeda, Boolean> ativoColumn;
    private ObservableList<isep.crescendo.model.User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (labelTotalUsers != null){
        int totalUsers = userRepo.countUsers();
        labelTotalUsers.setText(String.valueOf(totalUsers));}

        if (idColumn != null && nomeColumn != null && emailColumn != null && isAdminColumn != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            isAdminColumn.setCellValueFactory(new PropertyValueFactory<>("admin"));

            carregarUtilizadores();

        }

        if (idCriptoColumn != null && nomeCriptoColumn != null && simboloColumn != null && descricaoColumn != null && ativoColumn != null) {
            idCriptoColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nomeCriptoColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
            simboloColumn.setCellValueFactory(new PropertyValueFactory<>("simbolo"));
            descricaoColumn.setCellValueFactory(new PropertyValueFactory<>("descricao"));
            ativoColumn.setCellValueFactory(new PropertyValueFactory<>("ativo"));

            // Se quiser, adicionar cellFactory para 'ativo' para mostrar "Sim"/"Não"
            ativoColumn.setCellFactory(column -> new TableCell<isep.crescendo.model.Criptomoeda, Boolean>() {
                @Override
                protected void updateItem(Boolean ativo, boolean empty) {
                    super.updateItem(ativo, empty);
                    if (empty || ativo == null) {
                        setText(null);
                    } else {
                        setText(ativo ? "Sim" : "Não");
                    }
                }
            });

            carregarCriptomoedas();
        }
    }

    private void carregarUtilizadores() {
        users.setAll(userRepo.listarTodos());
        userTable.setItems(users);
    }

    private void carregarCriptomoedas() {
        criptomoedas.clear();
        criptomoedas.addAll(criptoRepo.getAllCriptomoedas());
        listaCriptomoedas.setItems(criptomoedas);
    }

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
                userRepo.atualizar(selecionado);
                carregarUtilizadores();      // <--- repõe dados da DB
                userTable.refresh();
            });
        } else {
            mostrarAlerta("Selecione um utilizador primeiro.");
        }
    }

    @FXML
    private void handleTornarAdmin() {
        isep.crescendo.model.User selecionado = userTable.getSelectionModel().getSelectedItem();
        if (selecionado != null && !selecionado.isAdmin()) {

            isep.crescendo.model.Carteira carteira = isep.crescendo.Repository.Carteira.procurarPorUserId(selecionado.getId());

            if (carteira != null && carteira.getSaldo() > 0) {
                mostrarAlerta("Não é possível tornar admin. O utilizador tem saldo na carteira.");
                return;
            }

            // Apagar carteira se existir
            if (carteira != null && carteira.getSaldo() == 0) {
                Carteira.apagarPorUserId(selecionado.getId());
            }

            selecionado.setAdmin(true);
            userRepo.atualizarAdmin(selecionado);
            carregarUtilizadores();
            mostrarAlerta("Utilizador promovido a admin com sucesso.");
        } else {
            mostrarAlerta("Selecione um utilizador primeiro.");
        }
    }



    @FXML
    private void handleApagarUtilizador() {
        isep.crescendo.model.User selecionado = userTable.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            Alert confirm = new Alert(AlertType.CONFIRMATION, "Tem a certeza que quer apagar este utilizador?", ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Confirmar remoção");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                userRepo.apagar(selecionado.getId());
                carregarUtilizadores();
            }
        } else {
            mostrarAlerta("Selecione um utilizador primeiro.");
        }
    }

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
                userRepo.atualizar(selecionado);
                carregarUtilizadores();      // <--- repõe dados da DB
                userTable.refresh();
            });
        } else {
            mostrarAlerta("Selecione um utilizador primeiro.");
        }
    }

    @FXML
    private void handleVoltar(ActionEvent event) {
        SceneSwitcher.switchScene(
                "/isep/crescendo/admin-view.fxml",
                "/isep/crescendo/styles/login.css",
                "Área do Administrador",
                userTable  // qualquer Control da cena atual
        );
    }

    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    private void handleUserManagement(ActionEvent event) {
        SceneSwitcher.switchScene(
                "/isep/crescendo/admin-user-management-view.fxml",
                "/isep/crescendo/styles/login.css",
                "Marketplace",
                (Control) event.getSource()
        );
    }
    @FXML
    private void handleLogout() {
        SessionManager.setCurrentUser(null);
        SceneSwitcher.switchScene("/isep/crescendo/login-view.fxml", "/isep/crescendo/styles/login.css", "Login",searchField );
    }

    @FXML
    private void handleCripto() {
        SceneSwitcher.switchScene(
                "/isep/crescendo/admin-cripto-view.fxml",
                "/isep/crescendo/styles/login.css",
                "Admin Cripto",
                searchField
        );
    }

    @FXML
    private void handleDash() {
        SceneSwitcher.switchScene(
                "/isep/crescendo/admin-view.fxml",
                "/isep/crescendo/styles/login.css",
                "Admin Cripto",
                searchField
        );
    }



    @FXML
    private void handleCriar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/isep/crescendo/cripto-criar-dialog.fxml"));
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


    @FXML
    private void handleToggleAtivo() {
        isep.crescendo.model.Criptomoeda selecionada = listaCriptomoedas.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            boolean estadoAtual = selecionada.isAtivo();
            selecionada.setAtivo(!estadoAtual);

            // Atualiza o banco de dados, se tiver método no repo
            criptoRepo.atualizar(selecionada);

            listaCriptomoedas.refresh();

            // Atualiza o texto do botão para a próxima ação
            if (selecionada.isAtivo()) {
                btnDesativar.setText("Desativar Criptomoeda");
            } else {
                btnDesativar.setText("Ativar Criptomoeda");
            }
        } else {
            mostrarAlerta("Selecione uma criptomoeda primeiro.");
        }
    }

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

    }




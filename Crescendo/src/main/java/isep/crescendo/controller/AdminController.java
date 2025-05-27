package isep.crescendo.controller;

import isep.crescendo.model.Criptomoeda;
import isep.crescendo.model.User;
import isep.crescendo.model.UserRepository;
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
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import isep.crescendo.model.CriptomoedaRepository;
import isep.crescendo.model.Criptomoeda;

import java.io.IOException;
import java.util.Optional;

public class AdminController {

    private final CriptomoedaRepository criptoRepo = new CriptomoedaRepository();
    private final ObservableList<Criptomoeda> criptomoedas = FXCollections.observableArrayList();
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nomeColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, Boolean> isAdminColumn;
    @FXML private Button btnUser;
    @FXML
    private Button btnCriar, btnDesativar;

    @FXML
    private Label labelTotalUsers;

    @FXML
    private Label labelTotalMoedas;


    private final UserRepository userRepo = new UserRepository();
    private final ObservableList<User> users = FXCollections.observableArrayList();
    @FXML
    private TextField searchField;


    @FXML
    private TableView<Criptomoeda> listaCriptomoedas;

    @FXML private TableColumn<Criptomoeda, Integer> idCriptoColumn;
    @FXML private TableColumn<Criptomoeda, String> nomeCriptoColumn;
    @FXML private TableColumn<Criptomoeda, String> simboloColumn;
    @FXML private TableColumn<Criptomoeda, String> descricaoColumn;
    @FXML private TableColumn<Criptomoeda, Boolean> ativoColumn;
    private ObservableList<User> userList = FXCollections.observableArrayList();

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
            ativoColumn.setCellFactory(column -> new TableCell<Criptomoeda, Boolean>() {
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
        User selecionado = userTable.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            TextInputDialog dialog = new TextInputDialog(selecionado.getNome());
            dialog.setTitle("Editar Nome");
            dialog.setHeaderText("Editar nome do utilizador");
            dialog.setContentText("Novo nome:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(novoNome -> {
                selecionado.setNome(novoNome);
                userRepo.atualizar(selecionado);
                carregarUtilizadores();
            });
        } else {
            mostrarAlerta("Selecione um utilizador primeiro.");
        }
    }

    @FXML
    private void handleTornarAdmin() {
        User selecionado = userTable.getSelectionModel().getSelectedItem();
        if (selecionado != null && !selecionado.isAdmin()) {
            selecionado.setAdmin(true);
            userRepo.atualizarAdmin(selecionado);
            carregarUtilizadores();
        }
    }

    @FXML
    private void handleRemoverAdmin() {
        User selecionado = userTable.getSelectionModel().getSelectedItem();
        if (selecionado != null && selecionado.isAdmin()) {
            selecionado.setAdmin(false);
            userRepo.atualizarAdmin(selecionado);
            carregarUtilizadores();
        }
    }

    @FXML
    private void handleApagarUtilizador() {
        User selecionado = userTable.getSelectionModel().getSelectedItem();
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
                Criptomoeda cripto = controller.getNovaCripto();

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
        Criptomoeda selecionada = listaCriptomoedas.getSelectionModel().getSelectedItem();
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

        FilteredList<User> filteredData = new FilteredList<>(users, user -> {
            if (filtro == null || filtro.isEmpty()) {
                return true;
            }
            return user.getNome().toLowerCase().contains(filtro) ||
                    user.getEmail().toLowerCase().contains(filtro);
        });

        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sortedData);
    }

    }




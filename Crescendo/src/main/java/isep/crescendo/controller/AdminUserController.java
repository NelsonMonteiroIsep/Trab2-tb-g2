package isep.crescendo.controller;

import isep.crescendo.model.User;
import isep.crescendo.model.UserRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class AdminUserController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nomeColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, Boolean> isAdminColumn;

    private final UserRepository userRepo = new UserRepository();
    private final ObservableList<User> users = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        isAdminColumn.setCellValueFactory(new PropertyValueFactory<>("admin"));

        carregarUtilizadores();
    }

    private void carregarUtilizadores() {
        users.setAll(userRepo.listarTodos());
        userTable.setItems(users);
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
        Stage stage = (Stage) userTable.getScene().getWindow();
        stage.close();
        // Ou trocar para outra view se tiver um router implementado
    }

    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}


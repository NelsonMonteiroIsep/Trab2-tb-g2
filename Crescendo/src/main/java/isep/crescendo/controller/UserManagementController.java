package isep.crescendo.controller;

import isep.crescendo.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class UserManagementController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleRegister() {
        String nome = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        try {
            User novoUser = new User(email, nome, password);

            System.out.println("Novo utilizador registado: " + novoUser.getNome());
            System.out.println("email: " + novoUser.getEmail());
            System.out.println("pass com hash:"+ novoUser.getPasswordHash());
            // Voltar para login-view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/isep/crescendo/login-view.fxml"));
            Scene scene = new Scene(loader.load(), 400, 300);

            UserManagementController loginController = loader.getController();
            loginController.setMessage("Registo efetuado com sucesso. Pode agora iniciar sessão.", true);

            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(scene);

        } catch (IllegalArgumentException e) {
            setMessage(e.getMessage(), false);
        } catch (IOException e) {
            setMessage("Erro ao voltar para login.", false);
            e.printStackTrace();
        }
    }
    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.equals("admin@email.com") && password.equals("admin123")) {
            messageLabel.setText("Login bem-sucedido!");
        } else {
            messageLabel.setText("Credenciais inválidas.");
        }
    }

    @FXML
    private void handleGoToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/isep/crescendo/register-view.fxml"));
            Scene scene = new Scene(loader.load(), 400, 300);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            setMessage("Erro ao carregar tela de registo.", false);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/isep/crescendo/login-view.fxml"));
            Scene scene = new Scene(loader.load(), 400, 300);
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            setMessage("Erro ao voltar para login.", false);
            e.printStackTrace();
        }
    }

    public void setMessage(String msg, boolean isSuccess) {
        messageLabel.setText(msg);
        if (isSuccess) {
            messageLabel.setStyle("-fx-text-fill: green;");
        } else {
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
}

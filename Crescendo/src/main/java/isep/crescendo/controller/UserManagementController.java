package isep.crescendo.controller;

import isep.crescendo.Repository.UserRepository;
import isep.crescendo.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class UserManagementController {

    private final UserRepository userRepositoryRepo = new UserRepository();
    private static final Map<String, String> tokenToEmailMap = new HashMap<>();

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField recoveryEmailField;

    @FXML
    private VBox loginVBox;
    @FXML
    private VBox registerVBox;
    @FXML
    private VBox recoveryVBox;

    @FXML
    private Label messageLabel;
    @FXML
    private Label messageLabelRegister;
    @FXML
    private Label recoveryMessageLabel;

    @FXML
    private StackPane root;
    @FXML
    private VBox mainVBox;

    // Referência para o MainController
    private MainController mainController;

    // Callback para login
    private LoginCallback loginCallback;

    public void setLoginCallback(LoginCallback callback) {
        this.loginCallback = callback;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        showLoginForm();
    }

    private void setMessage(String msg, boolean isSuccess, Label targetLabel) {
        if (targetLabel != null) {
            targetLabel.setText(msg);
            if (isSuccess) {
                targetLabel.setStyle("-fx-text-fill: green;");
            } else {
                targetLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    // Métodos de Transição

    @FXML
    private void handleGoToRegister() {
        if (mainController != null) {
            mainController.loadContent("RegisterView.fxml");
        } else {
            System.err.println("ERRO: mainController é null em handleGoToRegister.");
        }
    }

    @FXML
    private void handleGoToLogin() {
        if (mainController != null) {
            mainController.loadContent("UserManagementView.fxml");
        } else {
            System.err.println("ERRO: mainController é null em handleGoToLogin.");
        }
    }

    @FXML
    private void handleGoToRecovery() {
        if (mainController != null) {
            mainController.loadContent("RecoveryView.fxml");
        } else {
            System.err.println("ERRO: mainController é null em handleGoToRecovery.");
        }
    }

    // Métodos antigos para mostrar/ocultar VBoxes (mantidos caso precises no futuro se usares tudo no mesmo FXML)
    private void showLoginForm() {
        setFormVisibility(loginVBox, true);
        setFormVisibility(registerVBox, false);
        setFormVisibility(recoveryVBox, false);
        setMessage("", true, messageLabel);
        setMessage("", true, messageLabelRegister);
        setMessage("", true, recoveryMessageLabel);
    }

    private void setFormVisibility(VBox formVBox, boolean visible) {
        if (formVBox != null) {
            formVBox.setVisible(visible);
            formVBox.setManaged(visible);
        }
    }

    // Lógica de Negócio

    @FXML
    private void handleRegister() {
        String nome = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        try {
            isep.crescendo.model.User novoUser = new isep.crescendo.model.User(email, nome, password);
            userRepositoryRepo.adicionar(novoUser);
            setMessage("Registo bem-sucedido! Por favor, faça login.", true, messageLabelRegister);

            // Depois do registo, volta para login
            if (mainController != null) {
                mainController.loadContent("UserManagementView.fxml");
            }

        } catch (IllegalArgumentException e) {
            setMessage(e.getMessage(), false, messageLabelRegister);
        } catch (RuntimeException e) {
            setMessage("Erro ao registar utilizador: " + e.getMessage(), false, messageLabelRegister);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        try {
            isep.crescendo.model.User user = userRepositoryRepo.procurarPorEmail(email);

            if (user != null && user.verificarPassword(password)) {
                setMessage("Login bem-sucedido! Bem-vindo, " + user.getNome(), true, messageLabel);
                SessionManager.setCurrentUser(user);

                if (loginCallback != null) {
                    loginCallback.onLoginSuccess(user.isAdmin());
                }

            } else {
                setMessage("Credenciais inválidas.", false, messageLabel);
            }

        } catch (RuntimeException e) {
            setMessage("Erro ao fazer login: " + e.getMessage(), false, messageLabel);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRecoverPassword() {
        String email = recoveryEmailField.getText();
        setMessage("Se o e-mail estiver registado, receberá um link de recuperação (funcionalidade não implementada).", true, recoveryMessageLabel);
    }
}

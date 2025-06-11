package isep.crescendo.controller;

import isep.crescendo.Repository.UserRepository;
import isep.crescendo.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*; // Consolidated imports for common controls
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Removed: import java.io.IOException;
// Removed: import java.net.URL;
// Removed: import javafx.beans.binding.Bindings;
// Removed: import java.awt.event.ActionEvent; // Ensure this is not present, use javafx.event.ActionEvent in relevant controllers

import java.util.HashMap;
import java.util.Map;

public class UserManagementController {

    private final UserRepository userRepositoryRepo = new UserRepository();
    // Assuming tokenToEmailMap is for a feature not fully shown, if not used, remove.
    private static final Map<String, String> tokenToEmailMap = new HashMap<>();

    // Campos do formulário de Registo (assumindo que todos os campos estão neste FXML)
    @FXML
    private TextField nameField; // Ensure this is explicitly TextField

    // Campos do formulário de Login e Registo (se partilharem fx:id's no FXML)
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    // Campos do formulário de Recuperação de Password
    @FXML
    private TextField recoveryEmailField;

    // VBoxes para gerir a visibilidade dos formulários
    @FXML
    private VBox loginVBox;
    @FXML
    private VBox registerVBox;
    @FXML
    private VBox recoveryVBox;

    // Labels de mensagem
    @FXML
    private Label messageLabel;
    @FXML
    private Label messageLabelRegister;
    @FXML
    private Label recoveryMessageLabel;

    // Elementos gráficos - REMOVIDO: backgroundImageView e logoImageView
    @FXML
    private StackPane root; // O StackPane principal do UserManagementView.fxml
    @FXML
    private VBox mainVBox; // Assumindo que este VBox envolve o conteúdo principal.

    // Callback para o MainController (para notificar sucesso de login)
    private LoginCallback loginCallback;

    public void setLoginCallback(LoginCallback callback) {
        this.loginCallback = callback;
    }

    // Método de inicialização, chamado quando o FXML é carregado
    @FXML
    public void initialize() {
        // REMOVIDO: TODO O CÓDIGO RELACIONADO A backgroundImageView e logoImageView
        // REMOVIDO AQUI: FXMLLoader userManagementLoader = new FXMLLoader(getClass().getResource("/isep/crescendo/view/UserManagementView.fxml"));
        // Esta linha era redundante e potencialmente problemática.

        // Inicialmente, mostra apenas o formulário de login
        showLoginForm();
    }

    /**
     * Define a mensagem na Label apropriada e o seu estilo.
     * @param msg A mensagem a exibir.
     * @param isSuccess True para sucesso (verde), False para erro (vermelho).
     * @param targetLabel A Label onde a mensagem será exibida (e.g., messageLabel, messageLabelRegister).
     */
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

    // --- Métodos de Transição Interna (Gerir Visibilidade dos VBoxes) ---

    @FXML
    private void handleGoToRegister() {
        showRegisterForm();
    }

    @FXML
    private void handleGoToLogin() {
        showLoginForm();
    }

    @FXML
    private void handleGoToRecovery() {
        showRecoveryForm();
    }

    // Métodos para mostrar/esconder VBoxes
    private void showLoginForm() {
        setFormVisibility(loginVBox, true);
        setFormVisibility(registerVBox, false);
        setFormVisibility(recoveryVBox, false);
        // Limpa mensagens ao trocar de formulário
        setMessage("", true, messageLabel);
        setMessage("", true, messageLabelRegister);
        setMessage("", true, recoveryMessageLabel);
    }

    private void showRegisterForm() {
        setFormVisibility(loginVBox, false);
        setFormVisibility(registerVBox, true);
        setFormVisibility(recoveryVBox, false);
        // Limpa mensagens ao trocar de formulário
        setMessage("", true, messageLabel);
        setMessage("", true, messageLabelRegister);
        setMessage("", true, recoveryMessageLabel);
    }

    private void showRecoveryForm() {
        setFormVisibility(loginVBox, false);
        setFormVisibility(registerVBox, false);
        setFormVisibility(recoveryVBox, true);
        // Limpa mensagens ao trocar de formulário
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

    // --- Lógica de Negócio (Login e Registo) ---

    @FXML
    private void handleRegister() {
        String nome = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        try {
            isep.crescendo.model.User novoUser = new isep.crescendo.model.User(email, nome, password);
            userRepositoryRepo.adicionar(novoUser);
            setMessage("Registo bem-sucedido! Por favor, faça login.", true, messageLabelRegister);
            showLoginForm();

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
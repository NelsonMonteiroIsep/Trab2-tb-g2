package isep.crescendo.controller;

import isep.crescendo.Repository.UserRepository;
import isep.crescendo.util.SessionManager;
import javafx.beans.binding.Bindings; // Esta importação pode ser removida se Bindings não for mais usado
import javafx.fxml.FXML;
import javafx.scene.control.*;
// import javafx.scene.image.Image; // Pode ser removida
// import javafx.scene.image.ImageView; // Pode ser removida
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException; // Esta importação pode ser removida se IOException não for mais usada
import java.net.URL; // Esta importação pode ser removida se URL não for mais usada
import java.util.HashMap;
import java.util.Map;

public class UserManagementController {

    private final UserRepository userRepositoryRepo = new UserRepository();
    private static final Map<String, String> tokenToEmailMap = new HashMap<>();

    // Campos do formulário de Registo (assumindo que todos os campos estão neste FXML)
    @FXML
    private TextField nameField;

    // Campos do formulário de Login
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
        // (Isso inclui as linhas 75-84 do seu código anterior)

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
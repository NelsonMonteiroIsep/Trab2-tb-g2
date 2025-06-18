package isep.crescendo.controller;

import isep.crescendo.Repository.UserRepository;
import isep.crescendo.util.EmailService;
import isep.crescendo.util.SceneSwitcher;
import isep.crescendo.util.SessionManager;
import isep.crescendo.util.TokenInfo;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManagementController {

    private final UserRepository userRepositoryRepo = new UserRepository();
    private static final Map<String, String> tokenToEmailMap = new HashMap<>();
    @FXML
    private Label passwordHintLabel;

    @FXML
    private TextField nameField;
    @FXML
    private PasswordField newPasswordField;

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField recoveryEmailField;
    @FXML
    private TextField tokenField;

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
    private final UserRepository userRepo = new UserRepository();

    // Referência para o MainController
    private MainController mainController;

    // Callback para login
    private LoginCallback loginCallback;

    @FXML
    private Label resetMessageLabel;
    private static final Map<String, TokenInfo> tokens = new HashMap<>();

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

            // Limpar mensagens
            passwordHintLabel.setText("");
            setMessage("Registo bem-sucedido! Por favor, faça login.", true, messageLabelRegister);

            if (mainController != null) {
                mainController.loadContent("UserManagementView.fxml");
            }

        } catch (IllegalArgumentException e) {
            // Mostrar erro na label de password
            passwordHintLabel.setText(e.getMessage());
            setMessage("", false, messageLabelRegister); // Limpa label principal
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

                // NOVO: redireciona consoante se é admin ou não
                if (mainController != null) {
                    if (user.isAdmin()) {
                        mainController.loadContent("admin-view.fxml");
                    } else {
                        mainController.loadContent("HomeView.fxml");
                    }
                } else {
                    System.err.println("ERRO: mainController é null em handleLogin.");
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
    private void handleSendRecoveryCode() {
        String email = emailField.getText();

        if (email.isEmpty()) {
            messageLabel.setText("Insere o teu email.");
            return;
        }

        if (userRepo.procurarPorEmail(email) != null) {
            // 1) Gera e envia token
            String token = gerarTokenParaEmail(email);
            EmailService.enviarTokenRedefinicao(email, token);

            // 2) Mensagem de sucesso
            messageLabel.setText("Código enviado. Verifica o teu email.");
            messageLabel.setStyle("-fx-text-fill: green;");

            if (mainController != null) {
                mainController.loadContent("reset-password-view.fxml");
            } else {
                System.err.println("ERRO: mainController é null em handleGoToRecovery.");
            }
        } else {
            messageLabel.setText("Email não encontrado.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleRecoverPassword() {
        String email = recoveryEmailField.getText();
        setMessage("Se o e-mail estiver registado, receberá um link de recuperação (funcionalidade não implementada).", true, recoveryMessageLabel);
    }

    public static String gerarTokenParaEmail(String email) {
        String token = UUID.randomUUID().toString().substring(0, 6);
        tokens.put(token, new TokenInfo(email));
        return token;
    }

    public static String getEmailByToken(String token) {
        TokenInfo info = tokens.get(token);
        if (info == null) return null;

        // verifica expiração
        if (info.isExpired()) {
            // expirou: remove e devolve null
            tokens.remove(token);
            return null;
        }

        return info.getEmail();
    }

    public static void removeToken(String token) {
        tokens.remove(token);
    }


    @FXML
    private void handleResetPassword() {
        String token = tokenField.getText();
        String novaPassword = newPasswordField.getText();

        if (token.isEmpty() || novaPassword.isEmpty()) {
            resetMessageLabel.setText("Preenche todos os campos.");
            resetMessageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        String email = getEmailByToken(token);

        if (email != null) {
            isep.crescendo.model.User user = userRepo.procurarPorEmail(email);
            if (user != null) {
                try {
                    // ⚠️ Validação ocorre no setPassword() — pode lançar IllegalArgumentException
                    user.setPassword(novaPassword);
                    userRepo.atualizar(user);
                    removeToken(token);

                    resetMessageLabel.setText("Password redefinida com sucesso!");
                    resetMessageLabel.setStyle("-fx-text-fill: green;");

                    PauseTransition pause = new PauseTransition(Duration.seconds(3));
                    pause.setOnFinished(evt -> {
                        SceneSwitcher.switchScene(
                                "/isep/crescendo/view/UserManagementView.fxml",
                                "/isep/crescendo/styles/login.css",
                                "Login",
                                tokenField
                        );
                    });
                    pause.play();

                } catch (IllegalArgumentException e) {
                    resetMessageLabel.setText(e.getMessage());
                    resetMessageLabel.setStyle("-fx-text-fill: red;");
                } catch (Exception e) {
                    resetMessageLabel.setText("Erro ao redefinir password.");
                    resetMessageLabel.setStyle("-fx-text-fill: red;");
                    e.printStackTrace();
                }
            } else {
                resetMessageLabel.setText("Utilizador não encontrado.");
                resetMessageLabel.setStyle("-fx-text-fill: red;");
            }
        } else {
            resetMessageLabel.setText("Token inválido ou expirado.");
            resetMessageLabel.setStyle("-fx-text-fill: red;");
        }
    }


    private String getPasswordHint(String password) {
        StringBuilder hints = new StringBuilder();

        if (password.length() < 10)
            hints.append("• Mínimo 10 caracteres\n");
        if (!password.matches(".*[A-Z].*"))
            hints.append("• Pelo menos 1 letra maiúscula\n");
        if (!password.matches(".*\\d.*"))
            hints.append("• Pelo menos 1 número\n");
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?].*"))
            hints.append("• Pelo menos 1 carácter especial\n");

        return hints.toString().trim();
    }
}

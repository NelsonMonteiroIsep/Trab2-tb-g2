package isep.crescendo.controller;

import isep.crescendo.model.User;
import isep.crescendo.model.UserRepository;
import isep.crescendo.util.EmailService;
import isep.crescendo.util.SceneSwitcher;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResetPasswordController {

    @FXML
    private TextField tokenField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private Label resetMessageLabel;
    private static final Map<String, String> tokenToEmailMap = new HashMap<>();
    @FXML
    private TextField emailField;
    @FXML
    private Label messageLabel;
    @FXML
    private TextField nameField;

    private final UserRepository userRepo = new UserRepository();


    @FXML
    private void handleResetPassword() {
        String token = tokenField.getText();
        String novaPassword = newPasswordField.getText();

        if (token.isEmpty() || novaPassword.isEmpty()) {
            resetMessageLabel.setText("Preenche todos os campos.");
            return;
        }

        String email = getEmailByToken(token);

        if (email != null) {
            User user = userRepo.procurarPorEmail(email);
            if (user != null) {
                try {
                    user.setPassword(novaPassword);
                    userRepo.atualizar(user);
                    removeToken(token);

                    // Mensagem de sucesso
                    resetMessageLabel.setText("Password redefinida com sucesso!");
                    resetMessageLabel.setStyle("-fx-text-fill: green;");

                    // Aguarda 3 segundos e chama handleGoToLogin()
                    PauseTransition pause = new PauseTransition(Duration.seconds(3));
                    pause.setOnFinished(evt -> {
                        SceneSwitcher.switchScene(
                                "/isep/crescendo/login-view.fxml",
                                "/isep/crescendo/styles/login.css",
                                "Login",
                                tokenField    // nó válido nesta cena
                        );
                    });
                    pause.play();

                } catch (Exception e) {
                    resetMessageLabel.setText("Erro ao redefinir password.");
                    e.printStackTrace();
                }
            } else {
                resetMessageLabel.setText("Utilizador não encontrado.");
            }
        } else {
            resetMessageLabel.setText("Token inválido ou expirado.");
        }
    }
    public static String gerarTokenParaEmail(String email) {
        String token = UUID.randomUUID().toString().substring(0, 6); // ex: 6 chars
        tokenToEmailMap.put(token, email);
        return token;
    }

    public static String getEmailByToken(String token) {
        return tokenToEmailMap.get(token);
    }

    public static void removeToken(String token) {
        tokenToEmailMap.remove(token);
    }

    @FXML
    private void handleGoToLogin() {
        SceneSwitcher.switchScene("/isep/crescendo/login-view.fxml", "/isep/crescendo/styles/login.css", "Login", nameField);
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

            // 3) Vai para o reset-password.fxml
            SceneSwitcher.switchScene(
                    "/isep/crescendo/reset-password-view.fxml",   // caminho do FXML de reset
                    "/isep/crescendo/styles/login.css",      // CSS que quiseres
                    "Redefinir Password",                    // título da janela
                    emailField                               // qualquer Node da cena atual
            );
        } else {
            messageLabel.setText("Email não encontrado.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
}

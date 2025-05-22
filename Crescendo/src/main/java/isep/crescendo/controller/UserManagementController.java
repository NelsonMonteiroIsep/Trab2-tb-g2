package isep.crescendo.controller;

import isep.crescendo.model.User;
import isep.crescendo.model.UserRepository;
import isep.crescendo.util.SceneSwitcher;
import isep.crescendo.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManagementController {
    private final UserRepository userRepo = new UserRepository();
    private static final Map<String, String> tokenToEmailMap = new HashMap<>();
    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;
    @FXML
    private ImageView backgroundImageView;
    @FXML
    private StackPane root;
    @FXML
    private TextField recoveryEmailField;
    @FXML
    private Label recoveryMessageLabel;


    @FXML
    private void handleRegister() {
        String nome = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        try {
            User novoUser = new User(email, nome, password);
            userRepo.adicionar(novoUser);

            URL fxmlLocation = getClass().getResource("/isep/crescendo/login-view.fxml");
            System.out.println(fxmlLocation);

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Parent root = fxmlLoader.load();

            Scene scene = new Scene(root);

            URL cssLocation = getClass().getResource("/isep/crescendo/styles/login.css");
            if (cssLocation != null) {
                scene.getStylesheets().add(cssLocation.toExternalForm());
            } else {
                System.err.println("Arquivo CSS não encontrado!");
            }

            Stage stage = (Stage) nameField.getScene().getWindow();

            // Guarda o tamanho atual da janela
            double width = stage.getWidth();
            double height = stage.getHeight();

            stage.setTitle("Login");
            stage.setScene(scene);

            // Restaura o tamanho para evitar resize da janela
            stage.setWidth(width);
            stage.setHeight(height);

            stage.show();

        } catch (IllegalArgumentException e) {
            setMessage(e.getMessage(), false);
        } catch (RuntimeException | IOException e) {
            setMessage("Erro ao registar utilizador: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        try {
            User user = userRepo.procurarPorEmail(email);

            if (user != null && user.verificarPassword(password)) {
                setMessage("Login bem-sucedido! Bem-vindo, " + user.getNome(), true);
                SessionManager.setCurrentUser(user);

                // Transição para a market-view
                URL fxmlLocation = getClass().getResource("/isep/crescendo/market-view.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
                Parent root = fxmlLoader.load();

                Scene scene = new Scene(root);

                // Aplica CSS, se necessário
                URL cssLocation = getClass().getResource("/isep/crescendo/styles/login.css");
                if (cssLocation != null) {
                    scene.getStylesheets().add(cssLocation.toExternalForm());
                }

                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setTitle("Marketplace");
                stage.setScene(scene);
                stage.show();

            } else {
                setMessage("Credenciais inválidas.", false);
            }

        } catch (RuntimeException | IOException e) {
            setMessage("Erro ao fazer login: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoToRegister() {
        SceneSwitcher.switchScene("/isep/crescendo/register-view.fxml", "/isep/crescendo/styles/login.css", "Registo", emailField);
    }

    @FXML
    private void handleGoToLogin() {
        SceneSwitcher.switchScene("/isep/crescendo/login-view.fxml", "/isep/crescendo/styles/login.css", "Login", nameField);
    }

    @FXML
    private void handleGoToRecovery() {
        SceneSwitcher.switchScene("/isep/crescendo/forgot-password-view.fxml", "/isep/crescendo/styles/login.css", "Recuperar Password", emailField);
    }


    public void setMessage(String msg, boolean isSuccess) {
        messageLabel.setText(msg);
        if (isSuccess) {
            messageLabel.setStyle("-fx-text-fill: green;");
        } else {
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    public void initialize() {
        // Load background image
        Image bgImage = new Image(getClass().getResourceAsStream("/isep/crescendo/images/background.jpg"));
        backgroundImageView.setImage(bgImage);

        // Bind ImageView size to StackPane size
        backgroundImageView.fitWidthProperty().bind(root.widthProperty());
        backgroundImageView.fitHeightProperty().bind(root.heightProperty());
    }




}

package isep.crescendo.controller;

import isep.crescendo.model.User;
import isep.crescendo.model.UserRepository;
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

public class UserManagementController {
    private final UserRepository userRepo = new UserRepository();
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
            } else {
                setMessage("Credenciais inválidas.", false);
            }

        } catch (RuntimeException e) {
            setMessage("Erro na ligação à base de dados.", false);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoToRegister() {
        try {
            URL fxmlLocation = getClass().getResource("/isep/crescendo/register-view.fxml");
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

            Stage stage = (Stage) emailField.getScene().getWindow();

            // Guarda tamanho atual da janela
            double width = stage.getWidth();
            double height = stage.getHeight();

            stage.setTitle("Registo");
            stage.setScene(scene);

            // Restaura o tamanho para evitar resize
            stage.setWidth(width);
            stage.setHeight(height);

            stage.show();

        } catch (IOException e) {
            setMessage("Erro ao carregar tela de registo.", false);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/isep/crescendo/login-view.fxml"));
            Scene scene = new Scene(loader.load());
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

    public void initialize() {
        // Load background image
        Image bgImage = new Image(getClass().getResourceAsStream("/isep/crescendo/images/background.jpg"));
        backgroundImageView.setImage(bgImage);

        // Bind ImageView size to StackPane size
        backgroundImageView.fitWidthProperty().bind(root.widthProperty());
        backgroundImageView.fitHeightProperty().bind(root.heightProperty());
    }


}

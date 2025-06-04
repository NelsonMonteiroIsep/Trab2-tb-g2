package isep.crescendo.controller;

import isep.crescendo.util.SceneSwitcher;
import isep.crescendo.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class LeftBarController {

    public Label userNameLabel;
    @FXML
    private Label menu_label;
    @FXML
    private void handleLogout() {
        SessionManager.setCurrentUser(null);
        SceneSwitcher.switchScene("/isep/crescendo/login-view.fxml", "/isep/crescendo/styles/login.css", "Login", menu_label);
    }
    @FXML
    private void handleRedirectWallet(ActionEvent mouseEvent){
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/isep/crescendo/wallet-view.fxml"));


            Parent newPage = loader.load();


            Scene newScene = new Scene(newPage);


            newScene.getStylesheets().add(getClass().getResource("/isep/crescendo/styles/login.css").toExternalForm());


            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();


            stage.setScene(newScene);


            stage.show();


        } catch (IOException e) {
            e.printStackTrace(); // Ou melhor: logar o erro
        }
    }

    public void handleRedirectPortfolio(ActionEvent actionEvent) {
        SceneSwitcher.switchScene("/isep/crescendo/portfolio-view.fxml", "/isep/crescendo/styles/login.css", "Carteira", menu_label);
    }

    public void handleRedirectEnviar(ActionEvent actionEvent) {
        SceneSwitcher.switchScene("/isep/crescendo/enviar-view.fxml", "/isep/crescendo/styles/login.css", "Carteira", menu_label);
    }

    public void handleRedirectReceber(ActionEvent actionEvent) {
        SceneSwitcher.switchScene("/isep/crescendo/receber-view.fxml", "/isep/crescendo/styles/login.css", "Carteira", menu_label);
    }

    public void handleRedirectSwap(ActionEvent actionEvent) {
        SceneSwitcher.switchScene("/isep/crescendo/swap-view.fxml", "/isep/crescendo/styles/login.css", "Carteira", menu_label);
    }

    public void handleRedirectGestao(ActionEvent actionEvent) {
        SceneSwitcher.switchScene("/isep/crescendo/gestao-view.fxml", "/isep/crescendo/styles/login.css", "Carteira", menu_label);
    }
}

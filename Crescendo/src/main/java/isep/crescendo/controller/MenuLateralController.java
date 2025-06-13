package isep.crescendo.controller;

import isep.crescendo.util.SceneSwitcher;
import isep.crescendo.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MenuLateralController {
    @FXML
    private Label menu_label;
    @FXML
    private void handleLogout() {
        SessionManager.setCurrentUser(null);
        SceneSwitcher.switchScene("/isep/crescendo/view/login-view.fxml", "/isep/crescendo/styles/login.css", "Login", menu_label);
    }
    @FXML
    private void handleRedirectWallet(){
        SceneSwitcher.switchScene("/isep/crescendo/wallet-view.fxml", "/isep/crescendo/styles/login.css", "Carteira", menu_label);
    }
    @FXML
    public void handleRedirectPortfolio(ActionEvent actionEvent) {
        SceneSwitcher.switchScene("/isep/crescendo/market-view.fxml", "/isep/crescendo/styles/login.css", "Carteira", menu_label);
    }
    @FXML
    public void handleRedirectEnviar(ActionEvent actionEvent) {
        SceneSwitcher.switchScene("/isep/crescendo/enviar-view.fxml", "/isep/crescendo/styles/login.css", "Carteira", menu_label);
    }
    @FXML
    public void handleRedirectReceber(ActionEvent actionEvent) {
        SceneSwitcher.switchScene("/isep/crescendo/receber-view.fxml", "/isep/crescendo/styles/login.css", "Carteira", menu_label);
    }
    @FXML
    public void handleRedirectSwap(ActionEvent actionEvent) {
        SceneSwitcher.switchScene("/isep/crescendo/swap-view.fxml", "/isep/crescendo/styles/login.css", "Carteira", menu_label);
    }
    @FXML
    public void handleRedirectGestao(ActionEvent actionEvent) {
        SceneSwitcher.switchScene("/isep/crescendo/gestao-view.fxml", "/isep/crescendo/styles/login.css", "Carteira", menu_label);
    }
    @FXML
    public void handleRedirectEmContrucao(ActionEvent actionEvent) {
        SceneSwitcher.switchScene("/isep/crescendo/underconstruction-view.fxml", "/isep/crescendo/styles/login.css", "Transações", menu_label);
    }
    @FXML
    public void handleRedirectTransacoes(ActionEvent actionEvent) {
        SceneSwitcher.switchScene("/isep/crescendo/transacoes-view.fxml", "/isep/crescendo/styles/login.css", "Transações", menu_label);
    }
}

// Controlador responsável pela barra lateral esquerda da interface de administração
package isep.crescendo.controller;

import isep.crescendo.util.SceneSwitcher;
import isep.crescendo.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class AdminLeftBarController {

    // Referência ao controlador principal para alternar o conteúdo da área central
    private MainController mainController;

    @FXML
    private Label adminLabel; // Label para mostrar informação do admin logado (se necessário)

    // Botões do menu lateral
    @FXML
    private Button dashboardButton;
    @FXML
    private Button userManagementButton;
    @FXML
    private Button cryptosButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button logoutButton;

    // Define o controlador principal para permitir a troca de conteúdo no painel principal
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Handler para quando o botão "Dashboard" for clicado
    @FXML
    private void handleDash(ActionEvent event) {
        System.out.println("AdminLeftBarController: Dashboard clicado.");
        if (mainController != null) {
            mainController.loadContent("admin-view.fxml");
        } else {
            System.err.println("ERRO (AdminLeftBarController): mainController não definido para Dashboard.");
        }
    }

    // Handler para quando o botão "Gestão de Utilizadores" for clicado
    @FXML
    private void handleUserManagement(ActionEvent event) {
        System.out.println("AdminLeftBarController: Utilizadores clicado.");
        if (mainController != null) {
            mainController.loadContent("admin-user-management-view.fxml");
        } else {
            System.err.println("ERRO (AdminLeftBarController): mainController não definido para Utilizadores.");
        }
    }

    // Handler para quando o botão "Criptomoedas" for clicado
    @FXML
    private void handleCryptos(ActionEvent event) {
        System.out.println("AdminLeftBarController: Criptomoedas clicado.");
        if (mainController != null) {
            mainController.loadContent("admin-cripto-view.fxml");
        } else {
            System.err.println("ERRO (AdminLeftBarController): mainController não definido para Criptomoedas.");
        }
    }

    // Handler para quando o botão "Definições" for clicado
    @FXML
    private void handleSettings(ActionEvent event) {
        System.out.println("AdminLeftBarController: Definições clicado.");
        if (mainController != null) {
            mainController.loadContent("admin-settings-view.fxml");
        } else {
            System.err.println("ERRO (AdminLeftBarController): mainController não definido para Definições.");
        }
    }

    // Handler para quando o botão "Logout" for clicado
    @FXML
    private void handleLogout(ActionEvent event) { // <<< MANTIDO COM ActionEvent, conforme solicitado
        System.out.println("LeftBarController: Botão de Logout clicado.");
        if (mainController != null) {
            mainController.handleLogout(event); // <<< MANTIDO COM ActionEvent, conforme solicitado
        } else {
            System.err.println("ERRO (LeftBarController): mainController não está definido para realizar o logout.");
        }
    }
}

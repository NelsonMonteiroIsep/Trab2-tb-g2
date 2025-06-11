package isep.crescendo.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class NavBarController implements Initializable {

    private MainController mainController; // Referência para o MainController

    // Este método será chamado pelo MainController após carregar a Navbar
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Lógica de inicialização da Navbar, se houver
        System.out.println("NavbarController inicializado.");
    }

    @FXML
    private void handleShowSettings(ActionEvent event) {
        if (mainController != null) {
            System.out.println("Navbar: Botão Configurações clicado. Carregando settings-view.fxml...");
            mainController.loadContent("settings-view.fxml"); // Chama o método no MainController
        } else {
            System.err.println("Erro: mainController não está definido no NavbarController.");
        }
    }

    // Adicione outros métodos de ação para outros botões na Navbar, se houver
    // Exemplo:
    // @FXML
    // private void handleShowHelp(ActionEvent event) {
    //     if (mainController != null) {
    //         mainController.loadContent("help-view.fxml");
    //     }
    // }
}
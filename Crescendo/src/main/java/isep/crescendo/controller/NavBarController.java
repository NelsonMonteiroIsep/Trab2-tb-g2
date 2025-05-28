package isep.crescendo.controller;

import isep.crescendo.util.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Control;

public class NavBarController {


    @FXML
    private Control navBarAnyControl;

    @FXML
    private void goHome(ActionEvent event) {
        SceneSwitcher.switchScene(
                "/isep/crescendo/market-view.fxml",
                "/isep/crescendo/styles/login.css",
                "Página Inicial",
                navBarAnyControl
        );
    }

    @FXML
    private void goProdutos(ActionEvent event) {
        SceneSwitcher.switchScene(
                "/isep/crescendo/produtos.fxml",
                "/isep/crescendo/styles/login.css",
                "Produtos",
                navBarAnyControl
        );
    }

    @FXML
    private void goPerfil(ActionEvent event) {
        SceneSwitcher.switchScene(
                "/isep/crescendo/perfil.fxml",
                "/isep/crescendo/styles/login.css",
                "Perfil do Usuário",
                navBarAnyControl
        );
    }

    @FXML
    private void logout(ActionEvent event) {
        System.out.println("Usuário fez logout.");
        // Pode redirecionar para tela de login, se quiser
        SceneSwitcher.switchScene(
                "/isep/crescendo/login-view.fxml",
                "/isep/crescendo/styles/login.css",
                "Login",
                navBarAnyControl
        );
    }
}

package isep.crescendo.controller;

import isep.crescendo.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox; // Importa VBox

import java.net.URL;
import java.util.ResourceBundle;

public class LeftBarController implements Initializable {

    private MainController mainController;

    @FXML
    public Label userNameLabel;

    @FXML
    private VBox loggedInContentContainer;

    @FXML
    private VBox rootVBox; // <---- ESTA DECLARAÇÃO É CRÍTICA!

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        updateUserNameLabel();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Esconde o conteúdo logado por padrão
        if (loggedInContentContainer != null) {
            loggedInContentContainer.setVisible(false);
            loggedInContentContainer.setManaged(false);
        }

        // Esconde a barra lateral inteira por padrão (no início da aplicação)
        hideEntireLeftBar(); // NOVA CHAMADA AQUI
        updateUserNameLabel();
    }

    public void updateUserNameLabel() {
        if (userNameLabel != null) {
            if (SessionManager.getCurrentUser() != null) {
                userNameLabel.setText("Olá, " + SessionManager.getCurrentUser().getNome());
                userNameLabel.setVisible(true);
                userNameLabel.setManaged(true);
            } else {
                userNameLabel.setText("Convidado");
                userNameLabel.setVisible(false);
                userNameLabel.setManaged(false);
            }
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        if (mainController != null) {
            mainController.handleLogout(event);
        } else {
            System.err.println("Erro: mainController não está definido para realizar o logout.");
        }
    }

    // --- MÉTODOS NOVOS E CRUCIAIS PARA ESCONDER/MOSTRAR A BARRA LATERAL INTEIRA ---
    public void showEntireLeftBar() {
        System.out.println("LeftBarController: showEntireLeftBar() chamado.");
        if (rootVBox != null) {
            System.out.println("LeftBarController: rootVBox NÃO é null. Definindo visível/managed para true.");
            rootVBox.setVisible(true);
            rootVBox.setManaged(true);
        } else {
            System.out.println("LeftBarController: ERRO! rootVBox é null em showEntireLeftBar().");
        }
    }

    public void hideEntireLeftBar() {
        System.out.println("LeftBarController: hideEntireLeftBar() chamado.");
        if (rootVBox != null) {
            System.out.println("LeftBarController: rootVBox NÃO é null. Definindo visível/managed para false.");
            rootVBox.setVisible(false);
            rootVBox.setManaged(false);
        } else {
            System.out.println("LeftBarController: ERRO! rootVBox é null em hideEntireLeftBar().");
        }
    }

    // --- Métodos existentes para controlar a visibilidade do conteúdo logado na barra lateral ---
    public void showLoggedInContent() {
        if (loggedInContentContainer != null) {
            loggedInContentContainer.setVisible(true);
            loggedInContentContainer.setManaged(true);
            updateUserNameLabel();
            showEntireLeftBar(); // Garante que a barra lateral apareça quando o conteúdo logado é mostrado
        }
    }

    public void hideLoggedInContent() {
        if (loggedInContentContainer != null) {
            loggedInContentContainer.setVisible(false);
            loggedInContentContainer.setManaged(false);
            updateUserNameLabel();
            // Não chame hideEntireLeftBar aqui, pois o MainController irá gerenciar isso após o logout.
        }
    }

    // --- Métodos de redirecionamento (já estavam no seu código) ---
    @FXML
    private void handleRedirectWallet(ActionEvent actionEvent){
        if (mainController != null) {
            mainController.loadContent("wallet-view.fxml");
        } else {
            System.err.println("Erro: mainController não está definido no LeftBarController para carregar wallet-view.");
        }
    }

    @FXML
    public void handleRedirectPortfolio(ActionEvent actionEvent) {
        if (mainController != null) {
            mainController.loadContent("portfolio-view.fxml");
        } else {
            System.err.println("Erro: mainController não está definido no LeftBarController para carregar portfolio-view.");
        }
    }

    @FXML
    public void handleRedirectSwap(ActionEvent actionEvent) {
        if (mainController != null) {
            mainController.loadContent("swap-view.fxml");
        } else {
            System.err.println("Erro: mainController não está definido no LeftBarController para carregar swap-view.");
        }
    }

    @FXML
    public void handleRedirectTransacoes(ActionEvent actionEvent) {
        if (mainController != null) {
            mainController.loadContent("transacoes-view.fxml");
        } else {
            System.err.println("Erro: mainController não está definido no LeftBarController para carregar transacoes-view.");
        }
    }
}
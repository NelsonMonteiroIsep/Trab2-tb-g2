package isep.crescendo.controller;

import isep.crescendo.util.SessionManager;
import javafx.event.ActionEvent; // Correct JavaFX ActionEvent
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button; // Certifique-se de importar Button
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox; // Importa VBox

import java.net.URL;
import java.util.ResourceBundle;

public class LeftBarController implements Initializable {
    @FXML
    public ImageView logoImageView;
    private MainController mainController;

    @FXML
    public Label userNameLabel;

    @FXML
    private VBox loggedInContentContainer;

    @FXML
    private VBox rootVBox; // O VBox raiz do leftbar-componente.fxml

    // Declare os botões com @FXML para que possam ser injetados do FXML
    @FXML
    private Button walletButton;
    @FXML
    private Button portfolioButton;
    @FXML
    private Button swapButton;
    @FXML
    private Button transacoesButton;
    @FXML
    private Button logoutButton; // Assumindo que você tem um botão de logout

    /**
     * Define a referência para o MainController.
     * Este método é chamado pelo MainController após carregar o leftbar-componente.fxml.
     * @param mainController A instância do MainController.
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        // Após definir o MainController, podemos atualizar o nome de usuário
        // e decidir se mostramos ou escondemos o conteúdo logado
        updateUserNameLabel();
        if (SessionManager.getCurrentUser() != null) {
            showLoggedInContent();
            showEntireLeftBar(); // Garante que a barra inteira apareça se o usuário já estiver logado na inicialização
        } else {
            hideLoggedInContent();
            hideEntireLeftBar(); // Garante que a barra inteira esteja escondida se não houver usuário
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar as ações dos botões aqui, depois que eles forem injetados pelo FXML
        if (walletButton != null) {
            walletButton.setOnAction(this::handleRedirectWallet);
        }
        if (portfolioButton != null) {
            portfolioButton.setOnAction(this::handleRedirectPortfolio);
        }
        if (swapButton != null) {
            swapButton.setOnAction(this::handleRedirectSwap);
        }
        if (transacoesButton != null) {
            transacoesButton.setOnAction(this::handleRedirectTransacoes);
        }
        if (logoutButton != null) {
            logoutButton.setOnAction(this::handleLogout);
        }
    }

    /**
     * Atualiza o texto do Label de nome de usuário e gerencia sua visibilidade.
     */
    public void updateUserNameLabel() {
        if (userNameLabel != null) {
            if (SessionManager.getCurrentUser() != null) {
                userNameLabel.setText("Olá, " + SessionManager.getCurrentUser().getNome());
                userNameLabel.setVisible(true);
                userNameLabel.setManaged(true);
            } else {
                userNameLabel.setText("Convidado"); // Exibe algo como "Convidado" ou esconde a label
                userNameLabel.setVisible(false);
                userNameLabel.setManaged(false);
            }
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) { // <<< MANTIDO COM ActionEvent, conforme solicitado
        System.out.println("LeftBarController: Botão de Logout clicado.");
        if (mainController != null) {
            mainController.handleLogout(event); // <<< MANTIDO COM ActionEvent, conforme solicitado
        } else {
            System.err.println("ERRO (LeftBarController): mainController não está definido para realizar o logout.");
        }
    }

    // --- MÉTODOS PARA GERENCIAR A VISIBILIDADE DA BARRA LATERAL INTEIRA ---
    public void showEntireLeftBar() {
        if (rootVBox != null) {
            rootVBox.setVisible(true);
            rootVBox.setManaged(true);
            System.out.println("DEBUG (LeftBarController): LeftBar inteira VISÍVEL.");
        } else {
            System.err.println("ERRO (LeftBarController): rootVBox é null em showEntireLeftBar().");
        }
    }

    public void hideEntireLeftBar() {
        if (rootVBox != null) {
            rootVBox.setVisible(false);
            rootVBox.setManaged(false);
            System.out.println("DEBUG (LeftBarController): LeftBar inteira ESCONDIDA.");
        } else {
            System.err.println("ERRO (LeftBarController): rootVBox é null em hideEntireLeftBar().");
        }
    }

    // --- Métodos para controlar a visibilidade do CONTEÚDO ESPECÍFICO DE LOGADO na barra lateral ---
    public void showLoggedInContent() {
        if (loggedInContentContainer != null) {
            loggedInContentContainer.setVisible(true);
            loggedInContentContainer.setManaged(true);
            System.out.println("DEBUG (LeftBarController): Conteúdo logado VISÍVEL.");
            updateUserNameLabel(); // Atualiza o nome de usuário ao mostrar o conteúdo
        } else {
            System.err.println("ERRO (LeftBarController): loggedInContentContainer é null em showLoggedInContent().");
        }
    }

    public void hideLoggedInContent() {
        if (loggedInContentContainer != null) {
            loggedInContentContainer.setVisible(false);
            loggedInContentContainer.setManaged(false);
            System.out.println("DEBUG (LeftBarController): Conteúdo logado ESCONDIDO.");
            updateUserNameLabel(); // Atualiza o nome de usuário ao esconder o conteúdo
        } else {
            System.err.println("ERRO (LeftBarController): loggedInContentContainer é null em hideLoggedInContent().");
        }
    }

    // --- Métodos de redirecionamento para o MainController ---
    @FXML
    private void handleRedirectWallet(ActionEvent actionEvent){
        System.out.println("LeftBarController: Botão Carteira clicado.");
        if (mainController != null) {
            // This is the call to load the wallet-view.fxml
            mainController.loadContent("WalletView.fxml");
        } else {
            System.err.println("ERRO (LeftBarController): mainController não está definido no LeftBarController para carregar wallet-view.");
        }
    }


    @FXML
    public void handleRedirectPortfolio(ActionEvent actionEvent) {
        System.out.println("LeftBarController: Botão Portfólio clicado.");
        if (mainController != null) {
            mainController.loadContent("HomeView.fxml");
        } else {
            System.err.println("ERRO (LeftBarController): mainController não está definido para carregar portfolio-view.");
        }
    }

    @FXML
    public void handleRedirectSwap(ActionEvent actionEvent) {
        System.out.println("LeftBarController: Botão Swap clicado.");
        if (mainController != null) {
            mainController.loadContent("swap-view.fxml");
        } else {
            System.err.println("ERRO (LeftBarController): mainController não está definido para carregar swap-view.");
        }
    }

    @FXML
    public void handleRedirectTransacoes(ActionEvent actionEvent) {
        System.out.println("LeftBarController: Botão Transações clicado.");
        if (mainController != null) {
            // Certifique-se de que o nome do ficheiro FXML está correto
            mainController.loadContent("TransacoesView.fxml");
        } else {
            System.err.println("ERRO (LeftBarController): mainController não está definido para carregar transacoes-view.");
        }
    }
}
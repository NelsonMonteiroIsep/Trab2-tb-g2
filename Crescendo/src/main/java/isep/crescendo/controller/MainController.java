package isep.crescendo.controller;

import isep.crescendo.model.Criptomoeda;
import isep.crescendo.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable, LoginCallback {

    @FXML
    private BorderPane rootPane;
    @FXML
    private StackPane contentArea;

    private LeftBarController leftBarController;
    private RightBarController rightBarController;
    // Removido marketController se não for usado após a remoção de navigateToMarket,
    // mas mantido se handleCriptomoedaClick ainda precisar dele.
    private MarketController marketController;

    private String currentContentFxml = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (rootPane == null) {
            System.err.println("ERRO CRÍTICO (MainController): rootPane é null no initialize. Verifique o fx:id no MainView.fxml.");
            return;
        }

        try {
            loadLeftBar();
            loadRightBar();

            isep.crescendo.model.User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                System.out.println("DEBUG (MainController): Usuário logado. Carregando wallet-view.fxml.");
                loadContent("WalletView.fxml");
            } else {
                System.out.println("DEBUG (MainController): Nenhum usuário logado. Exibindo formulário de login.");
                loadUserManagementView();
            }

        } catch (IOException e) {
            System.err.println("Erro geral ao carregar layout inicial ou gestão de utilizadores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadLeftBar() throws IOException {
        System.out.println("DEBUG (MainController): Tentando carregar LeftBarView.fxml...");
        String leftBarFxml = SessionManager.isAdminSession() ? "AdminLeftBarView.fxml" : "LeftBarView.fxml";

        FXMLLoader leftBarLoader = new FXMLLoader(getClass().getResource("/isep/crescendo/view/" + leftBarFxml));
        Parent leftBar = leftBarLoader.load();
        if (SessionManager.isAdminSession()) {
            AdminLeftBarController adminLeftBarController = leftBarLoader.getController();
            adminLeftBarController.setMainController(this);
        } else {
            leftBarController = leftBarLoader.getController();
            leftBarController.setMainController(this);
        }
        if (leftBarController != null) {
            leftBarController.setMainController(this);
            System.out.println("DEBUG (MainController): LeftBarController obtido e MainController injetado.");
        } else {
            System.err.println("ERRO (MainController): getController() retornou null para LeftBarController!");
        }
        rootPane.setLeft(leftBar);
        System.out.println("DEBUG (MainController): LeftBar definida na região LEFT do rootPane.");
    }

    // Mantido loadNavbar se estiver no seu código, mas não é central para a discussão atual
    private void loadNavbar() throws IOException {
        System.out.println("DEBUG (MainController): Tentando carregar navbar.fxml...");
        FXMLLoader navbarLoader = new FXMLLoader(getClass().getResource("/isep/crescendo/navbar.fxml"));
        Parent navbar = navbarLoader.load();
        rootPane.setTop(navbar);
        System.out.println("DEBUG (MainController): Navbar definida na região TOP do rootPane.");
    }

    private void loadRightBar() {
        System.out.println("DEBUG (MainController): Tentando carregar RightBarView.fxml...");
        try {
            FXMLLoader rightBarLoader = new FXMLLoader(getClass().getResource("/isep/crescendo/view/RightBarView.fxml"));
            Parent rightBar = rightBarLoader.load();
            System.out.println("DEBUG (MainController): RightBarView.fxml carregado. Nó Raiz: " + rightBar);

            rightBarController = rightBarLoader.getController();
            if (rightBarController != null) {
                System.out.println("DEBUG (MainController): RightBarController obtido: " + rightBarController);
                rightBarController.setMainController(this); // Injeta MainController no RightBarController
                System.out.println("DEBUG (MainController): MainController (" + this + ") passado para RightBarController (" + rightBarController + ").");
            } else {
                System.err.println("ERRO (MainController): getController() retornou null para RightBarController!");
            }

            if (rootPane != null) {
                rootPane.setRight(rightBar);
                System.out.println("DEBUG (MainController): RightBar definida na região RIGHT do rootPane.");
            } else {
                System.err.println("ERRO (MainController): rootPane é null ao tentar definir a RightBar.");
            }

        } catch (IOException e) {
            System.err.println("Erro ao carregar a barra lateral direita: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadUserManagementView() throws IOException {
        FXMLLoader userManagementLoader = new FXMLLoader(getClass().getResource("/isep/crescendo/view/UserManagementView.fxml"));
        Parent userManagementContent = userManagementLoader.load();
        UserManagementController userManagementController = userManagementLoader.getController();
        if (userManagementController != null) {
            userManagementController.setLoginCallback(this);
            userManagementController.setMainController(this); // <---- ADICIONAR ESTA LINHA
            System.out.println("DEBUG (MainController): LoginCallback e MainController definidos para UserManagementController.");
        } else {
            System.err.println("ERRO (MainController): getController() retornou null para UserManagementController!");
        }

        contentArea.getChildren().clear();
        contentArea.getChildren().add(userManagementContent);
        this.currentContentFxml = "UserManagementView.fxml";
        System.out.println("DEBUG (MainController): Conteúdo 'UserManagementView.fxml' carregado na inicialização.");
    }

    public void loadContent(String fxmlFileName) {
        loadContentWithObject(fxmlFileName, null); // Chama o método mais genérico

    }

    public <T> void loadContentWithObject(String fxmlFileName, T dataObject) {
        try {
            URL fxmlUrl = getClass().getResource("/isep/crescendo/view/" + fxmlFileName);
            if (fxmlUrl == null) {
                System.err.println("FXML file not found: /isep/crescendo/view/" + fxmlFileName);
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent content = loader.load();

            Object controller = loader.getController();
            // Apenas para o CoinController/MarketController
            if (controller instanceof CoinController && dataObject instanceof Criptomoeda) {
                ((CoinController) controller).setCriptomoeda((Criptomoeda) dataObject);
                System.out.println("DEBUG (MainController): Criptomoeda passada para CoinController.");
            } else if (controller instanceof MarketController && dataObject instanceof Criptomoeda) {
                ((MarketController) controller).setCriptomoedaSelecionada((Criptomoeda) dataObject);
                System.out.println("DEBUG (MainController): Criptomoeda passada para MarketController.");
            }
            if (controller instanceof UserManagementController) {
                ((UserManagementController) controller).setMainController(this);
                ((UserManagementController) controller).setLoginCallback(this);
                System.out.println("DEBUG (MainController): LoginCallback e MainController definidos para UserManagementController.");
            } else if (controller instanceof UserManagementController) {
                ((UserManagementController) controller).setMainController(this);
                System.out.println("DEBUG (MainController): MainController definido para RegisterController.");
            } else if (controller instanceof UserManagementController) {
                ((UserManagementController) controller).setMainController(this);
                System.out.println("DEBUG (MainController): MainController definido para RecoveryController.");
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);

            this.currentContentFxml = fxmlFileName;
            System.out.println("DEBUG (MainController): Conteúdo '" + fxmlFileName + "' carregado.");
        } catch (IOException e) {
            System.err.println("Erro ao carregar conteúdo " + fxmlFileName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getCurrentContentFxml() {
        return currentContentFxml;
    }

    @Override
    public void onLoginSuccess(boolean isAdmin) {
        System.out.println("MainController: Login bem-sucedido. is Admin: " + isAdmin);

        try {
            // Força recarregar a LeftBar com base no isAdminSession atualizado
            loadLeftBar();
        } catch (IOException e) {
            System.err.println("Erro ao recarregar LeftBar após login: " + e.getMessage());
            e.printStackTrace();
        }

        // CORRIGIDO: carrega a view adequada
        if (isAdmin) {
            loadContent("admin-view.fxml");
        } else {
            loadContent("WalletView.fxml");
        }

        if (rightBarController != null) {
            rightBarController.showEntireRightBar();
        }
    }

    @Override
    public void onLoginFailure(String message) {
        System.out.println("MainController: Login falhou: " + message);
    }

    public void handleLogout(ActionEvent event) {
        SessionManager.setCurrentUser(null);
        if (leftBarController != null) {
            leftBarController.hideEntireLeftBar();
            leftBarController.hideLoggedInContent();
            rootPane.setLeft(null);
        }
        try {
            loadUserManagementView();
        } catch (IOException e) {
            System.err.println("Erro ao carregar tela de login após logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Métodos Adicionados para Navegação e Manipulação de Criptomoedas ---

    /**
     * Lida com o clique em uma criptomoeda na RightBar.
     * Altera o conteúdo principal para a MarketView e exibe o gráfico da criptomoeda.
     * @param cripto O objeto Criptomoeda que foi clicado.
     */
    public void handleCriptomoedaClick(Criptomoeda cripto) {
        System.out.println("DEBUG (MainController): Criptomoeda clicada na RightBar: " + cripto.getNome());
        // Assumindo que 'MarketView.fxml' é a vista que mostra o gráfico e seu controlador é um CoinController
        loadContentWithObject("MarketView.fxml", cripto);
    }

    /**
     * Navega para a vista da carteira, geralmente chamada da LeftBar.
     */
    public void navigateToWallet() {
        System.out.println("DEBUG (MainController): Navegando para a Vista da Carteira (via menu).");
        loadContent("WalletView.fxml");
    }

    // REMOVIDO: O método 'navigateToMarket()' e toda a sua lógica.

    // Exemplo de manipulação de configuração global do RightBarController (se necessário)
    public void handleGlobalSettingChange(Boolean newValue) {
        System.out.println("MainController received global setting change: " + newValue);
        if (newValue != null && newValue) {
            // Faça algo quando verdadeiro
        } else {
            // Faça algo quando falso ou nulo
        }
    }

}
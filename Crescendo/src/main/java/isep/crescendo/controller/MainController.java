// isep.crescendo.controller.MainController.java

package isep.crescendo.controller;

import isep.crescendo.model.Criptomoeda;
import isep.crescendo.util.ContentManager; // Importe o novo ContentManager
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
    private StackPane contentArea; // Onde os conteúdos FXML serão exibidos

    // Mantemos as referências aos controladores da barra lateral, se necessário,
    // mas o ContentManager gerenciará o cache dos conteúdos principais.
    private LeftBarController leftBarController;
    private RightBarController rightBarController;

    // Remover currentContentFxml se o ContentManager for o único a rastreá-lo.
    // private String currentContentFxml = ""; // O ContentManager cuidará disso

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (rootPane == null) {
            System.err.println("ERRO CRÍTICO (MainController): rootPane é null no initialize. Verifique o fx:id no MainView.fxml.");
            return;
        }
        if (contentArea == null) {
            System.err.println("ERRO CRÍTICO (MainController): contentArea é null no initialize. Verifique o fx:id no MainView.fxml.");
            return;
        }

        // *** PASSO 1: Inicialize o ContentManager ***
        ContentManager.getInstance().initialize(contentArea, this);
        System.out.println("DEBUG (MainController): ContentManager inicializado.");

        try {
            // Carregamento das barras laterais (elas não são gerenciadas pelo ContentManager
            // porque são partes fixas do BorderPane)
            loadLeftBar();
            loadRightBar();

            // *** PASSO 2: Pré-carregar conteúdos principais para acesso rápido ***
            // Estes FXMLs serão carregados uma única vez na inicialização e armazenados.


            // Lógica inicial para exibir a view correta
            isep.crescendo.model.User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                System.out.println("DEBUG (MainController): Usuário logado. Exibindo wallet-view.fxml.");
                ContentManager.getInstance().showContent("MarketView.fxml"); // Carrega e exibe MarketView para que o MarketController seja inicializado e disponível para injetar criptomoeda.
                ContentManager.getInstance().showContent("WalletView.fxml");
                ContentManager.getInstance().showContent("admin-view.fxml"); // Se você tiver uma view de administração
                ContentManager.getInstance().showContent("UserManagementView.fxml"); // Para o formulário de login/registro
            } else {
                System.out.println("DEBUG (MainController): Nenhum usuário logado. Exibindo formulário de login.");
                ContentManager.getInstance().showContent("UserManagementView.fxml");
            }

        } catch (IOException e) {
            System.err.println("Erro geral ao carregar layout inicial ou barras laterais: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Métodos para carregar as barras laterais (mantidos como estão, pois não são no StackPane)
    private void loadLeftBar() throws IOException {
        System.out.println("DEBUG (MainController): Tentando carregar LeftBarView.fxml...");
        String leftBarFxml = SessionManager.isAdminSession() ? "AdminLeftBarView.fxml" : "LeftBarView.fxml";

        FXMLLoader leftBarLoader = new FXMLLoader(getClass().getResource("/isep/crescendo/view/" + leftBarFxml));
        Parent leftBar = leftBarLoader.load();
        if (SessionManager.isAdminSession()) {
            AdminLeftBarController adminLeftBarController = leftBarLoader.getController();
            // Garante que o MainController seja injetado no AdminLeftBarController
            if (adminLeftBarController != null) {
                adminLeftBarController.setMainController(this);
            }
        } else {
            leftBarController = leftBarLoader.getController();
            // Garante que o MainController seja injetado no LeftBarController
            if (leftBarController != null) {
                leftBarController.setMainController(this);
                System.out.println("DEBUG (MainController): LeftBarController obtido e MainController injetado.");
            } else {
                System.err.println("ERRO (MainController): getController() retornou null para LeftBarController!");
            }
        }
        rootPane.setLeft(leftBar);
        System.out.println("DEBUG (MainController): LeftBar definida na região LEFT do rootPane.");
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

    // Métodos de navegação simplificados para usar o ContentManager

    // Antigo loadUserManagementView() pode ser removido ou apenas chamar showContent
    // private void loadUserManagementView() throws IOException { /* Removido */ }


    /**
     * Carrega e exibe um conteúdo FXML na área principal.
     * Agora usa o ContentManager para gerenciar o cache.
     * @param fxmlFileName O nome do arquivo FXML a ser carregado e exibido.
     */
    public void loadContent(String fxmlFileName) {
        ContentManager.getInstance().showContent(fxmlFileName);
    }

    /**
     * Carrega e exibe um conteúdo FXML na área principal, passando um objeto de dados.
     * Agora usa o ContentManager para gerenciar o cache e a injeção de dados.
     * @param fxmlFileName O nome do arquivo FXML.
     * @param dataObject O objeto de dados a ser passado para o controlador.
     * @param <T> O tipo do objeto de dados.
     */
    public <T> void loadContentWithObject(String fxmlFileName, T dataObject) {
        ContentManager.getInstance().showContentWithObject(fxmlFileName, dataObject);
    }

    // Removido getCurrentContentFxml() se não for mais rastreado pelo MainController
    // public String getCurrentContentFxml() {
    //     return ContentManager.getInstance().getCurrentContentFileName(); // Ou adicione um método ao ContentManager
    // }

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

        // CORRIGIDO: carrega a view adequada usando o ContentManager
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
            rootPane.setLeft(null); // Remove a LeftBar para garantir que o layout fique limpo
        }
        // Exibe a tela de login usando o ContentManager
        ContentManager.getInstance().showContent("UserManagementView.fxml");
    }

    // --- Métodos Adicionados para Navegação e Manipulação de Criptomoedas ---

    /**
     * Lida com o clique em uma criptomoeda na RightBar.
     * Altera o conteúdo principal para a MarketView e exibe o gráfico da criptomoeda.
     * Agora usa o ContentManager.
     * @param cripto O objeto Criptomoeda que foi clicado.
     */
    public void handleCriptomoedaClick(Criptomoeda cripto) {
        System.out.println("DEBUG (MainController): Criptomoeda clicada na RightBar: " + cripto.getNome());
        loadContentWithObject("MarketView.fxml", cripto);
    }

    /**
     * Navega para a vista da carteira, geralmente chamada da LeftBar.
     * Agora usa o ContentManager.
     */
    public void navigateToWallet() {
        System.out.println("DEBUG (MainController): Navegando para a Vista da Carteira (via menu).");
        reloadContent("WalletView.fxml");
    }

    // Exemplo de manipulação de configuração global do RightBarController (se necessário)
    public void handleGlobalSettingChange(Boolean newValue) {
        System.out.println("MainController received global setting change: " + newValue);
        if (newValue != null && newValue) {
            // Faça algo quando verdadeiro
        } else {
            // Faça algo quando falso ou nulo
        }
    }


    public void reloadContent(String fxmlFileName) {
        ContentManager.getInstance().reloadContent(fxmlFileName);
    }

    public <T> void reloadContentWithObject(String fxmlFileName, T dataObject) {
        ContentManager.getInstance().reloadContentWithObject(fxmlFileName, dataObject);
    }
}
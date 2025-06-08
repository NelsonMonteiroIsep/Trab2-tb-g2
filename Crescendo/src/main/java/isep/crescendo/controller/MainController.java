package isep.crescendo.controller;

import isep.crescendo.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Node; // Importe Node
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region; // Importe Region para o cast

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable, LoginCallback {

    @FXML
    private BorderPane rootPane;
    @FXML
    private StackPane contentArea;

    private LeftBarController leftBarController;
    private UserManagementController userManagementController;

    public void setLoginCallback(LoginCallback callback) {
        // ... (Este método não precisa de ser alterado) ...
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            loadLeftBar();

            isep.crescendo.model.User currentUser = SessionManager.getCurrentUser();

            if (currentUser != null) {
                // ... (logic for logged-in user) ...
            } else {
                // *** Cenário: NENHUM UTILIZADOR LOGADO - MOSTRA O FORMULÁRIO DE LOGIN ***
                System.out.println("Nenhum usuário logado. Exibindo formulário de login.");
                setLeftBarVisibility(false);
                leftBarController.hideLoggedInContent();

                // Make sure this block is exactly as below
                FXMLLoader userManagementLoader = new FXMLLoader(getClass().getResource("/isep/crescendo/view/UserManagementView.fxml"));
                Parent userManagementContent = userManagementLoader.load();
                userManagementController = userManagementLoader.getController();
                userManagementController.setLoginCallback(this);

                contentArea.getChildren().clear(); // <--- Is this line executing?
                contentArea.getChildren().add(userManagementContent); // <--- Is this line adding the content?
            }

        } catch (IOException e) {
            System.err.println("Erro ao carregar layout inicial ou gestão de utilizadores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadLeftBar() {
        try {
            FXMLLoader leftBarLoader = new FXMLLoader(getClass().getResource("/isep/crescendo/view/LeftBarView.fxml"));
            Parent leftBar = leftBarLoader.load();
            leftBarController = leftBarLoader.getController();
            leftBarController.setMainController(this);

            rootPane.setLeft(leftBar);

            // CORREÇÃO AQUI: Chame setPrefWidth no próprio objeto 'leftBar'
            leftBar.prefWidth(0); // Define a largura preferencial para 0 inicialmente
            // Esta é a forma correta de manipular a largura preferencial de um Node.

        } catch (IOException e) {
            System.err.println("Erro ao carregar a barra lateral: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setLeftBarVisibility(boolean visible) {
        // rootPane.getLeft() retorna um Node, que é o 'leftBar' (o VBox raiz da barra lateral)
        Node leftBarNode = rootPane.getLeft();

        if (rootPane != null && leftBarNode != null) {
            // É seguro fazer o cast para Region, pois VBox (o root da LeftBarView) é um Region.
            Region leftBarRegion = (Region) leftBarNode;

            if (visible) {
                // Se visível, restaura a largura preferencial para a largura normal (ex: 200px)
                leftBarRegion.setPrefWidth(200);
            } else {
                // Se não visível, define a largura preferencial para 0
                leftBarRegion.setPrefWidth(0);
            }
            // As propriedades setVisible/setManaged no rootVBox do LeftBarController ainda são importantes para o conteúdo interno
            if (leftBarController != null) {
                if (visible) {
                    leftBarController.showEntireLeftBar();
                } else {
                    leftBarController.hideEntireLeftBar();
                }
            }
        }
    }

    @Override
    public void onLoginSuccess(boolean isAdmin) {
        System.out.println("Login bem-sucedido! A mostrar o conteúdo principal.");
        setLeftBarVisibility(true);
        leftBarController.showLoggedInContent();
        // Altere esta linha para carregar a carteira
        loadContent("WalletView.fxml"); // <--- MUDANÇA AQUI
        // Aqui você pode adicionar lógica adicional para utilizadores admin vs. normal, se necessário
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        SessionManager.clearSession();
        System.out.println("Sessão encerrada. Voltando para o formulário de login.");

        setLeftBarVisibility(false);
        leftBarController.hideLoggedInContent();

        try {
            FXMLLoader userManagementLoader = new FXMLLoader(getClass().getResource("/isep/crescendo/view/UserManagementView.fxml"));
            Parent userManagementContent = userManagementLoader.load();
            userManagementController = userManagementLoader.getController();
            userManagementController.setLoginCallback(this);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(userManagementContent);
        } catch (IOException e) {
            System.err.println("Erro ao carregar o formulário de login após o logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadContent(String fxmlFileName) {
        try {
            URL fxmlUrl = getClass().getResource("/isep/crescendo/view/" + fxmlFileName);
            if (fxmlUrl == null) {
                System.err.println("FXML file not found: /isep/crescendo/view/" + fxmlFileName);
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent content = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        } catch (IOException e) {
            System.err.println("Erro ao carregar conteúdo " + fxmlFileName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
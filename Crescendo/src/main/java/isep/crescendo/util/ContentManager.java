// isep.crescendo.util.ContentManager.java
package isep.crescendo.util;

import isep.crescendo.controller.CoinController;
import isep.crescendo.controller.MainController;
import isep.crescendo.controller.MarketController;
import isep.crescendo.controller.UserManagementController;
import isep.crescendo.model.Criptomoeda;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Gerencia o carregamento e a exibição de conteúdos FXML dentro de um StackPane,
 * utilizando cache para otimizar o desempenho.
 */
public class ContentManager {

    private static ContentManager instance;
    private StackPane contentArea;
    private MainController mainController; // Referência ao MainController para injeção
    private Map<String, Parent> contentCache = new HashMap<>(); // Cache para armazenar os layouts carregados
    private Map<String, Object> controllerCache = new HashMap<>(); // Cache para armazenar os controladores associados

    private ContentManager() {
        // Construtor privado para o padrão Singleton
    }

    /**
     * Retorna a instância única do ContentManager.
     * @return A instância do ContentManager.
     */
    public static ContentManager getInstance() {
        if (instance == null) {
            instance = new ContentManager();
        }
        return instance;
    }

    /**
     * Define o StackPane onde os conteúdos serão exibidos e o MainController para injeção.
     * Este método deve ser chamado uma vez na inicialização do MainController.
     * @param contentArea O StackPane principal.
     * @param mainController O MainController da aplicação.
     */
    public void initialize(StackPane contentArea, MainController mainController) {
        this.contentArea = contentArea;
        this.mainController = mainController;
        System.out.println("DEBUG (ContentManager): Inicializado com contentArea e mainController.");
    }

    /**
     * Carrega um FXML e o armazena no cache se ele ainda não estiver lá.
     * Os controladores também são armazenados no cache, se o fxmlFileName for especificado.
     *
     * @param fxmlFileName O nome do arquivo FXML (ex: "HomePage.fxml").
     * @param <T> O tipo de objeto de dado a ser passado para o controlador, se aplicável.
     * @param dataObject O objeto de dado a ser passado para o controlador.
     * @return O Parent (layout) carregado ou null se houver erro.
     */
    private <T> Parent loadAndCacheContent(String fxmlFileName, T dataObject) {
        if (contentCache.containsKey(fxmlFileName)) {
            System.out.println("DEBUG (ContentManager): Conteúdo '" + fxmlFileName + "' já está no cache.");
            // Se já está no cache, mas precisa de dados, vamos injetar os dados novamente
            injectDataIntoController(controllerCache.get(fxmlFileName), dataObject);
            return contentCache.get(fxmlFileName);
        }

        try {
            URL fxmlUrl = getClass().getResource("/isep/crescendo/view/" + fxmlFileName);
            if (fxmlUrl == null) {
                System.err.println("FXML file not found: /isep/crescendo/view/" + fxmlFileName);
                return null;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent content = loader.load();
            Object controller = loader.getController();

            // Armazena no cache
            contentCache.put(fxmlFileName, content);
            controllerCache.put(fxmlFileName, controller); // Armazena o controlador também

            System.out.println("DEBUG (ContentManager): Conteúdo '" + fxmlFileName + "' carregado e armazenado no cache.");

            // Injeta o MainController e quaisquer dados no controlador recém-carregado
            if (controller != null) {
                injectControllerDependencies(controller);
                injectDataIntoController(controller, dataObject);
            }

            return content;
        } catch (IOException e) {
            System.err.println("Erro ao carregar FXML para o conteúdo '" + fxmlFileName + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Injeta dependências (como o MainController) nos controladores.
     * @param controller O controlador a ter as dependências injetadas.
     */
    private void injectControllerDependencies(Object controller) {
        if (controller instanceof UserManagementController) {
            ((UserManagementController) controller).setMainController(mainController);
            ((UserManagementController) controller).setLoginCallback(mainController); // O MainController é o LoginCallback
            System.out.println("DEBUG (ContentManager): MainController e LoginCallback definidos para UserManagementController.");
        }
        // Adicione outras injeções de dependência aqui se outros controladores precisarem do MainController
        // Ex:
        // else if (controller instanceof AlgumOutroController) {
        //     ((AlgumOutroController) controller).setMainController(mainController);
        // }
    }

    /**
     * Injeta dados específicos (como Criptomoeda) em controladores que os aceitam.
     * @param controller O controlador.
     * @param dataObject O objeto de dados a ser injetado.
     * @param <T> O tipo do objeto de dados.
     */
    private <T> void injectDataIntoController(Object controller, T dataObject) {
        if (dataObject == null) {
            return; // Não há dados para injetar
        }

        if (controller instanceof CoinController && dataObject instanceof Criptomoeda) {
            ((CoinController) controller).setCriptomoeda((Criptomoeda) dataObject);
            System.out.println("DEBUG (ContentManager): Criptomoeda injetada em CoinController.");
        } else if (controller instanceof MarketController && dataObject instanceof Criptomoeda) {
            ((MarketController) controller).setCriptomoedaSelecionada((Criptomoeda) dataObject);
            System.out.println("DEBUG (ContentManager): Criptomoeda injetada em MarketController.");
        }
        // Adicione outras injeções de dados aqui
    }

    /**
     * Exibe um conteúdo carregado no StackPane.
     * O conteúdo será carregado e cacheado se ainda não estiver.
     *
     * @param fxmlFileName O nome do arquivo FXML a ser exibido.
     */
    public void showContent(String fxmlFileName) {
        showContentWithObject(fxmlFileName, null); // Chama a versão com objeto nulo
    }

    /**
     * Exibe um conteúdo carregado no StackPane, passando um objeto de dados para o controlador.
     * O conteúdo será carregado e cacheado se ainda não estiver.
     *
     * @param fxmlFileName O nome do arquivo FXML a ser exibido.
     * @param dataObject O objeto de dados a ser passado para o controlador da view.
     * @param <T> O tipo do objeto de dados.
     */
    public <T> void showContentWithObject(String fxmlFileName, T dataObject) {
        if (contentArea == null) {
            System.err.println("ERRO (ContentManager): StackPane 'contentArea' não foi inicializado. Chame initialize() primeiro.");
            return;
        }

        // Carrega (ou recupera do cache) o conteúdo e injeta os dados
        Parent content = loadAndCacheContent(fxmlFileName, dataObject);

        if (content != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
            System.out.println("DEBUG (ContentManager): Conteúdo '" + fxmlFileName + "' exibido.");
        } else {
            System.err.println("ERRO (ContentManager): Não foi possível obter conteúdo para '" + fxmlFileName + "'.");
        }
    }

    /**
     * Remove um conteúdo do cache. Útil para liberar memória se uma "página" não for mais necessária.
     * @param fxmlFileName O nome do arquivo FXML associado ao conteúdo a ser removido.
     */
    public void unloadContent(String fxmlFileName) {
        if (contentCache.containsKey(fxmlFileName)) {
            contentCache.remove(fxmlFileName);
            controllerCache.remove(fxmlFileName); // Remove o controlador também
            System.out.println("DEBUG (ContentManager): Conteúdo '" + fxmlFileName + "' removido do cache.");
        }
    }
}
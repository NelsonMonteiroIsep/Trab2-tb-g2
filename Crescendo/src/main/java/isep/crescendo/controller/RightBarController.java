package isep.crescendo.controller;

import isep.crescendo.Repository.CriptomoedaRepository;
import isep.crescendo.model.Criptomoeda;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets; // Adicione este import
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.controlsfx.control.ToggleSwitch;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class RightBarController implements Initializable {

    private MainController mainController;

    @FXML
    private VBox rightBarRoot;

    @FXML
    private ListView<Criptomoeda> coinListView;

    @FXML
    private ToggleSwitch globalToggleSwitch;

    private final BooleanProperty darkModeEnabled = new SimpleBooleanProperty(false);

    private final CriptomoedaRepository criptoRepo = new CriptomoedaRepository();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (rightBarRoot != null) {
            System.out.println("DEBUG (RightBarController): RightBar inicializada. rightBarRoot injetado.");
        } else {
            System.err.println("ERRO CRÍTICO (RightBarController): rightBarRoot NÃO foi injetado pelo FXML! Verifique fx:id no FXML.");
        }

        if (globalToggleSwitch != null) {
            globalToggleSwitch.selectedProperty().bindBidirectional(darkModeEnabled);
            darkModeEnabled.addListener((obs, oldVal, newVal) -> {
                System.out.println("Modo Escuro: " + (newVal ? "Ativado" : "Desativado"));
                if (mainController != null) {
                    mainController.handleGlobalSettingChange(newVal);
                }
            });
        } else {
            System.err.println("ERRO (RightBarController): globalToggleSwitch NÃO foi injetado pelo FXML!");
        }

        if (coinListView != null) {
            coinListView.setCellFactory(lv -> new ListCell<Criptomoeda>() {
                @Override
                protected void updateItem(Criptomoeda cripto, boolean empty) {
                    super.updateItem(cripto, empty);
                    if (empty || cripto == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox itemLayout = new HBox(10);
                        itemLayout.setPadding(new Insets(5));

                        ImageView icon = new ImageView();
                        icon.setFitHeight(24);
                        icon.setFitWidth(24);
                        if (cripto.getImagemUrl() != null && !cripto.getImagemUrl().isEmpty()) {
                            try {
                                icon.setImage(new Image(cripto.getImagemUrl(), true));
                            } catch (Exception e) {
                                System.err.println("Erro ao carregar imagem para " + cripto.getNome() + ": " + e.getMessage());
                                icon.setImage(new Image(getClass().getResourceAsStream("/isep/crescendo/images/default_coin.png")));
                            }
                        } else {
                            icon.setImage(new Image(getClass().getResourceAsStream("/isep/crescendo/images/default_coin.png")));
                        }

                        Label nameLabel = new Label(cripto.getNome() + " (" + cripto.getSimbolo() + ")");
                        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                        itemLayout.getChildren().addAll(icon, nameLabel);
                        setGraphic(itemLayout);

                        itemLayout.setOnMouseClicked(event -> {
                            if (mainController != null && cripto != null) {
                                System.out.println("Criptomoeda clicada na RightBar: " + cripto.getNome());
                                mainController.loadContentWithObject("MarketView.fxml", cripto); // Abre MarketView com a cripto
                            }
                        });
                    }
                }
            });
            loadCriptomoedasToList();
        } else {
            System.err.println("ERRO (RightBarController): coinListView NÃO foi injetado pelo FXML! Verifique fx:id no FXML.");
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        System.out.println("DEBUG (RightBarController): MainController injetado.");
    }

    public void showEntireRightBar() {
        if (rightBarRoot != null) {
            rightBarRoot.setVisible(true);
            rightBarRoot.setManaged(true);
            System.out.println("DEBUG (RightBarController): RightBar VISÍVEL.");
        }
    }

    public void hideEntireRightBar() {
        if (rightBarRoot != null) {
            rightBarRoot.setVisible(false);
            rightBarRoot.setManaged(false);
            System.out.println("DEBUG (RightBarController): RightBar ESCONDIDA.");
        }
    }

    // O método handleShowSettings foi removido, pois o botão não existe mais no FXML.

    private void loadCriptomoedasToList() {
        try {
            List<Criptomoeda> moedas = criptoRepo.getAllCriptomoedasAtivas();
            ObservableList<Criptomoeda> observableMoedas = FXCollections.observableArrayList(moedas);
            if (coinListView != null) {
                coinListView.setItems(observableMoedas);
                System.out.println("Criptomoedas carregadas na RightBar ListView: " + moedas.size());
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar criptomoedas para a ListView da RightBar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
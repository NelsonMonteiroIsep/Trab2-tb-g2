package isep.crescendo.controller;

import isep.crescendo.Repository.CriptomoedaRepository;
import isep.crescendo.model.Criptomoeda;
import isep.crescendo.Repository.HistoricoValorRepository;
import isep.crescendo.model.HistoricoValor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Novos imports para os controles de ordenação
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import java.net.URL;
import java.util.Comparator; // Import para ordenação
import java.util.List;
import java.util.ResourceBundle;

public class RightBarController implements Initializable {

    private MainController mainController;

    @FXML
    private VBox rightBarRoot;

    @FXML
    private ListView<Criptomoeda> coinListView;

    // FXML IDs para os novos controles de ordenação
    @FXML
    private ComboBox<String> sortCriteriaComboBox;
    @FXML
    private RadioButton ascendenteRadioButton;
    @FXML
    private RadioButton descendenteRadioButton;
    @FXML
    private ToggleGroup sortDirectionToggleGroup; // Este FXML ID será injetado, mas não está no FXML como Node

    private final HistoricoValorRepository historicoValorRepository = new HistoricoValorRepository();
    private final BooleanProperty darkModeEnabled = new SimpleBooleanProperty(false);
    private final CriptomoedaRepository criptoRepo = new CriptomoedaRepository();

    private ObservableList<Criptomoeda> masterCriptoList; // Lista original sem ordenação

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (rightBarRoot != null) {
            System.out.println("DEBUG (RightBarController): RightBar inicializada. rightBarRoot injetado.");
        } else {
            System.err.println("ERRO CRÍTICO (RightBarController): rightBarRoot NÃO foi injetado pelo FXML! Verifique fx:id no FXML.");
        }

        // Inicialização do ComboBox de critérios de ordenação
        if (sortCriteriaComboBox != null) {
            sortCriteriaComboBox.setItems(FXCollections.observableArrayList("Nome", "Preço"));
            sortCriteriaComboBox.setValue("Nome"); // Valor padrão
            sortCriteriaComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                System.out.println("DEBUG: Critério de ordenação alterado para: " + newVal); // Depuração
                applySorting();
            });
        } else {
            System.err.println("ERRO (RightBarController): sortCriteriaComboBox NÃO foi injetado pelo FXML! Verifique fx:id.");
        }

        // Adicionar listener para os RadioButtons de direção de ordenação
        // O ToggleGroup é injetado pelo FXML porque os RadioButtons o referenciam.
        if (sortDirectionToggleGroup != null) {
            sortDirectionToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    RadioButton selected = (RadioButton) newVal;
                    System.out.println("DEBUG: Direção de ordenação alterada para: " + selected.getText()); // Depuração
                } else {
                    System.out.println("DEBUG: Nenhuma direção de ordenação selecionada."); // Caso raro
                }
                applySorting();
            });
            // Assegurar que o RadioButton ascendente está selecionado por padrão na inicialização
            // Se já estiver no FXML com selected="true", esta linha pode ser redundante mas não prejudica.
            if (ascendenteRadioButton != null) {
                ascendenteRadioButton.setSelected(true);
            }
        } else {
            System.err.println("ERRO (RightBarController): sortDirectionToggleGroup NÃO foi injetado pelo FXML! Verifique se os RadioButtons referenciam corretamente o ToggleGroup.");
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
                        Label priceLabel = new Label();
                        priceLabel.setStyle("-fx-text-fill: lightgreen; -fx-font-weight: bold;");

                        try {
                            HistoricoValor ultimoValor = historicoValorRepository.getUltimoValorPorCripto(cripto.getId());

                            if (ultimoValor != null) {
                                priceLabel.setText(String.format("%.2f €", ultimoValor.getValor()));
                            } else {
                                priceLabel.setText("-- €");
                            }
                        } catch (Exception e) {
                            System.err.println("Erro ao buscar último valor para " + cripto.getNome() + ": " + e.getMessage());
                            priceLabel.setText("-- €");
                        }


                        itemLayout.getChildren().addAll(icon, nameLabel, priceLabel);
                        setGraphic(itemLayout);

                        itemLayout.setOnMouseClicked(event -> {
                            if (mainController != null && cripto != null) {
                                System.out.println("Criptomoeda clicada na RightBar: " + cripto.getNome());
                                mainController.loadContentWithObject("MarketView.fxml", cripto);
                            }
                        });
                    }
                }
            });

            loadCriptomoedasToList(); // Carrega as criptomoedas e popula masterCriptoList
            applySorting(); // Aplica a ordenação inicial (por padrão, Nome Ascendente)
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

    private void loadCriptomoedasToList() {
        try {
            List<Criptomoeda> moedas = criptoRepo.getAllCriptomoedasAtivas();
            masterCriptoList = FXCollections.observableArrayList(moedas); // Popula a lista mestre
            System.out.println("Criptomoedas carregadas na masterCriptoList: " + moedas.size());
        } catch (Exception e) {
            System.err.println("Erro ao carregar criptomoedas para a ListView da RightBar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applySorting() {
        if (masterCriptoList == null || masterCriptoList.isEmpty()) {
            System.out.println("DEBUG: masterCriptoList está vazia ou nula. Sem ordenação aplicada.");
            return;
        }

        String criteria = sortCriteriaComboBox.getValue();
        boolean ascending = ascendenteRadioButton.isSelected();

        System.out.println("DEBUG (applySorting): Critério: " + criteria + ", Ascendente: " + ascending);

        Comparator<Criptomoeda> comparator = null;

        if ("Nome".equals(criteria)) {
            comparator = Comparator.comparing(Criptomoeda::getNome);
        } else if ("Preço".equals(criteria)) {
            comparator = (c1, c2) -> {
                double valor1 = 0.0;
                double valor2 = 0.0;
                try {
                    HistoricoValor hv1 = historicoValorRepository.getUltimoValorPorCripto(c1.getId());
                    if (hv1 != null) {
                        valor1 = hv1.getValor();
                    }
                    HistoricoValor hv2 = historicoValorRepository.getUltimoValorPorCripto(c2.getId());
                    if (hv2 != null) {
                        valor2 = hv2.getValor();
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao obter valor para comparação (para " + c1.getNome() + " ou " + c2.getNome() + "): " + e.getMessage());
                }
                return Double.compare(valor1, valor2);
            };
        }

        if (comparator != null) {
            if (!ascending) {
                comparator = comparator.reversed();
            }
            FXCollections.sort(masterCriptoList, comparator);
            coinListView.setItems(masterCriptoList);
            System.out.println("Lista de criptomoedas ordenada por " + criteria + (ascending ? " (Ascendente)" : " (Descendente)"));
        } else {
            System.out.println("DEBUG (applySorting): Nenhum comparador definido ou critério inválido.");
        }
    }
}
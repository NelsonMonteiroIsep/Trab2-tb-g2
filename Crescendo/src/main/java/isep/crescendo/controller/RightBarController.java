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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors; // Import adicionado

public class RightBarController implements Initializable {

    private MainController mainController;

    @FXML
    private VBox rightBarRoot;

    @FXML
    private ListView<Criptomoeda> coinListView;

    @FXML
    private ComboBox<String> sortCriteriaComboBox;
    @FXML
    private RadioButton ascendenteRadioButton;
    @FXML
    private RadioButton descendenteRadioButton;
    @FXML
    private ToggleGroup sortDirectionToggleGroup;

    // NOVO: FXML IDs para os filtros de preço
    @FXML
    private TextField minPriceFilterField;
    @FXML
    private TextField maxPriceFilterField;
    // O botão não precisa de um fx:id se o onAction for definido diretamente no FXML

    private final HistoricoValorRepository historicoValorRepository = new HistoricoValorRepository();
    private final BooleanProperty darkModeEnabled = new SimpleBooleanProperty(false);
    private final CriptomoedaRepository criptoRepo = new CriptomoedaRepository();

    private ObservableList<Criptomoeda> masterCriptoList; // Lista original, completa, sem ordenação/filtragem

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
                System.out.println("DEBUG: Critério de ordenação alterado para: " + newVal);
                applyFilteringAndSorting(); // Chama o novo método
            });
        } else {
            System.err.println("ERRO (RightBarController): sortCriteriaComboBox NÃO foi injetado pelo FXML! Verifique fx:id.");
        }

        // Adicionar listener para os RadioButtons de direção de ordenação
        if (sortDirectionToggleGroup != null) {
            sortDirectionToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    RadioButton selected = (RadioButton) newVal;
                    System.out.println("DEBUG: Direção de ordenação alterada para: " + selected.getText());
                } else {
                    System.out.println("DEBUG: Nenhuma direção de ordenação selecionada.");
                }
                applyFilteringAndSorting(); // Chama o novo método
            });
            if (ascendenteRadioButton != null) {
                ascendenteRadioButton.setSelected(true);
            }
        } else {
            System.err.println("ERRO (RightBarController): sortDirectionToggleGroup NÃO foi injetado pelo FXML! Verifique se os RadioButtons referenciam corretamente o ToggleGroup.");
        }

        // Inicialização dos campos de filtro de preço (opcional: adicionar listeners se quiser autofiltragem)
        if (minPriceFilterField == null || maxPriceFilterField == null) {
            System.err.println("ERRO (RightBarController): minPriceFilterField ou maxPriceFilterField NÃO foi injetado pelo FXML!");
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
                                var currentUser = isep.crescendo.util.SessionManager.getCurrentUser();
                                if (currentUser == null) {
                                    Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                                    alerta.setTitle("Acesso Restrito");
                                    alerta.setHeaderText("Autenticação necessária");
                                    alerta.setContentText("Por favor, inicie sessão ou registe-se para visualizar os detalhes da criptomoeda.");
                                    alerta.showAndWait();
                                    return;
                                }

                                System.out.println("Criptomoeda clicada na RightBar: " + cripto.getNome());
                                mainController.loadContentWithObject("MarketView.fxml", cripto);
                            }
                        });
                    }
                }
            });

            loadCriptomoedasToList(); // Carrega as criptomoedas na masterCriptoList
            applyFilteringAndSorting(); // Aplica filtragem e ordenação inicial
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

    @FXML
    private void applyFilteringAndSorting() {
        if (masterCriptoList == null || masterCriptoList.isEmpty()) {
            System.out.println("DEBUG: masterCriptoList está vazia ou nula. Sem filtragem/ordenação aplicada.");
            return;
        }

        // Carrega todos os últimos valores numa só vez para evitar chamadas repetidas
        Map<Integer, Double> ultimoValoresMap = new HashMap<>();
        for (Criptomoeda cripto : masterCriptoList) {
            try {
                HistoricoValor valor = historicoValorRepository.getUltimoValorPorCripto(cripto.getId());
                if (valor != null) {
                    ultimoValoresMap.put(cripto.getId(), valor.getValor());
                }
            } catch (Exception e) {
                System.err.println("Erro ao obter valor inicial de " + cripto.getNome() + ": " + e.getMessage());
            }
        }

        // 1. Filtragem
        double minPrice = -Double.MAX_VALUE;
        double maxPrice = Double.MAX_VALUE;

        try {
            if (minPriceFilterField != null && !minPriceFilterField.getText().trim().isEmpty()) {
                minPrice = Double.parseDouble(minPriceFilterField.getText().trim().replace(",", "."));
            }
            if (maxPriceFilterField != null && !maxPriceFilterField.getText().trim().isEmpty()) {
                maxPrice = Double.parseDouble(maxPriceFilterField.getText().trim().replace(",", "."));
            }
        } catch (NumberFormatException e) {
            System.err.println("Erro ao parsear preços: " + e.getMessage());
        }

        final double finalMin = minPrice;
        final double finalMax = maxPrice;

        List<Criptomoeda> tempFilteredList = masterCriptoList.stream()
                .filter(c -> {
                    Double valor = ultimoValoresMap.get(c.getId());
                    return valor != null && valor >= finalMin && valor <= finalMax;
                })
                .collect(Collectors.toList());

        ObservableList<Criptomoeda> filteredList = FXCollections.observableArrayList(tempFilteredList);
        System.out.println("DEBUG: Lista filtrada para " + filteredList.size() + " criptomoedas.");

        // 2. Ordenação
        String criteria = sortCriteriaComboBox.getValue();
        boolean ascending = ascendenteRadioButton.isSelected();

        Comparator<Criptomoeda> comparator = null;
        if ("Nome".equals(criteria)) {
            comparator = Comparator.comparing(Criptomoeda::getNome);
        } else if ("Preço".equals(criteria)) {
            comparator = Comparator.comparingDouble(c -> ultimoValoresMap.getOrDefault(c.getId(), 0.0));
        }

        if (comparator != null) {
            if (!ascending) comparator = comparator.reversed();
            FXCollections.sort(filteredList, comparator);
        }

        coinListView.setItems(filteredList);
    }}
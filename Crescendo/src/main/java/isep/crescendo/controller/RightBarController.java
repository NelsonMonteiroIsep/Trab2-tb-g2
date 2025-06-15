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

import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TextField; // Import adicionado
import javafx.scene.control.Button;   // Import adicionado

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
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

    // NOVO MÉTODO: Aplica filtragem E ordenação
    @FXML // Este método é chamado pelo onAction do botão Filtrar
    private void applyFilteringAndSorting() {
        if (masterCriptoList == null || masterCriptoList.isEmpty()) {
            System.out.println("DEBUG: masterCriptoList está vazia ou nula. Sem filtragem/ordenação aplicada.");
            return;
        }

        // 1. Filtragem
        ObservableList<Criptomoeda> filteredList = FXCollections.observableArrayList(masterCriptoList);

        double minPrice = -Double.MAX_VALUE; // Valor padrão baixo
        double maxPrice = Double.MAX_VALUE;  // Valor padrão alto

        // Tentar parsear o preço mínimo
        if (minPriceFilterField != null && !minPriceFilterField.getText().trim().isEmpty()) {
            try {
                minPrice = Double.parseDouble(minPriceFilterField.getText().trim().replace(",", ".")); // Lida com vírgula como separador decimal
            } catch (NumberFormatException e) {
                System.err.println("Aviso: Preço mínimo inválido. Usando valor padrão. Erro: " + e.getMessage());
                // Poderia mostrar um alerta ao usuário aqui
            }
        }

        // Tentar parsear o preço máximo
        if (maxPriceFilterField != null && !maxPriceFilterField.getText().trim().isEmpty()) {
            try {
                maxPrice = Double.parseDouble(maxPriceFilterField.getText().trim().replace(",", ".")); // Lida com vírgula como separador decimal
            } catch (NumberFormatException e) {
                System.err.println("Aviso: Preço máximo inválido. Usando valor padrão. Erro: " + e.getMessage());
                // Poderia mostrar um alerta ao usuário aqui
            }
        }

        final double finalMinPrice = minPrice;
        final double finalMaxPrice = maxPrice;

        // Filtra a lista com base nos preços
        List<Criptomoeda> tempFilteredList = filteredList.stream()
                .filter(cripto -> {
                    try {
                        HistoricoValor ultimoValor = historicoValorRepository.getUltimoValorPorCripto(cripto.getId());
                        if (ultimoValor != null) {
                            double valor = ultimoValor.getValor();
                            return valor >= finalMinPrice && valor <= finalMaxPrice;
                        }
                    } catch (Exception e) {
                        System.err.println("Erro ao obter valor para filtragem de " + cripto.getNome() + ": " + e.getMessage());
                    }
                    return false; // Se não conseguir obter o valor, não inclui na lista filtrada
                })
                .collect(Collectors.toList());

        filteredList = FXCollections.observableArrayList(tempFilteredList);
        System.out.println("DEBUG: Lista filtrada para " + filteredList.size() + " criptomoedas. (Min: " + finalMinPrice + ", Max: " + finalMaxPrice + ")");


        // 2. Ordenação (Aplicada à lista JÁ FILTRADA)
        String criteria = sortCriteriaComboBox.getValue();
        boolean ascending = ascendenteRadioButton.isSelected();

        System.out.println("DEBUG (applyFilteringAndSorting): Critério: " + criteria + ", Ascendente: " + ascending);

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
            FXCollections.sort(filteredList, comparator); // Ordena a lista filtrada
            coinListView.setItems(filteredList);
            System.out.println("Lista final ordenada e filtrada por " + criteria + (ascending ? " (Ascendente)" : " (Descendente)"));
        } else {
            // Se nenhum comparador, apenas define a lista filtrada
            coinListView.setItems(filteredList);
            System.out.println("DEBUG (applyFilteringAndSorting): Nenhum comparador definido ou critério inválido. Exibindo apenas lista filtrada.");
        }
    }
}
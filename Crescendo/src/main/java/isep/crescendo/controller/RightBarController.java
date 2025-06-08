package isep.crescendo.controller;

import isep.crescendo.Repository.CriptomoedaRepository;
import isep.crescendo.model.Criptomoeda;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox; // Import VBox for your new container
import javafx.scene.layout.AnchorPane; // Or HBox, depending on CoinListItem.fxml's root
import javafx.scene.control.Button; // If you added fx:id to your button
import java.io.IOException;

public class RightBarController {

    @FXML
    private VBox coinsContainer; // This is your new container for coin items

    @FXML
    private Button refreshButton; // Link to your refresh button

    private CriptomoedaRepository criptomoedaRepository = new CriptomoedaRepository();

    @FXML
    public void initialize() {
        // No more ListView specific code (no setPlaceholder, no setCellFactory)

        // Set an action for the refresh button (optional, but good practice)
        if (refreshButton != null) {
            refreshButton.setOnAction(event -> {
                try {
                    carregarCriptomoedas();
                } catch (IOException e) {
                    System.err.println("Erro ao recarregar criptomoedas: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        // Load initial data
        try {
            carregarCriptomoedas();
        } catch (IOException e) {
            System.err.println("Erro ao carregar criptomoedas na inicialização: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void carregarCriptomoedas() throws IOException {
        coinsContainer.getChildren().clear(); // Clear existing components before adding new ones

        System.out.println("Attempting to load criptomoedas from repository...");
        ObservableList<Criptomoeda> fetchedMoedas = criptomoedaRepository.getAllCriptomoedas();

        if (fetchedMoedas == null || fetchedMoedas.isEmpty()) {
            System.out.println("No criptomoedas fetched. Displaying placeholder.");
            coinsContainer.getChildren().add(new Label("Nenhuma criptomoeda disponível.")); // Placeholder if empty
            return;
        }

        System.out.println("Fetched " + fetchedMoedas.size() + " criptomoedas. Adding to container.");

        // This is your "loop" to manually add each component
        int i = 0;
        for (Criptomoeda moeda : fetchedMoedas) {
            if (i<5) {
                try {
                    // Load the FXML for a single coin item
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/isep/crescendo/coin-list-view.fxml"));
                    AnchorPane coinItemRoot = loader.load(); // Or HBox if that's your root element

                    // Get the controller for this specific coin item
                    CoinListViewController controller = loader.getController();

                    // Pass the data to the controller of the loaded item
                    controller.setCriptomoeda(moeda);

                    // Add the loaded FXML component (its root node) to your VBox container
                    coinsContainer.getChildren().add(coinItemRoot);

                } catch (IOException e) {
                    System.err.println("Erro ao carregar item da moeda " + moeda.getNome() + ": " + e.getMessage());
                    e.printStackTrace();
                    // Optionally add an error message to the container if a specific item fails
                    coinsContainer.getChildren().add(new Label("Erro ao carregar " + moeda.getSimbolo()));
                }
            }
            i=i+1;
        }
        System.out.println("Finished loading components into coinsContainer.");
    }
}
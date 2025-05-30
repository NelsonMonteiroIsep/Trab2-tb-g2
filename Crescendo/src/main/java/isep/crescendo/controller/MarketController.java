package isep.crescendo.controller;

import eu.hansolo.fx.countries.CountryPane;
import isep.crescendo.model.Criptomoeda;
import isep.crescendo.model.CriptomoedaRepository;
import isep.crescendo.model.User;
import isep.crescendo.util.SceneSwitcher;
import isep.crescendo.util.SessionManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class MarketController implements Initializable {



    public Label navBarAnyControl;
    @FXML
    private Label userNameLabel;

    private User loggedInUser;

    private CriptomoedaRepository criptomoedaRepository = new CriptomoedaRepository();

    @FXML
    private VBox coinContainer;
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        loggedInUser = SessionManager.getCurrentUser();

        if (loggedInUser != null) {
            userNameLabel.setText("Bem-vindo, " + loggedInUser.getNome());
        } else {
            userNameLabel.setText("Bem-vindo, visitante!");
        }

        if (coinContainer != null) {
            loadCoinComponents();
        } else {
            System.err.println("Erro: coinContainer não foi injetado pelo FXML!");
        }




    }
    private void loadCoinComponents() {
        try {
            ObservableList<Criptomoeda> moedas = criptomoedaRepository.getAllCriptomoedas();

            for (Criptomoeda moeda : moedas) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/isep/crescendo/coin-componente.fxml"));
                VBox coinComponent = loader.load();
                CoinComponent controller = loader.getController();
                controller.setCriptomoeda(moeda);
                coinContainer.getChildren().add(coinComponent);
            }

        } catch (IOException e) {
            System.err.println("Erro ao carregar componente de moeda:");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.setCurrentUser(null);
        SceneSwitcher.switchScene("/isep/crescendo/login-view.fxml", "/isep/crescendo/styles/login.css", "Login", userNameLabel);
    }


}

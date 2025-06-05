package isep.crescendo.controller;

import isep.crescendo.model.MoedaSaldo;
import isep.crescendo.util.SceneSwitcher;
import isep.crescendo.util.SessionManager;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import isep.crescendo.Repository.Carteira;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class UnderConstructionController {
    @FXML
    private Label menu_label;
    @FXML private TableView<MoedaSaldo> moedasTable;
    @FXML private TableColumn<MoedaSaldo, String> nomeColuna;
    @FXML private TableColumn<MoedaSaldo, Double> quantidadeColuna;
    @FXML
    public void voltarParaPaginaAnterior(ActionEvent actionEvent) {
        SceneSwitcher.switchScene("/isep/crescendo/market-view.fxml", "/isep/crescendo/styles/login.css", "Carteira", menu_label);
    }
    public void initialize() {
        nomeColuna.setCellValueFactory(new PropertyValueFactory<>("nome"));
        quantidadeColuna.setCellValueFactory(new PropertyValueFactory<>("quantidade"));

        int carteiraId = SessionManager.getCurrentUser().getId(); // ou obter via método
        Carteira carteiraRepo = new Carteira();
        ObservableList<MoedaSaldo> lista = carteiraRepo.listarMoedasCarteira(carteiraId);

        moedasTable.setItems(lista);
    }



}

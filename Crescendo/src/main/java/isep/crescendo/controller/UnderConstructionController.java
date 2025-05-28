package isep.crescendo.controller;

import isep.crescendo.util.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UnderConstructionController {
    @FXML
    private Label menu_label;
    @FXML
    public void voltarParaPaginaAnterior(ActionEvent actionEvent) {
        SceneSwitcher.switchScene("/isep/crescendo/market-view.fxml", "/isep/crescendo/styles/login.css", "Carteira", menu_label);
    }
}

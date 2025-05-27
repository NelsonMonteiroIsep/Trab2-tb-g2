package isep.crescendo.controller;

import isep.crescendo.util.SceneSwitcher;
import isep.crescendo.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MenuLateralController {
    @FXML
    private Label menu_label;
    @FXML
    private void handleLogout() {
        SessionManager.setCurrentUser(null);
        SceneSwitcher.switchScene("/isep/crescendo/login-view.fxml", "/isep/crescendo/styles/login.css", "Login", menu_label);
    }
}

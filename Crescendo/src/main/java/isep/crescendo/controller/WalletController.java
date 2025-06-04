package isep.crescendo.controller;

import isep.crescendo.model.User;
import isep.crescendo.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.awt.event.ActionEvent;


public class WalletController {

    @FXML
    private Label userNameLabel;

    private User loggedInUser;

    @FXML
    private void initialize() {
        loggedInUser = SessionManager.getCurrentUser();

        if (loggedInUser != null) {
            userNameLabel.setText("Bem-vindo, " + loggedInUser.getNome());
        } else {
            userNameLabel.setText("Bem-vindo, visitante!");
        }
    }
}

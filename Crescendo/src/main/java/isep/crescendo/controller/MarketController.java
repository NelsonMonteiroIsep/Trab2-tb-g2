package isep.crescendo.controller;


import isep.crescendo.model.User;
import isep.crescendo.util.SceneSwitcher;
import isep.crescendo.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MarketController implements Initializable {
    @FXML
    private Label userNameLabel;
    @FXML
    private ImageView goToOtherPage;
    private User loggedInUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loggedInUser = SessionManager.getCurrentUser();

        Font righteous = Font.loadFont(
                getClass().getResourceAsStream("/fonts/Righteous-Regular.ttf"), 48);

        if (loggedInUser != null) {
            userNameLabel.setText("Crescendo.");
            userNameLabel.setFont(righteous);

        } else {
            userNameLabel.setText("Crescendo.");
            userNameLabel.setFont(righteous);

        }
        Image img = new Image(getClass().getResourceAsStream("/isep/crescendo/images/IconsAmarelo.png"));
        goToOtherPage.setImage(img);



    }
    @FXML
    private void handleLogout() {
        SessionManager.setCurrentUser(null);
        SceneSwitcher.switchScene("/isep/crescendo/login-view.fxml", "/isep/crescendo/styles/login.css", "Login", userNameLabel);
    }
    @FXML
    private void handleImageClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/isep/crescendo/other-page.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) goToOtherPage.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

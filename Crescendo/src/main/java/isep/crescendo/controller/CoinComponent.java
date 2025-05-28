package isep.crescendo.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;


public class CoinComponent {

    public Label coinSymbol;
    public Label coinName;
    public Label coinPrice;
    public Label priceChangeArrow;
    public Label priceChangePercent;
    public Pane miniChart;
    public ImageView coinImage;

    @FXML
    private Label coinLabel;

    String name;

        public void setCoinName(String name) {
            coinLabel.setText(name);
        }
}


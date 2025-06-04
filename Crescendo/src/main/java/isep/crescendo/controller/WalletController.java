package isep.crescendo.controller;

import isep.crescendo.Repository.CarteiraRepository;
import isep.crescendo.model.User;
import isep.crescendo.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import isep.crescendo.model.Carteira;

import java.awt.event.ActionEvent;


public class WalletController {
    @FXML
    public Label saldoLabel;
    @FXML
    private Label userNameLabel;

    private User loggedInUser;

    @FXML
    private void initialize() {
        loggedInUser = SessionManager.getCurrentUser();

        if (loggedInUser != null) {
            userNameLabel.setText("Bem-vindo, " + loggedInUser.getNome());
            atualizarSaldoLabel();
        } else {
            userNameLabel.setText("Bem-vindo, visitante!");
        }
    }

    private void atualizarSaldoLabel() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            Carteira carteira = CarteiraRepository.procurarPorUserId(currentUser.getId());

            if (carteira != null) {
                saldoLabel.setText(String.format("%.2f €", carteira.getSaldo()));
            } else {
                saldoLabel.setText("0.00 €");
            }
        }
    }
}

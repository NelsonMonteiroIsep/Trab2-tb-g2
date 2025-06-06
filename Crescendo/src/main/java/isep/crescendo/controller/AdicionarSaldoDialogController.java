package isep.crescendo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class AdicionarSaldoDialogController {

    @FXML
    private TextField valorTextField;

    private Stage dialogStage;
    private Consumer<Double> onValorConfirmado;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOnValorConfirmado(Consumer<Double> consumer) {
        this.onValorConfirmado = consumer;
    }

    @FXML
    private void handleCancelar() {
        dialogStage.close();
    }

    @FXML
    private void handleConfirmar() {
        try {
            double valor = Double.parseDouble(valorTextField.getText());
            if (valor <= 0) {
                // Mostra mensagem de erro se quiseres
                return;
            }
            if (onValorConfirmado != null) {
                onValorConfirmado.accept(valor);
            }
            dialogStage.close();
        } catch (NumberFormatException e) {
            // Mostra mensagem de erro se quiseres
        }
    }
}

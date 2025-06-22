package isep.crescendo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class AdicionarSaldoDialogController {

    @FXML
    // Alterar de 'private' para 'protected' ou remover o modificador (package-private)
    TextField valorTextField; // OU protected TextField valorTextField;

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
        // Alterar de 'private' para 'protected' ou remover o modificador (package-private)
    void handleConfirmar() { // OU protected void handleConfirmar()
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

    @FXML
        // Alterar de 'private' para 'protected' ou remover o modificador (package-private)
    void handleConfirmarLevantar() { // OU protected void handleConfirmarLevantar()
        try {
            double valor = Double.parseDouble(valorTextField.getText());

            if (valor <= 0) {
                System.out.println("Valor inválido. Tem de ser maior que zero.");
                // Se quiseres mostrar uma Alert:
                // showAlert("Erro", "O valor deve ser maior que zero.");
                return;
            }

            if (valor > saldoDisponivel) {
                showAlert("Erro", "O valor não pode ser superior ao saldo disponível.");
                return;
            }

            if (onValorConfirmado != null) {
                onValorConfirmado.accept(valor);
            }

            dialogStage.close();

        } catch (NumberFormatException e) {
            System.out.println("Valor inválido. Introduza um número.");
            // Se quiseres mostrar uma Alert:
            // showAlert("Erro", "Introduza um valor numérico válido.");
        }
    }

    private double saldoDisponivel;

    public void setSaldoDisponivel(double saldoDisponivel) {
        this.saldoDisponivel = saldoDisponivel;
    }

    private void showAlert(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
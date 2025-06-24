package isep.crescendo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class AdicionarSaldoDialogController {

    // Campo de texto onde o utilizador insere o valor a adicionar ou levantar
    @FXML
    private TextField valorTextField;

    // Janela do diálogo atual
    private Stage dialogStage;

    // Função a executar quando o valor for confirmado (usada como callback)
    private Consumer<Double> onValorConfirmado;

    // Setter para a janela do diálogo
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    // Setter para a função a ser chamada quando o valor for confirmado
    public void setOnValorConfirmado(Consumer<Double> consumer) {
        this.onValorConfirmado = consumer;
    }

    // Fecha o diálogo quando o utilizador clica em "Cancelar"
    @FXML
    private void handleCancelar() {
        dialogStage.close();
    }

    // Trata a confirmação de valor quando o objetivo é adicionar saldo
    @FXML
    private void handleConfirmar() {
        try {
            // Converte o texto introduzido num número
            double valor = Double.parseDouble(valorTextField.getText());

            // Verifica se o valor é maior que zero
            if (valor <= 0) {
                // Mostra alerta se o valor for inválido
                showAlert("Erro", "O valor deve ser maior que zero.");
                return;
            }

            // Executa a ação definida no callback com o valor inserido
            if (onValorConfirmado != null) {
                onValorConfirmado.accept(valor);
            }

            // Fecha o diálogo
            dialogStage.close();
        } catch (NumberFormatException e) {
            // Mostra erro se o valor não for numérico
            showAlert("Erro", "Introduza um valor numérico válido.");
        }
    }

    // Trata a confirmação de valor quando o objetivo é levantar saldo
    @FXML
    private void handleConfirmarLevantar() {
        try {
            double valor = Double.parseDouble(valorTextField.getText());

            // Verifica se o valor é válido (maior que zero)
            if (valor <= 0) {
                showAlert("Erro", "O valor deve ser maior que zero.");
                return;
            }

            // Verifica se o valor não ultrapassa o saldo disponível
            if (valor > saldoDisponivel) {
                showAlert("Erro", "O valor não pode ser superior ao saldo disponível.");
                return;
            }

            // Executa a ação definida no callback com o valor inserido
            if (onValorConfirmado != null) {
                onValorConfirmado.accept(valor);
            }

            // Fecha o diálogo
            dialogStage.close();

        } catch (NumberFormatException e) {
            showAlert("Erro", "Introduza um valor numérico válido.");
        }
    }

    // Saldo atual disponível, usado para validação ao levantar saldo
    private double saldoDisponivel;

    // Setter para o saldo disponível (recebe do controlador principal)
    public void setSaldoDisponivel(double saldoDisponivel) {
        this.saldoDisponivel = saldoDisponivel;
    }

    // Método auxiliar para mostrar alertas de erro ao utilizador
    private void showAlert(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}

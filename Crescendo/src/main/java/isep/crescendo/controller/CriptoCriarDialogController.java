package isep.crescendo.controller;

import isep.crescendo.model.Criptomoeda;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CriptoCriarDialogController {

    @FXML private TextField nomeField;
    @FXML private TextField simboloField;
    @FXML private TextArea descricaoArea;
    @FXML private TextField imagemUrlField;
    @FXML private CheckBox ativoCheck;

    private Stage dialogStage;
    private boolean confirmado = false;
    private Criptomoeda novaCripto;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public Criptomoeda getNovaCripto() {
        return novaCripto;
    }

    @FXML
    private void handleSalvar() {
        // Validação simples
        if (nomeField.getText().isEmpty() || simboloField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Campos obrigatórios");
            alert.setHeaderText(null);
            alert.setContentText("Nome e Símbolo são obrigatórios!");
            alert.showAndWait();
            return;
        }

        novaCripto = new Criptomoeda();
        novaCripto.setNome(nomeField.getText());
        novaCripto.setSimbolo(simboloField.getText());
        novaCripto.setDescricao(descricaoArea.getText());
        novaCripto.setImagemUrl(imagemUrlField.getText());
        novaCripto.setAtivo(ativoCheck.isSelected());
        novaCripto.setDataCriacao(new java.sql.Timestamp(System.currentTimeMillis()));

        confirmado = true;
        dialogStage.close();
    }

    @FXML
    private void handleCancelar() {
        dialogStage.close();
    }
}

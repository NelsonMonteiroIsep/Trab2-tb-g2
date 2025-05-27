package isep.crescendo.model.crescendo.controller;

import isep.crescendo.model.crescendo.model.Criptomoeda;
import isep.crescendo.model.crescendo.model.CriptomoedaRepository;
import isep.crescendo.model.crescendo.model.HistoricoValorRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class CriptoCriarDialogController {

    @FXML private TextField nomeField;
    @FXML private TextField simboloField;
    @FXML private TextArea descricaoArea;
    @FXML private TextField imagemUrlField;
    @FXML private CheckBox ativoCheck;
    @FXML
    private TextField valorInicialField;

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
        if (nomeField.getText().isEmpty() || simboloField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Campos obrigatórios");
            alert.setHeaderText(null);
            alert.setContentText("Nome e Símbolo são obrigatórios!");
            alert.showAndWait();
            return;
        }

        double valorInicial;
        try {
            valorInicial = Double.parseDouble(valorInicialField.getText());
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Valor inválido");
            alert.setHeaderText(null);
            alert.setContentText("O valor inicial deve ser um número!");
            alert.showAndWait();
            return;
        }

        novaCripto = new Criptomoeda();
        novaCripto.setNome(nomeField.getText());
        novaCripto.setSimbolo(simboloField.getText());
        novaCripto.setDescricao(descricaoArea.getText());
        novaCripto.setImagemUrl(imagemUrlField.getText());
        novaCripto.setAtivo(ativoCheck.isSelected());
        novaCripto.setDataCriacao(new Timestamp(System.currentTimeMillis()));

        // Salvar criptomoeda
        CriptomoedaRepository repo = new CriptomoedaRepository();
        repo.adicionar(novaCripto);
        Timestamp dataHoraAtual = Timestamp.valueOf(LocalDateTime.now());
        // Guardar valor inicial no histórico
        HistoricoValorRepository historicoRepo = new HistoricoValorRepository();
        historicoRepo.adicionarValor(novaCripto.getId(), dataHoraAtual.toLocalDateTime(), valorInicial);

        confirmado = true;
        dialogStage.close();
    }

    @FXML
    private void handleCancelar() {
        dialogStage.close();
    }
}

package isep.crescendo.model;

import java.time.LocalDateTime;

public class HistoricoValor {
    private int criptoId;
    private LocalDateTime data;
    private double valor;

    public int getCriptoId() {
        return criptoId;
    }

    public void setCriptoId(int criptoId) {
        this.criptoId = criptoId;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }
}


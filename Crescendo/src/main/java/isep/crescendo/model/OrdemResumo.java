package isep.crescendo.model;

import isep.crescendo.model.Ordem;
import isep.crescendo.model.Transacao;

import java.util.ArrayList;
import java.util.List;

public class OrdemResumo {
    private Ordem ordem;
    private double quantidadeExecutada;
    private double valorTotalExecutado;
    private List<Transacao> transacoes;

    public OrdemResumo(Ordem ordem, double quantidadeExecutada, double valorTotalExecutado) {
        this.ordem = ordem;
        this.quantidadeExecutada = quantidadeExecutada;
        this.valorTotalExecutado = valorTotalExecutado;
        this.transacoes = new ArrayList<>();
    }

    public Ordem getOrdem() { return ordem; }
    public double getQuantidadeExecutada() { return quantidadeExecutada; }
    public double getValorTotalExecutado() { return valorTotalExecutado; }
    public double getValorMedio() {
        return quantidadeExecutada > 0 ? valorTotalExecutado / quantidadeExecutada : 0.0;
    }

    public List<Transacao> getTransacoes() { return transacoes; }
    public void setTransacoes(List<Transacao> transacoes) { this.transacoes = transacoes; }
}

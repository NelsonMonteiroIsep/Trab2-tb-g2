package isep.crescendo.model;

import java.time.LocalDateTime;

public class Transacao {
    private int id;
    private int ordemCompraId;
    private int ordemVendaId;
    private int idMoeda;
    private double quantidade;
    private double valorUnitario;
    private LocalDateTime dataHora;

    public Transacao(int id, int ordemCompraId, int ordemVendaId, int idMoeda, double quantidade, double valorUnitario, LocalDateTime dataHora) {
        this.id = id;
        this.ordemCompraId = ordemCompraId;
        this.ordemVendaId = ordemVendaId;
        this.idMoeda = idMoeda;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.dataHora = dataHora;
    }

    public Transacao(int ordemCompraId, int ordemVendaId, int idMoeda, double quantidade, double valorUnitario) {
        this(0, ordemCompraId, ordemVendaId, idMoeda, quantidade, valorUnitario, LocalDateTime.now());
    }

    // Getters e Setters
    public int getId() { return id; }
    public int getOrdemCompraId() { return ordemCompraId; }
    public int getOrdemVendaId() { return ordemVendaId; }
    public int getIdMoeda() { return idMoeda; }
    public double getQuantidade() { return quantidade; }
    public double getValorUnitario() { return valorUnitario; }
    public LocalDateTime getDataHora() { return dataHora; }
}


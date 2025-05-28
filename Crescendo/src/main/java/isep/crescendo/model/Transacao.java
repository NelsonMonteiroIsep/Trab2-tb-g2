package isep.crescendo.model;

import java.time.LocalDateTime;

public class Transacao {
    private int id;
    private int carteiraId;
    private String moeda;
    private double quantidade;
    private double valor; // valor unitário no momento da compra/venda
    private String tipo; // "compra" ou "venda"
    private LocalDateTime dataHora;

    public Transacao(int carteiraId, String moeda, double quantidade, double valor, String tipo, LocalDateTime dataHora) {
        this.carteiraId = carteiraId;
        this.moeda = moeda;
        this.quantidade = quantidade;
        this.valor = valor;
        this.tipo = tipo;
        this.dataHora = dataHora;
    }

    public Transacao(int id, int carteiraId, String moeda, double quantidade, double valor, String tipo, LocalDateTime dataHora) {
        this(carteiraId, moeda, quantidade, valor, tipo, dataHora);
        this.id = id;
    }

    // Getters
    public int getId() { return id; }
    public int getCarteiraId() { return carteiraId; }
    public String getMoeda() { return moeda; }
    public double getQuantidade() { return quantidade; }
    public double getValor() { return valor; }
    public String getTipo() { return tipo; }
    public LocalDateTime getDataHora() { return dataHora; }
}

package isep.crescendo.model;

import java.time.LocalDateTime;

public class Transacao {
    private int id;
    private int carteiraId;
    private String moeda; // pode ser ID da moeda como String
    private double quantidade;
    private double valor; // preço unitário
    private String tipo; // "compra" ou "venda"
    private LocalDateTime dataHora;
    private boolean executada;
    private boolean expirada;

    // Construtor para ordens novas
    public Transacao(int carteiraId, String moeda, double quantidade, double valor, String tipo, LocalDateTime dataHora) {
        this.carteiraId = carteiraId;
        this.moeda = moeda;
        this.quantidade = quantidade;
        this.valor = valor;
        this.tipo = tipo;
        this.dataHora = dataHora;
        this.executada = false;
        this.expirada = false;
    }

    // Construtor completo
    public Transacao(int id, int carteiraId, String moeda, double quantidade, double valor, String tipo, LocalDateTime dataHora) {
        this(carteiraId, moeda, quantidade, valor, tipo, dataHora);
        this.id = id;
    }

    // Getters e setters
    public int getId() { return id; }
    public int getCarteiraId() { return carteiraId; }
    public String getMoeda() { return moeda; }
    public double getQuantidade() { return quantidade; }
    public double getValor() { return valor; }
    public String getTipo() { return tipo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public boolean isExecutada() { return executada; }
    public boolean isExpirada() { return expirada; }

    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }
    public void setExecutada(boolean executada) { this.executada = executada; }
    public void setExpirada(boolean expirada) { this.expirada = expirada; }
}

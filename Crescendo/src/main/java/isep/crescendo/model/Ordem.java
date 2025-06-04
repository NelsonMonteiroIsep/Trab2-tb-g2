package isep.crescendo.model;

import java.time.LocalDateTime;

public class Ordem {
    private int id;
    private int carteiraId;
    private int idMoeda;
    private double quantidade;
    private double valor;
    private String tipo;
    private String status;
    private LocalDateTime dataHora;

    public Ordem(int id, int carteiraId, int idMoeda, double quantidade, double valor, String tipo, String status, LocalDateTime dataHora) {
        this.id = id;
        this.carteiraId = carteiraId;
        this.idMoeda = idMoeda;
        this.quantidade = quantidade;
        this.valor = valor;
        this.tipo = tipo;
        this.status = status;
        this.dataHora = dataHora;
    }

    public Ordem(int carteiraId, int idMoeda, double quantidade, double valor, String tipo) {
        this(0, carteiraId, idMoeda, quantidade, valor, tipo, "pendente", LocalDateTime.now());
    }

    // Getters e Setters
    public int getId() { return id; }
    public int getCarteiraId() { return carteiraId; }
    public int getIdMoeda() { return idMoeda; }
    public double getQuantidade() { return quantidade; }
    public double getValor() { return valor; }
    public String getTipo() { return tipo; }
    public String getStatus() { return status; }
    public LocalDateTime getDataHora() { return dataHora; }

    public void setId(int id) { this.id = id; }
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }
    public void setStatus(String status) { this.status = status; }
}

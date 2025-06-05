package isep.crescendo.model;

import java.time.LocalDateTime;

public class Ordem {
    private int id;
    private int carteiraId;
    private int idMoeda;
    private double quantidade;
    private double valor; // valor por unidade
    private String tipo; // "compra" ou "venda"
    private String status; // "pendente", "executada", "expirada"
    private LocalDateTime dataHora;
    private double valorTotalReservado;

    // Construtor completo (usado ao carregar do banco de dados)
    public Ordem(int id, int carteiraId, int idMoeda, double quantidade, double valor, String tipo, String status, LocalDateTime dataHora, double valorTotalReservado) {
        this.id = id;
        this.carteiraId = carteiraId;
        this.idMoeda = idMoeda;
        this.quantidade = quantidade;
        this.valor = valor;
        this.tipo = tipo;
        this.status = status;
        this.dataHora = dataHora;
        this.valorTotalReservado = valorTotalReservado;
    }

    // Construtor simplificado (usado ao criar nova ordem)
    public Ordem(int carteiraId, int idMoeda, double quantidade, double valor, String tipo) {
        this(0, carteiraId, idMoeda, quantidade, valor, tipo, "pendente", LocalDateTime.now(), 0.0);
    }

    // Getters
    public int getId() { return id; }
    public int getCarteiraId() { return carteiraId; }
    public int getIdMoeda() { return idMoeda; }
    public double getQuantidade() { return quantidade; }
    public double getValor() { return valor; }
    public String getTipo() { return tipo; }
    public String getStatus() { return status; }
    public LocalDateTime getDataHora() { return dataHora; }
    public double getValorTotalReservado() { return valorTotalReservado; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }
    public void setStatus(String status) { this.status = status; }
    public void setValorTotalReservado(double valorTotalReservado) { this.valorTotalReservado = valorTotalReservado; }
}

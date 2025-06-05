package isep.crescendo.model;

import java.sql.Timestamp;

public class MoedaSaldo {
    private String nome;
    private double quantidade;
    private String imagemUrl;
    private double precoMedioCompra;
    private Timestamp dataUltimaCompra;

    public MoedaSaldo(String nome, double quantidade, String imagemUrl, double precoMedioCompra, Timestamp dataUltimaCompra) {
        this.nome = nome;
        this.quantidade = quantidade;
        this.imagemUrl = imagemUrl;
        this.precoMedioCompra = precoMedioCompra;
        this.dataUltimaCompra = dataUltimaCompra;
    }

    // Getters
    public String getNome() { return nome; }
    public double getQuantidade() { return quantidade; }
    public String getImagemUrl() { return imagemUrl; }
    public double getPrecoMedioCompra() { return precoMedioCompra; }
    public Timestamp getDataUltimaCompra() { return dataUltimaCompra; }
}


package isep.crescendo.model;

import java.sql.Timestamp;

public class Criptomoeda {
    private int id;
    private String nome;
    private String simbolo;
    private String descricao;
    private boolean ativo;
    private String imagemUrl;
    private Timestamp dataCriacao;

    // Getters e setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSimbolo() { return simbolo; }
    public void setSimbolo(String simbolo) { this.simbolo = simbolo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }

    public Timestamp getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(Timestamp dataCriacao) { this.dataCriacao = dataCriacao; }
}

package isep.crescendo.model;

public class MoedaSaldo {
    private String nome;
    private double quantidade;

    public MoedaSaldo(String nome, double quantidade) {
        this.nome = nome;
        this.quantidade = quantidade;
    }

    public String getNome() {
        return nome;
    }

    public double getQuantidade() {
        return quantidade;
    }
}

package isep.crescendo.model;

public class Carteira {
    private int id;
    private int userId;
    private double saldo;

    public Carteira(int userId, double saldo) {
        this.userId = userId;
        this.saldo = saldo;
    }

    public Carteira(int id, int userId, double saldo) {
        this.id = id;
        this.userId = userId;
        this.saldo = saldo;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public double getSaldo() { return saldo; }

    public void setId(int id) { this.id = id; }
    public void setSaldo(double saldo) { this.saldo = saldo; }
}

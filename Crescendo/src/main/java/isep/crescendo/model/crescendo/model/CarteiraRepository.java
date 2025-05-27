package isep.crescendo.model.crescendo.model;

import java.sql.*;

public class CarteiraRepository {

    private static final String DB_URL = "jdbc:mysql://sql7.freesqldatabase.com:3306/sql7779870";
    private static final String DB_USER = "sql7779870";
    private static final String DB_PASSWORD = "vUwAKDaynR";

    public CarteiraRepository() {
        criarTabelaSeNaoExistir();
    }

    private void criarTabelaSeNaoExistir() {
        String sql = """
            CREATE TABLE IF NOT EXISTS carteiras (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                saldo DOUBLE NOT NULL DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela carteira: " + e.getMessage());
        }
    }

    public void adicionar(Carteira carteira) {
        String sql = "INSERT INTO carteiras (user_id, saldo) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, carteira.getUserId());
            pstmt.setDouble(2, carteira.getSaldo());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                carteira.setId(rs.getInt(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao adicionar carteira: " + e.getMessage());
        }
    }

    public static Carteira procurarPorUserId(int userId) {
        String sql = "SELECT * FROM carteiras WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                double saldo = rs.getDouble("saldo");
                return new Carteira(id, userId, saldo);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao procurar carteira: " + e.getMessage());
        }

        return null;
    }

    public void adicionarCarteiraParaUser(int userId) {
        String sql = "INSERT INTO carteiras (user_id, saldo) VALUES (?, 0.00)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar carteira: " + e.getMessage());
        }
    }

    public void atualizarSaldo(int userId, double novoSaldo) {
        String sql = "UPDATE carteiras SET saldo = ? WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, novoSaldo);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar saldo: " + e.getMessage());
        }
    }


}

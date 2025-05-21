package isep.crescendo.model;

import java.sql.*;

public class UserRepository {

    private static final String DB_URL = "jdbc:mysql://sql7.freesqldatabase.com:3306/sql7779870"; // Nome da BD
    private static final String DB_USER = "sql7779870"; // teu utilizador
    private static final String DB_PASSWORD = "vUwAKDaynR"; // tua password

     public UserRepository() {
        criarTabelaSeNaoExistir();
    }

   private void criarTabelaSeNaoExistir() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                email VARCHAR(255) PRIMARY KEY,
                nome VARCHAR(100) NOT NULL,
                password_hash VARCHAR(255) NOT NULL
            );
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela: " + e.getMessage());
        }
    }

    public void adicionar(User user) {
        String sql = "INSERT INTO users (email, nome, password_hash) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getNome());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao adicionar utilizador: " + e.getMessage());
        }
    }

    public User procurarPorEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String nome = rs.getString("nome");
                String hash = rs.getString("password_hash");
                return new User(email, nome, hash, true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao procurar utilizador: " + e.getMessage());
        }

        return null;
    }

    public boolean existeEmail(String email) {
        return procurarPorEmail(email) != null;
    }

    public void atualizar(User user) {
        String sql = "UPDATE users SET nome = ?, password_hash = ? WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getNome());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getEmail());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException("Nenhum utilizador atualizado, e-mail não encontrado.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar utilizador: " + e.getMessage());
        }
    }


}

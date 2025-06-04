package isep.crescendo.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private static final String DB_URL = "jdbc:mysql://sql7.freesqldatabase.com:3306/sql7779870";
    private static final String DB_USER = "sql7779870";
    private static final String DB_PASSWORD = "vUwAKDaynR";

    public UserRepository() {
        criarTabelaSeNaoExistir();
    }

    private void criarTabelaSeNaoExistir() {
        String sql = """
        CREATE TABLE IF NOT EXISTS users (
            id INT AUTO_INCREMENT PRIMARY KEY,
            email VARCHAR(255) UNIQUE NOT NULL,
            nome VARCHAR(100) NOT NULL,
            password_hash VARCHAR(255) NOT NULL,
            is_admin BOOLEAN DEFAULT FALSE
        );
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela: " + e.getMessage());
        }
    }

    public void adicionar(isep.crescendo.model.User user) {
        String sqlUser = "INSERT INTO users (email, nome, password_hash, is_admin) VALUES (?, ?, ?, ?)";
        String sqlUserId = "SELECT id FROM users WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmtUser = conn.prepareStatement(sqlUser);
             PreparedStatement pstmtUserId = conn.prepareStatement(sqlUserId)) {

            // Inserir o utilizador
            pstmtUser.setString(1, user.getEmail());
            pstmtUser.setString(2, user.getNome());
            pstmtUser.setString(3, user.getPasswordHash());
            pstmtUser.setBoolean(4, user.isAdmin());
            pstmtUser.executeUpdate();

            // Buscar o ID recém-criado
            pstmtUserId.setString(1, user.getEmail());
            ResultSet rs = pstmtUserId.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");

                // Criar a carteira com saldo inicial 0
                CarteiraRepository carteiraRepositoryRepo = new CarteiraRepository();
                carteiraRepositoryRepo.adicionarCarteiraParaUser(userId);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao adicionar utilizador e criar carteira: " + e.getMessage());
        }
    }

    public isep.crescendo.model.User procurarPorEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                String hash = rs.getString("password_hash");
                boolean isAdmin = rs.getBoolean("is_admin");  // <- NOVO

                return new isep.crescendo.model.User(id, email, nome, hash, isAdmin); // <- Usa novo construtor
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao procurar utilizador: " + e.getMessage());
        }

        return null;
    }


    public boolean existeEmail(String email) {
        return procurarPorEmail(email) != null;
    }

    public void atualizar(isep.crescendo.model.User user) {
        String sql = "UPDATE users SET nome = ?, email = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getNome());
            stmt.setString(2, user.getEmail());
            stmt.setInt(3, user.getId());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Nenhum utilizador atualizado, ID não encontrado.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar utilizador: " + e.getMessage(), e);
        }
    }

    public List<isep.crescendo.model.User> listarTodos() {
        List<isep.crescendo.model.User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String email = rs.getString("email");
                String nome = rs.getString("nome");
                String hash = rs.getString("password_hash");
                boolean isAdmin = rs.getBoolean("is_admin");

                users.add(new isep.crescendo.model.User(id, email, nome, hash, isAdmin));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar utilizadores: " + e.getMessage());
        }

        return users;
    }

    public void atualizarAdmin(isep.crescendo.model.User user) {
        String sql = "UPDATE users SET is_admin = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, user.isAdmin());
            pstmt.setInt(2, user.getId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar permissões: " + e.getMessage());
        }
    }

    public void apagar(int id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao apagar utilizador: " + e.getMessage());
        }
    }

    public int countUsers() {
        String query = "SELECT COUNT(*) FROM users";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}

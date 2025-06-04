package isep.crescendo.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransacaoRepository {
    private static final String DB_URL = "jdbc:mysql://sql7.freesqldatabase.com:3306/sql7779870";
    private static final String DB_USER = "sql7779870";
    private static final String DB_PASSWORD = "vUwAKDaynR";

    public TransacaoRepository() {
        criarTabelaSeNaoExistir();
    }

    private void criarTabelaSeNaoExistir() {
        String sql = """
            CREATE TABLE IF NOT EXISTS transacoes (
                id INT AUTO_INCREMENT PRIMARY KEY,
                carteira_id INT NOT NULL,
                id_moeda INT NOT NULL ,
                quantidade DOUBLE NOT NULL,
                valor DOUBLE NOT NULL,
                tipo VARCHAR(10) NOT NULL,
                data_hora DATETIME NOT NULL,
                FOREIGN KEY (carteira_id) REFERENCES carteiras(id)
            );
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela transacoes: " + e.getMessage());
        }
    }

    public void adicionar(isep.crescendo.model.Transacao t) {
        String sql = """
            INSERT INTO transacoes (carteira_id, id_moeda, quantidade, valor, tipo, data_hora)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, t.getCarteiraId());
            pstmt.setString(2, t.getMoeda());
            pstmt.setDouble(3, t.getQuantidade());
            pstmt.setDouble(4, t.getValor());
            pstmt.setString(5, t.getTipo());
            pstmt.setTimestamp(6, Timestamp.valueOf(t.getDataHora()));
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao adicionar transação: " + e.getMessage());
        }
    }

    public List<isep.crescendo.model.Transacao> listarPorCarteira(int carteiraId) {
        List<isep.crescendo.model.Transacao> lista = new ArrayList<>();
        String sql = "SELECT * FROM transacoes WHERE carteira_id = ? ORDER BY data_hora DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, carteiraId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                isep.crescendo.model.Transacao t = new isep.crescendo.model.Transacao(
                        rs.getInt("id"),
                        rs.getInt("carteira_id"),
                        rs.getString("moeda"),
                        rs.getDouble("quantidade"),
                        rs.getDouble("valor"),
                        rs.getString("tipo"),
                        rs.getTimestamp("data_hora").toLocalDateTime()
                );
                lista.add(t);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar transações: " + e.getMessage());
        }

        return lista;
    }
}

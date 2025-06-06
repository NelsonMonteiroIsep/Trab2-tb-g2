package isep.crescendo.Repository;

import isep.crescendo.model.Transacao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransacaoRepository {
    private static final String DB_URL = "jdbc:mysql://sql7.freesqldatabase.com:3306/sql7779870";
    private static final String DB_USER = "sql7779870";
    private static final String DB_PASSWORD = "vUwAKDaynR";

    public TransacaoRepository() {
        criarTabela();
    }

    private void criarTabela() {
        String sql = """
            CREATE TABLE IF NOT EXISTS transacoes (
                id INT AUTO_INCREMENT PRIMARY KEY,
                ordem_compra_id INT,
                ordem_venda_id INT,
                id_moeda INT NOT NULL,
                quantidade DOUBLE NOT NULL,
                valor_unitario DOUBLE NOT NULL,
                data_execucao DATETIME NOT NULL,
                FOREIGN KEY (ordem_compra_id) REFERENCES ordens(id),
                FOREIGN KEY (ordem_venda_id) REFERENCES ordens(id)
            );
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela transacoes: " + e.getMessage());
        }
    }

    public void adicionar(Transacao t) {
        String sql = """
            INSERT INTO transacoes (ordem_compra_id, ordem_venda_id, id_moeda, quantidade, valor_unitario, data_execucao)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, t.getOrdemCompraId());
            stmt.setInt(2, t.getOrdemVendaId());
            stmt.setInt(3, t.getIdMoeda());
            stmt.setDouble(4, t.getQuantidade());
            stmt.setDouble(5, t.getValorUnitario());
            stmt.setTimestamp(6, Timestamp.valueOf(t.getDataHora()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao adicionar transação: " + e.getMessage());
        }
    }

    public double somarQuantidadeExecutadaPorOrdemCompra(int ordemCompraId) {
        String sql = "SELECT SUM(quantidade) FROM transacoes WHERE ordem_compra_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ordemCompraId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double somarValorExecutadoPorOrdemCompra(int ordemCompraId) {
        String sql = "SELECT SUM(quantidade * valor_unitario) FROM transacoes WHERE ordem_compra_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ordemCompraId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public Map<Integer, List<Transacao>> listarTodasTransacoesDaCarteira(int carteiraId) {
        Map<Integer, List<Transacao>> map = new HashMap<>();

        String sql = """
        SELECT t.*, o.carteira_id
        FROM transacoes t
        JOIN ordens o ON t.ordem_compra_id = o.id
        WHERE o.carteira_id = ?
        ORDER BY t.data_execucao ASC
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carteiraId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Transacao t = new Transacao(
                        rs.getInt("id"),
                        rs.getInt("ordem_compra_id"),
                        rs.getInt("ordem_venda_id"),
                        rs.getInt("id_moeda"),
                        rs.getDouble("quantidade"),
                        rs.getDouble("valor_unitario"),
                        rs.getTimestamp("data_execucao").toLocalDateTime()
                );

                int ordemId = rs.getInt("ordem_compra_id");

                map.computeIfAbsent(ordemId, k -> new ArrayList<>()).add(t);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar transações da carteira: " + e.getMessage());
        }

        return map;
    }

    // Outros métodos futuros: listar por moeda, calcular médias, etc.
}
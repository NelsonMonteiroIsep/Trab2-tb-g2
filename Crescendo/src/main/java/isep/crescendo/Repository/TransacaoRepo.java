package isep.crescendo.Repository;

import isep.crescendo.model.Transacao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransacaoRepo {
    private static final String DB_URL = "jdbc:mysql://sql7.freesqldatabase.com:3306/sql7779870";
    private static final String DB_USER = "sql7779870";
    private static final String DB_PASSWORD = "vUwAKDaynR";

    public TransacaoRepo() {
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

    // Outros métodos futuros: listar por moeda, calcular médias, etc.
}
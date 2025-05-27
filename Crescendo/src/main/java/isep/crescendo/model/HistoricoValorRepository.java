package isep.crescendo.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoricoValorRepository {

    private static final String DB_URL = "jdbc:mysql://sql7.freesqldatabase.com:3306/sql7779870";
    private static final String DB_USER = "sql7779870";
    private static final String DB_PASSWORD = "vUwAKDaynR";

    public HistoricoValorRepository() {
        criarTabelaSeNaoExistir();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void criarTabelaSeNaoExistir() {
        String sql = """
            CREATE TABLE IF NOT EXISTS historico_valores (
                cripto_id INT NOT NULL,
                data DATETIME NOT NULL,
                valor DECIMAL(18, 8) NOT NULL,
                PRIMARY KEY (cripto_id, data),
                FOREIGN KEY (cripto_id) REFERENCES criptomoedas(id) ON DELETE CASCADE
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela historico_valores: " + e.getMessage());
        }
    }

    public void adicionarValor(int criptoId, LocalDateTime data, double valor) {
        String sql = "INSERT INTO historico_valores (cripto_id, data, valor) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, criptoId);
            pstmt.setTimestamp(2, Timestamp.valueOf(data));
            pstmt.setDouble(3, valor);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao adicionar valor histórico: " + e.getMessage());
        }
    }

    public List<HistoricoValor> listarPorCripto(int criptoId) {
        List<HistoricoValor> lista = new ArrayList<>();
        String sql = "SELECT * FROM historico_valores WHERE cripto_id = ? ORDER BY data ";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, criptoId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                HistoricoValor hv = new HistoricoValor();
                hv.setCriptoId(rs.getInt("cripto_id"));
                hv.setData(rs.getTimestamp("data").toLocalDateTime());
                hv.setValor(rs.getDouble("valor"));
                lista.add(hv);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar histórico de valores: " + e.getMessage());
        }

        return lista;
    }
}


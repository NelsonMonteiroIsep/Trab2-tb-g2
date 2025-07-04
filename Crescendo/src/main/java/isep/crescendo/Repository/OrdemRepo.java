package isep.crescendo.Repository;

import isep.crescendo.model.Ordem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import isep.crescendo.model.*;
import isep.crescendo.util.DatabaseConfig;

public class OrdemRepo {

    public OrdemRepo() {
        criarTabela();
    }

    private void criarTabela() {
        String sql = """
            CREATE TABLE IF NOT EXISTS ordens (
                id INT AUTO_INCREMENT PRIMARY KEY,
                carteira_id INT NOT NULL,
                id_moeda INT NOT NULL,
                quantidade DOUBLE NOT NULL,
                valor DOUBLE NOT NULL,
                tipo VARCHAR(10) NOT NULL,
                status VARCHAR(15) NOT NULL DEFAULT 'pendente',
                data_hora DATETIME NOT NULL,
                valor_total_reservado DOUBLE NOT NULL,
                FOREIGN KEY (carteira_id) REFERENCES carteiras(id)
            );
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela ordens: " + e.getMessage());
        }
    }

    public int adicionar(Ordem ordem) {
        String sql = """
            INSERT INTO ordens (carteira_id, id_moeda, quantidade, valor, tipo, status, data_hora, valor_total_reservado)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, ordem.getCarteiraId());
            stmt.setInt(2, ordem.getIdMoeda());
            stmt.setDouble(3, ordem.getQuantidade());
            stmt.setDouble(4, ordem.getValor());
            stmt.setString(5, ordem.getTipo());
            stmt.setString(6, ordem.getStatus());
            stmt.setTimestamp(7, Timestamp.valueOf(ordem.getDataHora()));
            stmt.setDouble(8, ordem.getValorTotalReservado());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            else throw new SQLException("Erro ao obter ID da ordem.");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao adicionar ordem: " + e.getMessage());
        }
    }

    public List<Ordem> buscarOrdensVendaCompativeis(int idMoeda, double precoMax) {
        List<Ordem> lista = new ArrayList<>();
        String sql = """
            SELECT * FROM ordens
            WHERE tipo = 'venda'
              AND id_moeda = ?
              AND valor <= ?
              AND status = 'pendente'
            ORDER BY valor ASC, data_hora ASC
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idMoeda);
            stmt.setDouble(2, precoMax);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(new Ordem(
                        rs.getInt("id"),
                        rs.getInt("carteira_id"),
                        rs.getInt("id_moeda"),
                        rs.getDouble("quantidade"),
                        rs.getDouble("valor"),
                        rs.getString("tipo"),
                        rs.getString("status"),
                        rs.getTimestamp("data_hora").toLocalDateTime(),
                        rs.getDouble("valor_total_reservado")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar ordens de venda compatíveis: " + e.getMessage());
        }

        return lista;
    }

    public List<Ordem> buscarOrdensCompraCompativeis(int idMoeda, double precoMin) {
        List<Ordem> lista = new ArrayList<>();
        String sql = """
            SELECT * FROM ordens
            WHERE tipo = 'compra'
              AND id_moeda = ?
              AND valor >= ?
              AND status = 'pendente'
            ORDER BY data_hora ASC
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idMoeda);
            stmt.setDouble(2, precoMin);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(new Ordem(
                        rs.getInt("id"),
                        rs.getInt("carteira_id"),
                        rs.getInt("id_moeda"),
                        rs.getDouble("quantidade"),
                        rs.getDouble("valor"),
                        rs.getString("tipo"),
                        rs.getString("status"),
                        rs.getTimestamp("data_hora").toLocalDateTime(),
                        rs.getDouble("valor_total_reservado")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar ordens de compra compatíveis: " + e.getMessage());
        }

        return lista;
    }

    public List<Ordem> buscarOrdensPendentes() {
        List<Ordem> ordens = new ArrayList<>();
        String sql = "SELECT * FROM ordens WHERE status = 'pendente'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ordens.add(new Ordem(
                        rs.getInt("id"),
                        rs.getInt("carteira_id"),
                        rs.getInt("id_moeda"),
                        rs.getDouble("quantidade"),
                        rs.getDouble("valor"),
                        rs.getString("tipo"),
                        rs.getString("status"),
                        rs.getTimestamp("data_hora").toLocalDateTime(),
                        rs.getDouble("valor_total_reservado")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ordens;
    }

    public void marcarComoExecutada(int id) {
        String sql = "UPDATE ordens SET status = 'executada' WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao marcar ordem como executada: " + e.getMessage());
        }
    }

    public void atualizarQuantidade(int id, double novaQuantidade) {
        String sql = "UPDATE ordens SET quantidade = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, novaQuantidade);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar quantidade da ordem: " + e.getMessage());
        }
    }

    public void atualizarValor(int id, double novoValor) {
        String sql = "UPDATE ordens SET valor = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, novoValor);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar valor da ordem: " + e.getMessage());
        }
    }

    public void marcarComoExpirada(int ordemId) {
        String sql = "UPDATE ordens SET status = 'expirada' WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ordemId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double somarOrdensPendentes(int carteiraId, int idMoeda, String tipo) {
        String sql = """
        SELECT SUM(quantidade) FROM ordens
        WHERE carteira_id = ? AND id_moeda = ? AND tipo = ? AND status = 'pendente'
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carteiraId);
            stmt.setInt(2, idMoeda);
            stmt.setString(3, tipo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao somar ordens pendentes: " + e.getMessage());
        }
        return 0;
    }

    public List<Ordem> listarOrdensDaCarteira(int carteiraId) {
        List<Ordem> ordens = new ArrayList<>();

        String sql = """
        SELECT * FROM ordens
        WHERE carteira_id = ?
        ORDER BY data_hora DESC
    """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carteiraId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Ordem ordem = new Ordem(
                        rs.getInt("id"),
                        rs.getInt("carteira_id"),
                        rs.getInt("id_moeda"),
                        rs.getDouble("quantidade"),
                        rs.getDouble("valor"),
                        rs.getString("tipo"),
                        rs.getString("status"),
                        rs.getTimestamp("data_hora").toLocalDateTime(),
                        rs.getDouble("valor_total_reservado")
                );

                ordens.add(ordem);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar ordens da carteira: " + e.getMessage());
        }

        return ordens;
    }


    public List<OrdemResumo> listarOrdensComResumo(int carteiraId) {
        List<OrdemResumo> lista = new ArrayList<>();

        String sql = """
        SELECT 
            o.id,
            o.carteira_id,
            o.id_moeda,
            o.quantidade,
            o.valor,
            o.tipo,
            o.status,
            o.data_hora,
            o.valor_total_reservado,
            COALESCE(SUM(t.quantidade), 0) AS quantidade_executada,
            COALESCE(SUM(t.quantidade * t.valor_unitario), 0) AS valor_total_executado
        FROM ordens o
        LEFT JOIN transacoes t ON t.ordem_compra_id = o.id OR t.ordem_venda_id = o.id
        WHERE o.carteira_id = ?
        GROUP BY o.id
        ORDER BY o.data_hora DESC
    """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carteiraId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Ordem ordem = new Ordem(
                        rs.getInt("id"),
                        rs.getInt("carteira_id"),
                        rs.getInt("id_moeda"),
                        rs.getDouble("quantidade"),
                        rs.getDouble("valor"),
                        rs.getString("tipo"),
                        rs.getString("status"),
                        rs.getTimestamp("data_hora").toLocalDateTime(),
                        rs.getDouble("valor_total_reservado")
                );

                OrdemResumo resumo = new OrdemResumo(
                        ordem,
                        rs.getDouble("quantidade_executada"),
                        rs.getDouble("valor_total_executado")
                );

                lista.add(resumo);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar ordens com resumo: " + e.getMessage());
        }

        return lista;
    }

    public void marcarComoCancelada(int ordemId) {
        String sql = "UPDATE ordens SET status = 'cancelada' WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ordemId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}

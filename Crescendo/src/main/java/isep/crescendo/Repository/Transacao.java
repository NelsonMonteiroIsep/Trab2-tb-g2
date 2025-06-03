package isep.crescendo.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Transacao {
    private static final String DB_URL = "jdbc:mysql://sql7.freesqldatabase.com:3306/sql7779870";
    private static final String DB_USER = "sql7779870";
    private static final String DB_PASSWORD = "vUwAKDaynR";

    public Transacao() {
        criarTabelaSeNaoExistir();
    }

    private void criarTabelaSeNaoExistir() {
        String sql = """
    CREATE TABLE IF NOT EXISTS transacoes (
        id INT AUTO_INCREMENT PRIMARY KEY,
        carteira_id INT NOT NULL,
        id_moeda INT NOT NULL,
        quantidade DOUBLE NOT NULL,
        valor DOUBLE NOT NULL,
        tipo VARCHAR(10) NOT NULL,
        data_hora DATETIME NOT NULL,
        executada BOOLEAN DEFAULT FALSE,
        expirada BOOLEAN DEFAULT FALSE,
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

    public List<isep.crescendo.model.Transacao> buscarVendasCompativeis(int idMoeda, double precoMax) {
        List<isep.crescendo.model.Transacao> lista = new ArrayList<>();
        String sql = """
        SELECT * FROM transacoes
        WHERE tipo = 'venda'
          AND id_moeda = ?
          AND valor <= ?
          AND executada = FALSE
          AND expirada = FALSE
          AND data_hora >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
        ORDER BY valor ASC, data_hora ASC
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idMoeda);
            pstmt.setDouble(2, precoMax);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                isep.crescendo.model.Transacao t = new isep.crescendo.model.Transacao(
                        rs.getInt("id"),
                        rs.getInt("carteira_id"),
                        String.valueOf(rs.getInt("id_moeda")),
                        rs.getDouble("quantidade"),
                        rs.getDouble("valor"),
                        rs.getString("tipo"),
                        rs.getTimestamp("data_hora").toLocalDateTime()
                );
                lista.add(t);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar ordens de venda: " + e.getMessage());
        }

        return lista;
    }

    public void atualizarQuantidade(int id, double novaQuantidade) {
        String sql = "UPDATE transacoes SET quantidade = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, novaQuantidade);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar quantidade: " + e.getMessage());
        }
    }

    public void marcarComoExecutada(int id) {
        String sql = "UPDATE transacoes SET executada = TRUE WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao marcar como executada: " + e.getMessage());
        }
    }

    public void marcarOrdensExpiradas() {
        String sql = """
        UPDATE transacoes
        SET expirada = TRUE
        WHERE executada = FALSE
          AND expirada = FALSE
          AND data_hora < DATE_SUB(NOW(), INTERVAL 24 HOUR)
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao expirar ordens: " + e.getMessage());
        }
    }

    public void processarVendaParcial(isep.crescendo.model.Transacao novaVenda, int idMoeda) {
        marcarOrdensExpiradas();

        List<isep.crescendo.model.Transacao> compras = buscarComprasCompativeis(idMoeda, novaVenda.getValor());

        double restante = novaVenda.getQuantidade();

        for (isep.crescendo.model.Transacao compra : compras) {
            if (restante <= 0) break;

            double qtdCompra = compra.getQuantidade();
            double quantidadeExecutada = Math.min(restante, qtdCompra);

            // Registar transação executada
            adicionar(new isep.crescendo.model.Transacao(
                    novaVenda.getCarteiraId(),
                    String.valueOf(idMoeda),
                    quantidadeExecutada,
                    compra.getValor(),
                    "venda",
                    java.time.LocalDateTime.now()
            ));

            // Atualizar compra existente
            if (quantidadeExecutada == qtdCompra) {
                marcarComoExecutada(compra.getId());
            } else {
                atualizarQuantidade(compra.getId(), qtdCompra - quantidadeExecutada);
            }

            restante -= quantidadeExecutada;
        }

        // Se ainda sobrou da venda, inserir como ordem pendente
        if (restante > 0) {
            isep.crescendo.model.Transacao ordemPendente = new isep.crescendo.model.Transacao(
                    novaVenda.getCarteiraId(),
                    String.valueOf(idMoeda),
                    restante,
                    novaVenda.getValor(),
                    "venda",
                    novaVenda.getDataHora()
            );
            adicionar(ordemPendente);
        }
    }

    public void processarCompraParcial(isep.crescendo.model.Transacao novaCompra, int idMoeda) {
        marcarOrdensExpiradas();

        List<isep.crescendo.model.Transacao> vendas = buscarVendasCompativeis(idMoeda, novaCompra.getValor());

        double restante = novaCompra.getQuantidade();

        for (isep.crescendo.model.Transacao venda : vendas) {
            if (restante <= 0) break;

            double qtdVenda = venda.getQuantidade();
            double quantidadeExecutada = Math.min(restante, qtdVenda);

            // Registar transação executada
            adicionar(new isep.crescendo.model.Transacao(
                    novaCompra.getCarteiraId(),
                    String.valueOf(idMoeda),
                    quantidadeExecutada,
                    venda.getValor(),
                    "compra",
                    java.time.LocalDateTime.now()
            ));

            // Atualizar venda existente
            if (quantidadeExecutada == qtdVenda) {
                marcarComoExecutada(venda.getId());
            } else {
                atualizarQuantidade(venda.getId(), qtdVenda - quantidadeExecutada);
            }

            restante -= quantidadeExecutada;
        }

        // Se ainda sobrou da compra, inserir como ordem pendente
        if (restante > 0) {
            isep.crescendo.model.Transacao ordemPendente = new isep.crescendo.model.Transacao(
                    novaCompra.getCarteiraId(),
                    String.valueOf(idMoeda),
                    restante,
                    novaCompra.getValor(),
                    "compra",
                    novaCompra.getDataHora()
            );
            adicionar(ordemPendente);
        }
    }

    public List<isep.crescendo.model.Transacao> buscarComprasCompativeis(int idMoeda, double precoMin) {
        List<isep.crescendo.model.Transacao> lista = new ArrayList<>();

        String sql = """
        SELECT * FROM transacoes
        WHERE tipo = 'compra'
          AND id_moeda = ?
          AND valor >= ?
          AND executada = FALSE
          AND expirada = FALSE
          AND data_hora >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
        ORDER BY valor DESC, data_hora ASC
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idMoeda);
            pstmt.setDouble(2, precoMin);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                isep.crescendo.model.Transacao t = new isep.crescendo.model.Transacao(
                        rs.getInt("id"),
                        rs.getInt("carteira_id"),
                        String.valueOf(rs.getInt("id_moeda")), // ou converte conforme necessário
                        rs.getDouble("quantidade"),
                        rs.getDouble("valor"),
                        rs.getString("tipo"),
                        rs.getTimestamp("data_hora").toLocalDateTime()
                );
                lista.add(t);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar ordens de compra compatíveis: " + e.getMessage());
        }

        return lista;
    }

}

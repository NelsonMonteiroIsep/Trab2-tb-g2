package isep.crescendo.Repository;

import isep.crescendo.model.Transacao;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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



    public Map<String, Double> getTotalInvestidoPorMoeda() {
        Map<String, Double> resultado = new HashMap<>();

        String sql = """
        SELECT c.nome AS nome_moeda, SUM(t.quantidade * t.valor_unitario) AS total_investido
        FROM transacoes t
        JOIN criptomoedas c ON t.id_moeda = c.id
        GROUP BY t.id_moeda, c.nome
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nomeMoeda = rs.getString("nome_moeda");
                double totalInvestido = rs.getDouble("total_investido");
                resultado.put(nomeMoeda, totalInvestido);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao obter total investido por moeda: " + e.getMessage());
        }

        return resultado;
    }

    public List<Object[]> getTop3MoedasMaisTransacoes() {
        List<Object[]> resultado = new ArrayList<>();

        String sql = """
        SELECT c.nome AS nome_moeda,
               COUNT(t.id) AS num_transacoes,
               c.imagem_url AS imagem_url
        FROM transacoes t
        JOIN criptomoedas c ON t.id_moeda = c.id
        GROUP BY t.id_moeda, c.nome, c.imagem_url
        ORDER BY num_transacoes DESC
        LIMIT 3
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nomeMoeda = rs.getString("nome_moeda");
                long numTransacoes = rs.getLong("num_transacoes");
                String imagemUrl = rs.getString("imagem_url");

                Object[] row = new Object[]{nomeMoeda, numTransacoes, imagemUrl};
                resultado.add(row);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao obter top 3 moedas com mais transações: " + e.getMessage());
        }

        return resultado;
    }


    public Map<Integer, Double> getVolumePorMoeda() {
        Map<Integer, Double> result = new HashMap<>();

        String sql = "SELECT id_moeda, SUM(quantidade * valor_unitario) AS volume_total " +
                "FROM transacoes GROUP BY id_moeda";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int idMoeda = rs.getInt("id_moeda");
                double volume = rs.getDouble("volume_total");
                result.put(idMoeda, volume);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao obter volume por moeda: " + e.getMessage());
        }

        return result;
    }

    // Top utilizadores por volume transacionado (user_id → soma de quantidade * valor_unitario)
// (precisa JOIN com ordens + carteiras + users)
    public Map<String, Double> getVolumePorUtilizador() {
        Map<String, Double> result = new HashMap<>();

        String sql = """
        SELECT u.nome, SUM(t.quantidade * t.valor_unitario) AS volume_total
        FROM transacoes t
        JOIN ordens o ON t.ordem_compra_id = o.id
        JOIN carteiras c ON o.carteira_id = c.id
        JOIN users u ON c.user_id = u.id
        GROUP BY u.id, u.nome
        ORDER BY volume_total DESC
        LIMIT 5
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nomeUtilizador = rs.getString("nome");
                double volume = rs.getDouble("volume_total");
                result.put(nomeUtilizador, volume);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao obter volume por utilizador: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Integer> getNumeroTransacoesPorData(LocalDateTime inicio, LocalDateTime fim) {
        Map<String, Integer> resultado = new HashMap<>();

        String sql = """
    SELECT DATE(data_execucao) AS dia, COUNT(*) AS num_transacoes
    FROM transacoes
    WHERE data_execucao BETWEEN ? AND ?
    GROUP BY dia
    ORDER BY dia ASC
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String dia = rs.getString("dia");
                int numTransacoes = rs.getInt("num_transacoes");
                resultado.put(dia, numTransacoes);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao obter número de transações por data: " + e.getMessage());
        }

        return resultado;
    }

    public Map<String, Integer> getTotalTransacoesPorTipoDoUser(int userId, LocalDateTime inicio, LocalDateTime fim) {
        Map<String, Integer> resultado = new HashMap<>();

        String sql = """
        SELECT tipo, SUM(num_transacoes) AS total_transacoes
        FROM (
            SELECT 'compra' AS tipo, COUNT(*) AS num_transacoes
            FROM transacoes t
            JOIN ordens o ON t.ordem_compra_id = o.id
            JOIN carteiras c ON o.carteira_id = c.id
            WHERE c.user_id = ? AND t.data_execucao BETWEEN ? AND ?
            
            UNION ALL
            
            SELECT 'venda' AS tipo, COUNT(*) AS num_transacoes
            FROM transacoes t
            JOIN ordens o ON t.ordem_venda_id = o.id
            JOIN carteiras c ON o.carteira_id = c.id
            WHERE c.user_id = ? AND t.data_execucao BETWEEN ? AND ?
        ) AS sub
        GROUP BY tipo
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setTimestamp(2, Timestamp.valueOf(inicio));
            stmt.setTimestamp(3, Timestamp.valueOf(fim));

            stmt.setInt(4, userId);
            stmt.setTimestamp(5, Timestamp.valueOf(inicio));
            stmt.setTimestamp(6, Timestamp.valueOf(fim));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String tipo = rs.getString("tipo");  // "compra" ou "venda"
                int numTransacoes = rs.getInt("total_transacoes");

                resultado.put(tipo, numTransacoes);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao obter total de transações por tipo do user: " + e.getMessage());
        }

        return resultado;
    }

    public static Map<String, Map<String, Double>> getHistoricoSaldoPorCripto(int userId, LocalDate dataInicio, LocalDate dataFim) {
        Map<String, Map<String, Double>> historico = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            for (LocalDate data = dataInicio; !data.isAfter(dataFim); data = data.plusDays(1)) {
                String sql = """
                SELECT nome_cripto, SUM(saldo) AS saldo_total
                FROM (
                    -- Compras
                    SELECT c.nome AS nome_cripto, t.quantidade AS saldo
                    FROM transacoes t
                    JOIN ordens o ON t.ordem_compra_id = o.id
                    JOIN carteiras ca ON o.carteira_id = ca.id
                    JOIN criptomoedas c ON t.id_moeda = c.id
                    WHERE ca.user_id = ?
                      AND t.data_execucao <= ?

                    UNION ALL

                    -- Vendas
                    SELECT c.nome AS nome_cripto, -t.quantidade AS saldo
                    FROM transacoes t
                    JOIN ordens o ON t.ordem_venda_id = o.id
                    JOIN carteiras ca ON o.carteira_id = ca.id
                    JOIN criptomoedas c ON t.id_moeda = c.id
                    WHERE ca.user_id = ?
                      AND t.data_execucao <= ?
                ) AS saldo_union
                GROUP BY nome_cripto
                """;

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setTimestamp(2, Timestamp.valueOf(data.atTime(23, 59, 59)));
                    stmt.setInt(3, userId);
                    stmt.setTimestamp(4, Timestamp.valueOf(data.atTime(23, 59, 59)));

                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        String nomeCripto = rs.getString("nome_cripto");
                        double saldo = rs.getDouble("saldo_total");

                        historico.putIfAbsent(nomeCripto, new LinkedHashMap<>());
                        historico.get(nomeCripto).put(data.toString(), saldo);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historico;
    }


    public static Map<Integer, Map<String, Double>> getVolumeTransacoesPorDia(List<Integer> userIdList, LocalDate dataInicio, LocalDate dataFim, boolean volumeEmEuros) {
        Map<Integer, Map<String, Double>> historico = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = """
            SELECT c.user_id,
                   DATE(t.data_execucao) AS dia,
                   %s AS valor
            FROM transacoes t
            JOIN ordens o ON t.ordem_compra_id = o.id OR t.ordem_venda_id = o.id
            JOIN carteiras c ON o.carteira_id = c.id
            WHERE c.user_id IN (%s)
              AND t.data_execucao BETWEEN ? AND ?
            GROUP BY c.user_id, dia
            ORDER BY c.user_id, dia
            """;

            String valorSelect = volumeEmEuros ? "SUM(t.quantidade * t.valor_unitario)" : "COUNT(*)";

            String placeholders = userIdList.stream().map(id -> "?").collect(java.util.stream.Collectors.joining(","));

            sql = String.format(sql, valorSelect, placeholders);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int index = 1;
                for (Integer userId : userIdList) {
                    stmt.setInt(index++, userId);
                }
                stmt.setTimestamp(index++, Timestamp.valueOf(dataInicio.atStartOfDay()));
                stmt.setTimestamp(index, Timestamp.valueOf(dataFim.atTime(23, 59, 59)));

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String dia = rs.getString("dia");
                    double valor = rs.getDouble("valor");

                    historico.putIfAbsent(userId, new LinkedHashMap<>());
                    historico.get(userId).put(dia, valor);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historico;
    }

    public String getNomeById(int id) {
        String sql = "SELECT nome FROM criptomoedas WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nome");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    // 1️⃣ Volume Global por Dia
    public static Map<String, Double> getVolumeGlobalPorDia() {
        Map<String, Double> result = new LinkedHashMap<>();

        String sql = """
            SELECT DATE(data_execucao) AS dia, SUM(quantidade * valor_unitario) AS volume
            FROM transacoes
            GROUP BY dia
            ORDER BY dia ASC
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String dia = rs.getString("dia");
                double volume = rs.getDouble("volume");
                result.put(dia, volume);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    // 2️⃣ Top 5 Utilizadores por Volume — com JOIN correto!
    public static Map<String, Double> getTop5UsersPorVolume() {
        Map<String, Double> result = new LinkedHashMap<>();

        String sql = """
            SELECT u.nome, SUM(t.quantidade * t.valor_unitario) AS volume
            FROM transacoes t
            JOIN ordens o ON t.ordem_compra_id = o.id OR t.ordem_venda_id = o.id
            JOIN carteiras c ON o.carteira_id = c.id
            JOIN users u ON c.user_id = u.id
            GROUP BY u.id
            ORDER BY volume DESC
            LIMIT 5
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nome = rs.getString("nome");
                double volume = rs.getDouble("volume");
                result.put(nome, volume);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    // 3️⃣ Top 3 Moedas com mais Transações — corrige para id_moeda
    public static Map<String, Integer> getTop3MoedasPorTransacoes() {
        Map<String, Integer> result = new LinkedHashMap<>();

        String sql = """
            SELECT c.nome, COUNT(t.id) AS num_transacoes
            FROM transacoes t
            JOIN criptomoedas c ON c.id = t.id_moeda
            GROUP BY c.id
            ORDER BY num_transacoes DESC
            LIMIT 3
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nome = rs.getString("nome");
                int numTransacoes = rs.getInt("num_transacoes");
                result.put(nome, numTransacoes);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    // 4️⃣ Distribuição do Volume por Moeda — corrige para id_moeda
    public static Map<String, Double> getDistribuicaoVolumePorMoeda() {
        Map<String, Double> result = new LinkedHashMap<>();

        String sql = """
            SELECT c.nome, SUM(t.quantidade * t.valor_unitario) AS volume
            FROM transacoes t
            JOIN criptomoedas c ON c.id = t.id_moeda
            GROUP BY c.id
            ORDER BY volume DESC
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nome = rs.getString("nome");
                double volume = rs.getDouble("volume");
                result.put(nome, volume);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    // 5️⃣ Percentagem de Atividade nos Últimos 30 Dias — com JOIN correto!
    public static double getPercentAtividadeUltimos30Dias() {
        double percent = 0.0;

        String sqlTotal = "SELECT COUNT(*) AS total FROM users";
        String sqlAtivos = """
            SELECT COUNT(DISTINCT c.user_id) AS ativos
            FROM transacoes t
            JOIN ordens o ON t.ordem_compra_id = o.id OR t.ordem_venda_id = o.id
            JOIN carteiras c ON o.carteira_id = c.id
            WHERE t.data_execucao >= (CURRENT_DATE - INTERVAL 30 DAY)
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmtTotal = conn.prepareStatement(sqlTotal);
             PreparedStatement stmtAtivos = conn.prepareStatement(sqlAtivos);
             ResultSet rsTotal = stmtTotal.executeQuery();
             ResultSet rsAtivos = stmtAtivos.executeQuery()) {

            int totalUsers = 0;
            int ativos = 0;

            if (rsTotal.next()) {
                totalUsers = rsTotal.getInt("total");
            }

            if (rsAtivos.next()) {
                ativos = rsAtivos.getInt("ativos");
            }

            if (totalUsers > 0) {
                percent = (double) ativos / totalUsers;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return percent;
    }

    // 6️⃣ Total Investido (ordem_compra_id != NULL)
    public static double getTotalInvestido() {
        double total = 0.0;

        String sql = """
            SELECT SUM(t.quantidade * t.valor_unitario) AS total_investido
            FROM transacoes t
            WHERE t.ordem_compra_id IS NOT NULL
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                total = rs.getDouble("total_investido");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return total;
    }

}



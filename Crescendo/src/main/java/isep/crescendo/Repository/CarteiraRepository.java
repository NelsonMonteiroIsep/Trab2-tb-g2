package isep.crescendo.Repository;

import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import isep.crescendo.model.MoedaSaldo;

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

    public static void apagarPorUserId(int userId) {
        String sql = "DELETE FROM carteiras WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao apagar carteira: " + e.getMessage());
        }
    }

    public void adicionar(isep.crescendo.model.Carteira carteira) {
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

    public static isep.crescendo.model.Carteira procurarPorUserId(int userId) {
        String sql = "SELECT * FROM carteiras WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                double saldo = rs.getDouble("saldo");
                return new isep.crescendo.model.Carteira(id, userId, saldo);
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

    public void adicionarSaldo(int carteiraId, double valor) {
        String sql = "UPDATE carteiras SET saldo = saldo + ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, valor);
            pstmt.setInt(2, carteiraId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao adicionar saldo à carteira: " + e.getMessage());
        }
    }

    public boolean temSaldoCripto(int carteiraId, int idMoeda, double quantidade) {
        double saldo = calcularSaldoMoeda(carteiraId, idMoeda);
        return saldo >= quantidade;
    }

    public double calcularSaldoMoeda(int carteiraId, int idMoeda) {
        String sql = """
        SELECT o.tipo, t.quantidade
        FROM transacoes t
        JOIN ordens o ON t.ordem_compra_id = o.id OR t.ordem_venda_id = o.id
        WHERE o.carteira_id = ? AND t.id_moeda = ?
    """;

        double saldo = 0.0;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carteiraId);
            stmt.setInt(2, idMoeda);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String tipo = rs.getString("tipo");
                double qtd = rs.getDouble("quantidade");
                saldo += tipo.equalsIgnoreCase("compra") ? qtd : -qtd;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao calcular saldo da moeda: " + e.getMessage());
        }

        return saldo;
    }

    public boolean temSaldo(int carteiraId, double valor) {
        String sql = "SELECT saldo FROM carteiras WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carteiraId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("saldo") >= valor;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar saldo: " + e.getMessage());
        }
        return false;
    }

    public void removerSaldo(int carteiraId, double valor) {
        String sql = "UPDATE carteiras SET saldo = saldo - ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, valor);
            stmt.setInt(2, carteiraId);
            int linhas = stmt.executeUpdate();
            System.out.println("RemoverSaldo - linhas afetadas: " + linhas);

            ResultSet rs = conn.createStatement().executeQuery("SELECT saldo FROM carteiras WHERE id = " + carteiraId);
            if (rs.next()) {
                System.out.println("Saldo atual (após remover): " + rs.getDouble("saldo"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover saldo: " + e.getMessage());
        }
    }

    public boolean podeVender(int carteiraId, int idMoeda, double novaQuantidade) {
        double saldoExecutado = getSaldoExecutadoCripto(carteiraId, idMoeda);
        double pendenteVenda = getQuantidadePendenteVenda(carteiraId, idMoeda);

        return (saldoExecutado - pendenteVenda) >= novaQuantidade;
    }

    private double getSaldoExecutadoCripto(int carteiraId, int idMoeda) {
        String sql = """
        SELECT SUM(
            CASE 
                WHEN o.tipo = 'compra' THEN t.quantidade 
                ELSE -t.quantidade 
            END
        ) AS saldo
        FROM transacoes t
        JOIN ordens o 
          ON (o.id = t.ordem_compra_id AND o.tipo = 'compra' AND o.carteira_id = ?)
          OR (o.id = t.ordem_venda_id AND o.tipo = 'venda' AND o.carteira_id = ?)
        WHERE t.id_moeda = ?
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carteiraId);
            stmt.setInt(2, carteiraId);
            stmt.setInt(3, idMoeda);

            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble("saldo") : 0.0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao calcular saldo executado: " + e.getMessage());
        }
    }


    private double getQuantidadePendenteVenda(int carteiraId, int idMoeda) {
        String sql = """
        SELECT SUM(quantidade) AS total
        FROM ordens
        WHERE carteira_id = ? AND id_moeda = ? AND tipo = 'venda' AND status = 'pendente'
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carteiraId);
            stmt.setInt(2, idMoeda);

            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble("total") : 0.0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao calcular vendas pendentes: " + e.getMessage());
        }
    }


    public double obterSaldoCripto(int carteiraId, int idMoeda) {
        double saldo = 0.0;

        String sql = """
        SELECT o.tipo, t.quantidade
        FROM transacoes t
        JOIN ordens o ON
            (o.id = t.ordem_compra_id AND o.tipo = 'compra' AND o.carteira_id = ?)
            OR (o.id = t.ordem_venda_id AND o.tipo = 'venda' AND o.carteira_id = ?)
        WHERE t.id_moeda = ?
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carteiraId); // para compra
            stmt.setInt(2, carteiraId); // para venda
            stmt.setInt(3, idMoeda);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String tipo = rs.getString("tipo");
                double qtd = rs.getDouble("quantidade");

                if ("compra".equalsIgnoreCase(tipo)) {
                    saldo += qtd;
                } else if ("venda".equalsIgnoreCase(tipo)) {
                    saldo -= qtd;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao calcular saldo executado: " + e.getMessage());
        }

        return saldo;
    }

    public static ObservableList<MoedaSaldo> listarMoedasCarteira(int carteiraId) {
        ObservableList<MoedaSaldo> lista = FXCollections.observableArrayList();

        String sql = """
        SELECT\s
            m.nome,\s
            m.imagem_url,
            SUM(CASE WHEN o.tipo = 'compra' THEN t.quantidade ELSE -t.quantidade END) AS quantidade,
            ROUND(
                SUM(CASE WHEN o.tipo = 'compra' THEN t.quantidade * t.valor_unitario ELSE 0 END) /
                NULLIF(SUM(CASE WHEN o.tipo = 'compra' THEN t.quantidade ELSE 0 END), 0), 6
            ) AS preco_medio_compra,
            MAX(CASE WHEN o.tipo = 'compra' THEN o.data_hora ELSE NULL END) AS ultima_compra
        FROM transacoes t
        JOIN ordens o\s
            ON (t.ordem_compra_id = o.id AND o.tipo = 'compra' AND o.carteira_id = ?)\s
            OR (t.ordem_venda_id = o.id AND o.tipo = 'venda' AND o.carteira_id = ?)
        JOIN criptomoedas m ON m.id = t.id_moeda
        GROUP BY m.nome, m.imagem_url
        HAVING quantidade > 0
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carteiraId);
            stmt.setInt(2, carteiraId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(new MoedaSaldo(
                        rs.getString("nome"),
                        rs.getDouble("quantidade"),
                        rs.getString("imagem_url"),
                        rs.getDouble("preco_medio_compra"),
                        rs.getTimestamp("ultima_compra")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar moedas da carteira: " + e.getMessage());
        }

        return lista;
    }

    public static double calcularSaldoInvestido(int carteiraId) {
        String sql = """
        SELECT SUM(quantidade * valor_mais_recente) AS saldo_total
        FROM (
            SELECT 
                m.id AS moeda_id,
                SUM(CASE 
                    WHEN o.tipo = 'compra' THEN t.quantidade 
                    ELSE -t.quantidade 
                END) AS quantidade,
                (
                    SELECT h.valor 
                    FROM historico_valores h 
                    WHERE h.cripto_id = m.id 
                    ORDER BY h.data DESC 
                    LIMIT 1
                ) AS valor_mais_recente
            FROM transacoes t
            JOIN ordens o 
                ON (t.ordem_compra_id = o.id AND o.tipo = 'compra' AND o.carteira_id = ?)
                OR (t.ordem_venda_id = o.id AND o.tipo = 'venda' AND o.carteira_id = ?)
            JOIN criptomoedas m ON m.id = t.id_moeda
            GROUP BY m.id
            HAVING quantidade > 0
        ) AS subquery
    """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, carteiraId);
            stmt.setInt(2, carteiraId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("saldo_total");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao calcular saldo investido: " + e.getMessage());
        }

        return 0.0;
    }

}

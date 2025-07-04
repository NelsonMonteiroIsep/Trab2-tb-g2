package isep.crescendo.Repository;

import isep.crescendo.model.HistoricoValor;
import isep.crescendo.util.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoricoValorRepository {


    /**
     * Construtor da classe HistoricoValorRepository.
     * Chama o método para criar a tabela se ela ainda não existir na base de dados.
     */
    public HistoricoValorRepository() {
        criarTabelaSeNaoExistir();
    }

    /**
     * Estabelece e retorna uma conexão com a base de dados.
     * @return Uma conexão JDBC com a base de dados MySQL.
     * @throws SQLException Se ocorrer um erro ao estabelecer a conexão.
     */
    private static Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    /**
     * Cria a tabela 'historico_valores' na base de dados se ela ainda não existir.
     * A tabela armazena o ID da criptomoeda, a data e o valor associado.
     * A chave primária é uma combinação de cripto_id e data para garantir unicidade e histórico.
     * Existe uma chave estrangeira referenciando a tabela 'criptomoedas'.
     */
    private void criarTabelaSeNaoExistir() {
        String sql = """
            CREATE TABLE IF NOT EXISTS historico_valores (
                cripto_id INT NOT NULL,
                data DATETIME NOT NULL,
                valor DECIMAL(18, 8) NOT NULL, -- CORREÇÃO AQUI: de 'NOT CELLS' para 'NOT NULL'
                PRIMARY KEY (cripto_id, data),
                FOREIGN KEY (cripto_id) REFERENCES criptomoedas(id) ON DELETE CASCADE
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabela 'historico_valores' verificada/criada com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabela 'historico_valores': " + e.getMessage());
            throw new RuntimeException("Erro fatal ao inicializar o repositório: Falha ao criar a tabela 'historico_valores'.", e);
        }
    }

    /**
     * Adiciona um novo registo de valor histórico para uma criptomoeda.
     * @param criptoId O ID da criptomoeda.
     * @param data A data e hora do registo.
     * @param valor O valor da criptomoeda nesse momento.
     */
    public void adicionarValor(int criptoId, LocalDateTime data, double valor) {
        String sql = "INSERT INTO historico_valores (cripto_id, data, valor) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, criptoId);
            pstmt.setTimestamp(2, Timestamp.valueOf(data));
            pstmt.setDouble(3, valor);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar valor histórico para cripto_id " + criptoId + " em " + data + ": " + e.getMessage());
            throw new RuntimeException("Erro ao adicionar valor histórico: " + e.getMessage(), e);
        }
    }

    /**
     * Lista todos os valores históricos para uma criptomoeda específica, ordenados por data.
     * @param criptoId O ID da criptomoeda para a qual listar o histórico.
     * @return Uma lista de objetos HistoricoValor.
     */
    public List<HistoricoValor> listarPorCripto(int criptoId) {
        List<HistoricoValor> lista = new ArrayList<>();
        // Adicionado LIMIT 100 para evitar carregar muitos dados no gráfico
        String sql = "SELECT cripto_id, data, valor FROM historico_valores WHERE cripto_id = ? ORDER BY data ASC LIMIT 100";

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
            System.err.println("Erro ao listar histórico de valores para cripto_id " + criptoId + ": " + e.getMessage());
            throw new RuntimeException("Erro ao listar histórico de valores: " + e.getMessage(), e);
        }

        return lista;
    }

    /**
     * Busca o último registro de valor para uma dada criptomoeda na base de dados.
     * O "último" é determinado pela data mais recente.
     * @param criptoId O ID da criptomoeda.
     * @return O objeto HistoricoValor mais recente para a criptomoeda, ou null se não houver registros.
     */
    public HistoricoValor getUltimoValorPorCripto(int criptoId) {
        String sql = "SELECT cripto_id, data, valor FROM historico_valores WHERE cripto_id = ? ORDER BY data DESC LIMIT 1";
        HistoricoValor ultimoValor = null;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, criptoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) { // Se encontrou um registro
                ultimoValor = new HistoricoValor();
                ultimoValor.setCriptoId(rs.getInt("cripto_id"));
                Timestamp timestamp = rs.getTimestamp("data");
                ultimoValor.setData(timestamp.toLocalDateTime());
                ultimoValor.setValor(rs.getDouble("valor"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar o último valor histórico para cripto ID " + criptoId + ": " + e.getMessage());
            throw new RuntimeException("Erro de acesso a dados ao buscar último valor histórico", e);
        }
        return ultimoValor;
    }

    public List<HistoricoValor> listarPorCripto(int criptoId, LocalDateTime dataInicial) {
        List<HistoricoValor> lista = new ArrayList<>();
        String sql = "SELECT cripto_id, data, valor FROM historico_valores WHERE cripto_id = ? AND data >= ? ORDER BY data ASC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, criptoId);
            pstmt.setTimestamp(2, Timestamp.valueOf(dataInicial));

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                HistoricoValor hv = new HistoricoValor();
                hv.setCriptoId(rs.getInt("cripto_id"));
                hv.setData(rs.getTimestamp("data").toLocalDateTime());
                hv.setValor(rs.getDouble("valor"));
                lista.add(hv);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar histórico de valores para cripto_id " + criptoId + ": " + e.getMessage());
            throw new RuntimeException("Erro ao listar histórico de valores: " + e.getMessage(), e);
        }

        return lista;
    }

    public static double getValorByCriptoNomeAndData(String nomeCripto, String dataYYYYMMDD) {
        String sql = """
        SELECT valor
        FROM historico_valores h
        JOIN criptomoedas c ON h.cripto_id = c.id
        WHERE c.nome = ?
          AND DATE(h.data) <= ?
        ORDER BY h.data DESC
        LIMIT 1
    """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nomeCripto);
            stmt.setString(2, dataYYYYMMDD);  // usar String "YYYY-MM-DD"

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("valor");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Se não houver NENHUM valor anterior → 0.0
        return 0.0;
    }

    public List<HistoricoValor> getDoisUltimosValores(int criptoId) {
        String sql = """
        SELECT cripto_id, data, valor 
        FROM historico_valores 
        WHERE cripto_id = ? 
        ORDER BY data DESC 
        LIMIT 2
    """;
        List<HistoricoValor> valores = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, criptoId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HistoricoValor hv = new HistoricoValor();
                hv.setCriptoId(rs.getInt("cripto_id"));
                hv.setData(rs.getTimestamp("data").toLocalDateTime());
                hv.setValor(rs.getDouble("valor"));
                valores.add(hv);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao obter dois últimos valores: " + e.getMessage());
        }

        return valores;
    }
}
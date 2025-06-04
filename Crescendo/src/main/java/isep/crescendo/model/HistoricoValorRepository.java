package isep.crescendo.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoricoValorRepository {

    // Credenciais da base de dados MySQL (FreeSQLDatabase.com)
    private static final String DB_URL = "jdbc:mysql://sql7.freesqldatabase.com:3306/sql7779870";
    private static final String DB_USER = "sql7779870";
    private static final String DB_PASSWORD = "vUwAKDaynR";

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
    private Connection getConnection() throws SQLException {
        // Para MySQL, não é estritamente necessário carregar o driver manualmente
        // em versões mais recentes do JDBC (Java 6+), mas é uma boa prática para garantir.
        // try {
        //     Class.forName("com.mysql.cj.jdbc.Driver");
        // } catch (ClassNotFoundException e) {
        //     throw new SQLException("Driver JDBC do MySQL não encontrado.", e);
        // }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
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
                valor DECIMAL(18, 8) NOT NULL,
                PRIMARY KEY (cripto_id, data),
                FOREIGN KEY (cripto_id) REFERENCES criptomoedas(id) ON DELETE CASCADE
            );
        """;

        try (Connection conn = getConnection(); // Obtém a conexão
             Statement stmt = conn.createStatement()) { // Cria um statement para executar o SQL
            stmt.execute(sql); // Executa a instrução SQL
            System.out.println("Tabela 'historico_valores' verificada/criada com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabela 'historico_valores': " + e.getMessage());
            // É importante lançar uma RuntimeException para sinalizar que o repositório não pode funcionar
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
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Prepara a instrução SQL

            pstmt.setInt(1, criptoId); // Define o primeiro parâmetro (cripto_id)
            pstmt.setTimestamp(2, Timestamp.valueOf(data)); // Define o segundo parâmetro (data)
            pstmt.setDouble(3, valor); // Define o terceiro parâmetro (valor)

            pstmt.executeUpdate(); // Executa a atualização (INSERT)
            // System.out.println("Valor histórico adicionado para cripto_id " + criptoId + " em " + data);
        } catch (SQLException e) {
            // Se houver uma duplicata de chave primária (cripto_id, data), isto pode ocorrer.
            // Pode querer tratar SQLException específica para duplicatas.
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
        // A sua query já estava a usar ORDER BY data, o que é ótimo para o gráfico.
        // A coluna ID não existe na sua tabela, então HistoricoValor precisa ser adaptado
        // para não esperar o ID, ou você pode gerar um ID fictício para o objeto.
        String sql = "SELECT cripto_id, data, valor FROM historico_valores WHERE cripto_id = ? ORDER BY data ASC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, criptoId); // Define o parâmetro cripto_id
            ResultSet rs = pstmt.executeQuery(); // Executa a query

            while (rs.next()) {
                HistoricoValor hv = new HistoricoValor();
                // Não há 'id' na sua tabela, então não tente rs.getInt("id")
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
}
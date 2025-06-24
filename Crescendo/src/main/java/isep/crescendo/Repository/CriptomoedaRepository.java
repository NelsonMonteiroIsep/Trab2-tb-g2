package isep.crescendo.Repository;

import isep.crescendo.model.Criptomoeda;
import isep.crescendo.util.DatabaseConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CriptomoedaRepository {


    public CriptomoedaRepository() {
        criarTabelaSeNaoExistir();
    }

    // Método auxiliar para obter a conexão com a base de dados
    private Connection getConnection() throws SQLException {
        try {
            // Este Class.forName é importante para carregar o driver.
            // Para MySQL, é "com.mysql.cj.jdbc.Driver".
            // Para SQLite, seria "org.sqlite.JDBC".
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC não encontrado: " + e.getMessage());
            throw new SQLException("Driver JDBC não encontrado. Verifique suas dependências (build.gradle).", e);
        }
        return DatabaseConfig.getConnection();
    }

    private void criarTabelaSeNaoExistir() {
        // Ajustei o SQL para remover 'descricao' e 'data_criacao' se não existirem na sua Criptomoeda model
        // e 'UNIQUE' nas colunas que não deveriam ter, se você as removeu da sua Criptomoeda.java
        // Mantenho a estrutura que você forneceu no diff, incluindo 'descricao' e 'ativo',
        // mas certifique-se que o seu modelo Criptomoeda tem estes campos e seus getters/setters.
        String sql = """
            CREATE TABLE IF NOT EXISTS criptomoedas (
                id INT AUTO_INCREMENT PRIMARY KEY,
                nome VARCHAR(100) NOT NULL UNIQUE,
                simbolo VARCHAR(10) NOT NULL UNIQUE,
                descricao TEXT,
                ativo BOOLEAN DEFAULT TRUE,
                imagem_url VARCHAR(255),
                data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        try (Connection conn = getConnection(); // Usando getConnection() agora para consistência
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabela 'criptomoedas' verificada/criada com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabela criptomoedas: " + e.getMessage());
            throw new RuntimeException("Erro ao criar tabela criptomoedas: " + e.getMessage(), e);
        }
    }

    /**
     * Adiciona uma nova criptomoeda à base de dados usando um objeto Criptomoeda.
     * Assume que o ID da criptomoeda será gerado pela DB.
     * @param cripto O objeto Criptomoeda a ser adicionado.
     */
    public void adicionar(Criptomoeda cripto) {
        String sql = "INSERT INTO criptomoedas (nome, simbolo, descricao, ativo, imagem_url) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, cripto.getNome());
            pstmt.setString(2, cripto.getSimbolo());
            pstmt.setString(3, cripto.getDescricao()); // Verifique se Criptomoeda tem getDescricao()
            pstmt.setBoolean(4, cripto.isAtivo());     // Verifique se Criptomoeda tem isAtivo()
            pstmt.setString(5, cripto.getImagemUrl());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        cripto.setId(generatedKeys.getInt(1)); // Define o ID gerado no objeto
                    }
                }
                System.out.println("Criptomoeda " + cripto.getNome() + " (" + cripto.getSimbolo() + ") adicionada com sucesso. ID: " + cripto.getId());
            } else {
                System.err.println("Falha ao adicionar criptomoeda " + cripto.getNome() + ".");
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL ao adicionar criptomoeda (objeto): " + e.getMessage());
            throw new RuntimeException("Erro ao adicionar criptomoeda.", e);
        }
    }

    /**
     * Atualiza uma criptomoeda existente na base de dados.
     * @param cripto O objeto Criptomoeda com os dados atualizados (o ID deve estar preenchido).
     */
    public void atualizar(Criptomoeda cripto) {
        // Ajustei a query para atualizar todos os campos relevantes
        String sql = "UPDATE criptomoedas SET nome = ?, simbolo = ?, descricao = ?, ativo = ?, imagem_url = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cripto.getNome());
            pstmt.setString(2, cripto.getSimbolo());
            pstmt.setString(3, cripto.getDescricao()); // Verifique se Criptomoeda tem getDescricao()
            pstmt.setBoolean(4, cripto.isAtivo());     // Verifique se Criptomoeda tem isAtivo()
            pstmt.setString(5, cripto.getImagemUrl());
            pstmt.setInt(6, cripto.getId()); // O ID é usado para identificar qual registro atualizar

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Criptomoeda " + cripto.getNome() + " (ID: " + cripto.getId() + ") atualizada com sucesso.");
            } else {
                System.err.println("Nenhuma criptomoeda encontrada com ID " + cripto.getId() + " para atualização.");
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL ao atualizar criptomoeda: " + e.getMessage());
            throw new RuntimeException("Erro ao atualizar criptomoeda.", e);
        }
    }

    /**
     * Obtém o ID de uma criptomoeda pelo seu símbolo.
     * @param simbolo O símbolo da criptomoeda.
     * @return Um Optional contendo o ID se encontrado, ou vazio se não encontrado.
     */
    public Optional<Integer> getCriptoIdBySimbolo(String simbolo) {
        String sql = "SELECT id FROM criptomoedas WHERE simbolo = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, simbolo);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL ao obter ID da criptomoeda por símbolo: " + e.getMessage());
            // Logar o erro, mas retornar Optional.empty() para indicar que não foi encontrado ou houve falha.
        }
        return Optional.empty();
    }

    /**
     * Mapeia um ResultSet para um objeto Criptomoeda.
     * Método auxiliar para evitar duplicação de código.
     */
    private Criptomoeda mapResultSetParaCriptomoeda(ResultSet rs) throws SQLException {
        Criptomoeda c = new Criptomoeda();
        c.setId(rs.getInt("id"));
        c.setNome(rs.getString("nome"));
        c.setSimbolo(rs.getString("simbolo"));
        // Adicione estas linhas se 'descricao', 'ativo' e 'data_criacao' existirem na sua DB e modelo
        c.setDescricao(rs.getString("descricao"));
        c.setAtivo(rs.getBoolean("ativo"));
        c.setImagemUrl(rs.getString("imagem_url"));
        c.setDataCriacao(rs.getTimestamp("data_criacao"));
        return c;
    }

    /**
     * Lista todas as criptomoedas da base de dados.
     * @return Uma ObservableList de objetos Criptomoeda.
     */
    public ObservableList<Criptomoeda> getAllCriptomoedas() {
        ObservableList<Criptomoeda> lista = FXCollections.observableArrayList();
        String sql = "SELECT id, nome, simbolo, descricao, ativo, imagem_url, data_criacao FROM criptomoedas ORDER BY nome";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String simbolo = rs.getString("simbolo");

                // Filtrando criptomoedas com símbolo inválido (nulo ou vazio)
                if (simbolo == null || simbolo.trim().isEmpty()) {
                    continue; // Ignora essa criptomoeda e passa para a próxima
                }

                // Adiciona a criptomoeda válida à lista
                lista.add(mapResultSetParaCriptomoeda(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL ao listar todas as criptomoedas: " + e.getMessage());
            throw new RuntimeException("Erro ao listar criptomoedas.", e);
        }
        return lista;
    }


    /**
     * Lista todas as criptomoedas ATIVAS da base de dados.
     * @return Uma ObservableList de objetos Criptomoeda que estão ativas.
     */
    public ObservableList<Criptomoeda> getAllCriptomoedasAtivas() {
        ObservableList<Criptomoeda> lista = FXCollections.observableArrayList();
        String sql = "SELECT id, nome, simbolo, descricao, ativo, imagem_url, data_criacao FROM criptomoedas WHERE ativo = TRUE ORDER BY nome";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapResultSetParaCriptomoeda(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL ao listar criptomoedas ativas: " + e.getMessage());
            throw new RuntimeException("Erro ao listar criptomoedas ativas.", e);
        }
        return lista;
    }

    /**
     * Procura uma criptomoeda pelo seu ID.
     * @param id O ID da criptomoeda.
     * @return O objeto Criptomoeda correspondente, ou null se não for encontrado.
     */
    public Criptomoeda procurarPorId(int id) {
        String sql = "SELECT id, nome, simbolo, descricao, ativo, imagem_url, data_criacao FROM criptomoedas WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetParaCriptomoeda(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL ao procurar criptomoeda por ID: " + e.getMessage());
            throw new RuntimeException("Erro ao procurar criptomoeda.", e);
        }
        return null; // Retorna null se não encontrar
    }

    public int countCriptomoedas() {
        String sql = "SELECT COUNT(*) FROM criptomoedas";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar criptomoedas: " + e.getMessage());
        }

        return 0;
    }

    /*
    // O método 'adicionarCriptomoeda(String nome, String simbolo)' da minha versão anterior foi removido
    // pois o método 'adicionar(Criptomoeda cripto)' é mais genérico e provavelmente o que você precisa.
    // Se ainda precisar daquele, pode adicioná-lo de volta e chamar o 'adicionar(Criptomoeda)' com um novo objeto Criptomoeda.
    */

    /*
    // O método 'listarTodasCriptomoedas()' da minha versão anterior também foi substituído por 'getAllCriptomoedas()'
    // que retorna uma ObservableList, o que é mais útil para o JavaFX.
    // Se você precisa de uma List normal, pode criar um método que chame getAllCriptomoedas().
    public List<Criptomoeda> listarTodasCriptomoedas() {
        return new ArrayList<>(getAllCriptomoedas());
    }
    */
    public String getNomeById(int id) {
        String sql = "SELECT nome FROM criptomoedas WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("nome");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Desconhecido"; // caso não encontre
    }
}
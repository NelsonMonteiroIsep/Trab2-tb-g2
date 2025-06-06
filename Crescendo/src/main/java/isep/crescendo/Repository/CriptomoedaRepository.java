package isep.crescendo.Repository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class CriptomoedaRepository {

    private static final String DB_URL = "jdbc:mysql://sql7.freesqldatabase.com:3306/sql7779870";
    private static final String DB_USER = "sql7779870";
    private static final String DB_PASSWORD = "vUwAKDaynR";

    public CriptomoedaRepository() {
        criarTabelaSeNaoExistir();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void criarTabelaSeNaoExistir() {
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

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela criptomoedas: " + e.getMessage());
        }
    }

    public void adicionar(isep.crescendo.model.Criptomoeda cripto) {
        String sql = "INSERT INTO criptomoedas (nome, simbolo, descricao, ativo, imagem_url) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, cripto.getNome());
            pstmt.setString(2, cripto.getSimbolo());
            pstmt.setString(3, cripto.getDescricao());
            pstmt.setBoolean(4, cripto.isAtivo());
            pstmt.setString(5, cripto.getImagemUrl());

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                cripto.setId(rs.getInt(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao adicionar criptomoeda: " + e.getMessage());
        }
    }

    public isep.crescendo.model.Criptomoeda procurarPorId(int id) {
        String sql = "SELECT * FROM criptomoedas WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetParaCriptomoeda(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao procurar criptomoeda: " + e.getMessage());
        }

        return null;
    }



    private isep.crescendo.model.Criptomoeda mapResultSetParaCriptomoeda(ResultSet rs) throws SQLException {
        isep.crescendo.model.Criptomoeda c = new isep.crescendo.model.Criptomoeda();
        c.setId(rs.getInt("id"));
        c.setNome(rs.getString("nome"));
        c.setSimbolo(rs.getString("simbolo"));
        c.setDescricao(rs.getString("descricao"));
        c.setAtivo(rs.getBoolean("ativo"));
        c.setImagemUrl(rs.getString("imagem_url"));
        c.setDataCriacao(rs.getTimestamp("data_criacao"));
        return c;
    }

    public void atualizar(isep.crescendo.model.Criptomoeda cripto) {
        String sql = "UPDATE criptomoedas SET ativo = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, cripto.isAtivo());
            stmt.setInt(2, cripto.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<isep.crescendo.model.Criptomoeda> getAllCriptomoedas() {
        ObservableList<isep.crescendo.model.Criptomoeda> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM criptomoedas ORDER BY nome";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapResultSetParaCriptomoeda(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar criptomoedas: " + e.getMessage());
        }
        return lista;
    }

    public ObservableList<isep.crescendo.model.Criptomoeda> getAllCriptomoedasAtivas() {
        ObservableList<isep.crescendo.model.Criptomoeda> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM criptomoedas WHERE ativo = true ORDER BY nome";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapResultSetParaCriptomoeda(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar criptomoedas ativas: " + e.getMessage());
        }
        return lista;
    }



}
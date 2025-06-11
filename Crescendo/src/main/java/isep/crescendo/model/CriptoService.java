package isep.crescendo.model;

import isep.crescendo.Repository.HistoricoValorRepository;
import isep.crescendo.Repository.CriptomoedaRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList; // Importar para usar ObservableList

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CriptoService {

    private HistoricoValorRepository historicoValorRepository;
    private CriptomoedaRepository criptomoedaRepository;

    public CriptoService() {
        this.historicoValorRepository = new HistoricoValorRepository();
        this.criptomoedaRepository = new CriptomoedaRepository();
    }

    public Optional<Double> getLatestCriptoValueBySymbol(String criptoSymbol) {
        Optional<Integer> dbCriptoIdOptional = criptomoedaRepository.getCriptoIdBySimbolo(criptoSymbol);

        if (dbCriptoIdOptional.isEmpty()) {
            System.err.println("Símbolo de criptomoeda não encontrado na DB: " + criptoSymbol);
            return Optional.empty();
        }

        HistoricoValor ultimoValor = historicoValorRepository.getUltimoValorPorCripto(dbCriptoIdOptional.get());

        if (ultimoValor != null) {
            return Optional.of(ultimoValor.getValor());
        } else {
            return Optional.empty();
        }
    }

    public List<HistoricoValor> getFullHistoricoValorBySymbol(String criptoSymbol) {
        Optional<Integer> dbCriptoIdOptional = criptomoedaRepository.getCriptoIdBySimbolo(criptoSymbol);

        if (dbCriptoIdOptional.isEmpty()) {
            System.err.println("Símbolo de criptomoeda não encontrado na DB para histórico: " + criptoSymbol);
            return List.of(); // Retorna lista vazia
        }

        return historicoValorRepository.listarPorCripto(dbCriptoIdOptional.get());
    }

    public void adicionarHistoricoValor(String criptoSymbol, LocalDateTime data, double valor) {
        Optional<Integer> dbCriptoIdOptional = criptomoedaRepository.getCriptoIdBySimbolo(criptoSymbol);

        if (dbCriptoIdOptional.isEmpty()) {
            System.err.println("Não foi possível adicionar histórico: Símbolo de criptomoeda não encontrado: " + criptoSymbol);
            return;
        }
        historicoValorRepository.adicionarValor(dbCriptoIdOptional.get(), data, valor);
    }

    // Método auxiliar para obter o ID numérico, útil para o CriptoAlgoritmo
    public Optional<Integer> getCriptoDbIdBySymbol(String symbol) {
        // Este método também garante que a criptomoeda seja adicionada se não existir
        Optional<Integer> dbCriptoIdOptional = criptomoedaRepository.getCriptoIdBySimbolo(symbol);

        if (dbCriptoIdOptional.isEmpty()) {
            // Cria um novo objeto Criptomoeda e adiciona-o usando o método 'adicionar'
            Criptomoeda novaCripto = new Criptomoeda();
            novaCripto.setSimbolo(symbol);
            novaCripto.setNome("Nome Provisório " + symbol); // Um nome gerado para evitar duplicação em caso de testes
            novaCripto.setAtivo(true); // Define como ativa por padrão
            // Defina outros campos padrão se o seu modelo Criptomoeda e DB os tiverem (ex: descricao, imagemUrl)
            // novaCripto.setDescricao("Descrição padrão");
            // novaCripto.setImagemUrl("caminho/para/imagem_padrao.png");

            criptomoedaRepository.adicionar(novaCripto); // Chamada corrigida para o método 'adicionar'
            return criptomoedaRepository.getCriptoIdBySimbolo(symbol); // Tenta obter o ID novamente após adicionar
        }
        return dbCriptoIdOptional;
    }

    // Adicionado um método para listar todas as criptomoedas, agora que CriptomoedaRepository tem essa capacidade
    // Alterado para retornar ObservableList<Criptomoeda> para melhor integração com JavaFX
    public ObservableList<Criptomoeda> getAllCriptomoedas() {
        // Certifique-se de que o retorno é do tipo ObservableList<Criptomoeda>
        ObservableList<Criptomoeda> criptomoedas = FXCollections.observableArrayList();

        // Lógica para carregar as criptomoedas da DB e adicioná-las à lista
        // Exemplo:
        // criptomoedas.add(new Criptomoeda(id, nome, simbolo, descricao, ativo, imagemUrl, dataCriacao));

        return criptomoedas;
    }


    // Opcional: Se precisar de todas as criptomoedas ativas, pode adicionar este método
    public ObservableList<Criptomoeda> getAllActiveCriptomoedas() {
        return criptomoedaRepository.getAllCriptomoedasAtivas();
    }
}
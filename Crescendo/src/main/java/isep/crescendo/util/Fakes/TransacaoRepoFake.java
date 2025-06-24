package isep.crescendo.util.Fakes;

import isep.crescendo.Repository.TransacaoRepo;
import isep.crescendo.model.Transacao;

import java.util.ArrayList;
import java.util.List;

public class TransacaoRepoFake extends TransacaoRepo {

    public final List<Transacao> transacoes = new ArrayList<>();

    @Override
    public void adicionar(Transacao t) {
        transacoes.add(t);
    }

    @Override
    public double somarQuantidadeExecutadaPorOrdemCompra(int ordemCompraId) {
        return transacoes.stream()
                .filter(t -> t.getOrdemCompraId() == ordemCompraId)
                .mapToDouble(Transacao::getQuantidade)
                .sum();
    }

    @Override
    public double somarValorExecutadoPorOrdemCompra(int ordemCompraId) {
        return transacoes.stream()
                .filter(t -> t.getOrdemCompraId() == ordemCompraId)
                .mapToDouble(t -> t.getQuantidade() * t.getValorUnitario())
                .sum();
    }
}

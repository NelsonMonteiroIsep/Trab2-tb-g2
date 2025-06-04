package isep.crescendo.util;

import isep.crescendo.Repository.Carteira;
import isep.crescendo.Repository.OrdemRepo;
import isep.crescendo.Repository.TransacaoRepo;
import isep.crescendo.model.Ordem;
import isep.crescendo.model.Transacao;
import java.time.LocalDateTime;

import java.util.List;

public class OrdemService {

    private final OrdemRepo ordemRepo;
    private final TransacaoRepo transacaoRepo;
    private final Carteira carteiraRepo;

    public OrdemService() {
        this.ordemRepo = new OrdemRepo();
        this.transacaoRepo = new TransacaoRepo();
        this.carteiraRepo = new Carteira();
    }

    public void processarOrdemCompra(Ordem ordemCompra) {
        double totalReserva = ordemCompra.getQuantidade() * ordemCompra.getValor();

        // 1. Verificar saldo e reservar
        if (!carteiraRepo.temSaldo(ordemCompra.getCarteiraId(), totalReserva)) return;
        carteiraRepo.removerSaldo(ordemCompra.getCarteiraId(), totalReserva);  // ✅ reservar

        int ordemId = ordemRepo.adicionar(ordemCompra);
        double restante = ordemCompra.getQuantidade();
        double valorExecutadoTotal = 0;

        List<Ordem> vendas = ordemRepo.buscarOrdensVendaCompativeis(ordemCompra.getIdMoeda(), ordemCompra.getValor());

        for (Ordem venda : vendas) {
            if (restante <= 0) break;

            double qtdVenda = venda.getQuantidade();
            double executada = Math.min(restante, qtdVenda);
            double custo = executada * venda.getValor();

            // 💰 Envia € para o vendedor
            carteiraRepo.adicionarSaldo(venda.getCarteiraId(), custo);

            // Registar transação
            transacaoRepo.adicionar(new Transacao(ordemId, venda.getId(), ordemCompra.getIdMoeda(), executada, venda.getValor()));

            restante -= executada;
            valorExecutadoTotal += custo;

            if (executada == qtdVenda) ordemRepo.marcarComoExecutada(venda.getId());
            else ordemRepo.atualizarQuantidade(venda.getId(), qtdVenda - executada);
        }

        if (restante > 0) ordemRepo.atualizarQuantidade(ordemId, restante);
        else ordemRepo.marcarComoExecutada(ordemId);

        // ✅ Devolver o que sobrou
        double valorNaoUsado = totalReserva - valorExecutadoTotal;
        if (valorNaoUsado > 0) {
            carteiraRepo.adicionarSaldo(ordemCompra.getCarteiraId(), valorNaoUsado);
        }
    }

    public void processarOrdemVenda(Ordem ordemVenda) {
        int carteiraId = ordemVenda.getCarteiraId();
        int idMoeda = ordemVenda.getIdMoeda();
        double quantidade = ordemVenda.getQuantidade();

        // 1. Obter saldo cripto disponível
        double saldoCripto = carteiraRepo.obterSaldoCripto(carteiraId, idMoeda);

        // 2. Somar ordens de venda pendentes
        double quantidadeEmOrdem = ordemRepo.somarOrdensPendentes(carteiraId, idMoeda, "venda");

        // 3. Validar se pode vender
        if (quantidade > (saldoCripto - quantidadeEmOrdem)) {
            System.out.println("Venda negada: quantidade excede saldo disponível após ordens pendentes.");
            return;
        }

        // 4. Adicionar ordem
        int ordemId = ordemRepo.adicionar(ordemVenda);
        double restante = quantidade;

        List<Ordem> compras = ordemRepo.buscarOrdensCompraCompativeis(idMoeda, ordemVenda.getValor());

        for (Ordem compra : compras) {
            if (restante <= 0) break;

            double qtdCompra = compra.getQuantidade();
            double executada = Math.min(restante, qtdCompra);
            double valor = executada * ordemVenda.getValor();

            carteiraRepo.adicionarSaldo(carteiraId, valor);

            transacaoRepo.adicionar(new Transacao(compra.getId(), ordemId, idMoeda, executada, ordemVenda.getValor()));

            restante -= executada;

            if (executada == qtdCompra) ordemRepo.marcarComoExecutada(compra.getId());
            else ordemRepo.atualizarQuantidade(compra.getId(), qtdCompra - executada);
        }

        if (restante > 0) ordemRepo.atualizarQuantidade(ordemId, restante);
        else ordemRepo.marcarComoExecutada(ordemId);
    }





    public void verificarOrdensExpiradas() {
        List<Ordem> ordensPendentes = ordemRepo.buscarOrdensPendentes(); // deve buscar com status = 'pendente'

        for (Ordem ordem : ordensPendentes) {
            if (!"compra".equalsIgnoreCase(ordem.getTipo())) continue;

            LocalDateTime agora = LocalDateTime.now();
            if (ordem.getDataHora().plusHours(24).isBefore(agora)) {
                double quantidadeRestante = ordem.getQuantidade();
                double valorADevolver = quantidadeRestante * ordem.getValor();

                carteiraRepo.adicionarSaldo(ordem.getCarteiraId(), valorADevolver);
                ordemRepo.marcarComoExpirada(ordem.getId());
            }
        }
    }

}

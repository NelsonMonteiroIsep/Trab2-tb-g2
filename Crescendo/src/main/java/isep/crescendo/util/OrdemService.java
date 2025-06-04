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
        double precoMaximo = ordemCompra.getValor();
        double totalReserva = ordemCompra.getQuantidade() * precoMaximo;
        System.out.println("Saldo reservado: " + totalReserva);
        // 1. Verificar saldo e reservar
        if (!carteiraRepo.temSaldo(ordemCompra.getCarteiraId(), totalReserva)) return;
        carteiraRepo.removerSaldo(ordemCompra.getCarteiraId(), totalReserva);  // ✅ reserva saldo máximo

        int ordemId = ordemRepo.adicionar(ordemCompra);
        double restante = ordemCompra.getQuantidade();
        double valorExecutadoTotal = 0;

        List<Ordem> vendas = ordemRepo.buscarOrdensVendaCompativeis(ordemCompra.getIdMoeda(), precoMaximo);

        for (Ordem venda : vendas) {
            if (restante <= 0) break;

            double qtdVenda = venda.getQuantidade();
            double executada = Math.min(restante, qtdVenda);

            double precoLimite = ordemCompra.getValor();
            double precoVenda = venda.getValor();
            double custoReal = executada * precoVenda;
            double custoReservado = executada * precoLimite;
            double diferenca = custoReservado - custoReal;

            // 💰 Transferência para o vendedor
            carteiraRepo.adicionarSaldo(venda.getCarteiraId(), custoReal);

            // 📄 Registar transação
            transacaoRepo.adicionar(new Transacao(
                    ordemId, venda.getId(), ordemCompra.getIdMoeda(),
                    executada, precoVenda
            ));

            // ✅ Devolver diferença ao comprador
            if (diferenca > 0) {
                carteiraRepo.adicionarSaldo(ordemCompra.getCarteiraId(), diferenca);
            }

            restante -= executada;
            valorExecutadoTotal += custoReal;

            if (executada == qtdVenda) ordemRepo.marcarComoExecutada(venda.getId());
            else ordemRepo.atualizarQuantidade(venda.getId(), qtdVenda - executada);
        }

        // 3. Atualizar a ordem
        if (restante > 0) {
            ordemRepo.atualizarQuantidade(ordemId, restante);
        } else {
            ordemRepo.marcarComoExecutada(ordemId);
        }

    }

    public void processarOrdemVenda(Ordem ordemVenda) {
        int carteiraId = ordemVenda.getCarteiraId();
        int idMoeda = ordemVenda.getIdMoeda();
        double quantidade = ordemVenda.getQuantidade();

        // 1. Verificar saldo da cripto (já reservado indiretamente)
        double saldoCripto = carteiraRepo.obterSaldoCripto(carteiraId, idMoeda);
        double quantidadeEmOrdem = ordemRepo.somarOrdensPendentes(carteiraId, idMoeda, "venda");

        if (quantidade > (saldoCripto - quantidadeEmOrdem)) {
            System.out.println("Venda negada: saldo cripto insuficiente após ordens pendentes.");
            return;
        }

        // 2. Inserir nova ordem
        int ordemVendaId = ordemRepo.adicionar(ordemVenda);
        double restanteVenda = quantidade;

        // 3. Buscar ordens de compra com preço >= pedido
        List<Ordem> compras = ordemRepo.buscarOrdensCompraCompativeis(idMoeda, ordemVenda.getValor());

        for (Ordem compra : compras) {
            if (restanteVenda <= 0) break;

            double qtdCompra = compra.getQuantidade();
            double executada = Math.min(restanteVenda, qtdCompra);

            double precoCompra = compra.getValor();
            double precoVenda = ordemVenda.getValor();

            double custoReal = executada * precoVenda;
            double reservadoCompra = executada * precoCompra;
            double diferenca = reservadoCompra - custoReal;

            // ✅ Transferir para vendedor
            carteiraRepo.adicionarSaldo(carteiraId, custoReal);

            // ✅ Devolver excesso ao comprador
            if (diferenca > 0) {
                carteiraRepo.adicionarSaldo(compra.getCarteiraId(), diferenca);
            }

            // 📄 Registar transação
            transacaoRepo.adicionar(new Transacao(
                    compra.getId(), ordemVendaId, idMoeda, executada, precoVenda
            ));

            // ✅ Atualizar ordens
            if (executada == qtdCompra) {
                ordemRepo.marcarComoExecutada(compra.getId());
            } else {
                ordemRepo.atualizarQuantidade(compra.getId(), qtdCompra - executada);
            }

            restanteVenda -= executada;
        }

        // ✅ Atualizar ou concluir ordem de venda
        if (restanteVenda <= 0) {
            ordemRepo.marcarComoExecutada(ordemVendaId);
        } else {
            ordemRepo.atualizarQuantidade(ordemVendaId, restanteVenda);
        }
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

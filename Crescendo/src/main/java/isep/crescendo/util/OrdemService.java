package isep.crescendo.util;

import isep.crescendo.Repository.CarteiraRepository;
import isep.crescendo.Repository.OrdemRepo;
import isep.crescendo.Repository.TransacaoRepo;
import isep.crescendo.model.Ordem;
import isep.crescendo.model.Transacao;
import isep.crescendo.util.Fakes.CarteiraRepositoryFake;

import java.time.LocalDateTime;

import java.util.List;

public class OrdemService {

    private final OrdemRepo ordemRepo;
    private final TransacaoRepo transacaoRepo;
    private final CarteiraRepository carteiraRepo;

    public OrdemService() {
        this.ordemRepo = new OrdemRepo();
        this.transacaoRepo = new TransacaoRepo();
        this.carteiraRepo = new CarteiraRepository();
    }

    public void processarOrdemCompra(Ordem ordemCompra) {
        double precoMaximo = ordemCompra.getValor();
        double totalReserva = ordemCompra.getQuantidade() * precoMaximo;
        System.out.println("Saldo reservado: " + totalReserva);
        // 1. Verificar saldo e reservar
        if (!carteiraRepo.temSaldo(ordemCompra.getCarteiraId(), totalReserva)) return;
        carteiraRepo.removerSaldo(ordemCompra.getCarteiraId(), totalReserva);  // ✅ reserva saldo máximo

        ordemCompra.setValorTotalReservado(totalReserva);
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

        if (restante > 0) {
                // 🔁 Atualizar quantidade e valor da ordem dinamicamente
                double totalExecutado = transacaoRepo.somarValorExecutadoPorOrdemCompra(ordemId);
                double totalReservado = ordemCompra.getValorTotalReservado();
                double novoValorUnitario = (totalReservado - totalExecutado) / restante;

                ordemRepo.atualizarQuantidade(ordemId, restante);
                ordemRepo.atualizarValor(ordemId, novoValorUnitario);

                System.out.printf("Ordem #%d atualizada: nova quantidade = %.6f, novo valor unitário = %.2f€\\n", ordemId, restante, novoValorUnitario);
        } else {
            ordemRepo.marcarComoExecutada(ordemId);

            // ✅ Calcular economia final e devolver ao utilizador
            double totalExecutado = transacaoRepo.somarValorExecutadoPorOrdemCompra(ordemId);
            double totalReservado = ordemCompra.getValorTotalReservado();
            double diferencaFinal = totalReservado - totalExecutado;

            if (diferencaFinal > 0) {
                carteiraRepo.adicionarSaldo(ordemCompra.getCarteiraId(), diferencaFinal);
                System.out.printf("Ordem #%d totalmente executada. Diferença devolvida: %.2f€\\n\"", ordemId, diferencaFinal);
            }
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

            // 📄 Registar transação
            transacaoRepo.adicionar(new Transacao(
                    compra.getId(), ordemVendaId, idMoeda, executada, precoVenda
            ));

            // ✅ Atualizar ordens
            if (executada == qtdCompra) {
                // Finaliza a ordem de compra
                ordemRepo.marcarComoExecutada(compra.getId());

                double totalExecutado = transacaoRepo.somarValorExecutadoPorOrdemCompra(compra.getId());
                double totalReservado = compra.getValorTotalReservado();
                double diferencaFinal = totalReservado - totalExecutado;

                if (diferencaFinal > 0) {
                    carteiraRepo.adicionarSaldo(compra.getCarteiraId(), diferencaFinal);
                    System.out.printf("Compra #%d finalizada. Diferença devolvida: %.2f€\n", compra.getId(), diferencaFinal);
                }

            } else {
                // Ordem de compra ainda pendente: atualizar quantidade e valor máximo por unidade
                double novaQuantidade = qtdCompra - executada;
                ordemRepo.atualizarQuantidade(compra.getId(), novaQuantidade);

                double totalExecutado = transacaoRepo.somarValorExecutadoPorOrdemCompra(compra.getId());
                double totalReservado = compra.getValorTotalReservado();
                double novoValorUnitario = (totalReservado - totalExecutado) / novaQuantidade;

                ordemRepo.atualizarValor(compra.getId(), novoValorUnitario);

                System.out.printf("Compra #%d atualizada: nova quantidade = %.6f, novo valor unitário = %.2f€\n",
                        compra.getId(), novaQuantidade, novoValorUnitario);
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
        List<Ordem> ordensPendentes = ordemRepo.buscarOrdensPendentes();

        for (Ordem ordem : ordensPendentes) {
            if (!"compra".equalsIgnoreCase(ordem.getTipo())) continue;

            LocalDateTime agora = LocalDateTime.now();
            if (ordem.getDataHora().plusHours(24).isBefore(agora)) {
                double quantidadeExecutada = transacaoRepo.somarQuantidadeExecutadaPorOrdemCompra(ordem.getId());
                double quantidadeRestante = ordem.getQuantidade() - quantidadeExecutada;

                if (quantidadeRestante > 0) {
                    double valorADevolver = quantidadeRestante * ordem.getValor();
                    carteiraRepo.adicionarSaldo(ordem.getCarteiraId(), valorADevolver);
                }

                ordemRepo.marcarComoExpirada(ordem.getId());
            }
        }
    }

    public void cancelarOrdem(Ordem ordem) {
        if ("compra".equalsIgnoreCase(ordem.getTipo())) {
            // Mesma lógica que já tens
            double quantidadeExecutada = transacaoRepo.somarQuantidadeExecutadaPorOrdemCompra(ordem.getId());
            double quantidadeRestante = ordem.getQuantidade() - quantidadeExecutada;

            if (quantidadeRestante > 0) {
                double valorADevolver = quantidadeRestante * ordem.getValor();
                carteiraRepo.adicionarSaldo(ordem.getCarteiraId(), valorADevolver);

                System.out.printf("Ordem de compra #%d cancelada. Devolvido %.2f€ ao utilizador.\n", ordem.getId(), valorADevolver);
            }

        } else if ("venda".equalsIgnoreCase(ordem.getTipo())) {


            System.out.printf("Ordem de venda #%d cancelada. Restante %.6f não será mais vendida.\n",
                    ordem.getId(), ordem.getQuantidade());


        }

        ordemRepo.marcarComoCancelada(ordem.getId());
    }

    public OrdemService(OrdemRepo ordemRepo, TransacaoRepo transacaoRepo, CarteiraRepositoryFake carteiraRepo) {
        this.ordemRepo = ordemRepo;
        this.transacaoRepo = transacaoRepo;
        this.carteiraRepo = carteiraRepo;
    }

}

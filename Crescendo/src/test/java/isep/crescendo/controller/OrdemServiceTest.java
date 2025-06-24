package isep.crescendo.controller;

import isep.crescendo.model.Ordem;
import isep.crescendo.model.Transacao;
import isep.crescendo.util.Fakes.CarteiraRepositoryFake;
import isep.crescendo.util.Fakes.OrdemRepoFake;
import isep.crescendo.util.Fakes.TransacaoRepoFake;
import isep.crescendo.util.OrdemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class OrdemServiceTest {

    CarteiraRepositoryFake carteiraRepo;
    OrdemRepoFake ordemRepo;
    TransacaoRepoFake transacaoRepo;
    OrdemService service;

    @BeforeEach
    void setup() {
        carteiraRepo = new CarteiraRepositoryFake();
        ordemRepo = new OrdemRepoFake();
        transacaoRepo = new TransacaoRepoFake();
        service = new OrdemService(ordemRepo, transacaoRepo, carteiraRepo);
    }

    @Test
    void deveExecutarCompraSimplesContraVenda() {
        int comprador = 1;
        int vendedor = 2;
        int idMoeda = 101;
        // Setup de saldos
        carteiraRepo.depositar(comprador, 2000); // saldo em euros
        carteiraRepo.setSaldoCripto(vendedor, idMoeda, 5); // saldo da cripto

        // Criar ordem de venda
        Ordem venda = new Ordem();
        venda.setCarteiraId(vendedor);
        venda.setIdMoeda(idMoeda);
        venda.setQuantidade(2);
        venda.setValor(500); // preço por unidade
        venda.setTipo("venda");
        venda.setStatus("pendente");
        venda.setDataHora(LocalDateTime.now().minusMinutes(2));
        venda.setValorTotalReservado(1000); // total

        service.processarOrdemVenda(venda);

        // Criar ordem de compra
        Ordem compra = new Ordem();
        compra.setCarteiraId(comprador);
        compra.setIdMoeda(idMoeda);
        compra.setQuantidade(2);
        compra.setValor(600); // comprador aceita pagar até 600/unidade
        compra.setTipo("compra");
        compra.setStatus("pendente");
        compra.setDataHora(LocalDateTime.now());

        service.processarOrdemCompra(compra);

        // Validação
        assertEquals(1, transacaoRepo.transacoes.size(), "Deveria haver 1 transação criada");

        Transacao transacao = transacaoRepo.transacoes.get(0);
        assertEquals(2, transacao.getQuantidade(), 0.001);
        assertEquals(500, transacao.getValorUnitario(), 0.001);

        double saldoCompradorFinal = carteiraRepo.getSaldo(comprador);
        double saldoVendedorFinal = carteiraRepo.getSaldo(vendedor);

        assertEquals(1200.0, carteiraRepo.getSaldo(comprador));
        assertEquals(1000.0, carteiraRepo.getSaldo(vendedor));
    }


    @Test
    void deveCancelarOrdemCompra() {
        int comprador = 1;
        int idMoeda = 101;
        carteiraRepo.depositar(comprador, 1000);

        Ordem compra = new Ordem();
        compra.setId(1); // define ID direto para simular já criada
        compra.setCarteiraId(comprador);
        compra.setIdMoeda(idMoeda);
        compra.setQuantidade(2);
        compra.setValor(500);
        compra.setTipo("compra");
        compra.setStatus("pendente");

        // Simula ordem pendente com saldo reservado
        double valorReservado = 2 * 500;
        compra.setValorTotalReservado(valorReservado);
        ordemRepo.adicionar(compra); // adiciona ordem no fake
        carteiraRepo.removerSaldo(comprador, valorReservado); // simula reserva

        service.cancelarOrdem(compra);

        assertEquals(1000.0, carteiraRepo.getSaldo(comprador), 0.01, "Saldo deveria ser devolvido após cancelamento");
        assertTrue(ordemRepo.canceladas.contains(1), "Ordem deveria estar marcada como cancelada");
    }


    @Test
    void deveMarcarOrdemComoExpiradaEDevolverSaldo() {
        int comprador = 1;
        int idMoeda = 101;
        carteiraRepo.depositar(comprador, 1000);

        Ordem compra = new Ordem();
        compra.setId(1);
        compra.setCarteiraId(comprador);
        compra.setIdMoeda(idMoeda);
        compra.setQuantidade(2);
        compra.setValor(500);
        compra.setTipo("compra");
        compra.setStatus("pendente");
        compra.setDataHora(LocalDateTime.now().minusHours(25)); // expirada
        compra.setValorTotalReservado(1000);

        ordemRepo.adicionar(compra);
        carteiraRepo.removerSaldo(comprador, 1000); // simula reserva

        service.verificarOrdensExpiradas();

        assertEquals(1000.0, carteiraRepo.getSaldo(comprador), 0.01, "Saldo deveria ter sido devolvido");
        assertTrue(ordemRepo.expiradas.contains(1), "Ordem deveria estar marcada como expirada");
    }


    @Test
    void deveExecutarCompraParcial() {
        int comprador = 1;
        int vendedor = 2;
        int idMoeda = 101;

        carteiraRepo.depositar(comprador, 3000);
        carteiraRepo.setSaldoCripto(vendedor, idMoeda, 2);

        Ordem venda = new Ordem();
        venda.setCarteiraId(vendedor);
        venda.setIdMoeda(idMoeda);
        venda.setQuantidade(2); // só 2 disponíveis
        venda.setValor(500); // preço por unidade
        venda.setTipo("venda");
        venda.setStatus("pendente");
        venda.setDataHora(LocalDateTime.now().minusMinutes(2));
        venda.setValorTotalReservado(1000);

        service.processarOrdemVenda(venda); // ⬅️ IMPORTANTE!

        Ordem compra = new Ordem();
        compra.setCarteiraId(comprador);
        compra.setIdMoeda(idMoeda);
        compra.setQuantidade(5); // quer comprar mais do que há
        compra.setValor(600); // aceita pagar mais
        compra.setTipo("compra");
        compra.setStatus("pendente");
        compra.setDataHora(LocalDateTime.now());

        service.processarOrdemCompra(compra);

        assertEquals(1, transacaoRepo.transacoes.size(), "Deveria ter 1 transação parcial");

        Transacao t = transacaoRepo.transacoes.get(0);
        assertEquals(2, t.getQuantidade(), 0.001); // apenas 2 executadas
        assertEquals(500, t.getValorUnitario(), 0.001);

        // O restante da ordem (3 unidades) deve estar pendente
        Ordem ordemAtualizada = ordemRepo.getUltimaOrdem();
        assertEquals(3, ordemAtualizada.getQuantidade(), 0.001);
        assertEquals("pendente", ordemAtualizada.getStatus());
    }

    @Test
    void deveExecutarVendaParcialParaMultiplasCompras() {
        int vendedor = 1;
        int comprador1 = 2;
        int comprador2 = 3;
        int idMoeda = 101;

        carteiraRepo.setSaldoCripto(vendedor, idMoeda, 5); // vendedor tem 5
        carteiraRepo.depositar(comprador1, 1000);
        carteiraRepo.depositar(comprador2, 2000);

        // Ordem de compra 1 (2 unidades a 500€)
        Ordem compra1 = new Ordem();
        compra1.setCarteiraId(comprador1);
        compra1.setIdMoeda(idMoeda);
        compra1.setQuantidade(2);
        compra1.setValor(500);
        compra1.setTipo("compra");
        compra1.setStatus("pendente");
        compra1.setDataHora(LocalDateTime.now().minusMinutes(5));

        // Ordem de compra 2 (3 unidades a 600€)
        Ordem compra2 = new Ordem();
        compra2.setCarteiraId(comprador2);
        compra2.setIdMoeda(idMoeda);
        compra2.setQuantidade(3);
        compra2.setValor(600);
        compra2.setTipo("compra");
        compra2.setStatus("pendente");
        compra2.setDataHora(LocalDateTime.now().minusMinutes(2));

        service.processarOrdemCompra(compra1);
        service.processarOrdemCompra(compra2);

        // Ordem de venda (5 unidades a 500€)
        Ordem venda = new Ordem();
        venda.setCarteiraId(vendedor);
        venda.setIdMoeda(idMoeda);
        venda.setQuantidade(5);
        venda.setValor(500); // preço fixo do vendedor
        venda.setTipo("venda");
        venda.setStatus("pendente");
        venda.setDataHora(LocalDateTime.now());

        service.processarOrdemVenda(venda);

        assertEquals(2, transacaoRepo.transacoes.size(), "Devem haver 2 transações (para 2 compradores)");
        double totalVendido = transacaoRepo.transacoes.stream().mapToDouble(Transacao::getQuantidade).sum();
        assertEquals(5, totalVendido, 0.001);
    }


    @Test
    void deveManterCompraPendenteSeNaoHouverVendas() {
        int comprador = 1;
        int idMoeda = 101;
        carteiraRepo.depositar(comprador, 1000);

        Ordem compra = new Ordem();
        compra.setCarteiraId(comprador);
        compra.setIdMoeda(idMoeda);
        compra.setQuantidade(2);
        compra.setValor(500);
        compra.setTipo("compra");
        compra.setStatus("pendente");
        compra.setDataHora(LocalDateTime.now());

        service.processarOrdemCompra(compra);

        assertEquals(0, transacaoRepo.transacoes.size(), "Nenhuma transação deveria ter ocorrido");
        Ordem ordemPendente = ordemRepo.getUltimaOrdem();
        assertEquals("pendente", ordemPendente.getStatus(), "Ordem deveria continuar pendente");
        assertEquals(2, ordemPendente.getQuantidade(), 0.001);
    }
}

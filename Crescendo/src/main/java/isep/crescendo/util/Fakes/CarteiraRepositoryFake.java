package isep.crescendo.util.Fakes;

import isep.crescendo.Repository.CarteiraRepository;

import java.util.HashMap;
import java.util.Map;

public class CarteiraRepositoryFake extends CarteiraRepository {
    private Map<Integer, Double> saldoEuros = new HashMap<>();
    private Map<String, Double> saldoCripto = new HashMap<>(); // key = carteiraId-moedaId

    public void depositar(int carteiraId, double valor) {
        saldoEuros.put(carteiraId, getSaldo(carteiraId) + valor);
    }

    public void removerSaldo(int carteiraId, double valor) {
        saldoEuros.put(carteiraId, getSaldo(carteiraId) - valor);
    }

    public void adicionarSaldo(int carteiraId, double valor) {
        depositar(carteiraId, valor);
    }

    public boolean temSaldo(int carteiraId, double valor) {
        return getSaldo(carteiraId) >= valor;
    }

    public double getSaldo(int carteiraId) {
        return saldoEuros.getOrDefault(carteiraId, 0.0);
    }

    public void setSaldoCripto(int carteiraId, int idMoeda, double valor) {
        saldoCripto.put(carteiraId + "-" + idMoeda, valor);
    }

    public double obterSaldoCripto(int carteiraId, int idMoeda) {
        return saldoCripto.getOrDefault(carteiraId + "-" + idMoeda, 0.0);
    }
}

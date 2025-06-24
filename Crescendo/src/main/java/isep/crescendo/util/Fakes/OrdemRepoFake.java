package isep.crescendo.util.Fakes;

import isep.crescendo.model.Ordem;
import isep.crescendo.Repository.OrdemRepo;

import java.util.*;
import java.util.stream.Collectors;

public class OrdemRepoFake extends OrdemRepo {
    public Map<Integer, Ordem> ordens = new HashMap<>();
    public List<Ordem> listaOrdens = new ArrayList<>();
    public Set<Integer> canceladas = new HashSet<>();
    public Set<Integer> expiradas = new HashSet<>();
    private int idCounter = 1;

    @Override
    public int adicionar(Ordem ordem) {
        if (ordem.getId() == 0) {
            ordem.setId(idCounter++);
        }
        ordens.put(ordem.getId(), ordem);
        listaOrdens.add(ordem);
        return ordem.getId();
    }

    @Override
    public List<Ordem> buscarOrdensVendaCompativeis(int idMoeda, double precoMaximo) {
        return listaOrdens.stream()
                .filter(o -> o.getTipo().equals("venda") &&
                        o.getIdMoeda() == idMoeda &&
                        o.getValor() <= precoMaximo &&
                        o.getStatus().equals("pendente"))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ordem> buscarOrdensCompraCompativeis(int idMoeda, double precoMinimo) {
        return listaOrdens.stream()
                .filter(o -> o.getTipo().equals("compra") &&
                        o.getIdMoeda() == idMoeda &&
                        o.getValor() >= precoMinimo &&
                        o.getStatus().equals("pendente"))
                .collect(Collectors.toList());
    }

    @Override
    public void marcarComoExecutada(int idOrdem) {
        if (ordens.containsKey(idOrdem)) {
            ordens.get(idOrdem).setStatus("executada");
        }
    }

    @Override
    public void atualizarQuantidade(int idOrdem, double novaQtd) {
        if (ordens.containsKey(idOrdem)) {
            ordens.get(idOrdem).setQuantidade(novaQtd);
        }
    }

    @Override
    public void atualizarValor(int idOrdem, double novoValor) {
        if (ordens.containsKey(idOrdem)) {
            ordens.get(idOrdem).setValor(novoValor);
        }
    }

    @Override
    public List<Ordem> buscarOrdensPendentes() {
        return listaOrdens.stream()
                .filter(o -> o.getStatus().equals("pendente"))
                .collect(Collectors.toList());
    }

    @Override
    public void marcarComoCancelada(int idOrdem) {
        canceladas.add(idOrdem);
        if (ordens.containsKey(idOrdem)) {
            ordens.get(idOrdem).setStatus("cancelada");
        }
    }

    @Override
    public void marcarComoExpirada(int idOrdem) {
        expiradas.add(idOrdem);
        if (ordens.containsKey(idOrdem)) {
            ordens.get(idOrdem).setStatus("expirada");
        }
    }

    @Override
    public double somarOrdensPendentes(int carteiraId, int idMoeda, String tipo) {
        return listaOrdens.stream()
                .filter(o -> o.getCarteiraId() == carteiraId &&
                        o.getIdMoeda() == idMoeda &&
                        o.getTipo().equals(tipo) &&
                        o.getStatus().equals("pendente"))
                .mapToDouble(Ordem::getQuantidade)
                .sum();
    }

    public Ordem getUltimaOrdem() {
        return listaOrdens.get(listaOrdens.size() - 1);
    }
}
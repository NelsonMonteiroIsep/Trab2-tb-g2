// src/main/java/isep/crescendo/controller/CriptoAlgoritmo.java
package isep.crescendo.controller;

import isep.crescendo.Repository.HistoricoValorRepository;
import isep.crescendo.controller.CyclePhase;
import isep.crescendo.model.HistoricoValor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CriptoAlgoritmo {

    private int criptoIdParaSimulacao;

    private Timeline simulationTimeline;

    private int currentMinute = 0;
    private static final int SIMULATION_INTERVAL_MS = 60; // 1 segundo real por "minuto" simulado

    private CyclePhase currentPhase = CyclePhase.ADOCAO;
    private int minutesInCurrentPhase = 0;
    private int phaseDuration = 0;

    private double currentPrice;
    private double currentVolume;

    private static final double PRICE_NOISE_FACTOR = 0.02;
    private static final double VOLUME_NOISE_FACTOR = 0.05;
    private static final Random random = new Random();

    private HistoricoValorRepository historicoValorRepo;

    private LocalDateTime ultimaHoraSalva = null;

    private List<HistoricoValor> historicoEmMemoria;
    private static final int MAX_HISTORICO_EM_MEMORIA = 3600; // Manter a última 1 hora de dados (3600 segundos/minutos)

    // Construtor principal que aceita um valor inicial de preço
    public CriptoAlgoritmo(int criptoId, Double initialPrice) { // Use Double para permitir 'null'
        this.criptoIdParaSimulacao = criptoId;
        this.historicoValorRepo = new HistoricoValorRepository();
        this.historicoEmMemoria = new ArrayList<>();

        if (initialPrice != null) {
            this.currentPrice = initialPrice;
            System.out.println("CriptoAlgoritmo: Inicializando Cripto ID " + criptoId + " com valor passado: " + String.format("%.2f", this.currentPrice));
        } else {
            // Valor padrão se nenhum for passado
            this.currentPrice = 100.0;
            System.out.println("CriptoAlgoritmo: Nenhum valor inicial passado para Cripto ID " + criptoId + ". Inicializando com valor padrão: " + String.format("%.2f", this.currentPrice));
        }

        this.currentVolume = 500.0; // Valor padrão inicial para volume

        System.out.println("CriptoAlgoritmo: Simulação para Cripto ID " + criptoId + " inicializada.");
        setPhaseDuration(currentPhase);
    }

    // Construtor de conveniência sem valor inicial (chama o principal com null)
    public CriptoAlgoritmo(int criptoId) {
        this(criptoId, null);
    }

    // Define a duração de cada fase (em "minutos" simulados), com alguma aleatoriedade
    private void setPhaseDuration(CyclePhase phase) {
        switch (phase) {
            case ADOCAO:
                phaseDuration = 60 + random.nextInt(25);
                break;
            case EUFORIA:
                phaseDuration = 30 + random.nextInt(10);
                break;
            case DISTRIBUICAO:
                phaseDuration = 20 + random.nextInt(10);
                break;
            case PANICO:
                phaseDuration = 25 + random.nextInt(10);
                break;
            case CAPITULACAO:
                phaseDuration = 40 + random.nextInt(15);
                break;
            case ACUMULACAO:
                phaseDuration = 60 + random.nextInt(25);
                break;
            default:
                phaseDuration = 20;
                break;
        }
    }

    // Inicia a simulação
    public void startSimulation() {
        if (simulationTimeline != null) {
            simulationTimeline.stop();
        }
        simulationTimeline = new Timeline(new KeyFrame(Duration.millis(SIMULATION_INTERVAL_MS), event -> {
            updateSimulationAndStore();
        }));
        simulationTimeline.setCycleCount(Timeline.INDEFINITE);
        simulationTimeline.play();

        ultimaHoraSalva = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);

        System.out.println("CriptoAlgoritmo para Cripto ID " + criptoIdParaSimulacao + " iniciada. Gravando dados em memória e na DB (hora cheia)...");
    }

    // Para a simulação
    public void stopSimulation() {
        if (simulationTimeline != null) {
            simulationTimeline.stop();
            System.out.println("CriptoAlgoritmo para Cripto ID " + criptoIdParaSimulacao + " parada. Total de " + currentMinute + " passos gerados.");
        }
    }

    // Retorna o histórico de valores armazenado em memória (somente leitura)
    public List<HistoricoValor> getHistoricoEmMemoria() {
        return Collections.unmodifiableList(new ArrayList<>(historicoEmMemoria));
    }

    // Atualiza a simulação e armazena os dados
    private void updateSimulationAndStore() {
        minutesInCurrentPhase++;
        if (minutesInCurrentPhase >= phaseDuration) {
            transitionToNextPhase();
        }

        generateMarketDataForCurrentPhase();

        LocalDateTime agora = LocalDateTime.now();
        double valorGerado = currentPrice;
        // double volumeGerado = currentVolume; // Comentado/Removido, pois HistoricoValor não tem campo volume

        HistoricoValor novoRegistro = new HistoricoValor();
        novoRegistro.setCriptoId(criptoIdParaSimulacao);
        novoRegistro.setData(agora);
        novoRegistro.setValor(valorGerado);

        historicoEmMemoria.add(novoRegistro);

        if (historicoEmMemoria.size() > MAX_HISTORICO_EM_MEMORIA) {
            historicoEmMemoria.remove(0);
        }

        LocalDateTime horaAtualTruncada = agora.truncatedTo(ChronoUnit.HOURS);

        if (ultimaHoraSalva == null || horaAtualTruncada.isAfter(ultimaHoraSalva)) {
            try {
                historicoValorRepo.adicionarValor(criptoIdParaSimulacao, agora, valorGerado);
                ultimaHoraSalva = horaAtualTruncada;
                System.out.println("Cripto ID " + criptoIdParaSimulacao + ": VALOR DA HORA CHEIA SALVO NA DB: " + String.format("%.2f", valorGerado) + " às " + agora + " (Fase: " + currentPhase.getDisplayName() + ")");
            } catch (RuntimeException e) {
                System.err.println("Erro ao salvar valor histórico na DB para Cripto ID " + criptoIdParaSimulacao + ": " + e.getMessage());
            }
        } else {
            // System.out.println("Cripto ID " + criptoIdParaSimulacao + ": Gerado " + String.format("%.2f", valorGerado) + " (Armazenado em memória)");
        }

        currentMinute++;
    }

    // Transita para a próxima fase do ciclo de mercado
    private void transitionToNextPhase() {
        switch (currentPhase) {
            case ADOCAO:
                currentPhase = CyclePhase.EUFORIA;
                break;
            case EUFORIA:
                currentPhase = CyclePhase.DISTRIBUICAO;
                break;
            case DISTRIBUICAO:
                currentPhase = CyclePhase.PANICO;
                break;
            case PANICO:
                currentPhase = CyclePhase.CAPITULACAO;
                break;
            case ACUMULACAO:
                currentPhase = CyclePhase.ADOCAO; // Volta ao início do ciclo
                break;
            default:
                currentPhase = CyclePhase.ADOCAO;
                break;
        }
        minutesInCurrentPhase = 0;
        setPhaseDuration(currentPhase);
        System.out.println("Cripto ID " + criptoIdParaSimulacao + ": Transição para a fase: " + currentPhase.getDisplayName());
    }

    // Gera o preço e volume com base na fase atual e ruído
    private void generateMarketDataForCurrentPhase() {
        double priceChangeFactor = 0.0;
        double volumeChangeFactor = 0.0;

        double randomNoisePrice = (random.nextDouble() * 2 - 1) * PRICE_NOISE_FACTOR;
        double randomNoiseVolume = (random.nextDouble() * 2 - 1) * VOLUME_NOISE_FACTOR;

        switch (currentPhase) {
            case ADOCAO:
                priceChangeFactor = 0.005 + randomNoisePrice;
                volumeChangeFactor = 0.01 + randomNoiseVolume;
                break;
            case EUFORIA:
                priceChangeFactor = 0.02 + randomNoisePrice;
                volumeChangeFactor = 0.03 + randomNoiseVolume;
                break;
            case DISTRIBUICAO:
                priceChangeFactor = -0.002 + randomNoisePrice;
                volumeChangeFactor = -0.01 + randomNoiseVolume;
                break;
            case PANICO:
                priceChangeFactor = -0.03 + randomNoisePrice;
                volumeChangeFactor = 0.04 + randomNoiseVolume;
                break;
            case CAPITULACAO:
                priceChangeFactor = -0.01 + randomNoisePrice;
                volumeChangeFactor = -0.02 + randomNoiseVolume;
                break;
            case ACUMULACAO:
                priceChangeFactor = 0.001 + randomNoisePrice;
                volumeChangeFactor = -0.005 + randomNoiseVolume;
                break;
        }

        currentPrice += currentPrice * priceChangeFactor;
        if (currentPrice < 0.01) currentPrice = 0.01;

        currentVolume += currentVolume * volumeChangeFactor * 100;
        if (currentVolume < 10.0) currentVolume = 10.0;

        // Adiciona eventos aleatórios de "flash crash" ou "pump" (1% de chance)
        if (random.nextDouble() < 0.01) {
            currentPrice *= (1 + (random.nextDouble() * 0.1 - 0.05));
            currentVolume *= (1 + (random.nextDouble() * 0.2 - 0.1));
        }
    }
}
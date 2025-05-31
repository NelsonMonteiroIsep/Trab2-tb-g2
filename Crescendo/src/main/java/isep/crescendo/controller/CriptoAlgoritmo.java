// src/main/java/isep/crescendo/controller/CriptoAlgoritmo.java
package isep.crescendo.controller;

import isep.crescendo.model.HistoricoValorRepository; // Ajuste para o nome correto
import isep.crescendo.controller.CyclePhase;
import isep.crescendo.model.HistoricoValor; // Importar o modelo correto

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Manter o nome original da classe
public class CriptoAlgoritmo {

    private int criptoIdParaSimulacao;

    private Timeline simulationTimeline;

    private int currentMinute = 0;
    private static final int SIMULATION_INTERVAL_MS = 1000; // 1 segundo real

    private CyclePhase currentPhase = CyclePhase.ADOCAO;
    private int minutesInCurrentPhase = 0;
    private int phaseDuration = 0;
    private double currentPrice = 100.0;
    private double currentVolume = 500.0;

    private static final double PRICE_NOISE_FACTOR = 0.02;
    private static final double VOLUME_NOISE_FACTOR = 0.05;

    private HistoricoValorRepository historicoValorRepo; // Seu repositório de histórico de valores

    private LocalDateTime ultimaHoraSalva = null; // Para controlar a gravação na DB

    private List<HistoricoValor> historicoEmMemoria; // Usa o modelo HistoricoValor
    private static final int MAX_HISTORICO_EM_MEMORIA = 3600; // Manter o último 1 hora de dados (3600 segundos)

    public CriptoAlgoritmo(int criptoId) { // Construtor mantém o nome da classe
        this.criptoIdParaSimulacao = criptoId;
        this.historicoValorRepo = new HistoricoValorRepository(); // Instancia seu repositório
        this.historicoEmMemoria = new ArrayList<>();
        System.out.println("CriptoAlgoritmo: Simulação para Cripto ID " + criptoId + " inicializada."); // Mensagem ajustada

        setPhaseDuration(currentPhase);
    }

    private void setPhaseDuration(CyclePhase phase) {
        switch (phase) {
            case ADOCAO:
                phaseDuration = 50;
                break;
            case EUFORIA:
                phaseDuration = 50;
                break;
            case DISTRIBUICAO:
                phaseDuration = 20;
                break;
            case PANICO:
                phaseDuration = 25;
                break;
            case CAPITULACAO:
                phaseDuration = 40;
                break;
            case ACUMULACAO:
                phaseDuration = 60;
                break;
            default:
                phaseDuration = 20;
        }
    }

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

        System.out.println("CriptoAlgoritmo para Cripto ID " + criptoIdParaSimulacao + " iniciada. Gravando dados em memória e na DB (hora cheia)..."); // Mensagem ajustada
    }

    public void stopSimulation() {
        if (simulationTimeline != null) {
            simulationTimeline.stop();
            System.out.println("CriptoAlgoritmo para Cripto ID " + criptoIdParaSimulacao + " parada. Total de " + currentMinute + " passos gerados."); // Mensagem ajustada
        }
    }

    public List<HistoricoValor> getHistoricoEmMemoria() {
        return Collections.unmodifiableList(new ArrayList<>(historicoEmMemoria));
    }

    private void updateSimulationAndStore() {
        minutesInCurrentPhase++;
        if (minutesInCurrentPhase >= phaseDuration) {
            transitionToNextPhase();
        }

        generateMarketDataForCurrentPhase();

        LocalDateTime agora = LocalDateTime.now();
        double valorGerado = currentPrice;

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
            System.out.println("Cripto ID " + criptoIdParaSimulacao + ": Gerado " + String.format("%.2f", valorGerado) + " (Armazenado em memória)");
        }

        currentMinute++;
    }

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
            case CAPITULACAO:
                currentPhase = CyclePhase.ACUMULACAO;
                break;
            case ACUMULACAO:
                currentPhase = CyclePhase.ADOCAO;
                break;
            default:
                currentPhase = CyclePhase.ADOCAO;
                break;
        }
        minutesInCurrentPhase = 0;
        setPhaseDuration(currentPhase);
        System.out.println("Cripto ID " + criptoIdParaSimulacao + ": Transição para a fase: " + currentPhase.getDisplayName());
    }

    private void generateMarketDataForCurrentPhase() {
        double priceChangeFactor = 0.0;
        double volumeChangeFactor = 0.0;
        double randomNoisePrice = (Math.random() * 2 - 1) * PRICE_NOISE_FACTOR;
        double randomNoiseVolume = (Math.random() * 2 - 1) * VOLUME_NOISE_FACTOR;

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
        if (currentPrice < 1.0) currentPrice = 1.0;
        currentVolume += currentVolume * volumeChangeFactor * 100;
        if (currentVolume < 50.0) currentVolume = 50.0;

        if (Math.random() < 0.01) {
            currentPrice *= (1 + (Math.random() * 0.1 - 0.05));
            currentVolume *= (1 + (Math.random() * 0.2 - 0.1));
        }
    }
}
package isep.crescendo.controller;

import isep.crescendo.Repository.HistoricoValorRepository;
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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;


public class CriptoAlgoritmo {

    private int criptoIdParaSimulacao;

    private Timeline simulationTimeline;

    private int currentMinute = 0;
    private static final int SIMULATION_INTERVAL_MS = 1000; // Frequência de atualização em ms

    private CyclePhase currentPhase = CyclePhase.ADOCAO;
    private int minutesInCurrentPhase = 0;
    private int phaseDuration = 0;

    private DoubleProperty currentPriceProperty = new SimpleDoubleProperty();
    private double currentVolume;

    private static final double PRICE_NOISE_FACTOR = 0.02;
    private static final double VOLUME_NOISE_FACTOR = 0.05;
    private static final Random random = new Random();

    private HistoricoValorRepository historicoValorRepo;

    private LocalDateTime ultimaHoraSalva = null;

    private List<HistoricoValor> historicoEmMemoria;
    public static final int MAX_HISTORICO_EM_MEMORIA = 3600; // 60 minutos * 60 horas = 3600 minutos
    public int getCriptoIdParaSimulacao() {
        return criptoIdParaSimulacao;
    }
    // Construtor principal que aceita um valor inicial de preço
    public CriptoAlgoritmo(int criptoId, Double initialPrice) {
        this.criptoIdParaSimulacao = criptoId;
        this.historicoValorRepo = new HistoricoValorRepository();
        this.historicoEmMemoria = new ArrayList<>();

        if (initialPrice != null) {
            this.currentPriceProperty.set(initialPrice);
            System.out.println("CriptoAlgoritmo: Inicializando Cripto ID " + criptoId + " com valor passado: " + String.format("%.2f", this.currentPriceProperty.get()));
        } else {
            this.currentPriceProperty.set(100.0); // Valor padrão se nenhum for passado
            System.out.println("CriptoAlgoritmo: Nenhum valor inicial passado para Cripto ID " + criptoId + ". Inicializando com valor padrão: " + String.format("%.2f", this.currentPriceProperty.get()));
        }

        this.currentVolume = 500.0;
        setPhaseDuration(currentPhase); // Define a duração da fase inicial
        System.out.println("CriptoAlgoritmo: Simulação para Cripto ID " + criptoId + " inicializada.");
    }

    // Construtor de conveniência sem valor inicial (usará o valor padrão 100.0)
    public CriptoAlgoritmo(int criptoId) {
        this(criptoId, null);
    }

    private void setPhaseDuration(CyclePhase phase) {
        // As durações das fases devem ser mais realistas ou configuráveis
        switch (phase) {
            case ADOCAO:
                phaseDuration = 60 + random.nextInt(25); // 60 a 84 minutos
                break;
            case EUFORIA:
                phaseDuration = 30 + random.nextInt(10); // 30 a 39 minutos
                break;
            case DISTRIBUICAO:
                phaseDuration = 20 + random.nextInt(10); // 20 a 29 minutos
                break;
            case PANICO:
                phaseDuration = 25 + random.nextInt(10); // 25 a 34 minutos
                break;
            case CAPITULACAO:
                phaseDuration = 40 + random.nextInt(15); // 40 a 54 minutos
                break;
            case ACUMULACAO:
                phaseDuration = 60 + random.nextInt(25); // 60 a 84 minutos
                break;
            default:
                currentPhase = CyclePhase.ADOCAO;
                phaseDuration = 20; // Default seguro
                break;
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

        // Inicializa ultimaHoraSalva para garantir que a primeira hora cheia seja salva
        ultimaHoraSalva = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        System.out.println("CriptoAlgoritmo para Cripto ID " + criptoIdParaSimulacao + " iniciada. Gravando dados em memória e na DB (hora cheia)...");
    }

    public void stopSimulation() {
        if (simulationTimeline != null) {
            simulationTimeline.stop();
            System.out.println("CriptoAlgoritmo para Cripto ID " + criptoIdParaSimulacao + " parada. Total de " + currentMinute + " passos gerados.");
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
        double valorGerado = currentPriceProperty.get();

        // Armazena em memória (útil para gráficos de curto prazo)
        HistoricoValor novoRegistro = new HistoricoValor();
        novoRegistro.setCriptoId(criptoIdParaSimulacao);
        novoRegistro.setData(agora);
        novoRegistro.setValor(valorGerado);
        historicoEmMemoria.add(novoRegistro);
        if (historicoEmMemoria.size() > MAX_HISTORICO_EM_MEMORIA) {
            historicoEmMemoria.remove(0); // Mantém o tamanho do histórico em memória
        }

        // Salva na base de dados apenas nas horas cheias
        LocalDateTime horaAtualTruncada = agora.truncatedTo(ChronoUnit.HOURS);

        if (ultimaHoraSalva == null || horaAtualTruncada.isAfter(ultimaHoraSalva)) {
            try {
                historicoValorRepo.adicionarValor(criptoIdParaSimulacao, agora, valorGerado);
                ultimaHoraSalva = horaAtualTruncada;
                System.out.println("Cripto ID " + criptoIdParaSimulacao + ": VALOR DA HORA CHEIA SALVO NA DB: " + String.format("%.2f", valorGerado) + " às " + agora + " (Fase: " + currentPhase.getDisplayName() + ")");
            } catch (RuntimeException e) {
                System.err.println("Erro ao salvar valor histórico na DB para Cripto ID " + criptoIdParaSimulacao + ": " + e.getMessage());
            }
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
                currentPhase = CyclePhase.ADOCAO; // Fallback
                break;
        }
        minutesInCurrentPhase = 0;
        setPhaseDuration(currentPhase);
        System.out.println("Cripto ID " + criptoIdParaSimulacao + ": Transição para a fase: " + currentPhase.getDisplayName());
    }

    private void generateMarketDataForCurrentPhase() {
        double priceChangeFactor = 0.0;
        double volumeChangeFactor = 0.0;

        double randomNoisePrice = (random.nextDouble() * 2 - 1) * PRICE_NOISE_FACTOR;
        double randomNoiseVolume = (random.nextDouble() * 2 - 1) * VOLUME_NOISE_FACTOR;

        switch (currentPhase) {
            case ADOCAO:
                priceChangeFactor = 0.005 + randomNoisePrice; // Leve aumento de preço
                volumeChangeFactor = 0.01 + randomNoiseVolume; // Aumento de volume
                break;
            case EUFORIA:
                priceChangeFactor = 0.02 + randomNoisePrice; // Aumento rápido de preço
                volumeChangeFactor = 0.03 + randomNoiseVolume; // Alto volume
                break;
            case DISTRIBUICAO:
                priceChangeFactor = -0.002 + randomNoisePrice; // Preço estagna ou ligeiramente cai
                volumeChangeFactor = -0.01 + randomNoiseVolume; // Volume diminui
                break;
            case PANICO:
                priceChangeFactor = -0.03 + randomNoisePrice; // Queda acentuada
                volumeChangeFactor = 0.04 + randomNoiseVolume; // Volume alto por vendas
                break;
            case CAPITULACAO:
                priceChangeFactor = -0.01 + randomNoisePrice; // Queda contínua, mas mais lenta
                volumeChangeFactor = -0.02 + randomNoiseVolume; // Volume baixo
                break;
            case ACUMULACAO:
                priceChangeFactor = 0.001 + randomNoisePrice; // Preço lateraliza ou sobe levemente
                volumeChangeFactor = -0.005 + randomNoiseVolume; // Volume baixo
                break;
        }

        double newPrice = currentPriceProperty.get() + currentPriceProperty.get() * priceChangeFactor;
        if (newPrice < 0.01) newPrice = 0.01; // Preço mínimo
        this.currentPriceProperty.set(newPrice);

        currentVolume += currentVolume * volumeChangeFactor * 100; // Ajuste para o volume ser mais significativo
        if (currentVolume < 10.0) currentVolume = 10.0; // Volume mínimo

        // Eventos de "Flash Crash/Pump"
        if (random.nextDouble() < 0.01) { // 1% de chance de um evento súbito
            double flashEventFactor = (random.nextDouble() * 0.1 - 0.05); // +/- 5%
            double flashEventPrice = currentPriceProperty.get() * (1 + flashEventFactor);
            this.currentPriceProperty.set(flashEventPrice);
            currentVolume *= (1 + (random.nextDouble() * 0.2 - 0.1)); // Volume também reage
        }
    }

    public DoubleProperty currentPriceProperty() {
        return currentPriceProperty;
    }

    public double getCurrentPrice() {
        return currentPriceProperty.get();
    }

    public void setCurrentPrice(double newPrice) {
        this.currentPriceProperty.set(newPrice);
    }
}
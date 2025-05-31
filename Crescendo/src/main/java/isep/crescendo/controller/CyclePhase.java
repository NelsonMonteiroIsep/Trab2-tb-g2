// src/main/java/isep/crescendo/simulation/CyclePhase.java
package isep.crescendo.controller; // Certifique-se de que o pacote está correto

public enum CyclePhase {
    ADOCAO("Adoção"),
    EUFORIA("Euforia"),
    DISTRIBUICAO("Distribuição"),
    PANICO("Pânico"),
    CAPITULACAO("Capitulação"),
    ACUMULACAO("Acumulação");

    private final String displayName;

    CyclePhase(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
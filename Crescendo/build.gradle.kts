plugins {
    id("java")
    id("org.openjfx.javafxplugin") version "0.1.0" // Mantenha a versão que você usa
    id("org.jetbrains.kotlin.jvm") version "2.1.21" // Versão do plugin Kotlin
    id("application")
}

group = "isep.crescendo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Configuração do JavaFX - Mantenha a sua versão original se não quiser atualizar
// Se o seu JDK for mais recente (ex: JDK 21, 22), pode precisar de JavaFX 21.0.x ou 22.0.x
javafx {
    version = "22.0.1" // Verifique se esta é a versão que você estava usando ou ajuste para a compatível com seu JDK
    modules("javafx.controls", "javafx.fxml", "javafx.web", "javafx.media", "javafx.swing", "javafx.graphics")
}

dependencies {
    // === Dependências Originais (com JNA e Jakarta Mail corrigidos) ===

    // ControlsFX (Versão Original)
    implementation("org.controlsfx:controlsfx:11.2.1")

    // FormsFX (Versão Original)
    implementation("com.dlsc.formsfx:formsfx-core:11.6.0") {
        exclude(group = "org.openjfx")
    }

    // ValidatorFX (Versão Original)
    implementation("net.synedra:validatorfx:0.5.0") {
        exclude(group = "org.openjfx")
    }

    // Ikonli (Versão Original)
    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")

    // BootstrapFX (Versão Original)
    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")

    // TilesFX (Versão Original)
    implementation("eu.hansolo:tilesfx:21.0.3") {
        exclude(group = "org.openjfx")
    }

    // FXGL (Versão Original)
    implementation("com.github.almasb:fxgl:17.3") {
        exclude(group = "org.openjfx")
        exclude(group = "org.jetbrains.kotlin")
    }

    // JNA (ATUALIZADO PARA A VERSÃO 5.17.0 e sintaxe correta)
    implementation("net.java.dev.jna:jna:5.17.0")
    implementation("net.java.dev.jna:jna-platform:5.17.0")

    // MySQL Connector/J (Versão Original)
    implementation("mysql:mysql-connector-java:8.0.33")

    // Kotlin Stdlib (Versão Original)
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.21") // Manter a versão compatível com o plugin Kotlin

    // Jakarta Mail (CORRIGIDO PARA A VERSÃO 2.1.2 E GROUPID/ARTIFACTID CORRETOS)
    implementation("jakarta.mail:jakarta.mail-api:2.1.2")
    implementation("org.eclipse.angus:angus-mail:2.0.2") // Implementação compatível com Jakarta Mail API 2.1.2

    // Jackson Core (Se você usa, manter a original ou remover se não for usada)
    // Se não tinha esta no seu original, pode remover. Se tinha, reponha a sua versão.
    // implementation("com.fasterxml.jackson.core:jackson-core:X.Y.Z")

    // === Dependências de Teste ===
    // Assumindo que junitVersion está definido no seu build.gradle.kts ou que é uma versão fixa
    // testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    // testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0") // Exemplo de versão JUnit
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0") // Exemplo de versão JUnit
}

application {
    mainModule.set("isep.crescendo")
    mainClass.set("isep.crescendo.Main")
}

tasks.test {
    useJUnitPlatform()
}
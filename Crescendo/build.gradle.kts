// Este bloco 'plugins' deve ser o PRIMEIRO bloco NÃO-COMENTÁRIO no seu ficheiro build.gradle.kts.
plugins {
    id("java")
    id("application")

    id("org.jetbrains.kotlin.jvm") version "2.1.21"

    // ATENÇÃO: AQUI ESTÁ A ALTERAÇÃO.
    // Vamos tentar a versão "0.0.14" do plugin JavaFX, que é muito comum e estável.
    // Ou, se a versão 0.0.15 realmente existe no portal e está a dar problemas,
    // tentamos outra. A 0.0.10 também é uma opção.
    id("org.openjfx.javafxplugin") version "0.0.14" // <<-- MUDANÇA AQUI
}


group = "isep.crescendo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

javafx {
    version = "22.0.1"
    modules("javafx.controls", "javafx.fxml", "javafx.web", "javafx.media", "javafx.swing", "javafx.graphics")
}

dependencies {
    // === Dependências da Aplicação ===
    implementation("org.controlsfx:controlsfx:11.2.1")
    implementation("com.dlsc.formsfx:formsfx-core:11.6.0") {
        exclude(group = "org.openjfx")
    }
    implementation ("org.openjfx:javafx-controls:17")
    implementation("net.synedra:validatorfx:0.5.0") {
        exclude(group = "org.openjfx")
    }
    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")
    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")
    implementation("eu.hansolo:tilesfx:21.0.3") {
        exclude(group = "org.openjfx")
    }
    // FXGL - Comente/Remova se não usa
    // implementation("com.github.almasb:fxgl:17.3") {
    //     exclude(group = "org.openjfx")
    //     exclude(group = "org.jetbrains.kotlin")
    // }

    implementation("net.java.dev.jna:jna:5.17.0")
    implementation("net.java.dev.jna:jna-platform:5.17.0")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.21")
    implementation("jakarta.mail:jakarta.mail-api:2.1.2")
    implementation("org.eclipse.angus:angus-mail:2.0.2")
    // Jackson Core - Comente/Remova se não usa
    // implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")

    // === Dependências de Teste ===
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

application {
    mainModule.set("isep.crescendo")
    mainClass.set("isep.crescendo.Main")
}

tasks.test {
    useJUnitPlatform()
}
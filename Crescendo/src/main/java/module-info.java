module isep.crescendo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.media;
    requires javafx.swing;
    requires kotlin.stdlib; // Mantenha se estiver usando Kotlin no projeto
    requires javafx.graphics;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires jakarta.mail;

    // Garante que o JavaFX pode refletir sobre classes no pacote raiz para o FXMLLoader
    // (Útil se você carregar FXMLs diretamente do pacote 'isep.crescendo')
    opens isep.crescendo to javafx.fxml;
    // Exporta o pacote raiz para que a classe Main e outras utilidades sejam acessíveis por outros módulos (se houver)
    exports isep.crescendo;

    // --- SOLUÇÃO PARA O ERRO `InaccessibleObjectException` ---
    // Esta linha permite que o JavaFX (módulo javafx.fxml) acesse as classes
    // e os métodos (incluindo privados) dentro do pacote 'isep.crescendo.controller'.
    // É essencial para a ligação entre FXML e controladores.
    opens isep.crescendo.controller to javafx.fxml;
    // Também exporta o pacote do controlador para que outros módulos possam usá-lo
    // (útil se você tiver lógica de negócio que interage diretamente com controladores).
    exports isep.crescendo.controller;


    opens isep.crescendo.model to javafx.fxml; // Abre para FXM se precisar de data binding
    exports isep.crescendo.model; // Exporta para uso geral

    opens isep.crescendo.util to javafx.fxml; // Abre para FXM se precisar de data binding
    exports isep.crescendo.util; // Exporta para uso geral
}
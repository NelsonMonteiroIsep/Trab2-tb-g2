package isep.crescendo.controller;

import javafx.application.Platform; // Importar Platform
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class AdicionarSaldoDialogControllerTest {

    private AdicionarSaldoDialogController controller;
    private TextField valorTextField;
    private MockStage mockStage;
    private Double confirmedValue;

    private static class MockStage extends Stage {
        boolean closeCalled = false;

        @Override
        public void close() {
            this.closeCalled = true;
        }
    }

    @BeforeAll
    static void initJFX() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        // Garantir que o toolkit JavaFX é iniciado uma vez
        Platform.startup(() -> {
            new JFXPanel();
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS); // Esperar que o toolkit inicie
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        // Usar um CountDownLatch para esperar que a execução na thread JavaFX termine
        final CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            controller = new AdicionarSaldoDialogController();
            valorTextField = new TextField();
            controller.valorTextField = valorTextField; // Injeta o TextField diretamente

            mockStage = new MockStage();
            controller.setDialogStage(mockStage);

            confirmedValue = null;
            controller.setOnValorConfirmado(value -> confirmedValue = value);

            latch.countDown(); // Sinaliza que a inicialização JavaFX está completa
        });

        latch.await(5, TimeUnit.SECONDS); // Espera que a thread JavaFX termine a inicialização
    }

    // --- Testes para handleConfirmar ---

    @Test
    void testHandleConfirmar_ValorPositivo() throws InterruptedException {
        // Os testes em si também podem precisar de ser executados na thread FX
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            valorTextField.setText("100.0");
            controller.handleConfirmar();
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS); // Espera que a ação do teste seja processada

        // As asserções podem ser feitas na thread do teste, pois o estado já foi modificado
        assertEquals(100.0, confirmedValue, 0.001);
        assertTrue(mockStage.closeCalled);
    }

    @Test
    void testHandleConfirmar_ValorZero() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            valorTextField.setText("0.0");
            controller.handleConfirmar();
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);

        assertNull(confirmedValue);
        assertFalse(mockStage.closeCalled);
    }

    @Test
    void testHandleConfirmar_ValorNegativo() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            valorTextField.setText("-50.0");
            controller.handleConfirmar();
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);

        assertNull(confirmedValue);
        assertFalse(mockStage.closeCalled);
    }

    @Test
    void testHandleConfirmar_ValorNaoNumerico() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            valorTextField.setText("abc");
            controller.handleConfirmar();
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);

        assertNull(confirmedValue);
        assertFalse(mockStage.closeCalled);
    }

    // --- Testes para handleConfirmarLevantar ---

    @Test
    void testHandleConfirmarLevantar_ValorPositivoValido() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.setSaldoDisponivel(200.0); // Este método não interage com UI diretamente, pode ser fora
            valorTextField.setText("50.0");
            controller.handleConfirmarLevantar();
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);

        assertEquals(50.0, confirmedValue, 0.001);
        assertTrue(mockStage.closeCalled);
    }

    @Test
    void testHandleConfirmarLevantar_ValorZero() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.setSaldoDisponivel(200.0);
            valorTextField.setText("0.0");
            controller.handleConfirmarLevantar();
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);

        assertNull(confirmedValue);
        assertFalse(mockStage.closeCalled);
    }

    @Test
    void testHandleConfirmarLevantar_ValorNegativo() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.setSaldoDisponivel(200.0);
            valorTextField.setText("-20.0");
            controller.handleConfirmarLevantar();
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);

        assertNull(confirmedValue);
        assertFalse(mockStage.closeCalled);
    }

    @Test
    void testHandleConfirmarLevantar_ValorMaiorQueSaldo() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.setSaldoDisponivel(100.0);
            valorTextField.setText("150.0");
            controller.handleConfirmarLevantar();
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);

        assertNull(confirmedValue);
        assertFalse(mockStage.closeCalled);
    }

    @Test
    void testHandleConfirmarLevantar_ValorNaoNumerico() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.setSaldoDisponivel(100.0);
            valorTextField.setText("xyz");
            controller.handleConfirmarLevantar();
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);

        assertNull(confirmedValue);
        assertFalse(mockStage.closeCalled);
    }

    @Test
    void testHandleConfirmarLevantar_ValorExatamenteIgualAoSaldo() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.setSaldoDisponivel(100.0);
            valorTextField.setText("100.0");
            controller.handleConfirmarLevantar();
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);

        assertEquals(100.0, confirmedValue, 0.001);
        assertTrue(mockStage.closeCalled);
    }
}
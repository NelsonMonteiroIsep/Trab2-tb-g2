package isep.crescendo.controller;

import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class AdicionarSaldoDialogControllerTest {

    private AdicionarSaldoDialogController controller;

    // --- Implementações de Teste para as dependências ---

    // TextField de teste que permite definir o texto
    private static class TestTextField extends TextField {
        private String textToReturn = "";

        public void setTextToReturn(String text) {
            this.textToReturn = text;
        }

        @Override
        public String getText() {
            return textToReturn;
        }
    }

    // Stage de teste que registra se close() foi chamado
    private static class TestStage extends Stage {
        private AtomicBoolean closed = new AtomicBoolean(false);

        @Override
        public void close() {
            closed.set(true);
        }

        public boolean isClosed() {
            return closed.get();
        }
    }

    // Consumer de teste que armazena o valor aceito
    private static class TestConsumer implements Consumer<Double> {
        private AtomicReference<Double> acceptedValue = new AtomicReference<>(null);
        private AtomicBoolean called = new AtomicBoolean(false);

        @Override
        public void accept(Double value) {
            this.acceptedValue.set(value);
            this.called.set(true);
        }

        public Double getAcceptedValue() {
            return acceptedValue.get();
        }

        public boolean isCalled() {
            return called.get();
        }
    }

    // --- Mocks manuais para uso nos testes ---
    private TestTextField testValorTextField;
    private TestStage testDialogStage;
    private TestConsumer testOnValorConfirmado;

    @BeforeEach
    void setUp() {
        // Inicializa as nossas implementações de teste
        testValorTextField = new TestTextField();
        testDialogStage = new TestStage();
        testOnValorConfirmado = new TestConsumer();

        // Instancia o controller
        controller = new AdicionarSaldoDialogController();

        // Injeta as nossas implementações de teste no controller
        // Assumindo que o valorTextField é acessível para injeção (package-private ou public)
        // Se for private e não tiver setter, precisaríamos de reflexão ou de um construtor para teste.
        controller.valorTextField = testValorTextField;
        controller.setDialogStage(testDialogStage);
        controller.setOnValorConfirmado(testOnValorConfirmado);
    }

    @Test
    void handleConfirmar_ValorPositivoValido_ChamaConsumerEFechaDialog() {
        // 1. Configurar o nosso TestTextField para retornar um valor válido
        testValorTextField.setTextToReturn("100.0");

        // 2. Chamar o método a ser testado
        controller.handleConfirmar();

        // 3. Verificar o comportamento esperado
        // Verifica se o consumer foi chamado com o valor correto
        assertTrue(testOnValorConfirmado.isCalled(), "O consumer deveria ter sido chamado.");
        assertEquals(100.0, testOnValorConfirmado.getAcceptedValue(), 0.001, "O valor passado para o consumer está incorreto.");
        // Verifica se o dialog foi fechado
        assertTrue(testDialogStage.isClosed(), "O dialog deveria ter sido fechado.");
    }

    @Test
    void handleConfirmar_ValorZeroOuNegativo_NaoChamaConsumerNemFechaDialog() {
        // Teste para valor zero
        testValorTextField.setTextToReturn("0.0");
        controller.handleConfirmar();
        assertFalse(testOnValorConfirmado.isCalled(), "O consumer NÃO deveria ter sido chamado para valor zero.");
        assertFalse(testDialogStage.isClosed(), "O dialog NÃO deveria ter sido fechado para valor zero.");

        // Resetar o estado para o próximo cenário de teste
        setUp(); // Re-inicializa o controller e as dependências
        testValorTextField.setTextToReturn("-50.0");
        controller.handleConfirmar();
        assertFalse(testOnValorConfirmado.isCalled(), "O consumer NÃO deveria ter sido chamado para valor negativo.");
        assertFalse(testDialogStage.isClosed(), "O dialog NÃO deveria ter sido fechado para valor negativo.");
    }

    @Test
    void handleConfirmar_TextoInvalido_NaoChamaConsumerNemFechaDialog() {
        // Configurar o nosso TestTextField para retornar um texto inválido (não numérico)
        testValorTextField.setTextToReturn("abc");

        // Chamar o método a ser testado
        controller.handleConfirmar();

        // Verificar o comportamento esperado: consumer não chamado, dialog não fechado
        assertFalse(testOnValorConfirmado.isCalled(), "O consumer NÃO deveria ter sido chamado para texto inválido.");
        assertFalse(testDialogStage.isClosed(), "O dialog NÃO deveria ter sido fechado para texto inválido.");
    }

    @Test
    void handleConfirmarLevantar_ValorPositivoValidoDentroDoSaldo_ChamaConsumerEFechaDialog() {
        // Configurar o saldo disponível e o valor a levantar
        controller.setSaldoDisponivel(200.0);
        testValorTextField.setTextToReturn("100.0");

        // Chamar o método a ser testado
        controller.handleConfirmarLevantar();

        // Verificar o comportamento esperado
        assertTrue(testOnValorConfirmado.isCalled(), "O consumer deveria ter sido chamado.");
        assertEquals(100.0, testOnValorConfirmado.getAcceptedValue(), 0.001, "O valor passado para o consumer está incorreto.");
        assertTrue(testDialogStage.isClosed(), "O dialog deveria ter sido fechado.");
    }

    @Test
    void handleConfirmarLevantar_ValorZeroOuNegativo_NaoChamaConsumerNemFechaDialog() {
        // Configurar o saldo disponível
        controller.setSaldoDisponivel(200.0);

        // Teste para valor zero
        testValorTextField.setTextToReturn("0.0");
        controller.handleConfirmarLevantar();
        assertFalse(testOnValorConfirmado.isCalled(), "O consumer NÃO deveria ter sido chamado para valor zero.");
        assertFalse(testDialogStage.isClosed(), "O dialog NÃO deveria ter sido fechado para valor zero.");

        setUp(); // Re-inicializa para o próximo teste
        controller.setSaldoDisponivel(200.0); // Re-configura o saldo

        // Teste para valor negativo
        testValorTextField.setTextToReturn("-50.0");
        controller.handleConfirmarLevantar();
        assertFalse(testOnValorConfirmado.isCalled(), "O consumer NÃO deveria ter sido chamado para valor negativo.");
        assertFalse(testDialogStage.isClosed(), "O dialog NÃO deveria ter sido fechado para valor negativo.");
    }

    @Test
    void handleConfirmarLevantar_ValorMaiorQueSaldo_NaoChamaConsumerNemFechaDialog() {
        // Configurar o saldo disponível e o valor a levantar (maior que o saldo)
        controller.setSaldoDisponivel(50.0);
        testValorTextField.setTextToReturn("100.0");

        // Chamar o método a ser testado
        controller.handleConfirmarLevantar();

        // Verificar o comportamento esperado: consumer não chamado, dialog não fechado
        assertFalse(testOnValorConfirmado.isCalled(), "O consumer NÃO deveria ter sido chamado para valor maior que saldo.");
        assertFalse(testDialogStage.isClosed(), "O dialog NÃO deveria ter sido fechado para valor maior que saldo.");
        // Nota: A mensagem de erro (showAlert) não é testada diretamente aqui sem mocking de Alert,
        // mas o comportamento de não prosseguir já é uma boa indicação.
    }

    @Test
    void handleConfirmarLevantar_TextoInvalido_NaoChamaConsumerNemFechaDialog() {
        // Configurar o saldo disponível
        controller.setSaldoDisponivel(100.0);
        // Configurar o nosso TestTextField para retornar um texto inválido
        testValorTextField.setTextToReturn("xyz");

        // Chamar o método a ser testado
        controller.handleConfirmarLevantar();

        // Verificar o comportamento esperado: consumer não chamado, dialog não fechado
        assertFalse(testOnValorConfirmado.isCalled(), "O consumer NÃO deveria ter sido chamado para texto inválido.");
        assertFalse(testDialogStage.isClosed(), "O dialog NÃO deveria ter sido fechado para texto inválido.");
    }

    @Test
    void handleCancelar_FechaDialog() {
        // Chamar o método a ser testado
        controller.handleCancelar();

        // Verificar se o dialog foi fechado
        assertTrue(testDialogStage.isClosed(), "O dialog deveria ter sido fechado.");
    }
}
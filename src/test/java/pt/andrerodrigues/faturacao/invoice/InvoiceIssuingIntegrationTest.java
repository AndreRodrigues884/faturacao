package pt.andrerodrigues.faturacao.invoice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import pt.andrerodrigues.faturacao.IntegrationTest;
import pt.andrerodrigues.faturacao.common.BusinessRuleException;
import pt.andrerodrigues.faturacao.invoice.domain.InvoiceStatus;
import pt.andrerodrigues.faturacao.invoice.dto.InvoiceLineRequest;
import pt.andrerodrigues.faturacao.invoice.dto.InvoiceRequest;
import pt.andrerodrigues.faturacao.invoice.dto.InvoiceResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TESTE DE INTEGRAÇÃO - emissão de faturas num PostgreSQL real, incluindo pedidos em simultâneo.
 * Prova que o lock pessimista (série) e o otimista (@Version) funcionam de verdade.
 *
 * Testa:  InvoiceService, InvoiceNumberGenerator, InvoiceSeriesRepository, Invoice
 */
@DisplayName("Emissão de faturas (PostgreSQL real)")
class InvoiceIssuingIntegrationTest extends IntegrationTest {

    private static final ZoneId LISBON = ZoneId.of("Europe/Lisbon");

    @Autowired
    private InvoiceService invoiceService;

    // ---------- Auxiliares ----------

    /** Cria um rascunho para o cliente 1 com uma linha do produto 1 (dados do seed). */
    private Long createDraft() {
        InvoiceRequest request = new InvoiceRequest(
                1L,
                LocalDate.now(LISBON).plusDays(30),
                null,
                List.of(new InvoiceLineRequest(1L, BigDecimal.ONE))
        );
        return invoiceService.create(request).id();
    }

    /** "FT 2026/0007" -> 7 */
    private static int sequenceOf(String number) {
        return Integer.parseInt(number.substring(number.lastIndexOf('/') + 1));
    }

    /** Emite todas as faturas ao mesmo tempo, em várias threads, e devolve os resultados. */
    private List<Future<InvoiceResponse>> issueAllAtOnce(List<Long> ids) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Future<InvoiceResponse>> results = new ArrayList<>();

        for (Long id : ids) {
            results.add(pool.submit(() -> {
                startSignal.await();              // todas as threads esperam aqui...
                return invoiceService.issue(id);
            }));
        }

        startSignal.countDown();                  // ...e arrancam todas no mesmo instante
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        return results;
    }

    // ---------- Testes ----------

    @Test
    @DisplayName("emite a fatura e guarda o número, os totais e a fotografia do cliente")
    void emiteEGuarda() {
        Long id = createDraft();

        invoiceService.issue(id);
        InvoiceResponse reloaded = invoiceService.findById(id);

        assertThat(reloaded.status()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(reloaded.number()).matches("FT \\d{4}/\\d{4}");
        assertThat(reloaded.client().name()).isEqualTo("Cliente Exemplo, Lda.");
        assertThat(reloaded.total()).isEqualByComparingTo("55.35");
    }

    @Test
    @DisplayName("20 emissões em simultâneo recebem números diferentes e seguidos, sem saltos")
    void emissoesEmSimultaneo() throws Exception {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ids.add(createDraft());
        }

        List<Integer> sequences = new ArrayList<>();
        for (Future<InvoiceResponse> result : issueAllAtOnce(ids)) {
            sequences.add(sequenceOf(result.get().number()));
        }

        assertThat(sequences)
                .hasSize(20)
                .doesNotHaveDuplicates();

        int first = Collections.min(sequences);
        int last = Collections.max(sequences);
        assertThat(last - first + 1)
                .as("20 números seguidos, sem nenhum salto")
                .isEqualTo(20);
    }

    @Test
    @DisplayName("a mesma fatura emitida duas vezes em simultâneo só é emitida uma vez, sem gastar números")
    void mesmaFaturaDuasVezes() throws Exception {
        Long id = createDraft();

        int successes = 0;
        int issuedSequence = 0;
        List<Throwable> failures = new ArrayList<>();

        for (Future<InvoiceResponse> result : issueAllAtOnce(List.of(id, id))) {
            try {
                issuedSequence = sequenceOf(result.get().number());
                successes++;
            } catch (ExecutionException e) {
                failures.add(e.getCause());
            }
        }

        assertThat(successes).isEqualTo(1);
        assertThat(failures).hasSize(1);
        assertThat(failures.get(0)).isInstanceOfAny(
                OptimisticLockingFailureException.class,   // as duas leram o rascunho ao mesmo tempo
                BusinessRuleException.class);              // a segunda já leu a fatura emitida

        // Prova de que a tentativa falhada não gastou nenhum número
        int next = sequenceOf(invoiceService.issue(createDraft()).number());
        assertThat(next).isEqualTo(issuedSequence + 1);
    }
}
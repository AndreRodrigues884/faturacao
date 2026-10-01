package pt.andrerodrigues.faturacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import pt.andrerodrigues.faturacao.client.ClientService;
import pt.andrerodrigues.faturacao.invoice.InvoiceService;
import pt.andrerodrigues.faturacao.invoice.dto.InvoiceLineRequest;
import pt.andrerodrigues.faturacao.invoice.dto.InvoiceRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TESTE DE INTEGRAÇÃO - as constraints do PostgreSQL protegem os dados mesmo sem passar pelo Java.
 *
 * Testa:  as migrações (CHECK, FOREIGN KEY), ClientService.delete
 */
@DisplayName("Regras garantidas pela base de dados")
class DatabaseConstraintsIntegrationTest extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private ClientService clientService;

    @Test
    @DisplayName("recusa uma despesa cuja base não é o total menos o IVA")
    void recusaDespesaIncoerente() {
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO expenses (category_id, description, expense_date,
                                      total_amount, vat_amount, net_amount, payment_method)
                VALUES (1, 'Teste', '2026-09-01', 100.00, 23.00, 50.00, 'CARD')
                """))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("recusa uma fatura emitida sem número")
    void recusaFaturaEmitidaSemNumero() {
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO invoices (client_id, status, due_date)
                VALUES (1, 'ISSUED', '2026-12-31')
                """))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("não deixa apagar um cliente que tem faturas")
    void recusaApagarClienteComFaturas() {
        invoiceService.create(new InvoiceRequest(
                2L, LocalDate.now().plusDays(30), null,
                List.of(new InvoiceLineRequest(1L, BigDecimal.ONE))));

        assertThatThrownBy(() -> clientService.delete(2L))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}